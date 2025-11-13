package com.github.john_g1t.domain.repository;

import com.github.john_g1t.domain.model.Test;

import java.util.List;
import java.util.Optional;

public interface TestRepository {
    Integer save(Test test);
    Optional<Test> findById(Integer id);
    List<Test> findAll();
    List<Test> findByCreator(Integer creatorId);
    List<Test> findActiveTests();
    void delete(Integer id);
}