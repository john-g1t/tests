package com.github.john_g1t;

import com.github.john_g1t.app.usecase.*;
import com.github.john_g1t.domain.repository.*;
import com.github.john_g1t.domain.service.PasswordGenerator;
import com.github.john_g1t.domain.service.TestAttemptService;
import com.github.john_g1t.domain.service.TestService;
import com.github.john_g1t.domain.service.UserService;
import com.github.john_g1t.infrastructure.repository.*;
import com.github.john_g1t.presentation.console.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
        PasswordGenerator.init("salt"); // hello cybersecurity

        UserRepository userRepository = new InMemoryUserRepository();
        TestRepository testRepository = new InMemoryTestRepository();
        QuestionRepository questionRepository = new InMemoryQuestionRepository();
        AnswerOptionRepository answerOptionRepository = new InMemoryAnswerOptionRepository();
        TestAttemptRepository attemptRepository = new InMemoryTestAttemptRepository();
        UserAnswerRepository userAnswerRepository = new InMemoryUserAnswerRepository();

        UserService userService = new UserService(userRepository);

        TestService testService = new TestService(
            testRepository,
            questionRepository,
            answerOptionRepository
        );

        TestAttemptService attemptService = new TestAttemptService(
            attemptRepository,
            testRepository,
            questionRepository,
            answerOptionRepository,
            userAnswerRepository,
            userRepository
        );

        ConsoleMenu consoleMenu = getConsoleMenu(userService, testService, attemptService);

        consoleMenu.start();
    }

    private static ConsoleMenu getConsoleMenu(UserService userService, TestService testService, TestAttemptService attemptService) {
        CreateUserUseCase createUserUseCase = new CreateUserUseCase(userService);
        CreateTestUseCase createTestUseCase = new CreateTestUseCase(testService);
        StartTestAttemptUseCase startTestAttemptUseCase = new StartTestAttemptUseCase(attemptService);
        SubmitAnswerUseCase submitAnswerUseCase = new SubmitAnswerUseCase(attemptService);
        FinishTestAttemptUseCase finishTestAttemptUseCase = new FinishTestAttemptUseCase(attemptService);

        return new ConsoleMenu(
            createUserUseCase,
            createTestUseCase,
            startTestAttemptUseCase,
            submitAnswerUseCase,
            finishTestAttemptUseCase,
            userService,
            testService,
            attemptService
        );
    }
}
