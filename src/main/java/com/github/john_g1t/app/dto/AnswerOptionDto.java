package com.github.john_g1t.app.dto;

public class AnswerOptionDto {
    private final Integer id;
    private final String text;

    public AnswerOptionDto(Integer id, String text) {
        this.id = id;
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
