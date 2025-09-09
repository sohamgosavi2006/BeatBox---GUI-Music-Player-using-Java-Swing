package beatbox_universe;

import database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent; // Import for DocumentEvent
import javax.swing.event.DocumentListener; // Import for DocumentListener

public class Home {
    private JPanel contentPanel;
    private String username;

    // Accent Color
    private final Color ACCENT = new Color(46, 204, 113); // Neon Green

    /**
     * Constructor for Home class.
     * @param contentPanel The panel to display home content.
     * @param username The logged-in user's username.
     */
    public Home(JPanel contentPanel, String username) {
        this.contentPanel = contentPanel;
        this.username = username;
        showHome();
    }

    /**
     * Displays the home page with welcome, search, and song list.
     */
    private void showHome() {
        contentPanel.removeAll();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(25, 25, 25)); // Dark modern background
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Capitalize first character of username if not empty
        String capitalizedUsername = (username != null && !username.isEmpty()) 
            ? username.substring(0, 1).toUpperCase() + username.substring(1) 
            : "Guest";
        JLabel lblTitle = new JLabel("Welcome to BeatBox " + capitalizedUsername + "!", SwingConstants.CENTER);
        lblTitle.setForeground(new Color(255, 255, 255)); // Bright white text
        lblTitle.setFont(new Font("Poppins", Font.BOLD, 24)); // Modern font (Poppins, replace if unavailable)

        // Search Bar with modern styling and auto-update
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        searchPanel.setBackground(new Color(40, 40, 40)); // Slightly lighter dark background
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JTextField txtSearch = new JTextField(20);
        txtSearch.setBackground(new Color(50, 50, 50)); // Dark input field
        txtSearch.setForeground(new Color(200, 200, 200)); // Light gray text
        txtSearch.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding
        JLabel lblSearch = new JLabel("Search Songs: ", SwingConstants.RIGHT);
        lblSearch.setForeground(new Color(100, 255, 100)); // Neon green for modern vibe
        lblSearch.setFont(new Font("Poppins", Font.PLAIN, 16));
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);

        // Songs Panel with modern vertical layout
        JPanel songsPanel = new JPanel();
        songsPanel.setLayout(new BoxLayout(songsPanel, BoxLayout.Y_AXIS)); // Vertical layout
        songsPanel.setBackground(new Color(30, 30, 30)); // Darker panel
        songsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        songsPanel.setPreferredSize(new Dimension(900, 400));

        JScrollPane scrollPane = new JScrollPane(songsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Faster scrolling

        // Auto-update search on text change
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSearch();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSearch();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSearch();
            }

            private void updateSearch() {
                String searchTerm = txtSearch.getText().trim();
                songsPanel.removeAll();
                loadSongs(songsPanel, searchTerm);
                songsPanel.revalidate();
                songsPanel.repaint();
            }
        });

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Loads songs from database and displays them (with artist and genre for better info).
     * @param songsPanel The panel to add song entries.
     * @param searchTerm The term to filter songs (empty for all).
     */
    private void loadSongs(JPanel songsPanel, String searchTerm) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                return;
            }
            String query = "SELECT id, music_title, artist, genre, file_path FROM UserMusic WHERE username = ? "
                         + (searchTerm.isEmpty() ? "" : "AND music_title LIKE ?");
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            if (!searchTerm.isEmpty()) {
                stmt.setString(2, "%" + searchTerm + "%");
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int musicId = rs.getInt("id");
                String musicTitle = rs.getString("music_title");
                String artist = rs.getString("artist");
                String genre = rs.getString("genre");
                String filePath = rs.getString("file_path");

                JPanel songPanel = new JPanel(new BorderLayout(10, 5));
                songPanel.setBackground(new Color(40, 40, 40)); // Modern card-like background
                songPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                songPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // Fixed height for consistency

                JLabel lblSong = new JLabel(musicTitle + " by " + artist + " (" + genre + ")");
                lblSong.setForeground(new Color(220, 220, 220)); // Light gray text
                lblSong.setFont(new Font("Poppins", Font.PLAIN, 14));
                lblSong.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        MusicPlayerWindow.getInstance(username).playSong(filePath, musicId);
                    }
                });

                songPanel.add(lblSong, BorderLayout.WEST);

                songsPanel.add(songPanel);
                songsPanel.add(Box.createVerticalStrut(10)); // Space between songs
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(contentPanel, "Error loading songs: " + ex.getMessage());
        }
        songsPanel.revalidate();
        songsPanel.repaint();
    }
}