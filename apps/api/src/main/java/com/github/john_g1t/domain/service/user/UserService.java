package com.github.john_g1t.domain.service.user;

import com.github.john_g1t.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Integer registerUser(String email, String password, String firstName, String lastName);
    Optional<User> authenticateUser(String email, String password);
    Optional<User> findById(Integer id);
    Optional<User> findByEmail(String email);
    List<User> getAllUsers();
    boolean existsById(Integer id);
    boolean existsByEmail(String email);
    void changePassword(Integer userId, String oldPassword, String newPassword);
}
