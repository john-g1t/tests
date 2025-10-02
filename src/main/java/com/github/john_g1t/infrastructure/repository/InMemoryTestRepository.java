package com.github.john_g1t.infrastructure.repository;

import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.repository.TestRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTestRepository implements TestRepository {
    private final Map<Integer, Test> tests = new HashMap<>();
    private int currentId = 1;

    @Override
    public Integer save(Test test) {
        if (test.getId() == null) {
            test.setId(currentId++);
        }
        tests.put(test.getId(), test);
        return test.getId();
    }

    @Override
    public Optional<Test> findById(Integer id) {
        return Optional.ofNullable(tests.get(id));
    }

    @Override
    public List<Test> findAll() {
        return new ArrayList<>(tests.values());
    }

    @Override
    public List<Test> findByCreator(Integer creatorId) {
        return tests.values().stream()
                .filter(test -> test.getCreatedBy().equals(creatorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Test> findActiveTests() {
        return tests.values().stream()
                .filter(Test::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        tests.remove(id);
    }
}