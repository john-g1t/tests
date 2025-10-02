package com.github.john_g1t.app.usecase;

import com.github.john_g1t.domain.service.UserService;

public class CreateUserUseCase {
    private final UserService userService;

    public CreateUserUseCase(UserService userService) {
        this.userService = userService;
    }

    public Integer execute(String email, String password, String firstName, String lastName) {
        return userService.registerUser(email, password, firstName, lastName);
    }
}