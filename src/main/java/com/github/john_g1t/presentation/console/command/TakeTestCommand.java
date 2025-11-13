package com.github.john_g1t.presentation.console.command;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.attempt.FinishTestAttemptRequest;
import com.github.john_g1t.app.usecase.attempt.StartTestAttemptRequest;
import com.github.john_g1t.app.usecase.attempt.SubmitAnswerRequest;
import com.github.john_g1t.domain.model.AnswerOption;
import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.model.User;
import com.github.john_g1t.domain.service.test.TestService;
import com.github.john_g1t.presentation.console.input.ConsoleInputReader;
import com.github.john_g1t.presentation.view.ConsoleView;
import java.util.List;
import java.util.Optional;

public class TakeTestCommand extends BaseCommand {
    private final UseCase<StartTestAttemptRequest, Integer> startTestAttemptUseCase;
    private final UseCase<SubmitAnswerRequest, Void> submitAnswerUseCase;
    private final UseCase<FinishTestAttemptRequest, Integer> finishTestAttemptUseCase;
    private final TestService testService;
    private final User currentUser;

    public TakeTestCommand(
        ConsoleView view,
        ConsoleInputReader reader,
        UseCase<StartTestAttemptRequest, Integer> startTestAttemptUseCase,
        UseCase<SubmitAnswerRequest, Void> submitAnswerUseCase,
        UseCase<FinishTestAttemptRequest, Integer> finishTestAttemptUseCase,
        TestService testService,
        User currentUser
    ) {
        super(view, reader);
        this.startTestAttemptUseCase = startTestAttemptUseCase;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.finishTestAttemptUseCase = finishTestAttemptUseCase;
        this.testService = testService;
        this.currentUser = currentUser;
    }

    @Override
    public void execute() {
        try {
            int testId = reader.readInt("Enter test ID: ");

            Optional<Test> test = testService.getTest(testId);
            if (test.isEmpty()) {
                view.showError("Test not found.");
                return;
            }

            var startRequest = new StartTestAttemptRequest(currentUser.getId(), testId);
            Integer attemptId = startTestAttemptUseCase.execute(startRequest);
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
                    var answerRequest = new SubmitAnswerRequest(
                            attemptId, question.getId(), null, answerText
                    );
                    submitAnswerUseCase.execute(answerRequest);
                } else {
                    int answerChoice = reader.readInt("\nEnter option number: ");
                    if (answerChoice > 0 && answerChoice <= options.size()) {
                        AnswerOption selectedOption = options.get(answerChoice - 1);
                        var answerRequest = new SubmitAnswerRequest(
                                attemptId, question.getId(), selectedOption.getId(), null
                        );
                        submitAnswerUseCase.execute(answerRequest);
                        view.showSuccess("Answer submitted!");
                    } else {
                        view.showError("Invalid option number.");
                    }
                }
            }

            var finishRequest = new FinishTestAttemptRequest(attemptId);
            Integer totalScore = finishTestAttemptUseCase.execute(finishRequest);
            view.showSuccess("Test completed! Your score: " + totalScore);

        } catch (Exception e) {
            handleError("Failed to take test", e);
        }
    }
}
