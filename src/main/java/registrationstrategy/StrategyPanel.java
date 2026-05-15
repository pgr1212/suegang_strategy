package registrationstrategy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * 전략 분석 탭 패널
 * 탭 구성:
 * 1) 우선순위 추천
 * 2) 경쟁률 시뮬레이션
 * 3) 시간표 보기
 * 4) 전략 리포트
 * 5) 맞춤형 플랜
 */
public class StrategyPanel extends JPanel {

    private final StrategyAnalyzer analyzer;
    private final CourseManager courseManager;
    private final WishList wishList;

    // 우선순위 탭
    private DefaultTableModel modelPriority;

    // 시뮬레이션 탭
    private JComboBox<String> cbSimCourse;
    private JSpinner spinIncrease;
    private JTextArea txtSimResult;

    // 시간표 탭
    private DefaultTableModel modelTimetable;
    private JTextArea txtTimetableWarning;

    // 리포트 탭
    private JTextArea txtReport;

    // 맞춤형 플랜 탭
    private DefaultTableModel modelPlanMain;
    private DefaultTableModel modelPlanSafe;
    private DefaultTableModel modelPlanChallenge;

    private JLabel lblPlanMainCredit;
    private JLabel lblPlanSafeCredit;
    private JLabel lblPlanChallengeCredit;

    private JCheckBox chkPreferMajor;
    private JCheckBox chkPreferRequired;
    private JCheckBox chkPreferSafe;
    private JCheckBox chkAvoidMorning;
    private JCheckBox chkPreferThreeCredit;

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

        tabs.addTab("우선순위 추천", buildPriorityTab());
        tabs.addTab("경쟁률 시뮬레이션", buildSimulationTab());
        tabs.addTab("시간표 보기", buildTimetableTab());
        tabs.addTab("전략 리포트", buildReportTab());
        tabs.addTab("맞춤형 플랜", buildPlanTab());

