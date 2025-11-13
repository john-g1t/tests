package com.github.john_g1t.domain.service.user;

import com.github.john_g1t.domain.model.User;
import com.github.john_g1t.domain.repository.UserRepository;
import com.github.john_g1t.domain.service.PasswordGenerator;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserFactory userFactory;

    public UserServiceImpl(UserRepository userRepository, UserFactory userFactory) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
    }

    @Override
    public Integer registerUser(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with email " + email + " already exists");
        }

        String hashedPassword = PasswordGenerator.getInstance().hash(password);
        User user = userFactory.createUser(email, hashedPassword, firstName, lastName);
        return userRepository.save(user);
    }

    @Override
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

    @Override
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean existsById(Integer id) {
        return userRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
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
