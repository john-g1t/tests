package com.github.john_g1t.infrastructure.repository;

import com.github.john_g1t.domain.model.TestAttempt;
import com.github.john_g1t.domain.repository.TestAttemptRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryTestAttemptRepository implements TestAttemptRepository {
    private final Map<Integer, TestAttempt> attempts = new HashMap<>();
    private int currentId = 1;

    @Override
    public Integer save(TestAttempt attempt) {
        if (attempt.getId() == null) {
            attempt.setId(currentId++);
        }
        attempts.put(attempt.getId(), attempt);
        return attempt.getId();
    }

    @Override
    public Optional<TestAttempt> findById(Integer id) {
        return Optional.ofNullable(attempts.get(id));
    }

    @Override
    public List<TestAttempt> findByUserId(Integer userId) {
        return attempts.values().stream()
                .filter(a -> a.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<TestAttempt> findByTestId(Integer testId) {
        return attempts.values().stream()
                .filter(a -> a.getTestId().equals(testId))
                .collect(Collectors.toList());
    }

    @Override
    public List<TestAttempt> findByUserAndTest(Integer userId, Integer testId) {
        return attempts.values().stream()
                .filter(a -> a.getUserId().equals(userId) && a.getTestId().equals(testId))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        attempts.remove(id);
    }
}