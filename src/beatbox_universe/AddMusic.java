package beatbox_universe;

import database.DBConnection;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AddMusic {
    private JPanel contentPanel;
    private String username; // To associate music with the logged-in user

    // Modern Colors
    private final Color BACKGROUND = new Color(20, 20, 30); // Deeper dark background
    private final Color CARD_BG = new Color(30, 30, 45); // Card background with gradient potential
    private final Color ACCENT = new Color(0, 150, 136); // Teal accent for 2025 vibe
    private final Color TEXT_LIGHT = new Color(230, 230, 240); // Light text for contrast

    // Constant for music file destination base
    private static final String MUSIC_BASE_FOLDER = "/Users/soham/Library/Mobile Documents/com~apple~CloudDocs/MacBook/Visual Studio Code/Java/Academics/Semester_3/Workspace/Project/Workspace/BeatBox-MusicPlayer/Music/";

    /**
     * Constructor for AddMusic class.
     * @param contentPanel The panel to display add music form.
     * @param username The logged-in user's username.
     */
    public AddMusic(JPanel contentPanel, String username) {
        this.contentPanel = contentPanel;
        this.username = username;
        showAddMusic();
    }

    /**
     * Displays the modernized form for adding new music and list for removing existing music.
     */
    private void showAddMusic() {
        contentPanel.removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout(20, 30));
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Top label for file type restriction with modern styling
        JLabel lblFileRestriction = new JLabel("Only (.wav) Files are Allowed", SwingConstants.CENTER);
        lblFileRestriction.setForeground(ACCENT);
        lblFileRestriction.setFont(new Font("Roboto", Font.BOLD, 18));
        lblFileRestriction.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        mainPanel.add(lblFileRestriction, BorderLayout.NORTH);

        // Center panel with card layout
        JPanel centeredPanel = new JPanel(new GridBagLayout());
        centeredPanel.setBackground(BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 25, 20, 25);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Add Music Form Card
        JPanel formCard = new JPanel(new GridLayout(9, 2, 20, 15));
        formCard.setBackground(CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 40, 60), 2, true),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        formCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Inner padding

        String[] labels = {"Artist:", "Music Title:", "Genre:", "Music File:"};
        JTextField txtArtist = new JTextField();
        styleTextField(txtArtist);
        JTextField txtMusicTitle = new JTextField();
        styleTextField(txtMusicTitle);
        JComboBox<String> cbGenre = new JComboBox<>(new String[]{"Classical", "Pop", "Rock", "Jazz", "Hip-Hop", "Electronic", "Country", "R&B", "Reggae", "Metal"});
        cbGenre.setBackground(CARD_BG);
        cbGenre.setForeground(Color.BLACK);
        cbGenre.setFont(new Font("Roboto", Font.BOLD, 14));
        cbGenre.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JTextField txtFilePath = new JTextField(20);
        styleTextField(txtFilePath);
        txtFilePath.setEditable(false);

        JButton btnChooseFile = new JButton("Browse");
        styleButton(btnChooseFile, Color.WHITE);
        btnChooseFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".wav");
                }
                @Override
                public String getDescription() {
                    return "WAV Files (*.wav)";
                }
            });
            int result = fileChooser.showOpenDialog(contentPanel);
            if (result == JFileChooser.APPROVE_OPTION) {
                txtFilePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton btnSave = new JButton("💾 Save");
        styleButton(btnSave, Color.WHITE); // White text
        btnSave.setForeground(Color.WHITE); // Ensure text is white
        btnSave.addActionListener(e -> {
            String artist = txtArtist.getText().trim();
            String musicTitle = txtMusicTitle.getText().trim();
            String genre = (String) cbGenre.getSelectedItem();
            String filePath = txtFilePath.getText().trim();

            if (artist.isEmpty() || musicTitle.isEmpty() || genre.isEmpty() || filePath.isEmpty()) {
                JOptionPane.showMessageDialog(contentPanel, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File sourceFile = new File(filePath);
            if (!sourceFile.exists() || !filePath.toLowerCase().endsWith(".wav")) {
                JOptionPane.showMessageDialog(contentPanel, "Only .wav files are allowed!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String destinationFolder = MUSIC_BASE_FOLDER + username + "/";
            File destFolder = new File(destinationFolder);
            if (!destFolder.exists()) destFolder.mkdirs();
            String destinationPath = destinationFolder + sourceFile.getName();
            File destFile = new File(destinationPath);

            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Connection conn = DBConnection.getConnection();
                if (conn != null) {
                    String query = "INSERT INTO UserMusic (username, artist, music_title, genre, file_path) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, username);
                    stmt.setString(2, artist);
                    stmt.setString(3, musicTitle);
                    stmt.setString(4, genre);
                    stmt.setString(5, destinationPath);
                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(contentPanel, "Music saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        showAddMusic();
                    }
                    conn.close();
                }
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(contentPanel, "Error saving music: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add components to form with proper alignment
        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setForeground(TEXT_LIGHT);
            lbl.setFont(new Font("Roboto", Font.PLAIN, 16));
            formCard.add(lbl);
            switch (i) {
                case 0: formCard.add(txtArtist); break;
                case 1: formCard.add(txtMusicTitle); break;
                case 2: formCard.add(cbGenre); break;
                case 3:
                    formCard.add(txtFilePath);
                    gbc.gridy++;
                    formCard.add(btnChooseFile);
                    gbc.gridy++;
                    formCard.add(btnSave);
                    break;
            }
        }
        formCard.add(new JLabel());
        formCard.add(new JLabel());

        gbc.gridx = 0;
        gbc.gridy = 0;
        centeredPanel.add(formCard, gbc);

        // Music List Panel with automatic search
        JPanel musicListPanel = new JPanel();
        musicListPanel.setLayout(new BoxLayout(musicListPanel, BoxLayout.Y_AXIS));
        musicListPanel.setBackground(BACKGROUND);
        musicListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtSearch = new JTextField(20);
        styleTextField(txtSearch);
        JLabel lblSearch = new JLabel("Search Music: ", SwingConstants.RIGHT);
        lblSearch.setForeground(ACCENT);
        lblSearch.setFont(new Font("Roboto", Font.PLAIN, 16));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        searchPanel.setBackground(BACKGROUND);
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        centeredPanel.add(searchPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(20, 0, 30, 0), 0, 0));

        JScrollPane scrollPane = new JScrollPane(musicListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ACCENT;
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateSearch(); }

            private void updateSearch() {
                String searchTerm = txtSearch.getText().trim().toLowerCase();
                musicListPanel.removeAll();
                loadMusicList(musicListPanel, searchTerm);
                musicListPanel.revalidate();
                musicListPanel.repaint();
            }
        });

        loadMusicList(musicListPanel);

        mainPanel.add(centeredPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        contentPanel.add(mainPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Styles a JTextField with modern 2025 look.
     */
    private void styleTextField(JTextField textField) {
        textField.setBackground(CARD_BG);
        textField.setForeground(TEXT_LIGHT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 40, 60), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        textField.setFont(new Font("Roboto", Font.PLAIN, 14));
    }

    /**
     * Styles a JButton with modern 2025 look and hover effect.
     */
    private void styleButton(JButton button, Color textColor) {
        button.setBackground(ACCENT);
        button.setForeground(textColor);
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT.darker(), 2, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT.darker(), 2, true),
                    BorderFactory.createEmptyBorder(9, 16, 9, 16)
                ));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT.darker(), 2, true),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
    }

    /**
     * Loads the list of music for the user with search filtering.
     */
    private void loadMusicList(JPanel musicListPanel, String searchTerm) {
        musicListPanel.removeAll();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(contentPanel, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String query = "SELECT id, music_title, artist, genre, file_path FROM UserMusic WHERE username = ? "
                         + (searchTerm.isEmpty() ? "" : "AND LOWER(music_title) LIKE ?");
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            if (!searchTerm.isEmpty()) stmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int musicId = rs.getInt("id");
                String musicTitle = rs.getString("music_title");
                String artist = rs.getString("artist");
                String genre = rs.getString("genre");
                String filePath = rs.getString("file_path");

                if (!searchTerm.isEmpty() && !musicTitle.toLowerCase().contains(searchTerm)) continue;

                JPanel musicCard = new JPanel(new BorderLayout(15, 10));
                musicCard.setBackground(CARD_BG);
                musicCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(40, 40, 60), 1, true),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
                musicCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

                JLabel lblMusic = new JLabel(musicTitle + " by " + artist + " (" + genre + ")");
                lblMusic.setForeground(TEXT_LIGHT);
                lblMusic.setFont(new Font("Roboto", Font.PLAIN, 14));

                JButton btnDelete = new JButton("Delete");
                styleButton(btnDelete, Color.WHITE); // White text
                btnDelete.addActionListener(e -> deleteMusic(musicId, filePath));

                musicCard.add(lblMusic, BorderLayout.CENTER);
                musicCard.add(btnDelete, BorderLayout.EAST);

                musicListPanel.add(musicCard);
                musicListPanel.add(Box.createVerticalStrut(15));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(contentPanel, "Error loading music: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        musicListPanel.revalidate();
        musicListPanel.repaint();
    }

    /**
     * Overloaded method for initial load without search term.
     */
    private void loadMusicList(JPanel musicListPanel) {
        loadMusicList(musicListPanel, "");
    }

    /**
     * Deletes the selected music from database and file system.
     */
    private void deleteMusic(int musicId, String filePath) {
        int confirm = JOptionPane.showConfirmDialog(contentPanel, "Are you sure you want to delete this music?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                if (conn == null) {
                    JOptionPane.showMessageDialog(contentPanel, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String updatePlaylistsQuery = "UPDATE UserPlaylist SET total_songs = total_songs - 1 WHERE id IN (SELECT playlist_id FROM UserPlaylistMusic WHERE music_id = ?)";
                PreparedStatement updatePlaylistsStmt = conn.prepareStatement(updatePlaylistsQuery);
                updatePlaylistsStmt.setInt(1, musicId);
                updatePlaylistsStmt.executeUpdate();
                String deletePlaylistMusicQuery = "DELETE FROM UserPlaylistMusic WHERE music_id = ?";
                PreparedStatement deletePlaylistMusicStmt = conn.prepareStatement(deletePlaylistMusicQuery);
                deletePlaylistMusicStmt.setInt(1, musicId);
                deletePlaylistMusicStmt.executeUpdate();
                String deleteMusicQuery = "DELETE FROM UserMusic WHERE id = ?";
                PreparedStatement deleteMusicStmt = conn.prepareStatement(deleteMusicQuery);
                deleteMusicStmt.setInt(1, musicId);
                deleteMusicStmt.executeUpdate();
                File fileToDelete = new File(filePath);
                if (fileToDelete.exists()) Files.delete(fileToDelete.toPath());
                JOptionPane.showMessageDialog(contentPanel, "Music deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                showAddMusic();
            } catch (SQLException | IOException ex) {
                JOptionPane.showMessageDialog(contentPanel, "Error deleting music: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if (conn != null) conn.close(); } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(contentPanel, "Error closing connection: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}