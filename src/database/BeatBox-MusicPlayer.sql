-- Step 1: Create the database
CREATE DATABASE IF NOT EXISTS MusicPlayerDatabase;

-- Step 2: Select (use) the database
USE MusicPlayerDatabase;

-- Step 3: Create Users table
DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- Step 4: Insert some sample users
INSERT INTO Users (username, password) VALUES ('soham', '12345');

-- Step 5: Retrieve all users
SELECT * FROM Users;

-- Step 6: Retrieve only usernames
SELECT username FROM Users;

-- Step 7: Create UserMusic table
DROP TABLE IF EXISTS UserMusic;
CREATE TABLE UserMusic (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    artist VARCHAR(100) NOT NULL,
    music_title VARCHAR(100) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_music_username
        FOREIGN KEY (username) REFERENCES Users(username)
        ON DELETE CASCADE
);

-- Step 8: Retrieve all Music
SELECT * FROM UserMusic;

-- Step 9: Create UserPlaylist table
DROP TABLE IF EXISTS UserPlaylist;
CREATE TABLE UserPlaylist (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    playlist_name VARCHAR(100) NOT NULL,
    total_songs INT DEFAULT 0,
    CONSTRAINT fk_user_playlist_username
        FOREIGN KEY (username) REFERENCES Users(username)
        ON DELETE CASCADE,
    UNIQUE (username, playlist_name)
);

-- Step 10: Create UserPlaylistMusic table
DROP TABLE IF EXISTS UserPlaylistMusic;
CREATE TABLE UserPlaylistMusic (
    id INT AUTO_INCREMENT PRIMARY KEY,
    playlist_id INT NOT NULL,
    music_id INT NOT NULL,
    CONSTRAINT fk_playlist_music_playlist
        FOREIGN KEY (playlist_id) REFERENCES UserPlaylist(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_playlist_music_song
        FOREIGN KEY (music_id) REFERENCES UserMusic(id)
        ON DELETE CASCADE,
    UNIQUE (playlist_id, music_id)
);

