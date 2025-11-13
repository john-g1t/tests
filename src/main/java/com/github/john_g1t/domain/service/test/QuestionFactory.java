package com.github.john_g1t.domain.service.test;

import com.github.john_g1t.domain.model.Question;

public class QuestionFactory {
    public Question createQuestion(Integer testId, String text, String answerType, Integer maxPoints) {
        return new Question(testId, text, answerType, maxPoints);
    }
}
