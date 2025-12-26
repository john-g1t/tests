package com.github.john_g1t.presentation.web.listener;

import com.github.john_g1t.domain.service.PasswordGenerator;
import com.github.john_g1t.infrastructure.ApplicationContext;
import com.github.john_g1t.infrastructure.repository.postgres.PostgresConnectionFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final String APP_CONTEXT_KEY = "applicationContext";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        try {
            initializePasswordGenerator();
            PostgresConnectionFactory connectionFactory = initializePostgres();
            ApplicationContext appContext = new ApplicationContext(connectionFactory);

            ctx.setAttribute(APP_CONTEXT_KEY, appContext);

            System.out.println("ApplicationContext initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize ApplicationContext: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        ctx.removeAttribute(APP_CONTEXT_KEY);
        System.out.println("ApplicationContext destroyed");
    }

    public static ApplicationContext getApplicationContext(ServletContext ctx) {
        ApplicationContext appContext = (ApplicationContext) ctx.getAttribute(APP_CONTEXT_KEY);
        if (appContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return appContext;
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

    private static void initializePasswordGenerator() {
        String salt = System.getenv("SALT");
        if (salt == null) {
            throw new IllegalArgumentException("SALT environment variable is not set");
        }
        PasswordGenerator.init(salt);
    }
}
