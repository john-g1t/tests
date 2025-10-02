package com.github.john_g1t.domain.service;

import com.github.john_g1t.domain.model.User;
import com.github.john_g1t.domain.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Integer registerUser(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with email " + email + " already exists");
        }

        String hashedPassword = PasswordGenerator.getInstance().hash(password);
        User user = new User(email, hashedPassword, firstName, lastName);
        return userRepository.save(user);
    }

    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            String storedHash = user.get().getPassword();
            if (PasswordGenerator.getInstance().verify(password, storedHash)) {
                return user;
            }
        }

        return Optional.empty();
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User u = user.get();
        if (!PasswordGenerator.getInstance().verify(oldPassword, u.getPassword())) {
            throw new IllegalStateException("Invalid old password");
        }

        String hashedPassword = PasswordGenerator.getInstance().hash(newPassword);
        u.setPassword(hashedPassword);
        userRepository.save(u);
    }
}