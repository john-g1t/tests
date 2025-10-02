package com.github.john_g1t.domain.service;

import com.github.john_g1t.domain.model.AnswerOption;
import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.repository.AnswerOptionRepository;
import com.github.john_g1t.domain.repository.QuestionRepository;
import com.github.john_g1t.domain.repository.TestRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class TestService {
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;

    public TestService(
        TestRepository testRepository, QuestionRepository questionRepository,
        AnswerOptionRepository answerOptionRepository
    ) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.answerOptionRepository = answerOptionRepository;
    }

    public Integer createTest(
        Integer creatorId, String title, String description, Integer timeLimit,
        Integer maxAttempts, ZonedDateTime startTime, ZonedDateTime endTime
    ) {
        Test test = new Test(creatorId, title, description, timeLimit, maxAttempts, true, startTime, endTime);
        return testRepository.save(test);
    }

    public Integer addQuestion(Integer testId, String text, String answerType, Integer maxPoints) {
        Optional<Test> test = testRepository.findById(testId);
        if (test.isEmpty()) {
            throw new IllegalArgumentException("Test not found");
        }

        Question question = new Question(testId, text, answerType, maxPoints);
        return questionRepository.save(question);
    }

    public Integer addAnswerOption(Integer questionId, String optionText, Integer score) {
        Optional<Question> question = questionRepository.findById(questionId);
        if (question.isEmpty()) {
            throw new IllegalArgumentException("Question not found");
        }

        AnswerOption option = new AnswerOption(questionId, optionText, score);
        return answerOptionRepository.save(option);
    }

    public Optional<Test> getTest(Integer testId) {
        return testRepository.findById(testId);
    }

    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    public List<Test> getActiveTests() {
        return testRepository.findActiveTests();
    }

    public List<Test> getTestsByCreator(Integer creatorId) {
        return testRepository.findByCreator(creatorId);
    }

    public List<Question> getQuestions(Integer testId) {
        return questionRepository.findByTestId(testId);
    }

    public List<AnswerOption> getAnswerOptions(Integer questionId) {
        return answerOptionRepository.findByQuestionId(questionId);
    }

    public void deactivateTest(Integer testId) {
        Optional<Test> test = testRepository.findById(testId);
        if (test.isPresent()) {
            Test t = test.get();
            t.setActive(false);
            testRepository.save(t);
        }
    }
}
