package com.github.john_g1t;

import com.github.john_g1t.domain.service.PasswordGenerator;
import com.github.john_g1t.infrastructure.ApplicationContext;
import com.github.john_g1t.presentation.console.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
        initializePasswordGenerator();

        ApplicationContext context = new ApplicationContext();
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
}
