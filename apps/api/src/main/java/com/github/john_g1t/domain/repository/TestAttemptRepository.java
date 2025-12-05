package com.github.john_g1t.domain.repository;

import com.github.john_g1t.domain.model.TestAttempt;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository {
    Integer save(TestAttempt attempt);
    Optional<TestAttempt> findById(Integer id);
    List<TestAttempt> findByUserId(Integer userId);
    List<TestAttempt> findByTestId(Integer testId);
    List<TestAttempt> findByUserAndTest(Integer userId, Integer testId);
    void delete(Integer id);
}