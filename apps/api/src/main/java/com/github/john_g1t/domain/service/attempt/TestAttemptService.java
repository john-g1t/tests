package com.github.john_g1t.domain.service.attempt;

import com.github.john_g1t.domain.model.TestAttempt;
import com.github.john_g1t.domain.model.UserAnswer;
import java.util.List;
import java.util.Optional;

public interface TestAttemptService {
    Integer startAttempt(Integer userId, Integer testId);
    void submitAnswer(Integer attemptId, Integer questionId, Integer answerId, String answerText);
    Integer finishAttempt(Integer attemptId);
    Optional<TestAttempt> getAttempt(Integer attemptId);
    List<TestAttempt> getUserAttempts(Integer userId);
    List<TestAttempt> getByTestId(Integer testId);
    List<UserAnswer> getAttemptAnswers(Integer attemptId);
}
