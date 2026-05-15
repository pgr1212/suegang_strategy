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
 * - 체크박스 기반 맞춤형 플랜 생성
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

    /**
     * 다중 기준 우선순위 점수 기준으로 정렬된 관심 과목 목록 반환
     * 기준:
     * 1. 우선순위 점수 높은 순
     * 2. 점수가 같으면 경쟁률 높은 순
     * 3. 경쟁률도 같으면 학점 높은 순
     */
    public List<Course> getPriorityList() {
        List<Course> sorted = new ArrayList<>(wishList.getCourses());

        sorted.sort(
                Comparator.comparingInt(Course::getPriorityScore).reversed()
                        .thenComparing(Comparator.comparingDouble(Course::getCompetitionRate).reversed())
                        .thenComparing(Comparator.comparingInt(Course::getCredit).reversed())
        );

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

            int tmp;
            if (expectedRate < 0.8)       tmp = 20;
            else if (expectedRate < 1.0)  tmp = 35;
            else if (expectedRate < 1.5)  tmp = 50;
            else if (expectedRate < 2.0)  tmp = 65;
            else if (expectedRate < 3.0)  tmp = 80;
            else                          tmp = 95;
            this.expectedScore = tmp;

            if (expectedRisk == RiskLevel.DANGER && currentRisk != RiskLevel.DANGER) {
                analysisMessage = "주의: 인원이 조금만 늘어나도 '매우 위험' 단계로 올라갑니다. 우선순위를 높이세요.";
            } else if (expectedRisk == RiskLevel.DANGER) {
                analysisMessage = "이미 매우 위험 단계이며 경쟁이 더 심해집니다. 가장 먼저 신청하거나 대체 과목을 고려하세요.";
            } else if (expectedRisk == RiskLevel.WARNING) {
                analysisMessage = "주의 단계입니다. 앞쪽 순서로 신청하는 것이 좋습니다.";
            } else {
                analysisMessage = "예상 증가 후에도 비교적 안정적입니다.";
            }
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
    // 4. 전략 리포트 데이터
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
                    case DANGER:
                        d++;
                        wCourses.add(c);
                        break;
                    case WARNING:
                        w++;
                        wCourses.add(c);
                        break;
                    case NORMAL:
                        n++;
                        break;
                    default:
                        r++;
                        break;
                }

                if (mostDangerous == null || c.getCompetitionRate() > mostDangerous.getCompetitionRate()) {
                    mostDangerous = c;
                }
            }

            dangerCount = d;
            warningCount = w;
            normalCount = n;
            relaxedCount = r;
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
    // 5. 체크박스 기반 맞춤형 플랜 생성
    // ──────────────────────────────────────────────

    /**
     * 사용자가 체크박스로 선택한 선호 조건
     */
    public static class CustomPlanOptions {
        public final boolean preferMajor;       // 전공 우선
        public final boolean preferRequired;    // 필수 과목 우선
        public final boolean preferSafe;        // 안정성 우선
        public final boolean avoidMorning;      // 오전 수업 피하기
        public final boolean preferThreeCredit; // 3학점 과목 선호

        public CustomPlanOptions(boolean preferMajor,
                                 boolean preferRequired,
                                 boolean preferSafe,
                                 boolean avoidMorning,
                                 boolean preferThreeCredit) {
            this.preferMajor = preferMajor;
            this.preferRequired = preferRequired;
            this.preferSafe = preferSafe;
            this.avoidMorning = avoidMorning;
            this.preferThreeCredit = preferThreeCredit;
        }
    }

    /**
     * 선택 조건을 바탕으로 생성되는 3가지 맞춤형 플랜
     */
    public static class CustomPlans {
        public final List<Course> planMain;      // 종합 추천형
        public final List<Course> planSafe;      // 안정 보완형
        public final List<Course> planChallenge; // 도전 보완형

        public final int planMainSuccessRate;
        public final int planSafeSuccessRate;
        public final int planChallengeSuccessRate;

        public final int planMainCredit;
        public final int planSafeCredit;
        public final int planChallengeCredit;

        CustomPlans(WishList wishList, CustomPlanOptions options) {
            planMain = new ArrayList<>(wishList.getCourses());
            planSafe = new ArrayList<>(wishList.getCourses());
            planChallenge = new ArrayList<>(wishList.getCourses());

            planMain.sort(createCustomComparator(options, PlanMode.MAIN));

            planSafe.sort(createCustomComparator(options, PlanMode.SAFE));

            planChallenge.sort(createCustomComparator(options, PlanMode.CHALLENGE));

            planMainSuccessRate = calculatePlanSuccessRate(planMain);
            planSafeSuccessRate = calculatePlanSuccessRate(planSafe);
            planChallengeSuccessRate = calculatePlanSuccessRate(planChallenge);

            planMainCredit = planMain.stream().mapToInt(Course::getCredit).sum();
            planSafeCredit = planSafe.stream().mapToInt(Course::getCredit).sum();
            planChallengeCredit = planChallenge.stream().mapToInt(Course::getCredit).sum();
        }
        private static int calculatePlanSuccessRate(List<Course> courses) {
            if (courses == null || courses.isEmpty()) {
                return 0;
            }

            int totalWeightedSuccess = 0;
            int totalWeight = 0;

            /*
             * 플랜 성공률 계산 방식
             * - 수강신청에서는 앞순서 과목이 더 중요하므로
             * - 앞에 배치된 과목일수록 더 큰 가중치를 준다.
             *
             * 예: 과목이 5개라면
             * 1순위 가중치 5
             * 2순위 가중치 4
             * 3순위 가중치 3
             * 4순위 가중치 2
             * 5순위 가중치 1
             */
            int size = courses.size();

            for (int i = 0; i < size; i++) {
                Course c = courses.get(i);

                int weight = size - i;
                totalWeightedSuccess += c.getSuccessProbability() * weight;
                totalWeight += weight;
            }

            return Math.round((float) totalWeightedSuccess / totalWeight);
        }

        private enum PlanMode {
            MAIN, SAFE, CHALLENGE
        }
        private static Comparator<Course> createCustomComparator(CustomPlanOptions options, PlanMode mode) {
            Comparator<Course> comparator = (c1, c2) -> {
                if (options.preferMajor) {
                    int majorCompare = Integer.compare(
                            majorFirstOrder(c1.getType()),
                            majorFirstOrder(c2.getType())
                    );

                    if (majorCompare != 0) {
                        return majorCompare;
                    }
                }

                if (options.preferRequired) {
                    int requiredCompare = Integer.compare(
                            requiredFirstOrder(c1.getType()),
                            requiredFirstOrder(c2.getType())
                    );

                    if (requiredCompare != 0) {
                        return requiredCompare;
                    }
                }

                return Integer.compare(
                        getCustomScore(c2, options, mode),
                        getCustomScore(c1, options, mode)
                );
            };

            if (mode == PlanMode.SAFE) {
                comparator = comparator
                        .thenComparing(Comparator.comparingInt(Course::getSuccessProbability).reversed())
                        .thenComparing(Comparator.comparingDouble(Course::getCompetitionRate));
            } else {
                comparator = comparator
                        .thenComparing(Comparator.comparingDouble(Course::getCompetitionRate).reversed());
            }

            return comparator;
        }
        private static int majorFirstOrder(String type) {
            if (type == null) {
                return 2;
            }

            if (type.startsWith("전")) {
                return 0; // 전공 먼저
            }

            if (type.startsWith("교")) {
                return 1; // 교양 나중
            }

            return 2;
        }
        private static int requiredFirstOrder(String type) {
            if ("전필".equals(type) || "교필".equals(type)) {
                return 0;
            }

            return 1;
        }

        private static int getCustomScore(Course c, CustomPlanOptions options, PlanMode mode) {
            int score = 0;

            // 기본 점수: 기존 과목 우선순위 점수 활용
            score += c.getPriorityScore();

            // 사용자가 선택한 조건 반영
            if (options.preferMajor && c.getType() != null && c.getType().startsWith("전")) {
                score += 40;
            }

            if (options.preferRequired && ("전필".equals(c.getType()) || "교필".equals(c.getType()))) {
                score += 35;
            }

            if (options.preferSafe) {
                score += c.getSuccessProbability();

                if (c.getRiskLevel() == RiskLevel.RELAXED) {
                    score += 25;
                } else if (c.getRiskLevel() == RiskLevel.NORMAL) {
                    score += 15;
                } else if (c.getRiskLevel() == RiskLevel.DANGER) {
                    score -= 20;
                }
            }

            if (options.avoidMorning && c.getStartPeriod() <= 2) {
                score -= 30;
            }

            if (options.preferThreeCredit && c.getCredit() == 3) {
                score += 20;
            }

            // 플랜별 보정
            if (mode == PlanMode.SAFE) {
                score += c.getSuccessProbability();

                if (c.getRiskLevel() == RiskLevel.RELAXED) {
                    score += 30;
                } else if (c.getRiskLevel() == RiskLevel.NORMAL) {
                    score += 20;
                } else if (c.getRiskLevel() == RiskLevel.DANGER) {
                    score -= 30;
                }
            } else if (mode == PlanMode.CHALLENGE) {
                score += (int) (c.getCompetitionRate() * 20);

                if ("전필".equals(c.getType())) {
                    score += 20;
                }
            }

            return score;
        }
    }

    public CustomPlans generateCustomPlans(CustomPlanOptions options) {
        return new CustomPlans(wishList, options);
    }

    // ──────────────────────────────────────────────
    // 6. 과목 구분별 평균 경쟁률
    // ──────────────────────────────────────────────

    public Map<String, Double> getAverageRateByType() {
        return courseManager.getAverageRateByType();
    }
}
