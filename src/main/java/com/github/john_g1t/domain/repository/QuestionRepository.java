package com.github.john_g1t.domain.repository;

import com.github.john_g1t.domain.model.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository {
    Integer save(Question question);
    Optional<Question> findById(Integer id);
    List<Question> findByTestId(Integer testId);
    void delete(Integer id);
}