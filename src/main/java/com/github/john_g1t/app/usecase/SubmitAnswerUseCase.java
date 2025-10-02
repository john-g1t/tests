package com.github.john_g1t.app.usecase;

import com.github.john_g1t.domain.service.TestAttemptService;

public class SubmitAnswerUseCase {
    private final TestAttemptService attemptService;

    public SubmitAnswerUseCase(TestAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    public void execute(Integer attemptId, Integer questionId, Integer answerId, String answerText) {
        attemptService.submitAnswer(attemptId, questionId, answerId, answerText);
    }
}