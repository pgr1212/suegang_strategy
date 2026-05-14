package registrationstrategy;

public enum RiskLevel {
    RELAXED("여유", new java.awt.Color(0, 120, 0)),
    NORMAL("보통", new java.awt.Color(0, 153, 51)),
    WARNING("주의", new java.awt.Color(204, 120, 0)),
    DANGER("매우 위험", new java.awt.Color(200, 0, 0));

    private final String label;
    private final java.awt.Color color;

    RiskLevel(String label, java.awt.Color color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public java.awt.Color getColor() {
        return color;
    }

    public static RiskLevel fromRate(double rate) {
        if (rate < 1.0) return RELAXED;
        else if (rate < 2.0) return NORMAL;
        else if (rate < 3.0) return WARNING;
        else return DANGER;
    }
}
