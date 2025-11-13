package com.github.john_g1t.domain.service.attempt;

import com.github.john_g1t.domain.model.TestAttempt;
import java.time.ZonedDateTime;

public class TestAttemptFactory {
    public TestAttempt createTestAttempt(Integer userId, Integer testId, int attemptNumber) {
        return new TestAttempt(userId, testId, ZonedDateTime.now(), null, 0, attemptNumber);
    }
}
