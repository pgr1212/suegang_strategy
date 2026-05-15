package registrationstrategy;

public class Course {
    private int id;
    private String code;       // 학정번호 (기존 코드 호환)
    private String name;
    private String professor;
    private String type;
    private String category; // 과목 분야 (프로그래밍/데이터/시스템/교양/언어 등)// 이수구분 (전필/전선/교필/교선)
    private int capacity;
    private int savedCount;
    private int credit;
    private String day;        // 요일 (월/화/수/목/금)
    private int startPeriod;   // 시작 교시
    private int endPeriod;     // 종료 교시
    private String room;       // 강의실

    public Course(int id, String code, String name, String professor, String type, String category,
                  int capacity, int savedCount, int credit,
                  String day, int startPeriod, int endPeriod, String room) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.professor = professor;
        this.type = type;
        this.category = category;
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
    public String getCategory()  { return category; }
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
        double rate = getCompetitionRate();

        // 정원이 0명이거나 경쟁률을 계산할 수 없는 경우
        if (rate <= 0) {
            return 100;
        }

        // 경쟁률이 2.0이면 100 / 2 = 50%
        // 경쟁률이 4.0이면 100 / 4 = 25%
        int probability = (int) Math.round(100 / rate);

        // 너무 높은 값은 95%까지만 제한
        if (probability > 95) {
            return 95;
        }

        // 너무 낮은 값은 최소 5%로 제한
        if (probability < 5) {
            return 5;
        }

        return probability;
    }
    /**
     * 이수구분에 따른 중요도 점수
     * 전필 > 교필 > 전선 > 교선 순서로 우선순위를 준다.
     */
    public int getTypePriorityScore() {
        if (type == null) {
            return 0;
        }

        if (type.equals("전필")) {
            return 30;
        } else if (type.equals("교필")) {
            return 25;
        } else if (type.equals("전선")) {
            return 20;
        } else if (type.equals("교선")) {
            return 10;
        }

        return 0;
    }

    /**
     * 학점에 따른 중요도 점수
     * 보통 3학점 과목이 전공/주요 과목일 가능성이 높으므로 가산점 부여
     */
    public int getCreditPriorityScore() {
        if (credit >= 3) {
            return 10;
        } else if (credit == 2) {
            return 5;
        }

        return 0;
    }

    /**
     * 성공 확률이 낮을수록 먼저 신청해야 하므로 가산점 부여
     */
    public int getSuccessPriorityScore() {
        int success = getSuccessProbability();

        if (success <= 20) {
            return 30;
        } else if (success <= 40) {
            return 20;
        } else if (success <= 60) {
            return 10;
        }

        return 0;
    }

    /**
     * 최종 우선순위 점수
     * 경쟁률 위험도 + 이수구분 중요도 + 학점 중요도 + 성공확률 위험도를 모두 반영한다.
     */
    public int getPriorityScore() {
        return getFailureRiskScore()
                + getTypePriorityScore()
                + getCreditPriorityScore()
                + getSuccessPriorityScore();
    }
    /** 시간표 충돌 여부 확인 */
    public boolean isTimeConflict(Course other) {
        if (!this.day.equals(other.day)) return false;
        return this.startPeriod <= other.endPeriod && other.startPeriod <= this.endPeriod;
    }

    /**
     * 추천 이유 텍스트
     */
    public String getRecommendReason() {
        StringBuilder reason = new StringBuilder();

        if (getRiskLevel() == RiskLevel.DANGER) {
            reason.append("경쟁률이 매우 높음");
        } else if (getRiskLevel() == RiskLevel.WARNING) {
            reason.append("경쟁률이 높은 편");
        } else if (getRiskLevel() == RiskLevel.NORMAL) {
            reason.append("경쟁률 보통");
        } else {
            reason.append("경쟁률 여유");
        }

        if ("전필".equals(type)) {
            reason.append(" + 전공필수");
        } else if ("교필".equals(type)) {
            reason.append(" + 교양필수");
        } else if ("전선".equals(type)) {
            reason.append(" + 전공선택");
        }

        if (credit >= 3) {
            reason.append(" + 3학점 이상");
        }

        reason.append(" → 우선순위 점수 ")
                .append(getPriorityScore())
                .append("점");

        return reason.toString();
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
