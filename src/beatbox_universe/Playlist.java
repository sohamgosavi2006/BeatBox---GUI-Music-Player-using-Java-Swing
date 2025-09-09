package beatbox_universe;

import database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class Playlist {
    private JPanel contentPanel;
    private String username;

    // Accent Color
    private final Color ACCENT = new Color(46, 204, 113); // Neon Green

    /**
     * Constructor for Playlist class.
     * @param contentPanel The panel to display playlists.
     * @param username The logged-in user's username.
     */
    public Playlist(JPanel contentPanel, String username) {
        this.contentPanel = contentPanel;
        this.username = username;
        showPlaylist();
    }

    /**
     * Displays the list of playlists with create button.
     */
    private void showPlaylist() {
        contentPanel.removeAll();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(25, 25, 25)); // Dark modern background
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lbl = new JLabel("🎼 Playlists", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton btnCreate = new JButton("Create New Playlist");
        btnCreate.setBackground(ACCENT);
        btnCreate.setForeground(Color.BLACK);
        btnCreate.setPreferredSize(new Dimension(150, 30));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(25, 25, 25));
        top.add(lbl, BorderLayout.CENTER);
        top.add(btnCreate, BorderLayout.EAST);

        // Panel for playlists displayed as boxes/cards
        JPanel playlistsPanel = new JPanel();
        playlistsPanel.setLayout(new BoxLayout(playlistsPanel, BoxLayout.Y_AXIS)); // Vertical layout
        playlistsPanel.setBackground(new Color(25, 25, 25));

        JScrollPane scrollPane = new JScrollPane(playlistsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(new Color(25, 25, 25));

        // Load playlists into cards
        loadPlaylists(playlistsPanel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create playlist action
        btnCreate.addActionListener(e -> createPlaylist());

        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Loads user's playlists from database into card format.
     * @param playlistsPanel The panel to add playlist cards.
     */
    private void loadPlaylists(JPanel playlistsPanel) {
        playlistsPanel.removeAll(); // Clear existing cards

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                return;
            }
            String query = "SELECT id, playlist_name, total_songs FROM UserPlaylist WHERE username = ? ORDER BY playlist_name";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int playlistId = rs.getInt("id");
                String playlistName = rs.getString("playlist_name");
                int totalSongs = rs.getInt("total_songs");

                // Create card for each playlist
                JPanel card = new JPanel(new BorderLayout(10, 5));
                card.setBackground(new Color(40, 40, 40)); // Modern card background
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // Fixed height for consistency
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor for click

                JLabel lblName = new JLabel(playlistName + " (" + totalSongs + " songs)");
                lblName.setForeground(Color.WHITE);
                lblName.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                lblName.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        System.out.println("Opening Playlist: " + playlistName); // Debug
                        new PlaylistDetail(contentPanel, username, playlistId, playlistName);
                    }
                });

                // Action buttons panel
                JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                buttonsPanel.setBackground(new Color(40, 40, 40));

                JButton btnRename = new JButton("Rename");
                btnRename.setBackground(ACCENT);
                btnRename.setForeground(Color.BLACK);
                btnRename.setPreferredSize(new Dimension(80, 25));
                btnRename.addActionListener(e -> renamePlaylist(playlistId, playlistName));

                JButton btnDelete = new JButton("Delete");
                btnDelete.setBackground(ACCENT);
                btnDelete.setForeground(Color.BLACK);
                btnDelete.setPreferredSize(new Dimension(80, 25));
                btnDelete.addActionListener(e -> deletePlaylist(playlistId));

                buttonsPanel.add(btnRename);
                buttonsPanel.add(btnDelete);

                card.add(lblName, BorderLayout.CENTER);
                card.add(buttonsPanel, BorderLayout.EAST);

                playlistsPanel.add(card);
                playlistsPanel.add(Box.createVerticalStrut(10)); // Space between cards
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(contentPanel, "Error loading playlists: " + ex.getMessage());
        }
        playlistsPanel.revalidate();
        playlistsPanel.repaint();
    }

    /**
     * Creates a new playlist.
     */
    private void createPlaylist() {
        String playlistName = JOptionPane.showInputDialog(contentPanel, "Enter playlist name:");
        if (playlistName == null || playlistName.trim().isEmpty()) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                return;
            }
            // Check if playlist name already exists
            String checkQuery = "SELECT COUNT(*) FROM UserPlaylist WHERE username = ? AND playlist_name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            checkStmt.setString(2, playlistName);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(contentPanel, "Playlist name already exists!");
                return;
            }

            // Insert new playlist
            String insertQuery = "INSERT INTO UserPlaylist (username, playlist_name, total_songs) VALUES (?, ?, 0)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, playlistName);
            insertStmt.executeUpdate();

            JOptionPane.showMessageDialog(contentPanel, "Playlist created successfully!");
            showPlaylist(); // Refresh
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(contentPanel, "Error creating playlist: " + ex.getMessage());
        }
    }

    /**
     * Renames an existing playlist.
     * @param playlistId The ID of the playlist to rename.
     * @param currentName The current name of the playlist.
     */
    private void renamePlaylist(int playlistId, String currentName) {
        String newName = JOptionPane.showInputDialog(contentPanel, "Enter new playlist name:", currentName);
        if (newName == null || newName.trim().isEmpty() || newName.equals(currentName)) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                return;
            }
            // Check if new name already exists
            String checkQuery = "SELECT COUNT(*) FROM UserPlaylist WHERE username = ? AND playlist_name = ? AND id != ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            checkStmt.setString(2, newName);
            checkStmt.setInt(3, playlistId);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(contentPanel, "Playlist name already exists!");
                return;
            }

            // Update name
            String updateQuery = "UPDATE UserPlaylist SET playlist_name = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newName);
            updateStmt.setInt(2, playlistId);
            updateStmt.executeUpdate();

            JOptionPane.showMessageDialog(contentPanel, "Playlist renamed successfully!");
            showPlaylist(); // Refresh
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(contentPanel, "Error renaming playlist: " + ex.getMessage());
        }
    }

    /**
     * Deletes a playlist from the database.
     * @param playlistId The ID of the playlist to delete.
     */
    private void deletePlaylist(int playlistId) {
        int confirm = JOptionPane.showConfirmDialog(contentPanel, "Are you sure you want to delete this playlist? (Songs will remain in your library)", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(contentPanel, "Database connection failed!");
                    return;
                }
                // Delete from UserPlaylistMusic
                String deleteMusicQuery = "DELETE FROM UserPlaylistMusic WHERE playlist_id = ?";
                PreparedStatement deleteMusicStmt = conn.prepareStatement(deleteMusicQuery);
                deleteMusicStmt.setInt(1, playlistId);
                deleteMusicStmt.executeUpdate();

                // Delete from UserPlaylist
                String deletePlaylistQuery = "DELETE FROM UserPlaylist WHERE id = ?";
                PreparedStatement deletePlaylistStmt = conn.prepareStatement(deletePlaylistQuery);
                deletePlaylistStmt.setInt(1, playlistId);
                deletePlaylistStmt.executeUpdate();

                JOptionPane.showMessageDialog(contentPanel, "Playlist deleted successfully!");
                showPlaylist(); // Refresh
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(contentPanel, "Error deleting playlist: " + ex.getMessage());
            }
        }
    }
}