package com.github.john_g1t.domain.model;

import java.util.Objects;

public class Question {
    private Integer id;
    private final Integer testId;
    private String text;
    private String answerType;
    private Integer maxPoints;

    public Question(Integer testId, String text, String answerType, Integer maxPoints) {
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be empty");
        }
        this.testId = testId;
        this.text = text;
        this.answerType = answerType;
        this.maxPoints = maxPoints;
    }

    public Question(Integer id, Integer testId, String text, String answerType, Integer maxPoints) {
        this(testId, text, answerType, maxPoints);
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

    public Integer getTestId() {
        return testId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAnswerType() {
        return answerType;
    }

    public void setAnswerType(String answerType) {
        this.answerType = answerType;
    }

    public Integer getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(Integer maxPoints) {
        this.maxPoints = maxPoints;
    }

    public boolean isPersisted() {
        return id != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return Objects.equals(id, question.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
