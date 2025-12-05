package com.github.john_g1t.app.usecase.attempt;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.domain.service.attempt.TestAttemptService;

public class SubmitAnswerUseCase implements UseCase<SubmitAnswerRequest, Void> {
    private final TestAttemptService attemptService;

    public SubmitAnswerUseCase(TestAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Override
    public Void execute(SubmitAnswerRequest request) {
        if (request.answerId() == null && request.answerText() == null) {
            throw new IllegalArgumentException("Either answerId or answerText must be provided");
        }

        attemptService.submitAnswer(
            request.attemptId(),
            request.questionId(),
            request.answerId(),
            request.answerText()
        );

        return null;
    }
}
