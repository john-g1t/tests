package com.github.john_g1t.infrastructure.repository.inmemory;

import com.github.john_g1t.domain.model.User;
import com.github.john_g1t.domain.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final Map<Integer, User> usersById = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();
    private int currentId = 1;

    @Override
    public Integer save(User user) {
        if (user.getId() == null) {
            user.setId(currentId++);
        }

        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);

        return user.getId();
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }

    @Override
    public boolean existsByEmail(String email) {
        return usersByEmail.containsKey(email);
    }

    @Override
    public boolean existsById(Integer id) {
        return usersById.containsKey(id);
    }

    @Override
    public void delete(Integer id) {
        User user = usersById.remove(id);
        if (user != null) {
            usersByEmail.remove(user.getEmail());
        }
    }
}
