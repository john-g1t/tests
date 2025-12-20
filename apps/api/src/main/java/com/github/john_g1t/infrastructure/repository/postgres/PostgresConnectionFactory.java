package com.github.john_g1t.infrastructure.repository.postgres;

import com.github.john_g1t.infrastructure.repository.ConnectionFactory;
import com.github.john_g1t.infrastructure.repository.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConnectionFactory implements ConnectionFactory {
    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    public PostgresConnectionFactory(String host, String port, String name, String user, String password) {
        this.url = String.format("jdbc:postgresql://%s:%s/%s", host, port, name);
        this.username = user;
        this.password = password;
    }

    public PostgresConnectionFactory(String jdbcUrl, String username, String password) {
        this.url = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Failed to obtain PostgreSQL connection", e);
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Error closing database connection", e);
            }
        }
    }
}
