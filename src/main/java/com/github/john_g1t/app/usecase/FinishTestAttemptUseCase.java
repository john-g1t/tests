package com.github.john_g1t.app.usecase;

import com.github.john_g1t.domain.service.TestAttemptService;

public class FinishTestAttemptUseCase {
    private final TestAttemptService attemptService;

    public FinishTestAttemptUseCase(TestAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    public Integer execute(Integer attemptId) {
        return attemptService.finishAttempt(attemptId);
    }
}