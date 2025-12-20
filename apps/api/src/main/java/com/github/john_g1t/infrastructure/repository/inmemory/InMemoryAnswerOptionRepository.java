package com.github.john_g1t.infrastructure.repository.inmemory;

import com.github.john_g1t.domain.model.AnswerOption;
import com.github.john_g1t.domain.repository.AnswerOptionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryAnswerOptionRepository implements AnswerOptionRepository {
    private final Map<Integer, AnswerOption> answerOptions = new HashMap<>();
    private int currentId = 1;

    @Override
    public Integer save(AnswerOption answerOption) {
        if (answerOption.getId() == null) {
            answerOption.setId(currentId++);
        }
        answerOptions.put(answerOption.getId(), answerOption);
        return answerOption.getId();
    }

    @Override
    public Optional<AnswerOption> findById(Integer id) {
        return Optional.ofNullable(answerOptions.get(id));
    }

    @Override
    public List<AnswerOption> findByQuestionId(Integer questionId) {
        return answerOptions.values().stream()
                .filter(ao -> ao.getQuestionId().equals(questionId))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        answerOptions.remove(id);
    }
}
