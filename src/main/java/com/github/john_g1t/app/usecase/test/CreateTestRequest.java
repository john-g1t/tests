package com.github.john_g1t.app.usecase.test;

import java.time.ZonedDateTime;

public record CreateTestRequest(
    Integer creatorId,
    String title,
    String description,
    Integer timeLimit,
    Integer maxAttempts,
    ZonedDateTime startTime,
    ZonedDateTime endTime
) {}