        add(tabs, BorderLayout.CENTER);
    }

    // ──────────────────────────────────────────────
    // 탭 1: 우선순위 추천
    // ──────────────────────────────────────────────

    private JPanel buildPriorityTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UIHelper.COLOR_BG);

        JLabel desc = new JLabel("경쟁률, 이수구분, 학점, 성공 확률을 종합해 우선순위 점수가 높은 과목부터 추천합니다.");
        desc.setFont(UIHelper.FONT_BODY);
        desc.setForeground(new Color(60, 80, 120));
        p.add(desc, BorderLayout.NORTH);

        String[] cols = {
                "순위", "과목명", "이수구분", "경쟁률", "위험도",
                "실패 위험 점수", "성공 확률", "우선순위 점수", "추천 이유"
        };

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
        if (modelPriority == null) {
            return;
        }

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
                    c.getSuccessProbability(),
                    c.getPriorityScore() + "점",
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

        if (selected == null) {
            showMsg("과목을 먼저 선택하세요.");
            return;
        }

        int id = Integer.parseInt(selected.split("\\.")[0].trim());
        Course course = courseManager.findById(id);

        if (course == null) {
            showMsg("과목을 찾을 수 없습니다.");
            return;
        }

        int increase = (Integer) spinIncrease.getValue();
        StrategyAnalyzer.SimulationResult r = analyzer.simulate(course, increase);

        StringBuilder sb = new StringBuilder();
        sb.append("[ 경쟁률 변화 시뮬레이션 결과 ]\n\n");
        sb.append(String.format("과목명        : %s\n", r.course.getName()));
        sb.append(String.format("정원          : %d명\n", r.course.getCapacity()));
        sb.append(String.format("현재 담은 인원: %d명\n", r.course.getSavedCount()));
        sb.append(String.format("예상 증가 인원: +%d명\n", r.expectedIncrease));
        sb.append(String.format("예상 담은 인원: %d명\n\n", r.expectedSavedCount));

        int currentSuccess = course.getSuccessProbability();
        int expectedSuccess = calculateExpectedSuccessProbability(r.expectedRate);

        sb.append(String.format("현재 경쟁률   : %.2f:1  (%s, 위험점수 %d점, 성공확률 %d%%)\n",
                r.currentRate, r.currentRisk.getLabel(), r.currentScore, currentSuccess));

        sb.append(String.format("예상 경쟁률   : %.2f:1  (%s, 위험점수 %d점, 성공확률 %d%%)\n\n",
                r.expectedRate, r.expectedRisk.getLabel(), r.expectedScore, expectedSuccess));

        sb.append(String.format("위험도 변화   : [%s] -> [%s]\n\n",
                r.currentRisk.getLabel(), r.expectedRisk.getLabel()));

        sb.append("현재  ").append(makeTextBar(r.currentRate)).append(String.format(" %.2f:1\n", r.currentRate));
        sb.append("예상  ").append(makeTextBar(r.expectedRate)).append(String.format(" %.2f:1\n\n", r.expectedRate));

        sb.append("분석: ").append(r.analysisMessage).append("\n");

        txtSimResult.setText(sb.toString());
        txtSimResult.setCaretPosition(0);
    }
    private int calculateExpectedSuccessProbability(double rate) {
        if (rate <= 0) {
            return 100;
        }

        int probability = (int) Math.round(100 / rate);

        if (probability > 95) {
            return 95;
        }

        if (probability < 5) {
            return 5;
        }

        return probability;
    }
    private String makeTextBar(double rate) {
        int filled = (int) Math.min(rate * 4, 20);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            bar.append(i < filled ? "#" : "-");
        }
        bar.append("]");
        return bar.toString();
    }

    // ──────────────────────────────────────────────
    // 탭 3: 시간표 보기
    // ──────────────────────────────────────────────

    private JPanel buildTimetableTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UIHelper.COLOR_BG);

        JLabel desc = new JLabel("관심 과목 목록을 월~금 / 1~8교시 시간표 형태로 보여줍니다.");
        desc.setFont(UIHelper.FONT_BODY);
        desc.setForeground(new Color(60, 80, 120));
        p.add(desc, BorderLayout.NORTH);

        String[] cols = {"교시", "월", "화", "수", "목", "금"};
        modelTimetable = UIHelper.createReadOnlyModel(cols);

        JTable table = new JTable(modelTimetable);
        UIHelper.styleTable(table);
        table.setRowHeight(90);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);

        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(160);
            table.getColumnModel().getColumn(i).setCellRenderer(new MultiLineCellRenderer());
        }

        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBackground(UIHelper.COLOR_BG);

        txtTimetableWarning = new JTextArea(3, 20);
        txtTimetableWarning.setEditable(false);
        txtTimetableWarning.setLineWrap(true);
        txtTimetableWarning.setWrapStyleWord(true);
        txtTimetableWarning.setFont(UIHelper.FONT_BODY);
        txtTimetableWarning.setBackground(new Color(255, 248, 230));
        txtTimetableWarning.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton btnRefresh = new JButton("시간표 새로고침");
        btnRefresh.setFont(UIHelper.FONT_BODY);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBackground(new Color(37, 99, 168));
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnRefresh.addActionListener(e -> refreshTimetable());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(UIHelper.COLOR_BG);
        btnPanel.add(btnRefresh);

        bottom.add(txtTimetableWarning, BorderLayout.CENTER);
        bottom.add(btnPanel, BorderLayout.EAST);

        p.add(bottom, BorderLayout.SOUTH);

        refreshTimetable();

        return p;
    }

    private void refreshTimetable() {
        if (modelTimetable == null) {
            return;
        }

        modelTimetable.setRowCount(0);

        String[] days = {"월", "화", "수", "목", "금"};
        TimetableCell[][] cells = new TimetableCell[8][5];

        for (Course c : wishList.getCourses()) {
            String day = c.getDay();

            int dayIndex = -1;
            for (int i = 0; i < days.length; i++) {
                if (days[i].equals(day)) {
                    dayIndex = i;
                    break;
                }
            }

            if (dayIndex == -1) {
                continue;
            }

            int start = c.getStartPeriod();
            int end = c.getEndPeriod();

            for (int period = start; period <= end; period++) {
                if (period < 1 || period > 8) {
                    continue;
                }

                String text = c.getName()
                        + "\n" + c.getProfessor()
                        + "\n" + c.getRoom()
                        + "\n" + c.getRiskLevel().getLabel();

                int row = period - 1;

                if (cells[row][dayIndex] == null) {
                    cells[row][dayIndex] = new TimetableCell(text, c.getRiskLevel());
                } else {
                    String mergedText = cells[row][dayIndex].getText()
                            + "\n----------\n"
                            + text;

                    cells[row][dayIndex] = new TimetableCell(mergedText, RiskLevel.DANGER);
                }
            }
        }

        for (int period = 1; period <= 8; period++) {
            modelTimetable.addRow(new Object[]{
                    period + "교시",
                    cells[period - 1][0] == null ? "" : cells[period - 1][0],
                    cells[period - 1][1] == null ? "" : cells[period - 1][1],
                    cells[period - 1][2] == null ? "" : cells[period - 1][2],
                    cells[period - 1][3] == null ? "" : cells[period - 1][3],
                    cells[period - 1][4] == null ? "" : cells[period - 1][4]
            });
        }

        if (txtTimetableWarning != null) {
            List<String> conflicts = wishList.getTimeConflicts();

            if (conflicts.isEmpty()) {
                txtTimetableWarning.setText("시간표 충돌 과목이 없습니다.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("시간표 충돌 과목:\n");

                for (String conflict : conflicts) {
                    sb.append("- ").append(conflict).append("\n");
                }

                txtTimetableWarning.setText(sb.toString());
            }
        }
    }

    // ──────────────────────────────────────────────
    // 탭 5: 전략 리포트
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
        if (wishList.isEmpty()) {
            showMsg("관심 과목을 먼저 담아주세요.");
            return;
        }

        StrategyAnalyzer.StrategyReport r = analyzer.generateReport();
        List<Course> priorityList = analyzer.getPriorityList();

        StringBuilder sb = new StringBuilder();

        sb.append("==================================================\n");
        sb.append("              수강신청 전략 리포트\n");
        sb.append("==================================================\n\n");

        sb.append("[ 기본 요약 ]\n");
        sb.append(String.format("총 관심 과목 : %d개\n", r.totalCount));
        sb.append(String.format("총 학점 : %d학점\n", r.totalCredit));
        sb.append(String.format("학점 상태 : %s\n\n", r.creditStatus));

        sb.append("[ 위험도 요약 ]\n");
        sb.append(String.format("매우 위험 : %d개\n", r.dangerCount));
        sb.append(String.format("주의 : %d개\n", r.warningCount));
        sb.append(String.format("보통 : %d개\n", r.normalCount));
        sb.append(String.format("여유 : %d개\n\n", r.relaxedCount));

        appendTopPriorityCourses(sb, priorityList);

        if (r.mostDangerousCourse != null) {
            sb.append("[ 가장 먼저 신청할 과목 ]\n");
            sb.append(String.format("- %s\n", r.mostDangerousCourse.getName()));
            sb.append(String.format(
                    "경쟁률: %.2f:1 | 위험도: %s | 위험점수: %d점 | 성공확률: %d%%\n",
                    r.mostDangerousCourse.getCompetitionRate(),
                    r.mostDangerousCourse.getRiskLevel().getLabel(),
                    r.mostDangerousCourse.getFailureRiskScore(),
                    r.mostDangerousCourse.getSuccessProbability()
            ));
            sb.append("분석: 가장 먼저 신청해야 하는 핵심 과목입니다.\n\n");
        }

        if (!r.warningCourses.isEmpty()) {
            sb.append("[ 주의가 필요한 과목 ]\n");

            for (Course c : r.warningCourses) {
                sb.append(String.format(
                        "- %s / %.2f:1 / %s / 성공확률 %d%%\n",
                        c.getName(),
                        c.getCompetitionRate(),
                        c.getRiskLevel().getLabel(),
                        c.getSuccessProbability()
                ));
            }

            sb.append("\n");
        }

        appendTypeAnalysis(sb);
        appendAlternativeSummary(sb, priorityList);

        sb.append("[ 시간표 충돌 확인 ]\n");
        if (r.timeConflicts.isEmpty()) {
            sb.append("시간표 충돌이 없습니다.\n\n");
        } else {
            for (String conflict : r.timeConflicts) {
                sb.append("- 충돌: ").append(conflict).append("\n");
            }
            sb.append("분석: 충돌 과목이 있으므로 둘 중 하나를 삭제하거나 대체 과목으로 변경해야 합니다.\n\n");
        }

        if (!r.compositionWarnings.isEmpty()) {
            sb.append("[ 학점 구성 경고 ]\n");

            for (String w : r.compositionWarnings) {
                sb.append("- ").append(w).append("\n");
            }

            sb.append("\n");
        }

        sb.append("[ 맞춤형 플랜 안내 ]\n");
        sb.append("맞춤형 플랜은 사용자가 선택한 조건을 바탕으로 3가지 신청 순서를 생성합니다.\n");
        sb.append("추천 플랜 1 - 종합 추천형: 선택 조건을 전체적으로 반영한 기본 추천안입니다.\n");
        sb.append("추천 플랜 2 - 안정 보완형: 선택 조건을 유지하면서 성공 가능성이 높은 과목을 더 앞에 둡니다.\n");
        sb.append("추천 플랜 3 - 도전 보완형: 선택 조건을 유지하면서 경쟁률이 높은 과목을 더 앞에 둡니다.\n\n");

        sb.append("[ 세부 전략 제안 ]\n");
        sb.append("1. 매우 위험 과목은 수강신청 시작 직후 가장 먼저 신청하세요.\n");
        sb.append("2. 주의 과목은 두 번째 그룹으로 신청하세요.\n");
        sb.append("3. 전공필수 또는 교양필수 과목은 경쟁률이 낮더라도 우선순위를 높게 두세요.\n");
        sb.append("4. 실패 가능성이 높은 과목은 반드시 대체 과목을 미리 정해두세요.\n");
        sb.append("5. 시간표 충돌이 있는 경우 신청 전 관심 과목 목록을 정리하세요.\n");
        sb.append("6. 여유 과목은 마지막 순서로 신청해도 비교적 안전합니다.\n\n");

        appendFinalStrategy(sb, r, priorityList);

        sb.append("\n[ 리포트 끝 ]\n");

        txtReport.setText(sb.toString());
        txtReport.setCaretPosition(0);
    }

    private void appendTopPriorityCourses(StringBuilder sb, List<Course> priorityList) {
        sb.append("[ 최종 추천 신청 순서 TOP 5 ]\n");

        if (priorityList.isEmpty()) {
            sb.append("관심 과목이 없습니다.\n\n");
            return;
        }

        int limit = Math.min(5, priorityList.size());

        for (int i = 0; i < limit; i++) {
            Course c = priorityList.get(i);

            sb.append(String.format("%d순위. %s [%s]\n", i + 1, c.getName(), c.getType()));
            sb.append(String.format(
                    "   경쟁률 %.2f:1 | 위험도 %s | 성공확률 %d%% | %s\n",
                    c.getCompetitionRate(),
                    c.getRiskLevel().getLabel(),
                    c.getSuccessProbability(),
                    c.getRecommendReason()
            ));
        }

        sb.append("\n");
    }

    private void appendTypeAnalysis(StringBuilder sb) {
        int majorCount = 0;
        int liberalCount = 0;
        int majorCredit = 0;
        int liberalCredit = 0;

        for (Course c : wishList.getCourses()) {
            if (c.getType().startsWith("전")) {
                majorCount++;
                majorCredit += c.getCredit();
            } else if (c.getType().startsWith("교")) {
                liberalCount++;
                liberalCredit += c.getCredit();
            }
        }

        sb.append("[ 전공/교양 구성 분석 ]\n");
        sb.append(String.format("전공 과목 : %d개 / %d학점\n", majorCount, majorCredit));
        sb.append(String.format("교양 과목 : %d개 / %d학점\n", liberalCount, liberalCredit));

        if (majorCredit > liberalCredit) {
            sb.append("분석: 전공 중심 시간표입니다. 전공 과목 실패에 대비한 대체 과목 확인이 중요합니다.\n");
        } else if (liberalCredit > majorCredit) {
            sb.append("분석: 교양 비중이 높은 시간표입니다. 졸업요건상 전공 학점이 부족하지 않은지 확인하세요.\n");
        } else {
            sb.append("분석: 전공과 교양 학점 비율이 비교적 균형적입니다.\n");
        }

        sb.append("\n");
    }

    private void appendAlternativeSummary(StringBuilder sb, List<Course> priorityList) {
        sb.append("[ 위험 과목별 대체 과목 추천 ]\n");

        boolean hasDangerCourse = false;

        for (Course target : priorityList) {
            if (target.getRiskLevel() != RiskLevel.DANGER
                    && target.getRiskLevel() != RiskLevel.WARNING) {
                continue;
            }

            hasDangerCourse = true;

            sb.append(String.format(
                    "[%s] %.2f:1 / %s\n",
                    target.getName(),
                    target.getCompetitionRate(),
                    target.getRiskLevel().getLabel()
            ));

            List<Course> alternatives = analyzer.getAlternatives(target);

            if (alternatives.isEmpty()) {
                sb.append("   -> 조건에 맞는 대체 과목이 없습니다. 수동으로 추가 후보를 확인하세요.\n");
            } else {
                int limit = Math.min(3, alternatives.size());

                for (int i = 0; i < limit; i++) {
                    Course alt = alternatives.get(i);

                    sb.append(String.format(
                            "   -> 대체 %d: %s / %.2f:1 / %s / %s\n",
                            i + 1,
                            alt.getName(),
                            alt.getCompetitionRate(),
                            alt.getRiskLevel().getLabel(),
                            alt.getTimeString()
                    ));
                }
            }

            sb.append("\n");
        }

        if (!hasDangerCourse) {
            sb.append("위험 또는 주의 과목이 없어 별도 대체 과목 추천이 필요하지 않습니다.\n\n");
        }
    }

    private void appendFinalStrategy(StringBuilder sb, StrategyAnalyzer.StrategyReport r, List<Course> priorityList) {
        sb.append("[ 최종 전략 요약 ]\n");

        if (priorityList.isEmpty()) {
            sb.append("관심 과목이 없어 전략을 생성할 수 없습니다.\n");
            return;
        }

        Course first = priorityList.get(0);

        if (r.dangerCount > 0) {
            sb.append(String.format(
                    "최종 전략: '%s'처럼 경쟁률이 매우 높은 과목을 1순위로 신청하고, 실패에 대비해 대체 과목을 함께 준비하는 전략이 필요합니다.\n",
                    first.getName()
            ));
        } else if (r.warningCount > 0) {
            sb.append(String.format(
                    "최종 전략: '%s'를 포함한 주의 과목을 먼저 신청한 뒤, 보통/여유 과목은 뒤쪽 순서로 배치하는 것이 좋습니다.\n",
                    first.getName()
            ));
        } else {
            sb.append("최종 전략: 전체적으로 안정적인 시간표입니다. 다만 필수 과목과 시간표 충돌 여부를 마지막으로 확인하세요.\n");
        }

        if (!r.timeConflicts.isEmpty()) {
            sb.append("추가 주의: 시간표 충돌이 있으므로 신청 전 충돌 과목 중 하나를 제거하거나 대체 과목으로 바꾸는 것이 좋습니다.\n");
        }

        if (!r.compositionWarnings.isEmpty()) {
            sb.append("추가 주의: 학점 구성 경고가 있으므로 총 학점과 이수구분 비율을 다시 확인하세요.\n");
        }
    }

    private void saveReport() {
        if (txtReport.getText().isEmpty()) {
            showMsg("먼저 리포트를 생성하세요.");
            return;
        }

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
    // 탭 5: 맞춤형 플랜
    // ──────────────────────────────────────────────

    private JPanel buildPlanTab() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(UIHelper.COLOR_BG);

        JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        optionPanel.setBackground(UIHelper.COLOR_BG);
        optionPanel.setBorder(BorderFactory.createTitledBorder("맞춤형 플랜 조건 선택"));

        chkPreferMajor = new JCheckBox("전공 우선");
        chkPreferRequired = new JCheckBox("필수 과목 우선");
        chkPreferSafe = new JCheckBox("안정성 우선");
        chkAvoidMorning = new JCheckBox("오전 수업 피하기");
        chkPreferThreeCredit = new JCheckBox("3학점 과목 선호");

        JCheckBox[] checks = {
                chkPreferMajor,
                chkPreferRequired,
                chkPreferSafe,
                chkAvoidMorning,
                chkPreferThreeCredit
        };

        for (JCheckBox chk : checks) {
            chk.setFont(UIHelper.FONT_BODY);
            chk.setBackground(UIHelper.COLOR_BG);
            optionPanel.add(chk);
        }

        JButton btnGen = UIHelper.createButton("맞춤형 플랜 생성", UIHelper.COLOR_HEADER, 0, 0, 150, 30);
        btnGen.addActionListener(e -> generateCustomPlans());
        optionPanel.add(btnGen);

        root.add(optionPanel, BorderLayout.NORTH);

        JPanel planPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        planPanel.setBackground(UIHelper.COLOR_BG);

        String[] cols = {
                "순위", "과목명", "이수구분", "경쟁률"
        };

        JPanel panelMain = createCustomPlanPanel(
                "추천 플랜 1 - 종합 추천형",
                "선택한 조건을 전체적으로 반영한 기본 추천 순서입니다.",
                new Color(37, 99, 168),
                cols,
                1
        );

        JPanel panelSafe = createCustomPlanPanel(
                "추천 플랜 2 - 안정 보완형",
                "선택 조건을 유지하면서 성공 가능성이 높은 과목을 더 앞에 둡니다.",
                new Color(80, 130, 60),
                cols,
                2
        );

        JPanel panelChallenge = createCustomPlanPanel(
                "추천 플랜 3 - 도전 보완형",
                "선택 조건을 유지하면서 경쟁률 높은 과목을 더 앞에 둡니다.",
                new Color(180, 90, 60),
                cols,
                3
        );

        planPanel.add(panelMain);
        planPanel.add(panelSafe);
        planPanel.add(panelChallenge);

        root.add(planPanel, BorderLayout.CENTER);

        return root;
    }

    private JPanel createCustomPlanPanel(String title,
                                         String description,
                                         Color borderColor,
                                         String[] cols,
                                         int type) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(UIHelper.COLOR_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                title
        ));

        JLabel desc = new JLabel("<html><small>" + description + "</small></html>");
        desc.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panel.add(desc, BorderLayout.NORTH);

        DefaultTableModel model = UIHelper.createReadOnlyModel(cols);
        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new UIHelper.RateColorRenderer());
        setSimplePlanColumnWidths(table);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel creditLabel = new JLabel("총 학점: - | 플랜 예상 성공률: -");
        creditLabel.setFont(UIHelper.FONT_BODY);
        creditLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panel.add(creditLabel, BorderLayout.SOUTH);

        if (type == 1) {
            modelPlanMain = model;
            lblPlanMainCredit = creditLabel;
        } else if (type == 2) {
            modelPlanSafe = model;
            lblPlanSafeCredit = creditLabel;
        } else {
            modelPlanChallenge = model;
            lblPlanChallengeCredit = creditLabel;
        }

        return panel;
    }

    private void generateCustomPlans() {
        if (wishList.isEmpty()) {
            showMsg("관심 과목을 먼저 담아주세요.");
            return;
        }

        StrategyAnalyzer.CustomPlanOptions options =
                new StrategyAnalyzer.CustomPlanOptions(
                        chkPreferMajor.isSelected(),
                        chkPreferRequired.isSelected(),
                        chkPreferSafe.isSelected(),
                        chkAvoidMorning.isSelected(),
                        chkPreferThreeCredit.isSelected()
                );

        StrategyAnalyzer.CustomPlans plans = analyzer.generateCustomPlans(options);

        modelPlanMain.setRowCount(0);
        fillCustomPlanModel(modelPlanMain, plans.planMain, "종합 추천형");
        lblPlanMainCredit.setText(
                "총 학점: " + plans.planMainCredit + "학점"
                        + " | 플랜 예상 성공률: " + plans.planMainSuccessRate + "%"
        );

        modelPlanSafe.setRowCount(0);
        fillCustomPlanModel(modelPlanSafe, plans.planSafe, "안정 보완형");
        lblPlanSafeCredit.setText(
                "총 학점: " + plans.planSafeCredit + "학점"
                        + " | 플랜 예상 성공률: " + plans.planSafeSuccessRate + "%"
        );

        modelPlanChallenge.setRowCount(0);
        fillCustomPlanModel(modelPlanChallenge, plans.planChallenge, "도전 보완형");
        lblPlanChallengeCredit.setText(
                "총 학점: " + plans.planChallengeCredit + "학점"
                        + " | 플랜 예상 성공률: " + plans.planChallengeSuccessRate + "%"
        );
    }

    private void fillCustomPlanModel(DefaultTableModel model, List<Course> courses, String planType) {
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);

            model.addRow(new Object[]{
                    (i + 1) + "순위",
                    c.getName(),
                    c.getType(),
                    String.format("%.2f:1", c.getCompetitionRate())
            });
        }
    }

    private String getCustomPlanReason(Course c, String planType) {
        StringBuilder reason = new StringBuilder();
        reason.append(planType).append(" 기준");

        if (chkPreferMajor.isSelected() && c.getType() != null && c.getType().startsWith("전")) {
            reason.append(" + 전공 우선");
        }

        if (chkPreferRequired.isSelected() && ("전필".equals(c.getType()) || "교필".equals(c.getType()))) {
            reason.append(" + 필수 과목");
        }

        if (chkPreferSafe.isSelected()) {
            reason.append(" + 안정성 고려");
        }

        if (chkAvoidMorning.isSelected() && c.getStartPeriod() <= 2) {
            reason.append(" + 오전 수업 감점");
        }

        if (chkPreferThreeCredit.isSelected() && c.getCredit() == 3) {
            reason.append(" + 3학점 선호");
        }

        return reason.toString();
    }

    // ──────────────────────────────────────────────
    // 공통 메서드
    // ──────────────────────────────────────────────

    /**
     * 시뮬레이션 콤보박스를 전체 과목으로 채움
     */
    public void refreshCombos() {
        if (cbSimCourse == null) {
            return;
        }

        cbSimCourse.removeAllItems();

        for (Course c : courseManager.getCourseList()) {
            String item = c.getId() + ". " + c.getName()
                    + " (" + String.format("%.2f", c.getCompetitionRate()) + ":1)";
            cbSimCourse.addItem(item);
        }
    }

    public void refreshAll() {
        refreshPriority();
        refreshCombos();
        refreshTimetable();
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private static class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
        public MultiLineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setFont(UIHelper.FONT_BODY);
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            RiskLevel riskLevel = null;

            if (value instanceof TimetableCell) {
                TimetableCell cell = (TimetableCell) value;
                setText(cell.getText());
                riskLevel = cell.getRiskLevel();
            } else {
                setText(value == null ? "" : value.toString());
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setForeground(Color.DARK_GRAY);
                setBackground(getLightRiskColor(riskLevel));
            }

            return this;
        }

        private Color getLightRiskColor(RiskLevel riskLevel) {
            if (riskLevel == null) {
                return Color.WHITE;
            }

            switch (riskLevel) {
                case RELAXED:
                    return new Color(225, 238, 255); // 여유: 연한 파랑
                case NORMAL:
                    return new Color(225, 245, 232); // 보통: 연한 초록
                case WARNING:
                    return new Color(255, 248, 210); // 주의: 연한 노랑
                case DANGER:
                    return new Color(255, 228, 225); // 매우 위험: 연한 빨강
                default:
                    return Color.WHITE;
            }
        }
    }

    private static class TimetableCell {
        private final String text;
        private final RiskLevel riskLevel;

        public TimetableCell(String text, RiskLevel riskLevel) {
            this.text = text;
            this.riskLevel = riskLevel;
        }

        public String getText() {
            return text;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }

        @Override
        public String toString() {
            return text;
        }
    }
    private void setSimplePlanColumnWidths(JTable table) {
        table.setRowHeight(30);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // 순위
        table.getColumnModel().getColumn(1).setPreferredWidth(180);  // 과목명
        table.getColumnModel().getColumn(2).setPreferredWidth(70);   // 이수구분
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // 경쟁률
    }
}
