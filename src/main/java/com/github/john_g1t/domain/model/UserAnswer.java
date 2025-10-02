package com.github.john_g1t.domain.model;

import java.util.Objects;

public class UserAnswer {
    private Integer id;
    private final Integer attemptId;
    private final Integer questionId;
    private final Integer answerId;
    private String answerText;

    public UserAnswer(Integer attemptId, Integer questionId, Integer answerId, String answerText) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID cannot be null");
        }
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID cannot be null");
        }
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.answerId = answerId;
        this.answerText = answerText;
    }

    public UserAnswer(Integer id, Integer attemptId, Integer questionId, Integer answerId, String answerText) {
        this(attemptId, questionId, answerId, answerText);
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

    public Integer getAttemptId() {
        return attemptId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public Integer getAnswerId() {
        return answerId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean isPersisted() {
        return id != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserAnswer that = (UserAnswer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
