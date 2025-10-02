package com.github.john_g1t.presentation.console;

import com.github.john_g1t.app.usecase.*;
import com.github.john_g1t.domain.model.*;
import com.github.john_g1t.domain.service.TestAttemptService;
import com.github.john_g1t.domain.service.TestService;
import com.github.john_g1t.domain.service.UserService;
import com.github.john_g1t.presentation.view.ConsoleView;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class ConsoleMenu {
    private final ConsoleView view;
    private final ConsoleInputReader reader;

    private final CreateUserUseCase createUserUseCase;
    private final CreateTestUseCase createTestUseCase;
    private final StartTestAttemptUseCase startTestAttemptUseCase;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final FinishTestAttemptUseCase finishTestAttemptUseCase;

    private final UserService userService;
    private final TestService testService;
    private final TestAttemptService attemptService;

    private User currentUser;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public ConsoleMenu(
        CreateUserUseCase createUserUseCase, CreateTestUseCase createTestUseCase,
        StartTestAttemptUseCase startTestAttemptUseCase, SubmitAnswerUseCase submitAnswerUseCase,
        FinishTestAttemptUseCase finishTestAttemptUseCase, UserService userService,
        TestService testService, TestAttemptService attemptService
    ) {
        this.view = new ConsoleView();
        this.reader = new ConsoleInputReader();
        this.createUserUseCase = createUserUseCase;
        this.createTestUseCase = createTestUseCase;
        this.startTestAttemptUseCase = startTestAttemptUseCase;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.finishTestAttemptUseCase = finishTestAttemptUseCase;
        this.userService = userService;
        this.testService = testService;
        this.attemptService = attemptService;
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

        switch (choice) {
            case 1:
                handleRegistration();
                break;
            case 2:
                handleLogin();
                break;
            case 3:
                view.showMessage("Goodbye!");
                return false;
            default:
                view.showError("Invalid choice. Please try again.");
        }
        return true;
    }

    private void handleRegistration() {
        try {
            String email = reader.readLine("Enter email: ");
            String password = reader.readLine("Enter password: ");
            String firstName = reader.readLine("Enter first name: ");
            String lastName = reader.readLine("Enter last name: ");

            Integer userId = createUserUseCase.execute(email, password, firstName, lastName);
            view.showSuccess("User registered successfully with ID: " + userId);
        } catch (Exception e) {
            view.showError("Registration failed: " + e.getMessage());
        }
    }

    private void handleLogin() {
        String email = reader.readLine("Enter email: ");
        String password = reader.readLine("Enter password: ");

        Optional<User> user = userService.authenticateUser(email, password);
        if (user.isPresent()) {
            currentUser = user.get();
            view.showSuccess("Login successful! Welcome, " + currentUser.getFirstName() +
                    " (ID: " + currentUser.getId() + ")!");
        } else {
            view.showError("Invalid email or password.");
        }
    }

    private void handleUserMenu() {
        view.showUserMenu(currentUser.getEmail());
        int choice = reader.readInt("Enter choice: ");

        switch (choice) {
            case 1:
                handleCreateTest();
                break;
            case 2:
                handleViewMyTests();
                break;
            case 3:
                handleViewActiveTests();
                break;
            case 4:
                handleTakeTest();
                break;
            case 5:
                handleViewMyResults();
                break;
            case 6:
                currentUser = null;
                view.showSuccess("Logged out successfully!");
                break;
            default:
                view.showError("Invalid choice. Please try again.");
        }
    }

    private void handleCreateTest() {
        try {
            String title = reader.readLine("Enter test title: ");
            String description = reader.readLine("Enter test description: ");
            Integer timeLimit = reader.readOptionalInt("Enter time limit in minutes (or press Enter for no limit): ");
            Integer maxAttempts = reader.readOptionalInt("Enter max attempts (or press Enter for unlimited): ");

            view.showMessage("Enter start time (yyyy-MM-dd HH:mm) or press Enter for immediate start:");
            ZonedDateTime startTime = readOptionalDateTime();

            view.showMessage("Enter end time (yyyy-MM-dd HH:mm) or press Enter for no end:");
            ZonedDateTime endTime = readOptionalDateTime();

            Integer testId = createTestUseCase.execute(
                currentUser.getId(), title, description,
                timeLimit, maxAttempts, startTime, endTime
            );

            view.showSuccess("Test created with ID: " + testId);

            String addQuestions = reader.readLine("Do you want to add questions now? (yes/no): ");
            if (addQuestions.equalsIgnoreCase("yes")) {
                handleAddQuestions(testId);
            }
        } catch (Exception e) {
            view.showError("Failed to create test: " + e.getMessage());
        }
    }

    private void handleAddQuestions(Integer testId) {
        boolean addingQuestions = true;

        while (addingQuestions) {
            try {
                String questionText = reader.readLine("\nEnter question text: ");
                String answerType = reader.readLine("Enter answer type (single/multiple/text): ");
                int maxPoints = reader.readInt("Enter max points: ");

                Integer questionId = testService.addQuestion(testId, questionText, answerType, maxPoints);
                view.showSuccess("Question added with ID: " + questionId);

                if (!answerType.equalsIgnoreCase("text")) {
                    handleAddAnswerOptions(questionId);
                }

                String addMore = reader.readLine("Add another question? (yes/no): ");
                addingQuestions = addMore.equalsIgnoreCase("yes");
            } catch (Exception e) {
                view.showError("Failed to add question: " + e.getMessage());
            }
        }
    }

    private void handleAddAnswerOptions(Integer questionId) {
        boolean addingOptions = true;

        while (addingOptions) {
            try {
                String optionText = reader.readLine("Enter answer option text: ");
                int score = reader.readInt("Enter score for this option: ");

                Integer optionId = testService.addAnswerOption(questionId, optionText, score);
                view.showSuccess("Answer option added with ID: " + optionId);

                String addMore = reader.readLine("Add another option? (yes/no): ");
                addingOptions = addMore.equalsIgnoreCase("yes");
            } catch (Exception e) {
                view.showError("Failed to add answer option: " + e.getMessage());
            }
        }
    }

    private void handleViewMyTests() {
        List<Test> myTests = testService.getTestsByCreator(currentUser.getId());
        view.showTests(myTests);
    }

    private void handleViewActiveTests() {
        List<Test> activeTests = testService.getActiveTests();
        view.showTests(activeTests);
    }

    private void handleTakeTest() {
        try {
            int testId = reader.readInt("Enter test ID: ");

            Optional<Test> test = testService.getTest(testId);
            if (test.isEmpty()) {
                view.showError("Test not found.");
                return;
            }

            Integer attemptId = startTestAttemptUseCase.execute(currentUser.getId(), testId);
            view.showSuccess("Test attempt started! Attempt ID: " + attemptId);

            List<Question> questions = testService.getQuestions(testId);
            if (questions.isEmpty()) {
                view.showError("This test has no questions.");
                return;
            }

            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                List<AnswerOption> options = testService.getAnswerOptions(question.getId());

                view.showQuestion(question, options, i + 1, questions.size());

                if (question.getAnswerType().equalsIgnoreCase("text")) {
                    String answerText = reader.readLine("\nYour answer: ");
                    submitAnswerUseCase.execute(attemptId, question.getId(), null, answerText);
                } else {
                    int answerChoice = reader.readInt("\nEnter option number: ");
                    if (answerChoice > 0 && answerChoice <= options.size()) {
                        AnswerOption selectedOption = options.get(answerChoice - 1);
                        submitAnswerUseCase.execute(attemptId, question.getId(),
                                selectedOption.getId(), null);
                        view.showSuccess("Answer submitted!");
                    } else {
                        view.showError("Invalid option number.");
                    }
                }
            }

            Integer totalScore = finishTestAttemptUseCase.execute(attemptId);
            view.showSuccess("Test completed! Your score: " + totalScore);

        } catch (Exception e) {
            view.showError("Failed to take test: " + e.getMessage());
        }
    }

    private void handleViewMyResults() {
        List<TestAttempt> attempts = attemptService.getUserAttempts(currentUser.getId());
        List<Test> tests = testService.getAllTests();
        view.showTestResults(attempts, tests);
    }

    private ZonedDateTime readOptionalDateTime() {
        String input = reader.readLine("");
        if (input.isEmpty()) {
            return null;
        }

        try {
            return ZonedDateTime.parse(input, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            view.showError("Invalid date format. Using null.");
            return null;
        }
    }
}
