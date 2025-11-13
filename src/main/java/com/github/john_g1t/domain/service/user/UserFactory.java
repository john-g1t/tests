package com.github.john_g1t.domain.service.user;

import com.github.john_g1t.domain.model.User;

public class UserFactory {
    public User createUser(String email, String hashedPassword, String firstName, String lastName) {
        return new User(email, hashedPassword, firstName, lastName);
    }
}
