package com.github.john_g1t.app.usecase;

import com.github.john_g1t.domain.service.TestService;
import java.time.ZonedDateTime;

public class CreateTestUseCase {
    private final TestService testService;

    public CreateTestUseCase(TestService testService) {
        this.testService = testService;
    }

    public Integer execute(
        Integer creatorId, String title, String description, Integer timeLimit,
        Integer maxAttempts, ZonedDateTime startTime, ZonedDateTime endTime
    ) {
        return testService.createTest(creatorId, title, description, timeLimit, maxAttempts, startTime, endTime);
    }
}