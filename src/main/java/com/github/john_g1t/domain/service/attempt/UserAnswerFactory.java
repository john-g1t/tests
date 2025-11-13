package com.github.john_g1t.domain.service.attempt;

import com.github.john_g1t.domain.model.UserAnswer;

public class UserAnswerFactory {
    public UserAnswer createUserAnswer(Integer attemptId, Integer questionId, Integer answerId, String answerText) {
        return new UserAnswer(attemptId, questionId, answerId, answerText);
    }
}
