package com.clinic.triage.util;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;

public final class UiUtils {
    private static boolean lookAndFeelConfigured;

    private UiUtils() {
    }

    public static void configureLookAndFeel() {
        if (lookAndFeelConfigured) {
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        Font font = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.getDefaults().entrySet().stream()
                .filter(entry -> entry.getValue() instanceof Font)
                .forEach(entry -> UIManager.put(entry.getKey(), font));
        lookAndFeelConfigured = true;
    }

    public static void center(Window window) {
        Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation(
                Math.max(0, (screen.width - window.getWidth()) / 2),
                Math.max(0, (screen.height - window.getHeight()) / 2));
    }

    public static JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return textArea;
    }

    public static JScrollPane wrap(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setPreferredSize(new Dimension(300, 120));
        return scrollPane;
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static void runOnUiThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
}
