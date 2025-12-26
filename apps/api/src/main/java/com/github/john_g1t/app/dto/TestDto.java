package com.github.john_g1t.app.dto;

import java.time.ZonedDateTime;

public class TestDto {
    private final Integer id;
    private final String title;
    private final String description;
    private final Integer createdBy;
    private final Integer timeLimit;
    private final Integer maxAttempts;
    private final boolean isActive;
    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;

    public TestDto(
            Integer id, String title, String description, Integer createdBy, Integer timeLimit,
            Integer maxAttempts, boolean isActive, ZonedDateTime startTime, ZonedDateTime endTime
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.timeLimit = timeLimit;
        this.maxAttempts = maxAttempts;
        this.isActive = isActive;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public boolean getIsActive() {
        return this.isActive;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }
}
