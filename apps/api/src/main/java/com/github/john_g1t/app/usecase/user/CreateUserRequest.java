package com.github.john_g1t.app.usecase.user;

public record CreateUserRequest(
    String email,
    String password,
    String firstName,
    String lastName
) {}
