package registrationstrategy;

import javax.swing.*;

/**
 * 프로그램 진입점
 * 실행 순서: SplashFrame (2초) → MainFrame
 */
public class Main {
    public static void main(String[] args) {
        // 시스템 Look & Feel 적용 (더 예쁜 UI)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            SplashFrame splash = new SplashFrame();
            splash.setVisible(true);
        });
    }
}
