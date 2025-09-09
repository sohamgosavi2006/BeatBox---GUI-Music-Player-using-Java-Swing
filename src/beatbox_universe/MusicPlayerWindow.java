package beatbox_universe;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener; // Added for database classes

// Placeholder for DBConnection if not already defined
class DBConnection {
    public static Connection getConnection() throws SQLException {
        // TODO: Implement with your database URL, username, and password
        // Example: Replace with actual database details
        String url = "jdbc:mysql://localhost:3306/beatbox_db";
        return DriverManager.getConnection(url, "username", "password");
    }
}

public class MusicPlayerWindow extends JFrame {

    private JPanel contentPanel;
    private String username;
    private JPanel playerPanel;
    private JLabel currentSongLabel;
    private int currentMusicId = -1; // Track current song
    private Clip currentClip; // For audio playback
    private ArrayList<String> songQueue = new ArrayList<>(); // Queue for songs
    private ArrayList<Integer> musicIdsQueue = new ArrayList<>(); // Queue for music IDs
    private int currentIndex = -1; // Current index in queue
    private long totalLength = 0; // Total length of current song in microseconds
    private long pausedPosition = 0; // Position to resume from pause
    private JSlider progressSlider;
    private Timer progressTimer;
    private JButton btnPlayPause;
    private JButton btnNext;

    private static MusicPlayerWindow instance;

    // Accent Color
    private final Color ACCENT = new Color(46, 204, 113); // Neon Green

    // Constant for music file base folder (append username/ for per-user storage)
    private static final String MUSIC_BASE_FOLDER = "/Users/soham/Library/Mobile Documents/com~apple~CloudDocs/MacBook/Visual Studio Code/Java/Academics/Semester_3/Workspace/Project/Workspace/BeatBox-MusicPlayer/Music/";

    /**
     * Private constructor for singleton MusicPlayerWindow.
     * @param username The logged-in user's username.
     */
    private MusicPlayerWindow(String username) {
        this.username = username;
        this.currentClip = null;

        setTitle("🎵 BeatBox - Music Player");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Sidebar =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(8, 1, 0, 15));
        sidebar.setBackground(new Color(20, 20, 20));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton btnHome = createSidebarButton("🏠 Home");
        JButton btnAddMusic = createSidebarButton("➕ Add Music");
        JButton btnPlaylist = createSidebarButton("🎼 Playlist");
        JButton btnProfile = createSidebarButton("👤 My Profile");
        JButton btnLogout = createSidebarButton("🚪 Logout");

        JLabel welcome = new JLabel("Semester 3 APP Project", SwingConstants.CENTER);
        welcome.setForeground(ACCENT);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblUser = new JLabel("Developed By Soham Gosavi");
        lblUser.setForeground(Color.ORANGE);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Add action listeners
        btnHome.addActionListener(e -> showHome());
        btnAddMusic.addActionListener(e -> showAddMusic());
        btnPlaylist.addActionListener(e -> showPlaylist());
        btnProfile.addActionListener(e -> showProfile());
        btnLogout.addActionListener(e -> logout());

        sidebar.add(btnHome);
        sidebar.add(btnAddMusic);
        sidebar.add(btnPlaylist);
        sidebar.add(btnProfile);
        sidebar.add(btnLogout);
        sidebar.add(welcome);
        sidebar.add(lblUser);

        add(sidebar, BorderLayout.WEST);

