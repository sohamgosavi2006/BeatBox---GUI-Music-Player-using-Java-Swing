import authentication.LoginPage;
import java.awt.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BeatBox");
            frame.setSize(400, 300); // Increased size for better visibility
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            // Maximize the frame
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            JPanel contentPanel = new JPanel(new CardLayout());
            frame.add(contentPanel);

            new LoginPage(contentPanel); // Start with LoginPage
            frame.setVisible(true);
        });
    }
}