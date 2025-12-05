package com.github.john_g1t.app.usecase.attempt;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.domain.service.attempt.TestAttemptService;

public class FinishTestAttemptUseCase implements UseCase<FinishTestAttemptRequest, Integer> {
    private final TestAttemptService attemptService;

    public FinishTestAttemptUseCase(TestAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Override
    public Integer execute(FinishTestAttemptRequest request) {
        return attemptService.finishAttempt(request.attemptId());
    }
}
