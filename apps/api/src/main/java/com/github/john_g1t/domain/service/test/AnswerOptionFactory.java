
package com.github.john_g1t.domain.service.test;

import com.github.john_g1t.domain.model.AnswerOption;

public class AnswerOptionFactory {
    public AnswerOption createAnswerOption(Integer questionId, String optionText, Integer score) {
        return new AnswerOption(questionId, optionText, score);
    }
}
