package com.github.john_g1t.app.usecase.attempt;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.domain.service.attempt.TestAttemptService;

public class StartTestAttemptUseCase implements UseCase<StartTestAttemptRequest, Integer> {
    private final TestAttemptService attemptService;

    public StartTestAttemptUseCase(TestAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Override
    public Integer execute(StartTestAttemptRequest request) {
        return attemptService.startAttempt(request.userId(), request.testId());
    }
}
