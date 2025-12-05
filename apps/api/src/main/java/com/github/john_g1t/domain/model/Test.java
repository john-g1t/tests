package com.github.john_g1t.domain.model;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Test {
    private Integer id;
    private final Integer createdBy;
    private String title;
    private String description;
    private Integer timeLimit;
    private Integer maxAttempts;
    private boolean isActive;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    public Test(
        Integer createdBy, String title, String description, Integer timeLimit,
        Integer maxAttempts, boolean isActive, ZonedDateTime startTime, ZonedDateTime endTime
    ) {
        if (createdBy == null) {
            throw new IllegalArgumentException("Creator id cannot be empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.timeLimit = timeLimit;
        this.maxAttempts = maxAttempts;
        this.isActive = isActive;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Test(
        Integer id, Integer createdBy, String title, String description, Integer timeLimit,
        Integer maxAttempts, boolean isActive, ZonedDateTime startTime, ZonedDateTime endTime
    ) {
        this(createdBy, title, description, timeLimit, maxAttempts, isActive, startTime, endTime);
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        if (this.id != null) {
            throw new IllegalStateException("Cannot change ID");
        }
        this.id = id;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public boolean isPersisted() {
        return id != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Test test = (Test) o;
        return Objects.equals(id, test.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
