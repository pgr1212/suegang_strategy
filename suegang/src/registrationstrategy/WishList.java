package registrationstrategy;

import java.util.ArrayList;
import java.util.List;

public class WishList {
    private List<Course> courses;

    public WishList() {
        courses = new ArrayList<>();
    }

    // ──────────── 추가 / 삭제 ────────────

    /**
     * 관심 과목 추가
     *
     * @return 0: 성공, 1: 이미 담김, 2: 시간 충돌
     */
    public int add(Course course) {
        if (courses.contains(course)) return 1;
        Course conflict = getConflictingCourse(course);
        if (conflict != null) return 2; // 충돌 과목 있음 (경고용, 강제 추가는 호출자가 결정)
        courses.add(course);
        return 0;
    }

    /**
     * 충돌 무시하고 강제 추가
     */
    public void forceAdd(Course course) {
        if (!courses.contains(course)) courses.add(course);
    }

    public boolean remove(int id) {
        return courses.removeIf(c -> c.getId() == id);
    }

    public boolean contains(Course course) {
        return courses.contains(course);
    }

    // ──────────── 조회 ────────────

    public List<Course> getCourses() {
        return courses;
    }

    public boolean isEmpty() {
        return courses.isEmpty();
    }

    /**
     * 시간표 충돌 상대 과목 반환 (없으면 null)
     */
    public Course getConflictingCourse(Course incoming) {
        for (Course c : courses)
            if (c.isTimeConflict(incoming)) return c;
        return null;
    }

    // ──────────── 학점 계산 ────────────

    public int getTotalCredit() {
        return courses.stream().mapToInt(Course::getCredit).sum();
    }

    public String getCreditStatus() {
        int total = getTotalCredit();
        if (total < 12) return "⚠ 12학점 미만 — 최소 학점 기준을 확인하세요.";
        if (total > 21) return "⚠ 21학점 초과 — 신청 가능 학점을 확인하세요.";
        return "✔ 적절한 학점입니다.";
    }

    // ──────────── 충돌 목록 ────────────

    /**
     * 관심 과목 내 전체 시간 충돌 쌍 반환
     */
    public List<String> getTimeConflicts() {
        List<String> conflicts = new ArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            for (int j = i + 1; j < courses.size(); j++) {
                Course a = courses.get(i), b = courses.get(j);
                if (a.isTimeConflict(b))
                    conflicts.add(a.getName() + " ↔ " + b.getName());
            }
        }
        return conflicts;
    }

    // ──────────── 이수구분 구성 경고 ────────────

    /**
     * 이수구분 구성 경고 메시지 목록
     */
    public List<String> getCompositionWarnings() {
        List<String> warnings = new ArrayList<>();
        long mandatoryCount = courses.stream()
                .filter(c -> c.getType().contains("필")).count();
        long electiveCount = courses.stream()
                .filter(c -> c.getType().contains("선")).count();

        if (mandatoryCount == 0)
            warnings.add("전공필수 과목이 없습니다. 졸업 요건을 확인하세요.");
        if (courses.size() > 0) {
            long generalCount = courses.stream()
                    .filter(c -> c.getType().startsWith("교")).count();
            int generalPct = (int) ((double) generalCount / courses.size() * 100);
            if (generalPct >= 60)
                warnings.add("교양 과목이 전체의 " + generalPct + "%를 차지합니다. 전공 비율을 확인하세요.");
        }
        return warnings;
    }
}
