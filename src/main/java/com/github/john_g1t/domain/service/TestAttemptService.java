package com.github.john_g1t.domain.service;

import com.github.john_g1t.domain.model.*;
import com.github.john_g1t.domain.repository.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class TestAttemptService {
    private final TestAttemptRepository attemptRepository;
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserRepository userRepository;

    public TestAttemptService(
        TestAttemptRepository attemptRepository, TestRepository testRepository,
        QuestionRepository questionRepository, AnswerOptionRepository answerOptionRepository,
        UserAnswerRepository userAnswerRepository, UserRepository userRepository
    ) {
        this.attemptRepository = attemptRepository;
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.answerOptionRepository = answerOptionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.userRepository = userRepository;
    }

    public Integer startAttempt(Integer userId, Integer testId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        Optional<Test> test = testRepository.findById(testId);
        if (test.isEmpty()) {
            throw new IllegalArgumentException("Test not found");
        }

        Test t = test.get();
        if (!t.isActive()) {
            throw new IllegalStateException("Test is not active");
        }

        ZonedDateTime now = ZonedDateTime.now();
        if (t.getStartTime() != null && now.isBefore(t.getStartTime())) {
            throw new IllegalStateException("Test has not started yet");
        }
        if (t.getEndTime() != null && now.isAfter(t.getEndTime())) {
            throw new IllegalStateException("Test has ended");
        }

        List<TestAttempt> previousAttempts = attemptRepository.findByUserAndTest(userId, testId);
        if (t.getMaxAttempts() != null && previousAttempts.size() >= t.getMaxAttempts()) {
            throw new IllegalStateException("Maximum attempts reached");
        }

        int attemptNumber = previousAttempts.size() + 1;
        TestAttempt attempt = new TestAttempt(userId, testId, now, null, 0, attemptNumber);

        return attemptRepository.save(attempt);
    }

    public void submitAnswer(Integer attemptId, Integer questionId, Integer answerId, String answerText) {
        Optional<TestAttempt> attempt = attemptRepository.findById(attemptId);
        if (attempt.isEmpty()) {
            throw new IllegalArgumentException("Test attempt not found");
        }

        if (attempt.get().getEndTime() != null) {
            throw new IllegalStateException("Test attempt already finished");
        }

        Optional<Question> question = questionRepository.findById(questionId);
        if (question.isEmpty()) {
            throw new IllegalArgumentException("Question not found");
        }

        UserAnswer userAnswer = new UserAnswer(attemptId, questionId, answerId, answerText);
        userAnswerRepository.save(userAnswer);
    }

    public Integer finishAttempt(Integer attemptId) {
        Optional<TestAttempt> attempt = attemptRepository.findById(attemptId);
        if (attempt.isEmpty()) {
            throw new IllegalArgumentException("Test attempt not found");
        }

        TestAttempt a = attempt.get();
        if (a.getEndTime() != null) {
            throw new IllegalStateException("Test attempt already finished");
        }

        Optional<Test> test = testRepository.findById(a.getTestId());
        if (test.isEmpty()) {
            throw new IllegalArgumentException("Test not found");
        }

        Test t = test.get();
        ZonedDateTime now = ZonedDateTime.now();

        if (t.getTimeLimit() != null) {
            ZonedDateTime deadline = a.getStartTime().plusMinutes(t.getTimeLimit());
            if (now.isAfter(deadline)) {
                now = deadline;
            }
        }

        int totalScore = calculateScore(attemptId);
        a.setEndTime(now);
        a.setScore(totalScore);
        attemptRepository.save(a);

        return totalScore;
    }

    private int calculateScore(Integer attemptId) {
        List<UserAnswer> userAnswers = userAnswerRepository.findByAttemptId(attemptId);
        int totalScore = 0;

        for (UserAnswer userAnswer : userAnswers) {
            if (userAnswer.getAnswerId() != null) {
                Optional<AnswerOption> answerOption = answerOptionRepository.findById(userAnswer.getAnswerId());
                if (answerOption.isPresent() && answerOption.get().getScore() != null) {
                    totalScore += answerOption.get().getScore();
                }
            }
        }

        return totalScore;
    }

    public Optional<TestAttempt> getAttempt(Integer attemptId) {
        return attemptRepository.findById(attemptId);
    }

    public List<TestAttempt> getUserAttempts(Integer userId) {
        return attemptRepository.findByUserId(userId);
    }

    public List<UserAnswer> getAttemptAnswers(Integer attemptId) {
        return userAnswerRepository.findByAttemptId(attemptId);
    }
}
