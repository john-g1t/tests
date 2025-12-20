package com.github.john_g1t.infrastructure.repository;

import com.github.john_g1t.domain.repository.*;
import com.github.john_g1t.infrastructure.repository.inmemory.*;
import com.github.john_g1t.infrastructure.repository.postgres.*;

import java.sql.SQLException;

public interface RepositoryProvider {
    UserRepository userRepository();
    TestRepository testRepository();
    QuestionRepository questionRepository();
    AnswerOptionRepository answerOptionRepository();
    TestAttemptRepository attemptRepository();
    UserAnswerRepository userAnswerRepository();

    static RepositoryProvider inMemory() {
        return new RepositoryProvider() {
            public UserRepository userRepository() {
                return new InMemoryUserRepository();
            }
            public TestRepository testRepository() {
                return new InMemoryTestRepository();
            }
            public QuestionRepository questionRepository() {
                return new InMemoryQuestionRepository();
            }
            public AnswerOptionRepository answerOptionRepository() {
                return new InMemoryAnswerOptionRepository();
            }
            public TestAttemptRepository attemptRepository() {
                return new InMemoryTestAttemptRepository();
            }
            public UserAnswerRepository userAnswerRepository() {
                return new InMemoryUserAnswerRepository();
            }
        };
    }

    static RepositoryProvider postgres(PostgresConnectionFactory cf) {
        return new RepositoryProvider() {
            public UserRepository userRepository() {
                return new PostgresUserRepository(cf.getConnection());
            }
            public TestRepository testRepository() {
                return new PostgresTestRepository(cf.getConnection());
            }
            public QuestionRepository questionRepository() {
                return new PostgresQuestionRepository(cf.getConnection());
            }
            public AnswerOptionRepository answerOptionRepository() {
                return new PostgresAnswerOptionRepository(cf.getConnection());
            }
            public TestAttemptRepository attemptRepository() {
                return new PostgresTestAttemptRepository(cf.getConnection());
            }
            public UserAnswerRepository userAnswerRepository() {
                return new PostgresUserAnswerRepository(cf.getConnection());
            }
        };
    }
}