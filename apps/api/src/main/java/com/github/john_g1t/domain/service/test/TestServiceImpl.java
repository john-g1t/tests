package com.github.john_g1t.domain.service.test;

import com.github.john_g1t.domain.model.AnswerOption;
import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.model.TestAttempt;
import com.github.john_g1t.domain.repository.AnswerOptionRepository;
import com.github.john_g1t.domain.repository.QuestionRepository;
import com.github.john_g1t.domain.repository.TestRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class TestServiceImpl implements TestService {
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final TestFactory testFactory;
    private final QuestionFactory questionFactory;
    private final AnswerOptionFactory answerOptionFactory;

    public TestServiceImpl(
        TestRepository testRepository,
        QuestionRepository questionRepository,
        AnswerOptionRepository answerOptionRepository,
        TestFactory testFactory,
        QuestionFactory questionFactory,
        AnswerOptionFactory answerOptionFactory
    ) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.answerOptionRepository = answerOptionRepository;
        this.testFactory = testFactory;
        this.questionFactory = questionFactory;
        this.answerOptionFactory = answerOptionFactory;
    }

    @Override
    public Integer createTest(
            Integer creatorId, String title, String description, Integer timeLimit,
            Integer maxAttempts, ZonedDateTime startTime, ZonedDateTime endTime
    ) {
        Test test = testFactory.createTest(creatorId, title, description, timeLimit,
                maxAttempts, startTime, endTime);
        return testRepository.save(test);
    }

    @Override
    public Integer addQuestion(Integer testId, String text, String answerType, Integer maxPoints) {
        Optional<Test> test = testRepository.findById(testId);
        if (test.isEmpty()) {
            throw new IllegalArgumentException("Test not found");
        }

        Question question = questionFactory.createQuestion(testId, text, answerType, maxPoints);
        return questionRepository.save(question);
    }

    @Override
    public Integer addAnswerOption(Integer questionId, String optionText, Integer score) {
        Optional<Question> question = questionRepository.findById(questionId);
        if (question.isEmpty()) {
            throw new IllegalArgumentException("Question not found");
        }

        AnswerOption option = answerOptionFactory.createAnswerOption(questionId, optionText, score);
        return answerOptionRepository.save(option);
    }

    @Override
    public Optional<Test> getTest(Integer testId) {
        return testRepository.findById(testId);
    }

    @Override
    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    @Override
    public List<Test> getActiveTests() {
        return testRepository.findActiveTests();
    }

    @Override
    public List<Test> getTestsByCreator(Integer creatorId) {
        return testRepository.findByCreator(creatorId);
    }

    @Override
    public List<Question> getQuestions(Integer testId) {
        return questionRepository.findByTestId(testId);
    }

    @Override
    public List<AnswerOption> getAnswerOptions(Integer questionId) {
        return answerOptionRepository.findByQuestionId(questionId);
    }

    @Override
    public Optional<Question> getQuestionById(Integer questionId) {
        return questionRepository.findById(questionId);
    }

    @Override
    public boolean isTestActive(Integer testId) {
        Optional<Test> test = getTest(testId);
        return test.map(Test::isActive).orElse(false);
    }

    @Override
    public void deactivateTest(Integer testId) {
        Optional<Test> test = testRepository.findById(testId);
        if (test.isPresent()) {
            Test t = test.get();
            t.setActive(false);
            testRepository.save(t);
        }
    }

    @Override
    public void saveTest(Test test) {
        this.testRepository.save(test);
    }

    @Override
    public void saveQuestion(Question question) {
        this.questionRepository.save(question);
    }


    @Override
    public void deleteQuestionById(Integer questionId) {
        this.questionRepository.delete(questionId);
    }
}
