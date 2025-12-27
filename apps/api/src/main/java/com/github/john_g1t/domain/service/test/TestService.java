package com.github.john_g1t.domain.service.test;

import com.github.john_g1t.domain.model.AnswerOption;
import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.model.Test;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface TestService {
    Integer createTest(
        Integer creatorId, String title, String description, Integer timeLimit,
        Integer maxAttempts, ZonedDateTime startTime, ZonedDateTime endTime
    );
    Integer addQuestion(Integer testId, String text, String answerType, Integer maxPoints);
    Integer addAnswerOption(Integer questionId, String optionText, Integer score);
    Optional<Test> getTest(Integer testId);
    List<Test> getAllTests();
    List<Test> getActiveTests();
    List<Test> getTestsByCreator(Integer creatorId);
    List<Question> getQuestions(Integer testId);
    List<AnswerOption> getAnswerOptions(Integer questionId);
    Optional<Question> getQuestionById(Integer questionId);
    boolean isTestActive(Integer testId);
    void deactivateTest(Integer testId);
    void saveTest(Test test);
    void saveQuestion(Question question);
    void deleteQuestionById(Integer questionId);
    List<AnswerOption> getOptions(Integer questionId);
}
