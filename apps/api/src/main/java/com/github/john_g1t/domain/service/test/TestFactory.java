package com.github.john_g1t.domain.service.test;

import com.github.john_g1t.domain.model.Test;
import java.time.ZonedDateTime;

public class TestFactory {
    public Test createTest(
        Integer creatorId, String title, String description, Integer timeLimit,
        Integer maxAttempts, ZonedDateTime startTime, ZonedDateTime endTime
    ) {
        return new Test(creatorId, title, description, timeLimit, maxAttempts, true, startTime, endTime);
    }
}
