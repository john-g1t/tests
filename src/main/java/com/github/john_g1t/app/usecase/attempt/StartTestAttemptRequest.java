package com.github.john_g1t.app.usecase.attempt;

public record StartTestAttemptRequest(
    Integer userId,
    Integer testId
) {}
