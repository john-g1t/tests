package com.github.john_g1t.infrastructure;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.attempt.FinishTestAttemptRequest;
import com.github.john_g1t.app.usecase.attempt.StartTestAttemptRequest;
import com.github.john_g1t.app.usecase.attempt.StartTestAttemptUseCase;
import com.github.john_g1t.app.usecase.attempt.SubmitAnswerUseCase;
import com.github.john_g1t.app.usecase.attempt.FinishTestAttemptUseCase;
import com.github.john_g1t.app.usecase.attempt.SubmitAnswerRequest;
import com.github.john_g1t.app.usecase.test.CreateTestRequest;
import com.github.john_g1t.app.usecase.test.CreateTestUseCase;
import com.github.john_g1t.app.usecase.user.CreateUserRequest;
import com.github.john_g1t.app.usecase.user.CreateUserUseCase;
import com.github.john_g1t.domain.repository.AnswerOptionRepository;
import com.github.john_g1t.domain.repository.QuestionRepository;
import com.github.john_g1t.domain.repository.TestRepository;
import com.github.john_g1t.domain.repository.UserRepository;
import com.github.john_g1t.domain.repository.TestAttemptRepository;
import com.github.john_g1t.domain.repository.UserAnswerRepository;
import com.github.john_g1t.domain.service.attempt.TestAttemptFactory;
import com.github.john_g1t.domain.service.attempt.TestAttemptService;
import com.github.john_g1t.domain.service.attempt.UserAnswerFactory;
import com.github.john_g1t.domain.service.test.AnswerOptionFactory;
import com.github.john_g1t.domain.service.test.QuestionFactory;
import com.github.john_g1t.domain.service.test.TestFactory;
import com.github.john_g1t.domain.service.test.TestService;
import com.github.john_g1t.domain.service.user.UserFactory;
import com.github.john_g1t.domain.service.user.UserService;
import com.github.john_g1t.domain.service.user.UserServiceImpl;
import com.github.john_g1t.domain.service.attempt.TestAttemptServiceImpl;
import com.github.john_g1t.domain.service.test.TestServiceImpl;
import com.github.john_g1t.infrastructure.repository.RepositoryProvider;
import com.github.john_g1t.infrastructure.repository.postgres.PostgresConnectionFactory;

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
        this(RepositoryProvider.inMemory());
    }

    public ApplicationContext(PostgresConnectionFactory connectionFactory) {
        this(RepositoryProvider.postgres(connectionFactory));
    }

    private ApplicationContext(RepositoryProvider repos) {
        this.userRepository = repos.userRepository();
        this.testRepository = repos.testRepository();
        this.questionRepository = repos.questionRepository();
        this.answerOptionRepository = repos.answerOptionRepository();
        this.attemptRepository = repos.attemptRepository();
        this.userAnswerRepository = repos.userAnswerRepository();

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
