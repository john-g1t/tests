package com.github.john_g1t.domain.repository;

import com.github.john_g1t.domain.model.AnswerOption;

import java.util.List;
import java.util.Optional;

public interface AnswerOptionRepository {
    Integer save(AnswerOption answerOption);
    Optional<AnswerOption> findById(Integer id);
    List<AnswerOption> findByQuestionId(Integer questionId);
    void delete(Integer id);
}
