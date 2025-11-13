package com.github.john_g1t.app.usecase.test;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.domain.service.test.TestService;
import com.github.john_g1t.domain.service.user.UserService;

import java.time.ZonedDateTime;

public class CreateTestUseCase implements UseCase<CreateTestRequest, Integer> {
    private final TestService testService;
    private final UserService userService;

    public CreateTestUseCase(TestService testService, UserService userService) {
        this.testService = testService;
        this.userService = userService;
    }

    @Override
    public Integer execute(CreateTestRequest request) {
        validateRequest(request);
        validateCreatorExists(request.creatorId());
        validateTimeRange(request.startTime(), request.endTime());

        return testService.createTest(
            request.creatorId(),
            request.title(),
            request.description(),
            request.timeLimit(),
            request.maxAttempts(),
            request.startTime(),
            request.endTime()
        );
    }

    private void validateRequest(CreateTestRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Test title cannot be empty");
        }
        if (request.timeLimit() != null && request.timeLimit() <= 0) {
            throw new IllegalArgumentException("Time limit must be positive");
        }
        if (request.maxAttempts() != null && request.maxAttempts() <= 0) {
            throw new IllegalArgumentException("Max attempts must be positive");
        }
    }

    private void validateCreatorExists(Integer creatorId) {
        if (!userService.existsById(creatorId)) {
            throw new IllegalArgumentException("Creator user does not exist");
        }
    }

    private void validateTimeRange(ZonedDateTime startTime, ZonedDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }
}
