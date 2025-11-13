package com.github.john_g1t.presentation.console.command;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.test.CreateTestRequest;
import com.github.john_g1t.domain.model.User;
import com.github.john_g1t.domain.service.test.TestService;
import com.github.john_g1t.presentation.console.input.ConsoleInputReader;
import com.github.john_g1t.presentation.view.ConsoleView;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CreateTestCommand extends BaseCommand {
    private final UseCase<CreateTestRequest, Integer> createTestUseCase;
    private final TestService testService;
    private final User currentUser;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CreateTestCommand(
        ConsoleView view,
        ConsoleInputReader reader,
        UseCase<CreateTestRequest, Integer> createTestUseCase,
        TestService testService,
        User currentUser
    ) {
        super(view, reader);
        this.createTestUseCase = createTestUseCase;
        this.testService = testService;
        this.currentUser = currentUser;
    }

    @Override
    public void execute() {
        try {
            String title = reader.readLine("Enter test title: ");
            String description = reader.readLine("Enter test description: ");
            Integer timeLimit = reader.readOptionalInt("Enter time limit in minutes (or press Enter for no limit): ");
            Integer maxAttempts = reader.readOptionalInt("Enter max attempts (or press Enter for unlimited): ");

            view.showMessage("Enter start time (yyyy-MM-dd HH:mm) or press Enter for immediate start:");
            ZonedDateTime startTime = readOptionalDateTime();

            view.showMessage("Enter end time (yyyy-MM-dd HH:mm) or press Enter for no end:");
            ZonedDateTime endTime = readOptionalDateTime();

            var request = new CreateTestRequest(
                currentUser.getId(), title, description,
                timeLimit, maxAttempts, startTime, endTime
            );
            Integer testId = createTestUseCase.execute(request);

            view.showSuccess("Test created with ID: " + testId);

            String addQuestions = reader.readLine("Do you want to add questions now? (yes/no): ");
            if (addQuestions.equalsIgnoreCase("yes")) {
                addQuestions(testId);
            }
        } catch (Exception e) {
            handleError("Failed to create test", e);
        }
    }

    private void addQuestions(Integer testId) {
        boolean addingQuestions = true;

        while (addingQuestions) {
            try {
                String questionText = reader.readLine("\nEnter question text: ");
                String answerType = reader.readLine("Enter answer type (single/multiple/text): ");
                int maxPoints = reader.readInt("Enter max points: ");

                Integer questionId = testService.addQuestion(testId, questionText, answerType, maxPoints);
                view.showSuccess("Question added with ID: " + questionId);

                if (!answerType.equalsIgnoreCase("text")) {
                    addAnswerOptions(questionId);
                }

                String addMore = reader.readLine("Add another question? (yes/no): ");
                addingQuestions = addMore.equalsIgnoreCase("yes");
            } catch (Exception e) {
                handleError("Failed to add question", e);
            }
        }
    }

    private void addAnswerOptions(Integer questionId) {
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
                handleError("Failed to add answer option", e);
            }
        }
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
