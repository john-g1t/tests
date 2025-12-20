package com.github.john_g1t;

import com.github.john_g1t.domain.service.PasswordGenerator;
import com.github.john_g1t.infrastructure.ApplicationContext;
import com.github.john_g1t.infrastructure.repository.ConnectionFactory;
import com.github.john_g1t.infrastructure.repository.postgres.PostgresConnectionFactory;
import com.github.john_g1t.presentation.console.ConsoleMenu;

public class Main {
    private static final boolean USE_INMEMORY = false;
    public static void main(String[] args) {
        initializePasswordGenerator();

        ApplicationContext context;
        if (USE_INMEMORY) {
            context = new ApplicationContext();
        } else {
            PostgresConnectionFactory connectionFactory = initializePostgres();
            context = new ApplicationContext(connectionFactory);
        }

        ConsoleMenu menu = new ConsoleMenu(context);

        menu.start();
    }

    private static void initializePasswordGenerator() {
        String salt = System.getenv("SALT");
        if (salt == null) {
            throw new IllegalArgumentException("SALT environment variable is not set");
        }
        PasswordGenerator.init(salt);
    }

    private static PostgresConnectionFactory initializePostgres() {
        final String host = "postgres";
        String port = System.getenv("DB_PORT");
        if (port == null) {
            throw new IllegalArgumentException("DB_PORT environment variable is not set");
        }
        String name = System.getenv("DB_NAME");
        if (name == null) {
            throw new IllegalArgumentException("DB_NAME environment variable is not set");
        }
        String user = System.getenv("DB_USER");
        if (user == null) {
            throw new IllegalArgumentException("DB_USER environment variable is not set");
        }
        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            throw new IllegalArgumentException("DB_PASSWORD environment variable is not set");
        }
        return new PostgresConnectionFactory(host, port, name, user, password);
    }
}
