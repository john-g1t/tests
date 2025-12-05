package com.github.john_g1t.app.dto;

import java.time.ZonedDateTime;

public class TestResultDto {
    private final Integer attemptId;
    private final String testTitle;
    private final Integer score;
    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;
    private final Integer attemptNumber;

    public TestResultDto(
            Integer attemptId, String testTitle, Integer score,
            ZonedDateTime startTime, ZonedDateTime endTime, Integer attemptNumber
    ) {
        this.attemptId = attemptId;
        this.testTitle = testTitle;
        this.score = score;
        this.startTime = startTime;
        this.endTime = endTime;
        this.attemptNumber = attemptNumber;
    }

    public Integer getAttemptId() {
        return attemptId;
    }

    public String getTestTitle() {
        return testTitle;
    }

    public Integer getScore() {
        return score;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }
}
