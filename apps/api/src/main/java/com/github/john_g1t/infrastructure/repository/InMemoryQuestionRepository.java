package com.github.john_g1t.infrastructure.repository;

import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.repository.QuestionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryQuestionRepository implements QuestionRepository {
    private final Map<Integer, Question> questions = new HashMap<>();
    private int currentId = 1;

    @Override
    public Integer save(Question question) {
        if (question.getId() == null) {
            question.setId(currentId++);
        }
        questions.put(question.getId(), question);
        return question.getId();
    }

    @Override
    public Optional<Question> findById(Integer id) {
        return Optional.ofNullable(questions.get(id));
    }

    @Override
    public List<Question> findByTestId(Integer testId) {
        return questions.values().stream()
                .filter(q -> q.getTestId().equals(testId))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        questions.remove(id);
    }
}