package com.github.john_g1t.infrastructure;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.attempt.*;
import com.github.john_g1t.app.usecase.test.CreateTestRequest;
import com.github.john_g1t.app.usecase.test.CreateTestUseCase;
import com.github.john_g1t.app.usecase.user.CreateUserRequest;
import com.github.john_g1t.app.usecase.user.CreateUserUseCase;
import com.github.john_g1t.domain.repository.*;
import com.github.john_g1t.domain.service.attempt.*;
import com.github.john_g1t.domain.service.test.*;
import com.github.john_g1t.domain.service.user.*;
import com.github.john_g1t.infrastructure.repository.*;

public class ApplicationContext {
    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final TestAttemptRepository attemptRepository;
    private final UserAnswerRepository userAnswerRepository;

    private final UserFactory userFactory;
    private final TestFactory testFactory;
    private final QuestionFactory questionFactory;
    private final AnswerOptionFactory answerOptionFactory;
    private final TestAttemptFactory attemptFactory;
    private final UserAnswerFactory userAnswerFactory;

    private final UserService userService;
    private final TestService testService;
    private final TestAttemptService attemptService;

    private final UseCase<CreateUserRequest, Integer> createUserUseCase;
    private final UseCase<CreateTestRequest, Integer> createTestUseCase;
    private final UseCase<StartTestAttemptRequest, Integer> startTestAttemptUseCase;
    private final UseCase<SubmitAnswerRequest, Void> submitAnswerUseCase;
    private final UseCase<FinishTestAttemptRequest, Integer> finishTestAttemptUseCase;

    public ApplicationContext() {
        this.userRepository = new InMemoryUserRepository();
        this.testRepository = new InMemoryTestRepository();
        this.questionRepository = new InMemoryQuestionRepository();
        this.answerOptionRepository = new InMemoryAnswerOptionRepository();
        this.attemptRepository = new InMemoryTestAttemptRepository();
        this.userAnswerRepository = new InMemoryUserAnswerRepository();

        this.userFactory = new UserFactory();
        this.testFactory = new TestFactory();
        this.questionFactory = new QuestionFactory();
        this.answerOptionFactory = new AnswerOptionFactory();
        this.attemptFactory = new TestAttemptFactory();
        this.userAnswerFactory = new UserAnswerFactory();

        this.userService = new UserServiceImpl(userRepository, userFactory);
        this.testService = new TestServiceImpl(
            testRepository, questionRepository, answerOptionRepository,
            testFactory, questionFactory, answerOptionFactory
        );
        this.attemptService = new TestAttemptServiceImpl(
            attemptRepository, testRepository, questionRepository,
            answerOptionRepository, userAnswerRepository, userRepository,
            attemptFactory, userAnswerFactory
        );

        this.createUserUseCase = new CreateUserUseCase(userService);
        this.createTestUseCase = new CreateTestUseCase(testService, userService);
        this.startTestAttemptUseCase = new StartTestAttemptUseCase(attemptService);
        this.submitAnswerUseCase = new SubmitAnswerUseCase(attemptService);
        this.finishTestAttemptUseCase = new FinishTestAttemptUseCase(attemptService);
    }


    public UserService getUserService() {
        return userService;
    }

    public TestService getTestService() {
        return testService;
    }

    public TestAttemptService getAttemptService() {
        return attemptService;
    }

    public UseCase<CreateUserRequest, Integer> getCreateUserUseCase() {
        return createUserUseCase;
    }

    public UseCase<CreateTestRequest, Integer> getCreateTestUseCase() {
        return createTestUseCase;
    }

    public UseCase<StartTestAttemptRequest, Integer> getStartTestAttemptUseCase() {
        return startTestAttemptUseCase;
    }

    public UseCase<SubmitAnswerRequest, Void> getSubmitAnswerUseCase() {
        return submitAnswerUseCase;
    }

    public UseCase<FinishTestAttemptRequest, Integer> getFinishTestAttemptUseCase() {
        return finishTestAttemptUseCase;
    }
}
