package registrationstrategy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * MainFrame
 * 기존 suegang/Form2 구조를 완전히 재설계한 메인 창
 * <p>
 * 레이아웃:
 * ┌──────────────────────────────────────────────────┐
 * │  헤더 (타이틀 + 요약 정보)                        │
 * ├─────────────────────┬────────────────────────────┤
 * │  LEFT PANEL         │  RIGHT TABS                │
 * │  - 과목 검색        │  - 과목 목록               │
 * │  - 관심 과목 목록   │  - 전략 분석 탭들          │
 * └─────────────────────┴────────────────────────────┘
 */
public class MainFrame extends JFrame {

    private final CourseManager courseManager;
    private final WishList wishList;
    private final StrategyPanel strategyPanel;

    // ── 과목 검색 ──
    private JTextField txtSearch;
    private DefaultTableModel modelAllCourses;
    private JTable tableAll;

    // ── 관심 과목 ──
    private DefaultTableModel modelWish;
    private JTable tableWish;
    private JLabel lblTotalCredit, lblWishCount;

    // ── 과목 상세 ──
    private JTextField fldCode, fldName, fldType, fldCredit, fldProf, fldTime, fldRoom;
    private JTextField fldRate, fldRisk, fldScore, fldSuccessProb;
    private JTextArea txtRateBar;

    public MainFrame() {
        courseManager = new CourseManager();
        courseManager.loadSampleCourses();
        wishList = new WishList();
        strategyPanel = new StrategyPanel(courseManager, wishList);

        buildFrame();
        refreshAllTable(courseManager.getCourseList());
        strategyPanel.refreshCombos();
    }

    private void buildFrame() {
        setTitle("경쟁률 기반 수강신청 전략 추천 시스템");
        setSize(1440, 820);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UIHelper.COLOR_BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ──────────────────────────────────────────────
    // 헤더
    // ──────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.COLOR_HEADER);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel title = new JLabel("📚 경쟁률 기반 수강신청 전략 추천 시스템");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("수강신청 전에 전략을 세워주는 프로그램");
        sub.setFont(UIHelper.FONT_BODY);
        sub.setForeground(new Color(180, 210, 255));
        header.add(sub, BorderLayout.EAST);
        return header;
    }

