package com.github.john_g1t.domain.repository;

import com.github.john_g1t.domain.model.UserAnswer;

import java.util.List;
import java.util.Optional;

public interface UserAnswerRepository {
    Integer save(UserAnswer answer);
    Optional<UserAnswer> findById(Integer id);
    List<UserAnswer> findByAttemptId(Integer attemptId);
    void delete(Integer id);
}
