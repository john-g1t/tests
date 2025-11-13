package com.github.john_g1t.presentation.console;

import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.model.TestAttempt;
import com.github.john_g1t.domain.model.User;
import com.github.john_g1t.infrastructure.ApplicationContext;
import com.github.john_g1t.presentation.console.command.CreateTestCommand;
import com.github.john_g1t.presentation.console.command.MenuCommand;
import com.github.john_g1t.presentation.console.command.RegisterUserCommand;
import com.github.john_g1t.presentation.console.command.TakeTestCommand;
import com.github.john_g1t.presentation.console.input.ConsoleInputReader;
import com.github.john_g1t.presentation.view.ConsoleView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConsoleMenu {
    private final ConsoleView view;
    private final ConsoleInputReader reader;
    private final ApplicationContext context;
    private User currentUser;

    public ConsoleMenu(ApplicationContext context) {
        this.view = new ConsoleView();
        this.reader = new ConsoleInputReader();
        this.context = context;
    }

    public void start() {
        view.showWelcome();

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                running = handleMainMenu();
            } else {
                handleUserMenu();
            }
        }

        reader.close();
    }

    private boolean handleMainMenu() {
        view.showMainMenu();
        int choice = reader.readInt("Enter choice: ");

        Map<Integer, MenuCommand> commands = new HashMap<>();
        commands.put(1, new RegisterUserCommand(view, reader, context.getCreateUserUseCase()));
        commands.put(2, this::handleLogin);

        MenuCommand command = commands.get(choice);
        if (command != null) {
            command.execute();
            return true;
        } else if (choice == 3) {
            view.showMessage("Goodbye!");
            return false;
        } else {
            view.showError("Invalid choice. Please try again.");
            return true;
        }
    }

    private void handleUserMenu() {
        view.showUserMenu(currentUser.getEmail());
        int choice = reader.readInt("Enter choice: ");

        Map<Integer, MenuCommand> commands = new HashMap<>();
        commands.put(1, new CreateTestCommand(
            view, reader, context.getCreateTestUseCase(),
            context.getTestService(), currentUser
        ));
        commands.put(2, this::handleViewMyTests);
        commands.put(3, this::handleViewActiveTests);
        commands.put(4, new TakeTestCommand(
            view, reader,
            context.getStartTestAttemptUseCase(),
            context.getSubmitAnswerUseCase(),
            context.getFinishTestAttemptUseCase(),
            context.getTestService(),
            currentUser
        ));
        commands.put(5, this::handleViewMyResults);
        commands.put(6, this::handleLogout);

        MenuCommand command = commands.get(choice);
        if (command != null) {
            command.execute();
        } else {
            view.showError("Invalid choice. Please try again.");
        }
    }

    private void handleLogin() {
        String email = reader.readLine("Enter email: ");
        String password = reader.readLine("Enter password: ");

        Optional<User> user = context.getUserService().authenticateUser(email, password);
        if (user.isPresent()) {
            currentUser = user.get();
            view.showSuccess("Login successful! Welcome, " + currentUser.getFirstName() +
                    " (ID: " + currentUser.getId() + ")!");
        } else {
            view.showError("Invalid email or password.");
        }
    }

    private void handleViewMyTests() {
        List<Test> myTests = context.getTestService().getTestsByCreator(currentUser.getId());
        view.showTests(myTests);
    }

    private void handleViewActiveTests() {
        List<Test> activeTests = context.getTestService().getActiveTests();
        view.showTests(activeTests);
    }

    private void handleViewMyResults() {
        List<TestAttempt> attempts = context.getAttemptService().getUserAttempts(currentUser.getId());
        List<Test> tests = context.getTestService().getAllTests();
        view.showTestResults(attempts, tests);
    }

    private void handleLogout() {
        currentUser = null;
        view.showSuccess("Logged out successfully!");
    }
}