    // ──────────────────────────────────────────────
    // 메인 레이아웃
    // ──────────────────────────────────────────────
    private JSplitPane buildMain() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeft(), buildRight());
        split.setDividerLocation(480);
        split.setDividerSize(5);
        split.setBackground(UIHelper.COLOR_BG);
        return split;
    }

    // ──────────────────────────────────────────────
    // LEFT: 과목 검색 + 관심 과목 + 상세 정보
    // ──────────────────────────────────────────────
    private JPanel buildLeft() {
        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setBackground(UIHelper.COLOR_BG);
        left.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 3));

        // ── 과목 검색 패널 ──
        JPanel searchPanel = UIHelper.createTitledPanel("과목 검색 및 전체 목록", 0, 0, 0, 0);
        searchPanel.setLayout(new BorderLayout(0, 4));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 190, 210)),
                        "과목 검색 및 전체 목록"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        // 검색 바
        JPanel searchBar = new JPanel(new BorderLayout(4, 0));
        searchBar.setBackground(Color.WHITE);
        txtSearch = new JTextField();
        txtSearch.setFont(UIHelper.FONT_BODY);
        txtSearch.setToolTipText("과목명, 교수명, 이수구분, 학정번호로 검색");
        JButton btnSearch = UIHelper.createButton("검색", UIHelper.COLOR_ACCENT, 0, 0, 60, 26);
        JButton btnReset = UIHelper.createButton("초기화", new Color(120, 120, 130), 0, 0, 60, 26);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(btnSearch);
        btnRow.add(btnReset);
        searchBar.add(new JLabel("  검색: "), BorderLayout.WEST);
        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(btnRow, BorderLayout.EAST);
        searchPanel.add(searchBar, BorderLayout.NORTH);

        // 전체 과목 테이블
        String[] colsAll = {"번호", "학정번호", "과목명", "구분", "교수", "정원", "담은인원", "경쟁률", "위험도", "성공확률"};
        modelAllCourses = UIHelper.createReadOnlyModel(colsAll);
        tableAll = new JTable(modelAllCourses);
        UIHelper.styleTable(tableAll);
        tableAll.getColumnModel().getColumn(7).setCellRenderer(new UIHelper.RateColorRenderer());
        tableAll.getColumnModel().getColumn(8).setCellRenderer(new UIHelper.RiskBgRenderer());
        tableAll.getColumnModel().getColumn(9).setCellRenderer(new UIHelper.ProgressBarRenderer());
        tableAll.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableAll.setRowHeight(26);

        // 컬럼 너비
        int[] widths = {35, 85, 120, 50, 60, 45, 60, 65, 70, 65};
        for (int i = 0; i < widths.length; i++)
            tableAll.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        searchPanel.add(new JScrollPane(tableAll), BorderLayout.CENTER);

        // 과목 선택시 상세 표시
        tableAll.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableAll.getSelectedRow();
                if (row >= 0) {
                    String code = (String) modelAllCourses.getValueAt(row, 1);
                    showDetail(courseManager.findByCode(code));
                }
            }
        });
        btnSearch.addActionListener(e -> doSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshAllTable(courseManager.getCourseList());
        });
        txtSearch.addActionListener(e -> doSearch());

        // ── 관심 과목 패널 ──
        JPanel wishPanel = new JPanel(new BorderLayout(0, 2));
        wishPanel.setBackground(UIHelper.COLOR_BG);
        wishPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 190, 210)),
                        "⭐ 관심 과목 (장바구니)"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        String[] colsWish = {"번호", "과목명", "구분", "학점", "교수", "경쟁률", "위험도", "성공확률"};
        modelWish = UIHelper.createReadOnlyModel(colsWish);
        tableWish = new JTable(modelWish);
        UIHelper.styleTable(tableWish);
        tableWish.getColumnModel().getColumn(5).setCellRenderer(new UIHelper.RateColorRenderer());
        tableWish.getColumnModel().getColumn(6).setCellRenderer(new UIHelper.RiskBgRenderer());
        tableWish.getColumnModel().getColumn(7).setCellRenderer(new UIHelper.ProgressBarRenderer());
        tableWish.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableWish.setRowHeight(26);
        wishPanel.add(new JScrollPane(tableWish), BorderLayout.CENTER);

        // 관심 과목 하단 버튼/정보 행
        JPanel wishBottom = new JPanel(new BorderLayout());
        wishBottom.setBackground(UIHelper.COLOR_BG);
        JPanel wishInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        wishInfo.setBackground(UIHelper.COLOR_BG);
        lblWishCount = new JLabel("0개");
        lblWishCount.setFont(UIHelper.FONT_BODY);
        lblTotalCredit = new JLabel("0학점");
        lblTotalCredit.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        wishInfo.add(new JLabel("담은 과목:"));
        wishInfo.add(lblWishCount);
        wishInfo.add(new JLabel("  |  총 학점:"));
        wishInfo.add(lblTotalCredit);
        wishBottom.add(wishInfo, BorderLayout.WEST);

        JPanel wishBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        wishBtns.setBackground(UIHelper.COLOR_BG);
        JButton btnAddWish = UIHelper.createButton("+ 관심 과목 추가", UIHelper.COLOR_HEADER, 0, 0, 130, 26);
        JButton btnDelWish = UIHelper.createButton("- 삭제", UIHelper.COLOR_DANGER, 0, 0, 70, 26);
        wishBtns.add(btnAddWish);
        wishBtns.add(btnDelWish);
        wishBottom.add(wishBtns, BorderLayout.EAST);
        wishPanel.add(wishBottom, BorderLayout.SOUTH);

        btnAddWish.addActionListener(e -> addToWishList());
        btnDelWish.addActionListener(e -> removeFromWishList());

        // ── 과목 상세 ──
        JPanel detailPanel = buildDetailPanel();

        // ── 수직 분할 ──
        JSplitPane vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchPanel, wishPanel);
        vSplit.setDividerLocation(320);
        vSplit.setDividerSize(4);
        vSplit.setBackground(UIHelper.COLOR_BG);

        left.add(vSplit, BorderLayout.CENTER);
        left.add(detailPanel, BorderLayout.SOUTH);
        return left;
    }

    // ──────────────────────────────────────────────
    // 과목 상세 패널
    // ──────────────────────────────────────────────
    private JPanel buildDetailPanel() {
        JPanel p = new JPanel(null);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 190, 210)),
                        "📌 과목 상세 분석"),
                BorderFactory.createEmptyBorder(2, 4, 4, 4)));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(0, 160));

        // 행 1: 학정번호, 과목명
        addLabel(p, "학정번호:", 8, 22, 55, 20);
        fldCode = UIHelper.createReadOnlyField(65, 22, 90);
        p.add(fldCode);
        addLabel(p, "과목명:", 165, 22, 45, 20);
        fldName = UIHelper.createReadOnlyField(210, 22, 250);
        p.add(fldName);

        // 행 2
        addLabel(p, "이수구분:", 8, 50, 55, 20);
        fldType = UIHelper.createReadOnlyField(65, 50, 50);
        p.add(fldType);
        addLabel(p, "학점:", 125, 50, 35, 20);
        fldCredit = UIHelper.createReadOnlyField(162, 50, 30);
        p.add(fldCredit);
        addLabel(p, "교수:", 200, 50, 35, 20);
        fldProf = UIHelper.createReadOnlyField(235, 50, 90);
        p.add(fldProf);
        addLabel(p, "강의시간:", 335, 50, 55, 20);
        fldTime = UIHelper.createReadOnlyField(392, 50, 65);
        p.add(fldTime);

        // 행 3
        addLabel(p, "경쟁률:", 8, 78, 45, 20);
        fldRate = UIHelper.createReadOnlyField(55, 78, 60);
        p.add(fldRate);
        addLabel(p, "위험도:", 125, 78, 45, 20);
        fldRisk = UIHelper.createReadOnlyField(172, 78, 60);
        p.add(fldRisk);
        addLabel(p, "위험점수:", 245, 78, 55, 20);
        fldScore = UIHelper.createReadOnlyField(302, 78, 50);
        p.add(fldScore);
        addLabel(p, "성공확률:", 360, 78, 55, 20);
        fldSuccessProb = UIHelper.createReadOnlyField(417, 78, 50);
        p.add(fldSuccessProb);

        // 행 4: 경쟁률 바
        addLabel(p, "경쟁률 바:", 8, 106, 55, 20);
        txtRateBar = new JTextArea();
        txtRateBar.setBounds(65, 104, 400, 20);
        txtRateBar.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtRateBar.setEditable(false);
        txtRateBar.setBackground(new Color(240, 243, 250));
        txtRateBar.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        p.add(txtRateBar);

        return p;
    }

    private void addLabel(JPanel p, String text, int x, int y, int w, int h) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, w, h);
        l.setFont(UIHelper.FONT_SMALL);
        l.setForeground(new Color(80, 80, 100));
        p.add(l);
    }

    private void showDetail(Course c) {
        if (c == null) return;
        fldCode.setText(c.getCode());
        fldName.setText(c.getName());
        fldType.setText(c.getType());
        fldCredit.setText(c.getCredit() + "학점");
        fldProf.setText(c.getProfessor());
        fldTime.setText(c.getTimeString());
        fldRate.setText(String.format("%.2f:1", c.getCompetitionRate()));
        fldRisk.setText(c.getRiskLevel().getLabel());
        fldRisk.setForeground(c.getRiskLevel().getColor());
        fldScore.setText(c.getFailureRiskScore() + "점");
        fldSuccessProb.setText(c.getSuccessProbability() + "%");
        txtRateBar.setText(c.getRateBar());
    }

    // ──────────────────────────────────────────────
    // RIGHT: 탭 (전체 과목 목록 + 전략 분석)
    // ──────────────────────────────────────────────
    private JTabbedPane buildRight() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.FONT_TITLE);
        tabs.setBackground(UIHelper.COLOR_BG);

        // 과목 현황 탭 (경쟁률 순위)
        tabs.addTab("🏆 경쟁률 순위", buildRankingTab());

        // 전략 분석 탭들 (StrategyPanel이 내부 탭 포함)
        tabs.addTab("🎯 전략 분석", strategyPanel);

        return tabs;
    }

    private JPanel buildRankingTab() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        p.setBackground(UIHelper.COLOR_BG);

        JLabel desc = new JLabel("전체 과목을 경쟁률 순으로 표시합니다. 위험도와 성공 확률을 확인하세요.");
        desc.setFont(UIHelper.FONT_BODY);
        desc.setForeground(new Color(60, 80, 120));
        p.add(desc, BorderLayout.NORTH);

        String[] cols = {"순위", "학정번호", "과목명", "이수구분", "교수", "정원", "담은인원", "경쟁률", "위험도", "위험점수", "성공확률", "경쟁률 바"};
        DefaultTableModel model = UIHelper.createReadOnlyModel(cols);
        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(7).setCellRenderer(new UIHelper.RateColorRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new UIHelper.RiskBgRenderer());
        table.getColumnModel().getColumn(10).setCellRenderer(new UIHelper.ProgressBarRenderer());
        table.setRowHeight(28);

        // 데이터 채우기
        List<Course> sorted = courseManager.getSortedByRateDesc();
        for (int i = 0; i < sorted.size(); i++) {
            Course c = sorted.get(i);
            model.addRow(new Object[]{
                    (i + 1), c.getCode(), c.getName(), c.getType(), c.getProfessor(),
                    c.getCapacity(), c.getSavedCount(),
                    String.format("%.2f:1", c.getCompetitionRate()),
                    c.getRiskLevel().getLabel(),
                    c.getFailureRiskScore() + "점",
                    c.getSuccessProbability(),
                    c.getRateBar()
            });
        }

        // 컬럼 너비
        int[] w = {40, 80, 120, 50, 55, 45, 60, 65, 70, 65, 65, 200};
        for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // ──────────────────────────────────────────────
    // 과목 검색
    // ──────────────────────────────────────────────
    private void doSearch() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) refreshAllTable(courseManager.getCourseList());
        else refreshAllTable(courseManager.search(kw));
    }

    private void refreshAllTable(List<Course> list) {
        modelAllCourses.setRowCount(0);
        int no = 1;
        for (Course c : list) {
            modelAllCourses.addRow(new Object[]{
                    no++, c.getCode(), c.getName(), c.getType(), c.getProfessor(),
                    c.getCapacity(), c.getSavedCount(),
                    String.format("%.2f:1", c.getCompetitionRate()),
                    c.getRiskLevel().getLabel(),
                    c.getSuccessProbability()
            });
        }
    }

    // ──────────────────────────────────────────────
    // 관심 과목 추가 / 삭제
    // ──────────────────────────────────────────────
    private void addToWishList() {
        int row = tableAll.getSelectedRow();
        if (row < 0) {
            showMsg("과목을 먼저 선택하세요.");
            return;
        }
        String code = (String) modelAllCourses.getValueAt(row, 1);
        Course course = courseManager.findByCode(code);
        if (course == null) return;

        int result = wishList.add(course);
        if (result == 1) {
            showMsg("이미 관심 과목에 담긴 과목입니다.");
            return;
        }
        if (result == 2) {
            // 시간 충돌 경고
            Course conflict = wishList.getConflictingCourse(course);
            int ans = JOptionPane.showConfirmDialog(this,
                    "⚠ 시간표 충돌!\n[" + course.getName() + "] 과목이 [" + conflict.getName() + "] 과목과 시간이 겹칩니다.\n그래도 추가하시겠습니까?",
                    "시간 충돌 경고", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ans == JOptionPane.YES_OPTION) wishList.forceAdd(course);
            else return;
        }
        refreshWishTable();
        strategyPanel.refreshCombos();
        showMsg("[" + course.getName() + "] 과목이 관심 과목에 추가되었습니다.\n현재 총 학점: " + wishList.getTotalCredit() + "학점");
    }

    private void removeFromWishList() {
        int row = tableWish.getSelectedRow();
        if (row < 0) {
            showMsg("삭제할 과목을 선택하세요.");
            return;
        }
        String name = (String) modelWish.getValueAt(row, 1);
        int id = (Integer) modelWish.getValueAt(row, 0);
        if (wishList.remove(id)) {
            refreshWishTable();
            strategyPanel.refreshCombos();
            showMsg("[" + name + "] 과목이 관심 과목에서 삭제되었습니다.");
        }
    }

    private void refreshWishTable() {
        modelWish.setRowCount(0);
        for (Course c : wishList.getCourses()) {
            modelWish.addRow(new Object[]{
                    c.getId(), c.getName(), c.getType(), c.getCredit(), c.getProfessor(),
                    String.format("%.2f:1", c.getCompetitionRate()),
                    c.getRiskLevel().getLabel(),
                    c.getSuccessProbability()
            });
        }
        lblWishCount.setText(wishList.getCourses().size() + "개");
        lblTotalCredit.setText(wishList.getTotalCredit() + "학점");

        // 학점 색상 경고
        int total = wishList.getTotalCredit();
        if (total < 12 || total > 21)
            lblTotalCredit.setForeground(UIHelper.COLOR_DANGER);
        else
            lblTotalCredit.setForeground(UIHelper.COLOR_NORMAL);
    }

    // ──────────────────────────────────────────────
    // 상태바
    // ──────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(230, 235, 245));
        bar.setPreferredSize(new Dimension(0, 24));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 190, 210)));
        JLabel lbl = new JLabel("  💡 과목 선택 후 '관심 과목 추가' 버튼을 눌러 담고, 전략 분석 탭에서 수강신청 전략을 확인하세요.");
        lbl.setFont(UIHelper.FONT_SMALL);
        lbl.setForeground(new Color(60, 80, 120));
        bar.add(lbl, BorderLayout.WEST);
        return bar;
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}
