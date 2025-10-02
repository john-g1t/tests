package com.github.john_g1t.domain.model;

import java.util.Objects;

public class AnswerOption {
    private Integer id;
    private final Integer questionId;
    private String optionText;
    private Integer score;

    public AnswerOption(Integer questionId, String optionText, Integer score) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID cannot be null");
        }
        this.questionId = questionId;
        this.optionText = optionText;
        this.score = score;
    }

    public AnswerOption(Integer id, Integer questionId, String optionText, Integer score) {
        this(questionId, optionText, score);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        if (this.id != null && !this.id.equals(id)) {
            throw new IllegalStateException("Cannot change ID ");
        }
        this.id = id;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AnswerOption that = (AnswerOption) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
