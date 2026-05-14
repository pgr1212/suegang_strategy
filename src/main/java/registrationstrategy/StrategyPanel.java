package registrationstrategy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * 전략 분석 탭 패널
 * 탭 구성:
 *   1) 우선순위 추천
 *   2) 경쟁률 시뮬레이션
 *   3) 대체 과목 추천
 *   4) 전략 리포트
 *   5) 플랜 A/B
 *   6) 구분별 평균 경쟁률
 */
public class StrategyPanel extends JPanel {

    private final StrategyAnalyzer analyzer;
    private final CourseManager courseManager;
    private final WishList wishList;

    // 탭 패널들
    private JPanel tabPriority, tabSimulation, tabAlternative, tabReport, tabPlan, tabAvgRate;

    // ── 우선순위 탭 ──
    private DefaultTableModel modelPriority;

    // ── 시뮬레이션 탭 ──
    private JComboBox<String> cbSimCourse;
    private JSpinner spinIncrease;
    private JTextArea txtSimResult;

    // ── 대체 과목 탭 ──
    private JComboBox<String> cbAltCourse;
    private DefaultTableModel modelAlt;

    // ── 리포트 탭 ──
    private JTextArea txtReport;

    // ── 플랜 A/B/C 탭 ──
    private DefaultTableModel modelPlanA, modelPlanB, modelPlanC;
    private JLabel lblPlanACredit, lblPlanBCredit, lblPlanCCredit;

    // ── 구분별 탭 ──
    private DefaultTableModel modelAvgRate;

    public StrategyPanel(CourseManager courseManager, WishList wishList) {
        this.courseManager = courseManager;
        this.wishList = wishList;
        this.analyzer = new StrategyAnalyzer(courseManager, wishList);
        setLayout(new BorderLayout());
        setBackground(UIHelper.COLOR_BG);
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.FONT_TITLE);
        tabs.setBackground(UIHelper.COLOR_BG);

        tabs.addTab("📊 우선순위 추천",   buildPriorityTab());
        tabs.addTab("🔮 경쟁률 시뮬레이션", buildSimulationTab());
        tabs.addTab("🔄 대체 과목 추천",   buildAlternativeTab());
        tabs.addTab("📋 전략 리포트",       buildReportTab());
        tabs.addTab("🗂 플랜 A/B",         buildPlanTab());
        tabs.addTab("📈 구분별 경쟁률",     buildAvgRateTab());

