package registrationstrategy;

import javax.swing.*;
import java.awt.*;

/**
 * SplashFrame - 로딩 스플래시 화면 (기존 Form1 역할)
 * 2초 후 자동으로 MainFrame으로 이동
 */
public class SplashFrame extends JFrame {

    public SplashFrame() {
        setUndecorated(true);
        setSize(480, 280);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 그라데이션 배경
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 60, 140),
                        getWidth(), getHeight(), new Color(40, 100, 200));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        panel.setOpaque(false);
        setContentPane(panel);

        // 아이콘 이모지 대용 텍스트
        JLabel icon = new JLabel("📚", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setBounds(0, 40, 480, 60);
        panel.add(icon);

        JLabel title = new JLabel("경쟁률 기반 수강신청 전략 추천 시스템", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 110, 480, 30);
        panel.add(title);

        JLabel sub = new JLabel("수강신청 전에 전략을 세워주는 프로그램", SwingConstants.CENTER);
        sub.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        sub.setForeground(new Color(180, 210, 255));
        sub.setBounds(0, 148, 480, 24);
        panel.add(sub);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(60, 196, 360, 10);
        progressBar.setStringPainted(false);
        progressBar.setForeground(new Color(100, 180, 255));
        progressBar.setBackground(new Color(40, 70, 150));
        progressBar.setBorderPainted(false);
        panel.add(progressBar);

        JLabel loading = new JLabel("로딩 중...", SwingConstants.CENTER);
        loading.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        loading.setForeground(new Color(150, 190, 255));
        loading.setBounds(0, 212, 480, 20);
        panel.add(loading);

        // 타이머로 프로그레스바 채우기 + 2초 후 전환
        Timer progressTimer = new Timer(20, null);
        progressTimer.addActionListener(e -> {
            int val = progressBar.getValue() + 1;
            progressBar.setValue(val);
            if (val >= 100) {
                progressTimer.stop();
                dispose();
                SwingUtilities.invokeLater(() -> {
                    MainFrame main = new MainFrame();
                    main.setVisible(true);
                });
            }
        });
        progressTimer.start();
    }
}
