// To Execute Program --->

// Compile : javac -cp .:/Users/soham/Libraries/MySQLConnector/mysql-connector-j-8.3.0.jar DBConnection.java
// Run     : java -cp .:/Users/soham/Libraries/MySQLConnector/mysql-connector-j-8.3.0.jar DBConnection

// STEP - 1 : Import SQL Packages [JDBC API Package] using 'java.sql.*'
//            which contains classes & interfaces to connect with database, execute query and handle results

package database;

import java.sql.*;

public class DBConnection {

    public static void main(String args[]) {
        Connection conn = null;
        Statement stmt = null;

        try {
            // STEP - 2 : Load Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // STEP - 3 : Establish Connection
            String url = "jdbc:mysql://localhost:3306/MusicPlayerDatabase";
            String user = "root";
            String password = "@Soham_30";

            conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connection Established with MusicPlayerDatabase");

            // STEP - 4 : Create Statement
            stmt = conn.createStatement();

            // STEP - 5 : Create 'Users' Table if not exists
            String createTable = "CREATE TABLE IF NOT EXISTS Users ("
                               + "id INT AUTO_INCREMENT PRIMARY KEY, "
                               + "username VARCHAR(100) UNIQUE NOT NULL, "
                               + "password VARCHAR(100) NOT NULL)";
            stmt.executeUpdate(createTable);
            System.out.println("✅ Table 'Users' is ready.");

        } catch (Exception e) {
            System.out.println("❌ Error Occurred : " + e);
        } finally {
            // STEP - 6 : Close Connection
            try {
                if (conn != null) conn.close();
                System.out.println("🔒 Connection Closed.");
            } catch (Exception e) {
                System.out.println("Exception at Connection Close : " + e);
            }
        }
    }

    // Utility Method - Other classes (LoginPage, SignupPage) will call this
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/MusicPlayerDatabase";
            String user = "root";
            String password = "@Soham_30";
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            System.out.println("❌ Error Connecting to DB: " + e);
            return null;
        }
    }
}