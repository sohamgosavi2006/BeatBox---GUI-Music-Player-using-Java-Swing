package beatbox_universe;

import authentication.SignupPage;
import database.DBConnection;
import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.swing.*;

public class Profile {
    private JPanel contentPanel;
    private String username;

    // Accent Color
    private final Color ACCENT = new Color(46, 204, 113); // Neon Green
    private final Color DELETE_COLOR = new Color(255, 0, 0); // Red
    // Constant for music file base folder
    private static final String MUSIC_BASE_FOLDER = "/Users/soham/Library/Mobile Documents/com~apple~CloudDocs/MacBook/Visual Studio Code/Java/Academics/Semester_3/Workspace/Project/Workspace/BeatBox-MusicPlayer/Music/";

    public Profile(JPanel contentPanel, String username) {
        this.contentPanel = contentPanel;
        this.username = username;
        showProfile();
    }

    private void showProfile() {
        contentPanel.removeAll();

        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Circular Initial Box
        JPanel initialPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(ACCENT);
                g.fillOval(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.setFont(new Font("Segoe UI", Font.BOLD, 40));
                FontMetrics fm = g.getFontMetrics();
                String initials = username.substring(0, 1).toUpperCase();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 5;
                g.drawString(initials, x, y);
            }
        };
        initialPanel.setPreferredSize(new Dimension(100, 100));
        initialPanel.setBackground(new Color(25, 25, 25));

        // User Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        infoPanel.setBackground(new Color(25, 25, 25));

        String capitalizedUsername = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
        JLabel lblName = new JLabel("Name: " + capitalizedUsername);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));

        int songCount = getSongCount();
        JLabel lblSongs = new JLabel("Songs Added: " + songCount);
        lblSongs.setForeground(Color.WHITE);
        lblSongs.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        infoPanel.add(lblName);
        infoPanel.add(lblSongs);

        JPanel topPanel = new JPanel(new BorderLayout(20, 20));
        topPanel.setBackground(new Color(25, 25, 25));
        topPanel.add(initialPanel, BorderLayout.WEST);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        // Change Password Section
        JPanel changePassPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        changePassPanel.setBackground(new Color(25, 25, 25));

        JLabel lblChangePass = new JLabel("Change Password:");
        lblChangePass.setForeground(Color.WHITE);
        lblChangePass.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton btnChangePass = new JButton("Change Password");
        btnChangePass.setBackground(ACCENT);
        btnChangePass.setForeground(Color.BLACK);
        btnChangePass.setPreferredSize(new Dimension(150, 30));
        btnChangePass.addActionListener(e -> changePassword());

        changePassPanel.add(lblChangePass);
        changePassPanel.add(btnChangePass);

        // Delete Account Section
        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
        deletePanel.setBackground(new Color(25, 25, 25));

        JButton btnDeleteAccount = new JButton("Delete My BeatBox Account");
        btnDeleteAccount.setBackground(DELETE_COLOR);
        btnDeleteAccount.setForeground(DELETE_COLOR);
        btnDeleteAccount.setPreferredSize(new Dimension(200, 40));
        btnDeleteAccount.addActionListener(e -> deleteAccount());

        deletePanel.add(btnDeleteAccount);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(changePassPanel, BorderLayout.CENTER);
        panel.add(deletePanel, BorderLayout.SOUTH);

        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private int getSongCount() {
        int count = 0;
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return 0;
            }
            String query = "SELECT COUNT(*) FROM UserMusic WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(contentPanel, "Error fetching song count: " + ex.getMessage());
        }
        return count;
    }

    private void changePassword() {
        String currentPass = JOptionPane.showInputDialog(contentPanel, "Enter current password:");
        if (currentPass == null) return;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                return;
            }
            String query = "SELECT password FROM Users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPass = rs.getString("password");
                if (storedPass.equals(currentPass)) {
                    String newPass = JOptionPane.showInputDialog(contentPanel, "Enter new password:");
                    if (newPass != null && !newPass.trim().isEmpty()) {
                        String updateQuery = "UPDATE Users SET password = ? WHERE username = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setString(1, newPass);
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();
                        JOptionPane.showMessageDialog(contentPanel, "Password changed successfully!");
                    }
                } else {
                    JOptionPane.showMessageDialog(contentPanel, "Incorrect current password!");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(contentPanel, "Error changing password: " + ex.getMessage());
        }
    }

    private void deleteAccount() {
        String password = JOptionPane.showInputDialog(contentPanel, "Enter your password to delete your account:");
        if (password == null) return;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                return;
            }

            // Start transaction
            conn.setAutoCommit(false);

            // Verify password
            String query = "SELECT password FROM Users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPass = rs.getString("password");
                if (storedPass.equals(password)) {
                    // Delete user from Users table (triggers cascade deletes)
                    String deleteUserQuery = "DELETE FROM Users WHERE username = ?";
                    PreparedStatement deleteUserStmt = conn.prepareStatement(deleteUserQuery);
                    deleteUserStmt.setString(1, username);
                    deleteUserStmt.executeUpdate();

                    // Delete user folder and contents (excluding main Music folder)
                    File userFolder = new File(MUSIC_BASE_FOLDER + username);
                    if (userFolder.exists() && userFolder.isDirectory()) {
                        deleteFolder(userFolder);
                    }

                    // Commit transaction
                    conn.commit();
                    JOptionPane.showMessageDialog(contentPanel, "Account and all data deleted successfully!");
                    JFrame signUpFrame = new JFrame("Login - BeatBox");
                    signUpFrame.setSize(400, 300);
                    signUpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    JPanel signUpPanel = new JPanel();
                    signUpFrame.add(signUpPanel);
                    signUpFrame.setLocationRelativeTo(null);
                    // Switch to SignupPage after deletion
                 signUpFrame.setSize(400, 300);
            signUpFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            signUpFrame.setLocationRelativeTo(null);
            signUpFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                signUpFrame.add(signUpPanel);
                new SignupPage(signUpPanel);
                 signUpFrame.setVisible(true);
                    MusicPlayerWindow.getInstance(username).dispose();
                } else {
                    JOptionPane.showMessageDialog(contentPanel, "Incorrect password!");
                    conn.rollback();
                }
            }
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    JOptionPane.showMessageDialog(contentPanel, "Error rolling back: " + rollbackEx.getMessage());
                }
            }
            JOptionPane.showMessageDialog(contentPanel, "Error deleting account: " + ex.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(contentPanel, "Error closing connection: " + ex.getMessage());
                }
            }
        }
    }

    private void deleteFolder(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
            folder.delete();
        }
    }
}