        add(tabs, BorderLayout.CENTER);
    }

    // ──────────────────────────────────────────────
    // 탭 1: 우선순위 추천
    // ──────────────────────────────────────────────
    private JPanel buildPriorityTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UIHelper.COLOR_BG);

        JLabel desc = new JLabel("경쟁률과 실패 위험 점수가 높은 과목부터 먼저 신청하는 것을 추천합니다.");
        desc.setFont(UIHelper.FONT_BODY);
        desc.setForeground(new Color(60, 80, 120));
        p.add(desc, BorderLayout.NORTH);

        String[] cols = {"순위", "과목명", "이수구분", "경쟁률", "위험도", "실패 위험 점수", "성공 확률", "추천 이유"};
        modelPriority = UIHelper.createReadOnlyModel(cols);
        JTable table = new JTable(modelPriority);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new UIHelper.RateColorRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new UIHelper.RiskBgRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new UIHelper.ProgressBarRenderer());
        table.setRowHeight(28);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = UIHelper.createButton("새로고침", UIHelper.COLOR_HEADER, 0, 0, 100, 30);
        btnRefresh.addActionListener(e -> refreshPriority());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(UIHelper.COLOR_BG);
        south.add(btnRefresh);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    public void refreshPriority() {
        modelPriority.setRowCount(0);
        List<Course> list = analyzer.getPriorityList();
        for (int i = 0; i < list.size(); i++) {
            Course c = list.get(i);
            modelPriority.addRow(new Object[]{
                    (i + 1) + "순위",
                    c.getName(),
                    c.getType(),
                    String.format("%.2f:1", c.getCompetitionRate()),
                    c.getRiskLevel().getLabel(),
                    c.getFailureRiskScore() + "점",
                    c.getSuccessProbability(),    // ProgressBarRenderer가 처리
                    c.getRecommendReason()
            });
        }
    }

    // ──────────────────────────────────────────────
    // 탭 2: 경쟁률 시뮬레이션
    // ──────────────────────────────────────────────
    private JPanel buildSimulationTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UIHelper.COLOR_BG);

        // 상단 입력 패널
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        inputPanel.setBackground(UIHelper.COLOR_BG);
        inputPanel.setBorder(BorderFactory.createTitledBorder("시뮬레이션 설정"));

        cbSimCourse = new JComboBox<>();
        cbSimCourse.setFont(UIHelper.FONT_BODY);
        cbSimCourse.setPreferredSize(new Dimension(200, 26));

        spinIncrease = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        spinIncrease.setFont(UIHelper.FONT_BODY);
        spinIncrease.setPreferredSize(new Dimension(80, 26));

        JButton btnRun = UIHelper.createButton("시뮬레이션 실행", UIHelper.COLOR_HEADER, 0, 0, 130, 28);

        inputPanel.add(new JLabel("과목 선택:"));
        inputPanel.add(cbSimCourse);
        inputPanel.add(new JLabel("예상 증가 인원:"));
        inputPanel.add(spinIncrease);
        inputPanel.add(new JLabel("명"));
        inputPanel.add(btnRun);

        p.add(inputPanel, BorderLayout.NORTH);

        txtSimResult = new JTextArea();
        txtSimResult.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        txtSimResult.setEditable(false);
        txtSimResult.setMargin(new Insets(10, 12, 10, 12));
        txtSimResult.setLineWrap(true);
        txtSimResult.setWrapStyleWord(true);
        p.add(new JScrollPane(txtSimResult), BorderLayout.CENTER);

        btnRun.addActionListener(e -> runSimulation());
        return p;
    }

    private void runSimulation() {
        String selected = (String) cbSimCourse.getSelectedItem();
        if (selected == null) { showMsg("과목을 먼저 선택하세요."); return; }
        int id = Integer.parseInt(selected.split("\\.")[0].trim());
        Course course = courseManager.findById(id);
        if (course == null) { showMsg("과목을 찾을 수 없습니다."); return; }

        int increase = (Integer) spinIncrease.getValue();
        StrategyAnalyzer.SimulationResult r = analyzer.simulate(course, increase);

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════ 경쟁률 변화 시뮬레이션 결과 ═══════════════\n\n");
        sb.append(String.format("  과목명        : %s\n", r.course.getName()));
        sb.append(String.format("  정원          : %d명\n", r.course.getCapacity()));
        sb.append(String.format("  현재 담은 인원: %d명\n", r.course.getSavedCount()));
        sb.append(String.format("  예상 증가 인원: +%d명\n", r.expectedIncrease));
        sb.append(String.format("  예상 담은 인원: %d명\n\n", r.expectedSavedCount));
        sb.append(String.format("  현재 경쟁률   : %.2f:1  (%s, 위험점수 %d점, 성공확률 %d%%)\n",
                r.currentRate, r.currentRisk.getLabel(), r.currentScore, 100 - r.currentScore));
        sb.append(String.format("  예상 경쟁률   : %.2f:1  (%s, 위험점수 %d점, 성공확률 %d%%)\n\n",
                r.expectedRate, r.expectedRisk.getLabel(), r.expectedScore, 100 - r.expectedScore));

        // 위험도 변화 화살표
        sb.append(String.format("  위험도 변화   : [%s]  →  [%s]\n\n",
                r.currentRisk.getLabel(), r.expectedRisk.getLabel()));

        // 경쟁률 바 비교
        int curFilled = (int) Math.min(r.currentRate * 4, 20);
        int expFilled = (int) Math.min(r.expectedRate * 4, 20);
        sb.append("  현재  [");
        for (int i = 0; i < 20; i++) sb.append(i < curFilled ? "█" : "░");
        sb.append(String.format("] %.2f:1\n", r.currentRate));
        sb.append("  예상  [");
        for (int i = 0; i < 20; i++) sb.append(i < expFilled ? "█" : "░");
        sb.append(String.format("] %.2f:1\n\n", r.expectedRate));

        sb.append("  ▶ 분석: ").append(r.analysisMessage).append("\n");
        sb.append("═══════════════════════════════════════════════════════════");

        txtSimResult.setText(sb.toString());
    }

    // ──────────────────────────────────────────────
    // 탭 3: 대체 과목 추천
    // ──────────────────────────────────────────────
    private JPanel buildAlternativeTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UIHelper.COLOR_BG);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        top.setBackground(UIHelper.COLOR_BG);
        top.setBorder(BorderFactory.createTitledBorder("기준 과목 선택"));

        cbAltCourse = new JComboBox<>();
        cbAltCourse.setFont(UIHelper.FONT_BODY);
        cbAltCourse.setPreferredSize(new Dimension(220, 26));

        JButton btnFind = UIHelper.createButton("대체 과목 찾기", new Color(80, 130, 60), 0, 0, 130, 28);
        top.add(new JLabel("위험 과목:"));
        top.add(cbAltCourse);
        top.add(btnFind);
        top.add(new JLabel("  ※ 같은 이수구분 + 낮은 경쟁률 + 시간 충돌 없음 기준"));
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"과목명", "이수구분", "교수", "경쟁률", "위험도", "성공 확률", "강의시간", "추천 이유"};
        modelAlt = UIHelper.createReadOnlyModel(cols);
        JTable table = new JTable(modelAlt);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new UIHelper.RateColorRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new UIHelper.RiskBgRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new UIHelper.ProgressBarRenderer());
        table.setRowHeight(28);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        btnFind.addActionListener(e -> runAlternativeSearch());
        return p;
    }

    private void runAlternativeSearch() {
        String selected = (String) cbAltCourse.getSelectedItem();
        if (selected == null) { showMsg("과목을 선택하세요."); return; }
        int id = Integer.parseInt(selected.split("\\.")[0].trim());
        Course target = courseManager.findById(id);
        if (target == null) { showMsg("과목을 찾을 수 없습니다."); return; }

        List<Course> alts = analyzer.getAlternatives(target);
        modelAlt.setRowCount(0);
        if (alts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "조건에 맞는 대체 과목이 없습니다.\n(같은 이수구분 + 낮은 경쟁률 + 시간 충돌 없음)",
                    "대체 과목 없음", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (Course c : alts) {
            modelAlt.addRow(new Object[]{
                    c.getName(), c.getType(), c.getProfessor(),
                    String.format("%.2f:1", c.getCompetitionRate()),
                    c.getRiskLevel().getLabel(),
                    c.getSuccessProbability(),
                    c.getTimeString(),
                    "같은 이수구분, 낮은 경쟁률, 시간 충돌 없음"
            });
        }
    }

    // ──────────────────────────────────────────────
    // 탭 4: 전략 리포트
    // ──────────────────────────────────────────────
    private JPanel buildReportTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UIHelper.COLOR_BG);

        txtReport = new JTextArea();
        txtReport.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        txtReport.setEditable(false);
        txtReport.setMargin(new Insets(10, 12, 10, 12));
        txtReport.setLineWrap(true);
        txtReport.setWrapStyleWord(true);
        p.add(new JScrollPane(txtReport), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(UIHelper.COLOR_BG);
        JButton btnGen = UIHelper.createButton("리포트 생성", UIHelper.COLOR_HEADER, 0, 0, 120, 30);
        JButton btnSave = UIHelper.createButton("파일 저장", new Color(80, 130, 60), 0, 0, 100, 30);
        south.add(btnSave);
        south.add(btnGen);
        p.add(south, BorderLayout.SOUTH);

        btnGen.addActionListener(e -> generateReport());
        btnSave.addActionListener(e -> saveReport());
        return p;
    }

    private void generateReport() {
        if (wishList.isEmpty()) { showMsg("관심 과목을 먼저 담아주세요."); return; }
        StrategyAnalyzer.StrategyReport r = analyzer.generateReport();
        StringBuilder sb = new StringBuilder();

        sb.append("╔══════════════════════════════════════════════════════╗\n");
        sb.append("║          수강신청 전략 리포트                        ║\n");
        sb.append("╚══════════════════════════════════════════════════════╝\n\n");

        sb.append(String.format("  총 관심 과목 : %d개\n", r.totalCount));
        sb.append(String.format("  총 학점      : %d학점\n", r.totalCredit));
        sb.append(String.format("  학점 상태    : %s\n\n", r.creditStatus));

        sb.append("──────────────── 위험도 요약 ────────────────\n");
        sb.append(String.format("  🔴 매우 위험 : %d개\n", r.dangerCount));
        sb.append(String.format("  🟠 주의      : %d개\n", r.warningCount));
        sb.append(String.format("  🟢 보통      : %d개\n", r.normalCount));
        sb.append(String.format("  🔵 여유      : %d개\n\n", r.relaxedCount));

        if (r.mostDangerousCourse != null) {
            sb.append("──────────── 가장 먼저 신청할 과목 ────────────\n");
            sb.append(String.format("  ★ %s\n", r.mostDangerousCourse.getName()));
            sb.append(String.format("     경쟁률: %.2f:1  |  위험도: %s  |  위험점수: %d점  |  성공확률: %d%%\n\n",
                    r.mostDangerousCourse.getCompetitionRate(),
                    r.mostDangerousCourse.getRiskLevel().getLabel(),
                    r.mostDangerousCourse.getFailureRiskScore(),
                    r.mostDangerousCourse.getSuccessProbability()));
        }

        if (!r.warningCourses.isEmpty()) {
            sb.append("──────────────── 주의가 필요한 과목 ────────────────\n");
            for (Course c : r.warningCourses) {
                sb.append(String.format("  • %s  /  %.2f:1  /  %s  /  성공확률 %d%%\n",
                        c.getName(), c.getCompetitionRate(),
                        c.getRiskLevel().getLabel(), c.getSuccessProbability()));

                // 경쟁률 바
                sb.append("    ").append(c.getRateBar()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("──────────────── 전략 제안 ────────────────\n");
        sb.append("  1. 매우 위험 과목을 가장 먼저 신청하세요.\n");
        sb.append("  2. 주의 과목은 두 번째 그룹으로 신청하세요.\n");
        sb.append("  3. 보통 과목은 중간 순서로 신청하세요.\n");
        sb.append("  4. 여유 과목은 마지막에 신청해도 비교적 안전합니다.\n");
        sb.append("  5. 위험 과목 신청 실패에 대비해 대체 과목을 확인하세요.\n\n");

        // 시간표 충돌
        sb.append("──────────────── 시간표 충돌 확인 ────────────────\n");
        if (r.timeConflicts.isEmpty()) {
            sb.append("  ✔ 시간표 충돌이 없습니다.\n\n");
        } else {
            for (String conflict : r.timeConflicts)
                sb.append("  ⚠ 충돌: ").append(conflict).append("\n");
            sb.append("\n");
        }

        // 학점 구성 경고
        if (!r.compositionWarnings.isEmpty()) {
            sb.append("──────────────── 학점 구성 경고 ────────────────\n");
            for (String w : r.compositionWarnings)
                sb.append("  ⚠ ").append(w).append("\n");
            sb.append("\n");
        }

        sb.append("═══════════════════════════════════════════════════════");
        txtReport.setText(sb.toString());
        txtReport.setCaretPosition(0);
    }

    private void saveReport() {
        if (txtReport.getText().isEmpty()) { showMsg("먼저 리포트를 생성하세요."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("수강신청_전략_리포트.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter fw = new java.io.FileWriter(fc.getSelectedFile())) {
                fw.write(txtReport.getText());
                showMsg("리포트가 저장되었습니다:\n" + fc.getSelectedFile().getAbsolutePath());
            } catch (java.io.IOException ex) {
                showMsg("저장 실패: " + ex.getMessage());
            }
        }
    }

    // ──────────────────────────────────────────────
    // 탭 5: 플랜 A/B/C
    // ──────────────────────────────────────────────
    private JPanel buildPlanTab() {
        JPanel p = new JPanel(new GridLayout(1, 3, 8, 0));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UIHelper.COLOR_BG);

        String[] cols = {"순위", "과목명", "이수구분", "경쟁률", "위험도", "위험점수", "성공확률"};

        // 플랜 A
        JPanel panelA = new JPanel(new BorderLayout(0, 4));
        panelA.setBackground(UIHelper.COLOR_BG);
        panelA.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIHelper.COLOR_DANGER, 1),
                "📌 플랜 A — 공격적 전략"));
        JLabel descA = new JLabel("<html><small>경쟁률 높은 순 — 인기 과목 우선<br>장점: 원하는 과목 시도 | 단점: 실패 위험 높음</small></html>");
        descA.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panelA.add(descA, BorderLayout.NORTH);
        modelPlanA = UIHelper.createReadOnlyModel(cols);
        JTable tA = new JTable(modelPlanA);
        UIHelper.styleTable(tA);
        tA.getColumnModel().getColumn(3).setCellRenderer(new UIHelper.RateColorRenderer());
        tA.getColumnModel().getColumn(4).setCellRenderer(new UIHelper.RiskBgRenderer());
        tA.getColumnModel().getColumn(6).setCellRenderer(new UIHelper.ProgressBarRenderer());
        panelA.add(new JScrollPane(tA), BorderLayout.CENTER);
        lblPlanACredit = new JLabel("총 학점: -");
        lblPlanACredit.setFont(UIHelper.FONT_BODY);
        lblPlanACredit.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panelA.add(lblPlanACredit, BorderLayout.SOUTH);

        // 플랜 B
        JPanel panelB = new JPanel(new BorderLayout(0, 4));
        panelB.setBackground(UIHelper.COLOR_BG);
        panelB.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIHelper.COLOR_RELAXED, 1),
                "📌 플랜 B — 안정적 전략"));
        JLabel descB = new JLabel("<html><small>위험점수 낮은 순 — 성공률 우선<br>장점: 전체 성공 가능성 높음 | 단점: 인기 과목 놓칠 수 있음</small></html>");
        descB.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panelB.add(descB, BorderLayout.NORTH);
        modelPlanB = UIHelper.createReadOnlyModel(cols);
        JTable tB = new JTable(modelPlanB);
        UIHelper.styleTable(tB);
        tB.getColumnModel().getColumn(3).setCellRenderer(new UIHelper.RateColorRenderer());
        tB.getColumnModel().getColumn(4).setCellRenderer(new UIHelper.RiskBgRenderer());
        tB.getColumnModel().getColumn(6).setCellRenderer(new UIHelper.ProgressBarRenderer());
        panelB.add(new JScrollPane(tB), BorderLayout.CENTER);
        lblPlanBCredit = new JLabel("총 학점: -");
        lblPlanBCredit.setFont(UIHelper.FONT_BODY);
        lblPlanBCredit.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panelB.add(lblPlanBCredit, BorderLayout.SOUTH);

        // 플랜 C (전공 우선)
        JPanel panelC = new JPanel(new BorderLayout(0, 4));
        panelC.setBackground(UIHelper.COLOR_BG);
        panelC.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 60, 180), 1),
                "📌 플랜 C — 전공 우선 전략"));
        JLabel descC = new JLabel("<html><small>전필→전선→교필→교선 순, 각 그룹 내 경쟁률 순<br>장점: 졸업요건 과목 우선 확보 | 단점: 교양 놓칠 수 있음</small></html>");
        descC.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panelC.add(descC, BorderLayout.NORTH);
        modelPlanC = UIHelper.createReadOnlyModel(cols);
        JTable tC = new JTable(modelPlanC);
        UIHelper.styleTable(tC);
        tC.getColumnModel().getColumn(3).setCellRenderer(new UIHelper.RateColorRenderer());
        tC.getColumnModel().getColumn(4).setCellRenderer(new UIHelper.RiskBgRenderer());
        tC.getColumnModel().getColumn(6).setCellRenderer(new UIHelper.ProgressBarRenderer());
        panelC.add(new JScrollPane(tC), BorderLayout.CENTER);
        lblPlanCCredit = new JLabel("총 학점: -");
        lblPlanCCredit.setFont(UIHelper.FONT_BODY);
        lblPlanCCredit.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panelC.add(lblPlanCCredit, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UIHelper.COLOR_BG);
        wrap.add(p, BorderLayout.CENTER);

        JButton btnGen = UIHelper.createButton("플랜 생성", UIHelper.COLOR_HEADER, 0, 0, 100, 30);
        btnGen.addActionListener(e -> generatePlanAB());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(UIHelper.COLOR_BG);
        south.add(btnGen);
        wrap.add(south, BorderLayout.SOUTH);

        p.add(panelA);
        p.add(panelB);
        p.add(panelC);
        return wrap;
    }

    private void generatePlanAB() {
        if (wishList.isEmpty()) { showMsg("관심 과목을 먼저 담아주세요."); return; }
        StrategyAnalyzer.PlanAB plan = analyzer.generatePlanAB();

        modelPlanA.setRowCount(0);
        for (int i = 0; i < plan.planA.size(); i++) {
            Course c = plan.planA.get(i);
            modelPlanA.addRow(new Object[]{
                    (i + 1) + "순위", c.getName(), c.getType(),
                    String.format("%.2f:1", c.getCompetitionRate()),
                    c.getRiskLevel().getLabel(),
                    c.getFailureRiskScore() + "점",
                    c.getSuccessProbability()
            });
        }
        lblPlanACredit.setText("총 학점: " + plan.planACredit + "학점");

        modelPlanB.setRowCount(0);
        for (int i = 0; i < plan.planB.size(); i++) {
            Course c = plan.planB.get(i);
            modelPlanB.addRow(new Object[]{
                    (i + 1) + "순위", c.getName(), c.getType(),
                    String.format("%.2f:1", c.getCompetitionRate()),
                    c.getRiskLevel().getLabel(),
                    c.getFailureRiskScore() + "점",
                    c.getSuccessProbability()
            });
        }
        lblPlanBCredit.setText("총 학점: " + plan.planBCredit + "학점");

        modelPlanC.setRowCount(0);
        for (int i = 0; i < plan.planC.size(); i++) {
            Course c = plan.planC.get(i);
            modelPlanC.addRow(new Object[]{
                    (i + 1) + "순위", c.getName(), c.getType(),
                    String.format("%.2f:1", c.getCompetitionRate()),
                    c.getRiskLevel().getLabel(),
                    c.getFailureRiskScore() + "점",
                    c.getSuccessProbability()
            });
        }
        lblPlanCCredit.setText("총 학점: " + plan.planCCredit + "학점");
    }


    // ──────────────────────────────────────────────
    // 탭 6: 구분별 평균 경쟁률
    // ──────────────────────────────────────────────
    private JPanel buildAvgRateTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UIHelper.COLOR_BG);

        JLabel desc = new JLabel("전체 과목의 이수구분별 평균 경쟁률을 비교합니다.");
        desc.setFont(UIHelper.FONT_BODY);
        p.add(desc, BorderLayout.NORTH);

        String[] cols = {"이수구분", "평균 경쟁률", "위험도", "경쟁률 바"};
        modelAvgRate = UIHelper.createReadOnlyModel(cols);
        JTable table = new JTable(modelAvgRate);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(1).setCellRenderer(new UIHelper.RateColorRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new UIHelper.RiskBgRenderer());
        table.setRowHeight(28);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnLoad = UIHelper.createButton("데이터 불러오기", UIHelper.COLOR_HEADER, 0, 0, 140, 30);
        btnLoad.addActionListener(e -> refreshAvgRate());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(UIHelper.COLOR_BG);
        south.add(btnLoad);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshAvgRate() {
        modelAvgRate.setRowCount(0);
        Map<String, Double> data = analyzer.getAverageRateByType();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double rate = entry.getValue();
            RiskLevel risk = RiskLevel.fromRate(rate);

            // 텍스트 바
            int filled = (int) Math.min(rate * 4, 20);
            StringBuilder bar = new StringBuilder("[");
            for (int i = 0; i < 20; i++) bar.append(i < filled ? "█" : "░");
            bar.append(String.format("] %.2f:1", rate));

            modelAvgRate.addRow(new Object[]{
                    entry.getKey(),
                    String.format("%.2f:1", rate),
                    risk.getLabel(),
                    bar.toString()
            });
        }
    }

    // ──────────────────────────────────────────────
    // 공통: ComboBox 새로고침 (전체 과목 / 관심 과목)
    // ──────────────────────────────────────────────

    /** 시뮬레이션·대체 과목 콤보박스를 전체 과목으로 채움 */
    public void refreshCombos() {
        cbSimCourse.removeAllItems();
        cbAltCourse.removeAllItems();
        for (Course c : courseManager.getCourseList()) {
            String item = c.getId() + ". " + c.getName()
                    + " (" + String.format("%.2f", c.getCompetitionRate()) + ":1)";
            cbSimCourse.addItem(item);
            cbAltCourse.addItem(item);
        }
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}