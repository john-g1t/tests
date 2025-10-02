package com.github.john_g1t.app.usecase;

import com.github.john_g1t.domain.service.TestAttemptService;

public class StartTestAttemptUseCase {
    private final TestAttemptService attemptService;

    public StartTestAttemptUseCase(TestAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    public Integer execute(Integer userId, Integer testId) {
        return attemptService.startAttempt(userId, testId);
    }
}