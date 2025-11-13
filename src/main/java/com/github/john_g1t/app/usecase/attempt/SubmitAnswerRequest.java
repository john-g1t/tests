package com.github.john_g1t.app.usecase.attempt;

public record SubmitAnswerRequest(
    Integer attemptId,
    Integer questionId,
    Integer answerId,
    String answerText
) {}
