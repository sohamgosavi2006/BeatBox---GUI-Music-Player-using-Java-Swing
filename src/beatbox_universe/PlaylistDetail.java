package beatbox_universe;

import database.DBConnection;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class PlaylistDetail {
    private JPanel contentPanel;
    private String username;
    private int playlistId;
    private String playlistName;
    private Clip currentClip; // To manage the current audio clip
    private ArrayList<String> songQueue; // To manage the playlist queue
    private JPanel songListPanel; // Instance variable for song list panel
    private JLabel lblTotalSongs; // Instance variable for total songs label

    // Modern Colors (matched to AddMusic)
    private final Color BACKGROUND = new Color(20, 20, 30); // Deeper dark background
    private final Color CARD_BG = new Color(30, 30, 45); // Card background
    private final Color ACCENT = new Color(0, 150, 136); // Teal accent for 2025 vibe
    private final Color TEXT_LIGHT = new Color(230, 230, 240); // Light text for contrast

    // Constant for music file base folder (append username/ for per-user storage)
    private static final String MUSIC_BASE_FOLDER = "/Users/soham/Library/Mobile Documents/com~apple~CloudDocs/MacBook/Visual Studio Code/Java/Academics/Semester_3/Workspace/Project/Workspace/BeatBox-MusicPlayer/Music/";

    public PlaylistDetail(JPanel contentPanel, String username, int playlistId, String playlistName) {
        this.contentPanel = contentPanel;
        this.username = username;
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.currentClip = null; // Initialize clip
        this.songQueue = new ArrayList<>(); // Initialize queue
        showPlaylistDetail();
    }

    private void showPlaylistDetail() {
        contentPanel.removeAll();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header with Title and Buttons
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = new JLabel("Playlist: " + playlistName, SwingConstants.LEFT);
        lblTitle.setForeground(TEXT_LIGHT);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 28));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        // Action Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        buttonPanel.setBackground(BACKGROUND);

        JButton btnPlay = new JButton("Play");
        styleButton(btnPlay, Color.BLACK);
        btnPlay.setPreferredSize(new Dimension(120, 40));
        btnPlay.addActionListener(e -> {
            songQueue.clear();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                    return;
                }
                String query = "SELECT m.file_path FROM UserMusic m JOIN UserPlaylistMusic pm ON m.id = pm.music_id WHERE pm.playlist_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, playlistId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    songQueue.add(rs.getString("file_path"));
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(contentPanel, "Error loading playlist: " + ex.getMessage());
            }
            if (!songQueue.isEmpty()) {
                MusicPlayerWindow.getInstance(username).setQueueAndPlay(songQueue, 0);
            } else {
                JOptionPane.showMessageDialog(contentPanel, "No songs in playlist!");
            }
        });

        JButton btnStop = new JButton("Stop");
        styleButton(btnStop, Color.BLACK);
        btnStop.setPreferredSize(new Dimension(120, 40));
        btnStop.addActionListener(e -> MusicPlayerWindow.getInstance(username).stopSong());

        JButton btnAddSong = new JButton("Add Song");
        styleButton(btnAddSong, Color.BLACK);
        btnAddSong.setPreferredSize(new Dimension(120, 40));
        btnAddSong.addActionListener(e -> addSong());

        buttonPanel.add(btnPlay);
        buttonPanel.add(btnStop);
        buttonPanel.add(btnAddSong);

        headerPanel.add(lblTitle, BorderLayout.CENTER);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Song List Panel
        songListPanel = new JPanel(); // Initialize instance variable
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));
        songListPanel.setBackground(BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(songListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        lblTotalSongs = new JLabel("Total Songs: 0", SwingConstants.CENTER); // Initialize instance variable
        lblTotalSongs.setForeground(TEXT_LIGHT);
        lblTotalSongs.setFont(new Font("Roboto", Font.PLAIN, 16));
        lblTotalSongs.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        panel.add(lblTotalSongs, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load songs
        loadSongs(songListPanel, lblTotalSongs);

        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Styles a JButton with modern 2025 look, matching AddMusic.
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
    }

    private void loadSongs(JPanel songListPanel, JLabel lblTotalSongs) {
        songListPanel.removeAll();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                return;
            }
            String query = "SELECT m.id, m.music_title, m.artist, m.genre, m.file_path FROM UserMusic m JOIN UserPlaylistMusic pm ON m.id = pm.music_id WHERE pm.playlist_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            int songCount = 0;
            while (rs.next()) {
                songCount++;
                int musicId = rs.getInt("id");
                String musicTitle = rs.getString("music_title");
                String artist = rs.getString("artist");
                String genre = rs.getString("genre");
                String filePath = rs.getString("file_path");

                JPanel songCard = new JPanel(new BorderLayout(10, 5));
                songCard.setBackground(CARD_BG);
                songCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                songCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                JLabel lblSong = new JLabel(musicTitle + " by " + artist + " (" + genre + ")");
                lblSong.setForeground(TEXT_LIGHT);
                lblSong.setFont(new Font("Roboto", Font.PLAIN, 14));
                lblSong.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        MusicPlayerWindow.getInstance(username).playSong(filePath, musicId);
                    }
                });

                JButton btnDelete = new JButton("Delete");
                styleButton(btnDelete, Color.WHITE); // Matched to AddMusic styling
                btnDelete.addActionListener(e -> deleteSong(musicId, filePath));

                songCard.add(lblSong, BorderLayout.WEST);
                songCard.add(btnDelete, BorderLayout.EAST);

                songListPanel.add(songCard);
                songListPanel.add(Box.createVerticalStrut(10));
            }
            lblTotalSongs.setText("Total Songs: " + songCount);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(contentPanel, "Error loading songs: " + ex.getMessage());
        }
        songListPanel.revalidate();
        songListPanel.repaint();
    }

    private void addSong() {
        JComboBox<String> cbMusic = new JComboBox<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                return;
            }
            String query = "SELECT id, music_title FROM UserMusic WHERE username = ? AND id NOT IN (SELECT music_id FROM UserPlaylistMusic WHERE playlist_id = ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setInt(2, playlistId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cbMusic.addItem(rs.getString("music_title"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(contentPanel, "Error loading available songs: " + ex.getMessage());
            return;
        }

        if (cbMusic.getItemCount() == 0) {
            JOptionPane.showMessageDialog(contentPanel, "No available songs to add!");
            return;
        }

        int result = JOptionPane.showConfirmDialog(contentPanel, cbMusic, "Select Song to Add", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String selectedTitle = (String) cbMusic.getSelectedItem();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                    return;
                }
                String getIdQuery = "SELECT id FROM UserMusic WHERE music_title = ? AND username = ?";
                PreparedStatement getIdStmt = conn.prepareStatement(getIdQuery);
                getIdStmt.setString(1, selectedTitle);
                getIdStmt.setString(2, username);
                ResultSet rs = getIdStmt.executeQuery();
                if (rs.next()) {
                    int musicId = rs.getInt("id");

                    String insertQuery = "INSERT INTO UserPlaylistMusic (playlist_id, music_id) VALUES (?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setInt(1, playlistId);
                    insertStmt.setInt(2, musicId);
                    insertStmt.executeUpdate();

                    String updateQuery = "UPDATE UserPlaylist SET total_songs = total_songs + 1 WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, playlistId);
                    updateStmt.executeUpdate();

                    JOptionPane.showMessageDialog(contentPanel, "Song added successfully!");
                    loadSongs(songListPanel, lblTotalSongs); // Refresh
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(contentPanel, "Error adding song: " + ex.getMessage());
            }
        }
    }

    private void deleteSong(int musicId, String filePath) {
        int confirm = JOptionPane.showConfirmDialog(contentPanel, "Are you sure you want to delete this song from all playlists and the folder?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                if (conn == null) {
                    JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                    return;
                }

                // Update total_songs in all affected playlists BEFORE deleting from UserPlaylistMusic
                String updatePlaylistsQuery = "UPDATE UserPlaylist SET total_songs = total_songs - 1 WHERE id IN "
                                            + "(SELECT playlist_id FROM UserPlaylistMusic WHERE music_id = ?)";
                PreparedStatement updatePlaylistsStmt = conn.prepareStatement(updatePlaylistsQuery);
                updatePlaylistsStmt.setInt(1, musicId);
                updatePlaylistsStmt.executeUpdate();

                // Delete from UserPlaylistMusic (all playlists)
                String deletePlaylistMusicQuery = "DELETE FROM UserPlaylistMusic WHERE music_id = ?";
                PreparedStatement deletePlaylistMusicStmt = conn.prepareStatement(deletePlaylistMusicQuery);
                deletePlaylistMusicStmt.setInt(1, musicId);
                deletePlaylistMusicStmt.executeUpdate();

                // Delete from UserMusic
                String deleteMusicQuery = "DELETE FROM UserMusic WHERE id = ?";
                PreparedStatement deleteMusicStmt = conn.prepareStatement(deleteMusicQuery);
                deleteMusicStmt.setInt(1, musicId);
                deleteMusicStmt.executeUpdate();

                // Delete the file from the folder
                File fileToDelete = new File(filePath);
                if (fileToDelete.exists()) {
                    Files.delete(fileToDelete.toPath());
                }

                JOptionPane.showMessageDialog(contentPanel, "Song deleted successfully!");
                loadSongs(songListPanel, lblTotalSongs); // Refresh using instance variables
            } catch (SQLException | IOException ex) {
                JOptionPane.showMessageDialog(contentPanel, "Error deleting song: " + ex.getMessage());
            } finally {
                try {
                    if (conn != null) conn.close(); // Always close connection
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(contentPanel, "Error closing connection: " + ex.getMessage());
                }
            }
        }
    }
}