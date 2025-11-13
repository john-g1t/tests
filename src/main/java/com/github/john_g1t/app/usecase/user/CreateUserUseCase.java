package com.github.john_g1t.app.usecase.user;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.domain.service.user.UserService;

public class CreateUserUseCase implements UseCase<CreateUserRequest, Integer> {
    private final UserService userService;

    public CreateUserUseCase(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Integer execute(CreateUserRequest request) {
        validatePassword(request.password());
        validateNames(request.firstName(), request.lastName());

        if (userService.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        return userService.registerUser(
            request.email(),
            request.password(),
            request.firstName(),
            request.lastName()
        );
    }

    private void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("No password provided");
        }
    }

    private void validateNames(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
    }
}
