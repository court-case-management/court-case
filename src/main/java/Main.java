import ui.MainFrame;

import javax.swing.*;

/**
 * Main - Entry point for Court Case Management System application.
 */
public class Main {
    public static void main(String[] args) {
        // Set System Look & Feel for native desktop experience
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Launch UI on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
