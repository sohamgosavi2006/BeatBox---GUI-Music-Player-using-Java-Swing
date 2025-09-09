package authentication;

import beatbox_universe.MusicPlayerWindow;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class LoginPage {
    private JPanel contentPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Simple Colors
    private final Color BACKGROUND = new Color(20, 20, 30); // Dark background
    private final Color ACCENT_ORANGE = new Color(255, 165, 0); // Vibrant orange
    private final Color ACCENT_GREEN = new Color(46, 204, 113); // Neon green
    private final Color INPUT_BG = new Color(40, 40, 50); // Dark input background
    private final Color TEXT_COLOR = Color.WHITE; // Light text
    private final Color HOVER_BG = new Color(255, 140, 0); // Slightly darker orange for hover background

    public LoginPage(JPanel contentPanel) {
        this.contentPanel = contentPanel;
        initializeUI();
    }

    private void initializeUI() {
        contentPanel.removeAll();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Welcome Text with two lines
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(BACKGROUND);
        GridBagConstraints wgbc = new GridBagConstraints();
        wgbc.anchor = GridBagConstraints.CENTER;
        wgbc.insets = new Insets(0, 0, 20, 0); // Increased vertical gap between lines

        JLabel lblWelcome1 = new JLabel("Welcome to BeatBox!");
        lblWelcome1.setForeground(ACCENT_ORANGE);
        lblWelcome1.setFont(new Font("Arial", Font.BOLD, 40)); // Doubled font size to 40
        wgbc.gridx = 0; wgbc.gridy = 0;
        welcomePanel.add(lblWelcome1, wgbc);

        JLabel lblWelcome2 = new JLabel("Login Page");
        lblWelcome2.setForeground(ACCENT_GREEN);
        lblWelcome2.setFont(new Font("Arial", Font.BOLD, 16));
        wgbc.gridy = 1;
        welcomePanel.add(lblWelcome2, wgbc);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(welcomePanel, gbc);

        // Username
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setForeground(TEXT_COLOR);
        usernameField = new JTextField(20); // Increased to 20 columns
        usernameField.setBackground(INPUT_BG);
        usernameField.setForeground(TEXT_COLOR);
        usernameField.setPreferredSize(new Dimension(200, 30)); // Set preferred size
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        contentPanel.add(lblUsername, gbc);
        gbc.gridx = 1;
        contentPanel.add(usernameField, gbc);

        // Password
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setForeground(TEXT_COLOR);
        passwordField = new JPasswordField(20); // Increased to 20 columns
        passwordField.setBackground(INPUT_BG);
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setPreferredSize(new Dimension(200, 30)); // Set preferred size
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(lblPassword, gbc);
        gbc.gridx = 1;
        contentPanel.add(passwordField, gbc);

        // Login and Signup Buttons (one below the other)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(BACKGROUND);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(ACCENT_GREEN);
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setPreferredSize(new Dimension(200, 50)); // Increased size to 200x50
        btnLogin.setMaximumSize(new Dimension(200, 50));
        btnLogin.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = String.valueOf(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(contentPanel, "Please enter both username and password!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = database.DBConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(contentPanel, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(contentPanel, "Login Successful!");
                    ((JFrame) SwingUtilities.getWindowAncestor(contentPanel)).dispose();
                    MusicPlayerWindow.getInstance(username).setVisible(true);
                    MusicPlayerWindow.getInstance(username).setSize(1200, 800);
                    MusicPlayerWindow.getInstance(username).setLocationRelativeTo(null);
                } else {
                    JOptionPane.showMessageDialog(contentPanel, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(contentPanel, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Hover effect for Login button
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogin.setForeground(ACCENT_ORANGE);
                btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
                btnLogin.setBackground(HOVER_BG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnLogin.setForeground(Color.BLACK);
                btnLogin.setFont(new Font("Arial", Font.PLAIN, 14));
                btnLogin.setBackground(ACCENT_GREEN);
            }
        });

        JButton btnSignup = new JButton("Signup");
        btnSignup.setBackground(ACCENT_GREEN);
        btnSignup.setForeground(Color.BLACK);
        btnSignup.setPreferredSize(new Dimension(200, 50)); // Increased size to 200x50
        btnSignup.setMaximumSize(new Dimension(200, 50));
        btnSignup.addActionListener(e -> {
            contentPanel.removeAll();
            new SignupPage(contentPanel); // Switch to SignupPage in the same panel
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        // No hover effect for Signup button in Login page
        // Keeping original colors and font

        buttonPanel.add(btnLogin);
        buttonPanel.add(Box.createVerticalStrut(10)); // Add 10-pixel gap between buttons
        buttonPanel.add(btnSignup);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        contentPanel.add(buttonPanel, gbc);

        contentPanel.revalidate();
        contentPanel.repaint();
    }
}