        // ===== Content Panel =====
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(30, 30, 30));
        add(contentPanel, BorderLayout.CENTER);

        // ===== Player Panel (Always Visible) =====
        playerPanel = new JPanel(new BorderLayout(10, 10));
        playerPanel.setBackground(new Color(40, 40, 40));
        playerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        playerPanel.setPreferredSize(new Dimension(1000, 100)); // Increased height for visibility

        currentSongLabel = new JLabel("No song playing", SwingConstants.LEFT);
        currentSongLabel.setForeground(Color.WHITE);
        currentSongLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        currentSongLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setPreferredSize(new Dimension(600, 30)); // Increased size for visibility
        progressSlider.setBackground(new Color(40, 40, 40));
        progressSlider.setForeground(ACCENT);
        progressSlider.setPaintTicks(false);
        progressSlider.setPaintTrack(true);
        progressSlider.setMajorTickSpacing(25);
        progressSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!progressSlider.getValueIsAdjusting() && currentClip != null && currentClip.isOpen() && totalLength > 0) {
                    long newPosition = (long) (progressSlider.getValue() * totalLength / 100.0);
                    currentClip.setMicrosecondPosition(newPosition);
                }
            }
        });

        btnPlayPause = new JButton("Play");
        btnPlayPause.setBackground(Color.GREEN);
        btnPlayPause.setForeground(Color.WHITE); // Changed to white
        btnPlayPause.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bold text
        btnPlayPause.setFocusPainted(false);
        btnPlayPause.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnPlayPause.addActionListener(e -> togglePlayPause());

        btnNext = new JButton("Next");
        btnNext.setBackground(Color.GREEN);
        btnNext.setForeground(Color.WHITE); // Changed to white
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bold text
        btnNext.setFocusPainted(false);
        btnNext.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnNext.addActionListener(e -> playNext());

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        controlsPanel.setBackground(new Color(40, 40, 40));
        controlsPanel.add(btnPlayPause);
        controlsPanel.add(btnNext);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(new Color(40, 40, 40));
        progressPanel.add(currentSongLabel, BorderLayout.WEST);
        progressPanel.add(progressSlider, BorderLayout.CENTER);

        playerPanel.add(progressPanel, BorderLayout.CENTER);
        playerPanel.add(controlsPanel, BorderLayout.EAST);

        add(playerPanel, BorderLayout.SOUTH);

        // Initial show home
        showHome();
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(30, 30, 30));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    /**
     * Plays a song and optionally loads a queue (e.g., from playlist).
     */
    public void playSong(String filePath, int musicId) {
        // Clear existing queue and start new one with this song
        songQueue.clear();
        musicIdsQueue.clear();
        songQueue.add(filePath);
        musicIdsQueue.add(musicId);
        currentIndex = 0;
        playCurrent();
    }

    /**
     * Plays a playlist by loading songs into queue.
     * Call this from PlaylistDetail when "Play" is clicked.
     */
    public void playPlaylist(int playlistId) {
        // Load songs from playlist into queue
        songQueue.clear();
        musicIdsQueue.clear();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String query = "SELECT m.id, m.file_path FROM UserMusic m " +
                               "JOIN UserPlaylistMusic pm ON m.id = pm.music_id " +
                               "WHERE pm.playlist_id = ? ORDER BY pm.id";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, playlistId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    songQueue.add(rs.getString("file_path"));
                    musicIdsQueue.add(rs.getInt("id"));
                }
                if (!songQueue.isEmpty()) {
                    currentIndex = 0;
                    playCurrent();
                } else {
                    JOptionPane.showMessageDialog(this, "No songs in playlist!");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading playlist: " + ex.getMessage());
        }
    }

    /**
     * Sets a custom queue and starts playing from a specified index.
     */
    public void setQueueAndPlay(ArrayList<String> queue, int startIndex) {
        songQueue.clear();
        musicIdsQueue.clear();
        songQueue.addAll(queue);
        // Note: musicIdsQueue should be populated with corresponding IDs if available
        // For now, assuming queue contains file paths only; adjust if IDs are needed
        for (int i = 0; i < queue.size(); i++) {
            musicIdsQueue.add(-1); // Placeholder, replace with actual IDs if available
        }
        currentIndex = startIndex;
        if (currentIndex >= 0 && currentIndex < songQueue.size()) {
            playCurrent();
        }
    }

    private void playCurrent() {
        if (currentIndex < 0 || currentIndex >= songQueue.size()) {
            resetPlayer();
            return;
        }

        String filePath = songQueue.get(currentIndex);
        currentMusicId = musicIdsQueue.get(currentIndex);
        currentSongLabel.setText("Playing: " + new File(filePath).getName());

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            JOptionPane.showMessageDialog(this, "File not found: " + filePath);
            playNext();
            return;
        }

        try {
            if (currentClip != null && currentClip.isOpen()) {
                currentClip.close();
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioInputStream);
            totalLength = currentClip.getMicrosecondLength();
            pausedPosition = 0;
            currentClip.setMicrosecondPosition(pausedPosition);
            currentClip.start();

            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && event.getFramePosition() == totalLength) {
                    playNext();
                }
            });

            startProgressTimer();
            btnPlayPause.setText("Pause");

        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error playing file: " + ex.getMessage());
            playNext();
        }
    }

    private void playNext() {
        if (songQueue.isEmpty()) return;
        currentIndex = (currentIndex + 1) % songQueue.size();
        if (currentIndex >= songQueue.size()) {
            resetPlayer();
            return;
        }
        playCurrent();
    }

    private void togglePlayPause() {
        if (currentClip != null && currentClip.isOpen()) {
            if (currentClip.isRunning()) {
                pausedPosition = currentClip.getMicrosecondPosition();
                currentClip.stop();
                btnPlayPause.setText("Play");
                stopProgressTimer();
            } else {
                currentClip.setMicrosecondPosition(pausedPosition);
                currentClip.start();
                btnPlayPause.setText("Pause");
                startProgressTimer();
            }
        }
    }

    private void updateProgress() {
        if (currentClip != null && currentClip.isOpen() && totalLength > 0) {
            long currentPos = currentClip.getMicrosecondPosition();
            int progress = (int) ((double) currentPos / totalLength * 100);
            progressSlider.setValue(progress);
        }
    }

    private void startProgressTimer() {
        stopProgressTimer();
        progressTimer = new Timer();
        progressTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updateProgress());
            }
        }, 0, 100); // Update every 100ms
    }

    private void stopProgressTimer() {
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer = null;
        }
    }

    private void stopCurrent() {
        if (currentClip != null) {
            if (currentClip.isRunning()) {
                currentClip.stop();
            }
            currentClip.close();
            currentClip = null;
        }
        stopProgressTimer();
    }

    private void resetPlayer() {
        stopCurrent();
        currentSongLabel.setText("No song playing");
        progressSlider.setValue(0);
        totalLength = 0;
        pausedPosition = 0;
        currentMusicId = -1;
        currentIndex = -1;
        songQueue.clear();
        musicIdsQueue.clear();
        btnPlayPause.setText("Play");
    }

    public void stopSong() {
        stopCurrent();
        resetPlayer();
    }

    // Navigation methods
    private void showHome() {
        contentPanel.removeAll();
        new Home(contentPanel, username);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showAddMusic() {
        contentPanel.removeAll();
        new AddMusic(contentPanel, username);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPlaylist() {
        contentPanel.removeAll();
        new Playlist(contentPanel, username);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showProfile() {
        contentPanel.removeAll();
        new Profile(contentPanel, username);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void logout() {
        this.dispose();
        MusicPlayerWindow.instance = null;
        stopSong();
        songQueue.clear();
        musicIdsQueue.clear();
        currentIndex = -1;
        SwingUtilities.invokeLater(() -> {
            JFrame loginFrame = new JFrame("BeatBox");
            loginFrame.setSize(400, 300);
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            JPanel loginPanel = new JPanel(new CardLayout());
            loginFrame.add(loginPanel);
            new authentication.LoginPage(loginPanel);
            loginFrame.setVisible(true);
        });
    }

    public static MusicPlayerWindow getInstance(String username) {
        if (instance == null) {
            instance = new MusicPlayerWindow(username);
        }
        return instance;
    }
}