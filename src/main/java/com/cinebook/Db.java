package com.cinebook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    private Db() {}
    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("CINEBOOK_DB_URL",
            "jdbc:mysql://localhost:3306/cinebook_db?useSSL=false&serverTimezone=UTC");
        String user = System.getenv().getOrDefault("CINEBOOK_DB_USER", "root");
        String password = System.getenv().getOrDefault("CINEBOOK_DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }
}
