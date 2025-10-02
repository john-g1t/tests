package com.github.john_g1t.domain.repository;

import com.github.john_g1t.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Integer save(User user);
    Optional<User> findById(Integer id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    boolean existsByEmail(String email);
    void delete(Integer id);
}
