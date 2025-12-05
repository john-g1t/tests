package com.github.john_g1t.domain.model;

import java.time.ZonedDateTime;
import java.util.Objects;

public class TestAttempt {
    private Integer id;
    private final Integer userId;
    private final Integer testId;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Integer score;
    private Integer attemptNumber;

    public TestAttempt(
        Integer userId, Integer testId, ZonedDateTime startTime,
        ZonedDateTime endTime, Integer score, Integer attemptNumber
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }
        this.userId = userId;
        this.testId = testId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.score = score;
        this.attemptNumber = attemptNumber;
    }

    public TestAttempt(
            Integer id, Integer userId, Integer testId, ZonedDateTime startTime,
            ZonedDateTime endTime, Integer score, Integer attemptNumber
    ) {
        this(userId, testId, startTime, endTime, score, attemptNumber);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        if (this.id != null && !this.id.equals(id)) {
            throw new IllegalStateException("Cannot change ID");
        }
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getTestId() {
        return testId;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public boolean isPersisted() {
        return id != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TestAttempt that = (TestAttempt) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
