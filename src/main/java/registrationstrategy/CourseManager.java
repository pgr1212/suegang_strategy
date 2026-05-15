package registrationstrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CourseManager {
    private List<Course> courseList;
    private int nextId;

    public CourseManager() {
        courseList = new ArrayList<>();
        nextId = 1;
    }

    // ──────────── 과목 추가 ────────────

    public Course addCourse(String code, String name, String professor, String type, String category,
                            int capacity, int savedCount, int credit,
                            String day, int startPeriod, int endPeriod, String room) {
        Course c = new Course(nextId++, code, name, professor, type, category,
                capacity, savedCount, credit, day, startPeriod, endPeriod, room);
        courseList.add(c);
        return c;
    }

    /** 기존 suegang 프로젝트 데이터 + 추가 샘플 과목 */
    public void loadSampleCourses() {

        // 기존 suegang 데이터 기반
        addCourse("1000-1-11", "데이터베이스", "김교수", "전필", "데이터",
                40, 205, 3, "월", 1, 2, "새404");

        addCourse("1000-2-22", "컴퓨터의이해", "이교수", "교선", "컴퓨터기초",
                100, 310, 3, "수", 3, 4, "참101");

        addCourse("1000-3-33", "자바프로그래밍", "박교수", "전필", "프로그래밍",
                35, 80, 3, "목", 2, 3, "새405");

        addCourse("1000-4-44", "현대사회와심리", "최교수", "교필", "인문사회",
                50, 75, 2, "화", 5, 6, "비202");

        addCourse("1000-5-55", "알고리즘", "정교수", "전선", "프로그래밍",
                45, 40, 3, "금", 1, 2, "새406");

        // 추가 샘플
        addCourse("1000-6-66", "객체지향프로그래밍", "한교수", "전필", "프로그래밍",
                40, 130, 3, "월", 3, 4, "새301");

        addCourse("1000-7-77", "운영체제", "오교수", "전선", "시스템",
                50, 60, 3, "화", 1, 2, "새302");

        addCourse("1000-8-88", "네트워크", "윤교수", "전선", "시스템",
                45, 35, 3, "수", 5, 6, "새303");

        addCourse("1000-9-99", "교양영어", "강교수", "교필", "언어",
                60, 55, 2, "목", 7, 8, "인101");

        addCourse("1000-10-00", "창의적사고", "신교수", "교선", "인문사회",
                80, 50, 2, "금", 3, 4, "인201");
    }

    // ──────────── 조회 ────────────

    public List<Course> getCourseList() { return courseList; }

    public Course findById(int id) {
        return courseList.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    public Course findByCode(String code) {
        return courseList.stream().filter(c -> c.getCode().equals(code)).findFirst().orElse(null);
    }

    public List<Course> search(String keyword) {
        List<Course> result = new ArrayList<>();
        String kw = keyword.toLowerCase();
        for (Course c : courseList) {
            if (c.getName().toLowerCase().contains(kw)
                    || c.getProfessor().toLowerCase().contains(kw)
                    || c.getType().toLowerCase().contains(kw)
                    || c.getCategory().toLowerCase().contains(kw)
                    || c.getCode().toLowerCase().contains(kw)) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Course> getSortedByRateDesc() {
        List<Course> sorted = new ArrayList<>(courseList);
        sorted.sort(Comparator.comparingDouble(Course::getCompetitionRate).reversed());
        return sorted;
    }

    public List<Course> getDangerCourses() {
        List<Course> result = new ArrayList<>();
        for (Course c : courseList)
            if (c.getCompetitionRate() >= 2.0) result.add(c);
        result.sort(Comparator.comparingDouble(Course::getCompetitionRate).reversed());
        return result;
    }

    /**
     * 대체 과목 추천
     * 기준: 같은 이수구분 + 낮은 경쟁률 + 보통 이하 위험도 + 시간 충돌 없음
     */
    /**
     * 대체 과목 추천
     * 기준:
     * 1. 자기 자신 제외
     * 2. 같은 이수구분
     * 3. 같은 과목 분야
     * 4. 낮은 경쟁률
     * 5. 보통 이하 위험도
     * 6. 시간표 충돌 없음
     * 7. 학점이 비슷함
     */
    /**
     * 대체 과목 추천
     * 1차: 같은 이수구분 + 같은 분야 + 낮은 경쟁률 + 안전 + 시간충돌 없음 + 유사 학점
     * 2차: 같은 이수구분 + 낮은 경쟁률 + 안전 + 시간충돌 없음 + 유사 학점
     *
     * 같은 분야 과목을 우선 추천하되,
     * 없을 경우 같은 이수구분 안에서 현실적인 대체 과목을 추천한다.
     */
    public List<Course> getAlternatives(Course target, List<Course> wishCourses) {

        List<Course> primary = new ArrayList<>();
        List<Course> secondary = new ArrayList<>();

        for (Course c : courseList) {

            if (c.getId() == target.getId()) {
                continue;
            }

            boolean sameType = c.getType().equals(target.getType());
            boolean sameCategory = c.getCategory().equals(target.getCategory());
            boolean lowerRate = c.getCompetitionRate() < target.getCompetitionRate();

            boolean safeLevel = c.getRiskLevel() == RiskLevel.RELAXED
                    || c.getRiskLevel() == RiskLevel.NORMAL;

            boolean noConflict = wishCourses.stream()
                    .filter(w -> w.getId() != target.getId())
                    .noneMatch(w -> w.isTimeConflict(c));

            boolean similarCredit = Math.abs(c.getCredit() - target.getCredit()) <= 1;

            if (sameType && lowerRate && safeLevel && noConflict && similarCredit) {
                if (sameCategory) {
                    primary.add(c);      // 가장 적합한 대체 과목
                } else {
                    secondary.add(c);    // 차선 대체 과목
                }
            }
        }

        primary.sort(Comparator.comparingDouble(Course::getCompetitionRate));
        secondary.sort(Comparator.comparingDouble(Course::getCompetitionRate));

        List<Course> result = new ArrayList<>();
        result.addAll(primary);
        result.addAll(secondary);

        return result;
    }

    /**
     * 과목 구분별 평균 경쟁률 계산
     * 반환: { "전필" -> 3.12, "전선" -> 1.45, ... }
     */
    public Map<String, Double> getAverageRateByType() {
        Map<String, List<Double>> grouped = new LinkedHashMap<>();
        for (Course c : courseList) {
            grouped.computeIfAbsent(c.getType(), k -> new ArrayList<>()).add(c.getCompetitionRate());
        }
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> entry : grouped.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            result.put(entry.getKey(), avg);
        }
        return result;
    }
}
