package registrationstrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * StrategyAnalyzer
 * - 수강신청 우선순위 추천
 * - 경쟁률 변화 시뮬레이션
 * - 대체 과목 추천
 * - 수강신청 전략 리포트
 * - 플랜 A/B 생성
 * - 과목 구분별 평균 경쟁률 비교
 */
public class StrategyAnalyzer {
    private final CourseManager courseManager;
    private final WishList wishList;

    public StrategyAnalyzer(CourseManager courseManager, WishList wishList) {
        this.courseManager = courseManager;
        this.wishList = wishList;
    }

    // ──────────────────────────────────────────────
    // 1. 수강신청 우선순위 추천
    // ──────────────────────────────────────────────

    /** 경쟁률 내림차순 정렬된 관심 과목 목록 반환 */
    public List<Course> getPriorityList() {
        List<Course> sorted = new ArrayList<>(wishList.getCourses());
        sorted.sort(Comparator.comparingDouble(Course::getCompetitionRate).reversed());
        return sorted;
    }

    // ──────────────────────────────────────────────
    // 2. 경쟁률 변화 시뮬레이션
    // ──────────────────────────────────────────────

    public static class SimulationResult {
        public final Course course;
        public final int expectedIncrease;
        public final int expectedSavedCount;
        public final double currentRate;
        public final double expectedRate;
        public final RiskLevel currentRisk;
        public final RiskLevel expectedRisk;
        public final int currentScore;
        public final int expectedScore;
        public final String analysisMessage;

        SimulationResult(Course course, int expectedIncrease) {
            this.course = course;
            this.expectedIncrease = expectedIncrease;
            this.expectedSavedCount = course.getSavedCount() + expectedIncrease;
            this.currentRate = course.getCompetitionRate();
            this.expectedRate = (course.getCapacity() <= 0) ? 0.0
                    : (double) expectedSavedCount / course.getCapacity();
            this.currentRisk = RiskLevel.fromRate(currentRate);
            this.expectedRisk = RiskLevel.fromRate(expectedRate);
            this.currentScore = course.getFailureRiskScore();

            // 예상 점수 계산
            int tmp;
            if (expectedRate < 0.8)       tmp = 20;
            else if (expectedRate < 1.0)  tmp = 35;
            else if (expectedRate < 1.5)  tmp = 50;
            else if (expectedRate < 2.0)  tmp = 65;
            else if (expectedRate < 3.0)  tmp = 80;
            else                          tmp = 95;
            this.expectedScore = tmp;

            // 분석 메시지
            if (expectedRisk == RiskLevel.DANGER && currentRisk != RiskLevel.DANGER)
                analysisMessage = "⚠ 인원이 조금만 늘어나도 '매우 위험' 단계로 올라갑니다. 우선순위를 높이세요.";
            else if (expectedRisk == RiskLevel.DANGER)
                analysisMessage = "⚠ 이미 매우 위험 단계이며 경쟁이 더 심해집니다. 가장 먼저 신청하거나 대체 과목을 고려하세요.";
            else if (expectedRisk == RiskLevel.WARNING)
                analysisMessage = "주의 단계입니다. 앞쪽 순서로 신청하는 것이 좋습니다.";
            else
                analysisMessage = "예상 증가 후에도 비교적 안정적입니다.";
        }
    }

    public SimulationResult simulate(Course course, int expectedIncrease) {
        return new SimulationResult(course, expectedIncrease);
    }

    // ──────────────────────────────────────────────
    // 3. 대체 과목 추천
    // ──────────────────────────────────────────────

    public List<Course> getAlternatives(Course target) {
        return courseManager.getAlternatives(target, wishList.getCourses());
    }

    // ──────────────────────────────────────────────
    // 4. 전략 리포트 데이터 (GUI에서 표시용)
    // ──────────────────────────────────────────────

    public static class StrategyReport {
        public final int totalCount;
        public final int totalCredit;
        public final String creditStatus;
        public final int dangerCount;
        public final int warningCount;
        public final int normalCount;
        public final int relaxedCount;
        public final Course mostDangerousCourse;
        public final List<Course> warningCourses;
        public final List<String> timeConflicts;
        public final List<String> compositionWarnings;

        StrategyReport(WishList wishList) {
            List<Course> courses = wishList.getCourses();
            totalCount = courses.size();
            totalCredit = wishList.getTotalCredit();
            creditStatus = wishList.getCreditStatus();

            int d = 0, w = 0, n = 0, r = 0;
            Course mostDangerous = courses.isEmpty() ? null : courses.get(0);
            List<Course> wCourses = new ArrayList<>();

            for (Course c : courses) {
                switch (c.getRiskLevel()) {
                    case DANGER:  d++; wCourses.add(c); break;
                    case WARNING: w++; wCourses.add(c); break;
                    case NORMAL:  n++; break;
                    default:      r++; break;
                }
                if (mostDangerous == null || c.getCompetitionRate() > mostDangerous.getCompetitionRate())
                    mostDangerous = c;
            }
            dangerCount = d; warningCount = w; normalCount = n; relaxedCount = r;
            mostDangerousCourse = mostDangerous;
            warningCourses = wCourses;
            timeConflicts = wishList.getTimeConflicts();
            compositionWarnings = wishList.getCompositionWarnings();
        }
    }

    public StrategyReport generateReport() {
        return new StrategyReport(wishList);
    }

    // ──────────────────────────────────────────────
    // 5. 플랜 A / B
    // ──────────────────────────────────────────────

    public static class PlanAB {
        public final List<Course> planA;   // 경쟁률 높은 순 (원하는 과목 우선)
        public final List<Course> planB;   // 위험 점수 낮은 순 (안정 전략)
        public final int planACredit;
        public final int planBCredit;

        PlanAB(WishList wishList) {
            planA = new ArrayList<>(wishList.getCourses());
            planA.sort(Comparator.comparingDouble(Course::getCompetitionRate).reversed());

            planB = new ArrayList<>(wishList.getCourses());
            planB.sort(Comparator.comparingInt(Course::getFailureRiskScore));

            planACredit = planA.stream().mapToInt(Course::getCredit).sum();
            planBCredit = planB.stream().mapToInt(Course::getCredit).sum();
        }
    }

    public PlanAB generatePlanAB() {
        return new PlanAB(wishList);
    }

    // ──────────────────────────────────────────────
    // 6. 과목 구분별 평균 경쟁률
    // ──────────────────────────────────────────────

    public Map<String, Double> getAverageRateByType() {
        return courseManager.getAverageRateByType();
    }
}
