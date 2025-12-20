package com.github.john_g1t.infrastructure.repository.inmemory;

import com.github.john_g1t.domain.model.UserAnswer;
import com.github.john_g1t.domain.repository.UserAnswerRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryUserAnswerRepository implements UserAnswerRepository {
    private final Map<Integer, UserAnswer> answers = new HashMap<>();
    private int currentId = 1;

    @Override
    public Integer save(UserAnswer answer) {
        if (answer.getId() == null) {
            answer.setId(currentId++);
        }
        answers.put(answer.getId(), answer);
        return answer.getId();
    }

    @Override
    public Optional<UserAnswer> findById(Integer id) {
        return Optional.ofNullable(answers.get(id));
    }

    @Override
    public List<UserAnswer> findByAttemptId(Integer attemptId) {
        return answers.values().stream()
                .filter(a -> a.getAttemptId().equals(attemptId))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        answers.remove(id);
    }
}
