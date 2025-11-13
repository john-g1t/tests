package com.github.john_g1t.presentation.console.command;

import com.github.john_g1t.presentation.console.input.ConsoleInputReader;
import com.github.john_g1t.presentation.view.ConsoleView;

public abstract class BaseCommand implements MenuCommand {
    protected final ConsoleView view;
    protected final ConsoleInputReader reader;

    protected BaseCommand(ConsoleView view, ConsoleInputReader reader) {
        this.view = view;
        this.reader = reader;
    }

    protected void handleError(String message, Exception e) {
        view.showError(message + ": " + e.getMessage());
    }
}
