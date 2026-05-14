package registrationstrategy;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Swing UI 공통 유틸리티
 */
public class UIHelper {

    // ── 색상 팔레트 ──
    public static final Color COLOR_BG = new Color(245, 247, 250);
    public static final Color COLOR_HEADER = new Color(30, 80, 160);
    public static final Color COLOR_PANEL_BG = Color.WHITE;
    public static final Color COLOR_ACCENT = new Color(30, 80, 160);
    public static final Color COLOR_DANGER = new Color(200, 30, 30);
    public static final Color COLOR_WARNING = new Color(204, 120, 0);
    public static final Color COLOR_NORMAL = new Color(0, 120, 60);
    public static final Color COLOR_RELAXED = new Color(0, 100, 180);

    // ── 폰트 ──
    public static final Font FONT_TITLE = new Font("맑은 고딕", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("맑은 고딕", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("맑은 고딕", Font.PLAIN, 11);
    public static final Font FONT_BUTTON = new Font("맑은 고딕", Font.BOLD, 12);

    /**
     * 제목 테두리 패널 생성
     */
    public static JPanel createTitledPanel(String title, int x, int y, int w, int h) {
        JPanel p = new JPanel(null);
        p.setBounds(x, y, w, h);
        p.setBackground(COLOR_PANEL_BG);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1), title);
        border.setTitleFont(FONT_TITLE);
        border.setTitleColor(COLOR_HEADER);
        p.setBorder(border);
        return p;
    }

    /**
     * 읽기 전용 텍스트필드 생성
     */
    public static JTextField createReadOnlyField(int x, int y, int w) {
        JTextField f = new JTextField();
        f.setBounds(x, y, w, 22);
        f.setEditable(false);
        f.setBackground(new Color(240, 243, 250));
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        return f;
    }

    /**
     * 스타일 버튼 생성
     */
    public static JButton createButton(String text, Color bg, int x, int y, int w, int h) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, w, h);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 호버 효과
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    /**
     * 편집 불가 테이블 모델 생성
     */
    public static DefaultTableModel createReadOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    /**
     * 기본 테이블 스타일 적용
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(24);
        table.getTableHeader().setFont(FONT_TITLE);
        table.getTableHeader().setBackground(COLOR_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(220, 225, 235));
        table.setSelectionBackground(new Color(180, 205, 240));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        // 줄무늬
        table.setDefaultRenderer(Object.class, new StripedRenderer());
    }

    /**
     * 경쟁률 셀 색상 렌더러
     */
    public static class RateColorRenderer extends DefaultTableCellRenderer {
        public RateColorRenderer() {
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected && value != null) {
                try {
                    double rate = Double.parseDouble(value.toString().replace(":1", ""));
                    if (rate >= 3.0) c.setForeground(COLOR_DANGER);
                    else if (rate >= 2.0) c.setForeground(COLOR_WARNING);
                    else if (rate >= 1.0) c.setForeground(COLOR_NORMAL);
                    else c.setForeground(COLOR_RELAXED);
                } catch (NumberFormatException ignored) {
                }
            }
            return c;
        }
    }

    /**
     * 위험도 셀 배경색 렌더러
     */
    public static class RiskBgRenderer extends DefaultTableCellRenderer {
        public RiskBgRenderer() {
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected && value != null) {
                String s = value.toString();
                switch (s) {
                    case "매우 위험":
                        c.setBackground(new Color(255, 220, 220));
                        c.setForeground(COLOR_DANGER);
                        break;
                    case "주의":
                        c.setBackground(new Color(255, 240, 200));
                        c.setForeground(COLOR_WARNING);
                        break;
                    case "보통":
                        c.setBackground(new Color(220, 245, 230));
                        c.setForeground(COLOR_NORMAL);
                        break;
                    case "여유":
                        c.setBackground(new Color(210, 230, 255));
                        c.setForeground(COLOR_RELAXED);
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                        break;
                }
            } else if (isSelected) {
                c.setBackground(new Color(180, 205, 240));
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    /**
     * 진행 바 렌더러 (성공 확률 등)
     */
    public static class ProgressBarRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            if (value instanceof Integer) {
                int pct = (Integer) value;
                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue(pct);
                bar.setStringPainted(true);
                bar.setString(pct + "%");
                bar.setForeground(pct >= 60 ? COLOR_RELAXED : pct >= 40 ? COLOR_NORMAL : COLOR_DANGER);
                bar.setBackground(new Color(230, 230, 230));
                return bar;
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        }
    }

    /**
     * 줄무늬 테이블 렌더러
     */
    static class StripedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252));
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    /**
     * 레이블 + 값 쌍으로 정보 행 추가 (BoxLayout 패널용)
     */
    public static JPanel createInfoRow(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(new Color(80, 80, 80));
        JLabel val = new JLabel(value);
        val.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        val.setForeground(valueColor != null ? valueColor : Color.BLACK);
        row.add(lbl);
        row.add(val);
        return row;
    }
}
