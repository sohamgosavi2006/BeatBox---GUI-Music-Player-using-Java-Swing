package authentication;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;

public class SignupPage {
    private JPanel contentPanel;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtVerifyPassword;

    // Simple Colors
    private final Color BACKGROUND = new Color(20, 20, 30); // Dark background
    private final Color ACCENT_ORANGE = new Color(255, 165, 0); // Vibrant orange
    private final Color ACCENT_GREEN = new Color(46, 204, 113); // Neon green
    private final Color INPUT_BG = new Color(40, 40, 50); // Dark input background
    private final Color TEXT_COLOR = Color.WHITE; // Light text
    private final Color HOVER_BG = new Color(255, 140, 0); // Slightly darker orange for hover background

    public SignupPage(JPanel contentPanel) {
        this.contentPanel = contentPanel;
        showSignup();
    }

    private void showSignup() {
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

        JLabel lblWelcome2 = new JLabel("Signup Page");
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
        txtUsername = new JTextField(20); // Increased to 20 columns
        txtUsername.setBackground(INPUT_BG);
        txtUsername.setForeground(TEXT_COLOR);
        txtUsername.setPreferredSize(new Dimension(200, 30)); // Set preferred size
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        contentPanel.add(lblUsername, gbc);
        gbc.gridx = 1;
        contentPanel.add(txtUsername, gbc);

        // Password
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setForeground(TEXT_COLOR);
        txtPassword = new JPasswordField(20); // Increased to 20 columns
        txtPassword.setBackground(INPUT_BG);
        txtPassword.setForeground(TEXT_COLOR);
        txtPassword.setPreferredSize(new Dimension(200, 30)); // Set preferred size
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(lblPassword, gbc);
        gbc.gridx = 1;
        contentPanel.add(txtPassword, gbc);

        // Verify Password
        JLabel lblVerifyPassword = new JLabel("Verify Password:");
        lblVerifyPassword.setForeground(TEXT_COLOR);
        txtVerifyPassword = new JPasswordField(20); // Increased to 20 columns
        txtVerifyPassword.setBackground(INPUT_BG);
        txtVerifyPassword.setForeground(TEXT_COLOR);
        txtVerifyPassword.setPreferredSize(new Dimension(200, 30)); // Set preferred size
        gbc.gridx = 0; gbc.gridy = 3;
        contentPanel.add(lblVerifyPassword, gbc);
        gbc.gridx = 1;
        contentPanel.add(txtVerifyPassword, gbc);

        // Signup and Go to Login Buttons (one below the other)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(BACKGROUND);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnSignup = new JButton("Signup");
        btnSignup.setBackground(ACCENT_GREEN);
        btnSignup.setForeground(Color.BLACK);
        btnSignup.setPreferredSize(new Dimension(200, 50)); // Increased size to 200x50
        btnSignup.setMaximumSize(new Dimension(200, 50));
        btnSignup.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            String verifyPassword = new String(txtVerifyPassword.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty() || verifyPassword.isEmpty()) {
                JOptionPane.showMessageDialog(contentPanel, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!password.equals(verifyPassword)) {
                JOptionPane.showMessageDialog(contentPanel, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = database.DBConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(contentPanel, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String checkQuery = "SELECT COUNT(*) FROM Users WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(contentPanel, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String insertQuery = "INSERT INTO Users (username, password) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(contentPanel, "Signup successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                contentPanel.removeAll();
                new LoginPage(contentPanel); // Switch back to LoginPage in the same panel
                contentPanel.revalidate();
                contentPanel.repaint();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(contentPanel, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Hover effect for Signup button
        btnSignup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSignup.setForeground(ACCENT_ORANGE);
                btnSignup.setFont(new Font("Arial", Font.BOLD, 14));
                btnSignup.setBackground(HOVER_BG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSignup.setForeground(Color.BLACK);
                btnSignup.setFont(new Font("Arial", Font.PLAIN, 14));
                btnSignup.setBackground(ACCENT_GREEN);
            }
        });

        JButton btnGoToLogin = new JButton("Go to Login Page");
        btnGoToLogin.setBackground(ACCENT_GREEN);
        btnGoToLogin.setForeground(Color.BLACK);
        btnGoToLogin.setPreferredSize(new Dimension(200, 50)); // Increased size to 200x50
        btnGoToLogin.setMaximumSize(new Dimension(200, 50));
        btnGoToLogin.addActionListener(e -> {
            contentPanel.removeAll();
            new LoginPage(contentPanel); // Switch to LoginPage in the same panel
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        // No hover effect for Go to Login button in Signup page
        // Keeping original colors and font

        buttonPanel.add(btnSignup);
        buttonPanel.add(Box.createVerticalStrut(10)); // Add 10-pixel gap between buttons
        buttonPanel.add(btnGoToLogin);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        contentPanel.add(buttonPanel, gbc);

        contentPanel.revalidate();
        contentPanel.repaint();
    }
}