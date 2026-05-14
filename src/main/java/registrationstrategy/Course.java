package registrationstrategy;

public class Course {
    private int id;
    private String code;       // 학정번호 (기존 코드 호환)
    private String name;
    private String professor;
    private String type;       // 이수구분 (전필/전선/교필/교선)
    private int capacity;
    private int savedCount;
    private int credit;
    private String day;        // 요일 (월/화/수/목/금)
    private int startPeriod;   // 시작 교시
    private int endPeriod;     // 종료 교시
    private String room;       // 강의실

    public Course(int id, String code, String name, String professor, String type,
                  int capacity, int savedCount, int credit,
                  String day, int startPeriod, int endPeriod, String room) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.professor = professor;
        this.type = type;
        this.capacity = capacity;
        this.savedCount = savedCount;
        this.credit = credit;
        this.day = day;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.room = room;
    }

    // ──────────── Getters ────────────
    public int getId()           { return id; }
    public String getCode()      { return code; }
    public String getName()      { return name; }
    public String getProfessor() { return professor; }
    public String getType()      { return type; }
    public int getCapacity()     { return capacity; }
    public int getSavedCount()   { return savedCount; }
    public int getCredit()       { return credit; }
    public String getDay()       { return day; }
    public int getStartPeriod()  { return startPeriod; }
    public int getEndPeriod()    { return endPeriod; }
    public String getRoom()      { return room; }

    public void setSavedCount(int savedCount) { this.savedCount = savedCount; }

    // ──────────── 핵심 계산 메서드 ────────────

    /** 경쟁률 = 담은 인원 / 정원 */
    public double getCompetitionRate() {
        if (capacity <= 0) return 0.0;
        return (double) savedCount / capacity;
    }

    /** 위험도 (RiskLevel enum) */
    public RiskLevel getRiskLevel() {
        return RiskLevel.fromRate(getCompetitionRate());
    }

    /**
     * 실패 위험 점수 (0~100)
     * 구간별 고정 점수 방식 - 직관적 난이도 표현용 지표
     */
    public int getFailureRiskScore() {
        double rate = getCompetitionRate();
        if (rate < 0.8)  return 20;
        if (rate < 1.0)  return 35;
        if (rate < 1.5)  return 50;
        if (rate < 2.0)  return 65;
        if (rate < 3.0)  return 80;
        return 95;
    }

    /** 수강신청 예상 성공 확률 (%) */
    public int getSuccessProbability() {
        return 100 - getFailureRiskScore();
    }

    /** 시간표 충돌 여부 확인 */
    public boolean isTimeConflict(Course other) {
        if (!this.day.equals(other.day)) return false;
        return this.startPeriod <= other.endPeriod && other.startPeriod <= this.endPeriod;
    }

    /** 추천 이유 텍스트 */
    public String getRecommendReason() {
        switch (getRiskLevel()) {
            case DANGER:  return "경쟁률이 매우 높으므로 가장 먼저 신청하는 것이 좋습니다.";
            case WARNING: return "경쟁률이 높은 편이므로 우선순위를 앞쪽에 두는 것이 좋습니다.";
            case NORMAL:  return "경쟁률이 보통 수준이므로 중간 순서로 신청해도 괜찮습니다.";
            default:      return "비교적 여유가 있으므로 뒤쪽 순서로 신청해도 괜찮습니다.";
        }
    }

    /**
     * 경쟁률 텍스트 바 (콘솔 시각화용)
     * 예: "████████░░░░░░░░░░░░ 2.00:1"
     */
    public String getRateBar() {
        int filled = (int) Math.min(getCompetitionRate() * 4, 20);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) sb.append(i < filled ? "█" : "░");
        sb.append(String.format("] %.2f:1", getCompetitionRate()));
        return sb.toString();
    }

    /** 강의 시간 문자열 (예: "월 3-4교시") */
    public String getTimeString() {
        return day + " " + startPeriod + "-" + endPeriod + "교시";
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) | %s 교수 | %s | %.2f:1 | %s",
                id, name, type, professor, getTimeString(),
                getCompetitionRate(), getRiskLevel().getLabel());
    }
}
