package com.github.john_g1t.presentation.view;

import com.github.john_g1t.domain.model.AnswerOption;
import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.model.TestAttempt;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsoleView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public void showWelcome() {
        System.out.println("---Welcome to Test Management System---");
    }

    public void showMainMenu() {
        System.out.println("\n---Main Menu---");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
    }

    public void showUserMenu(String userName) {
        System.out.println("\n---User Menu (" + userName + ")---");
        System.out.println("1. Create Test");
        System.out.println("2. View My Tests");
        System.out.println("3. View All Active Tests");
        System.out.println("4. Take Test");
        System.out.println("5. View My Results");
        System.out.println("6. Logout");
    }

    public void showTests(List<Test> tests) {
        if (tests.isEmpty()) {
            System.out.println("\nNo tests available.");
            return;
        }

        System.out.println("\n--- Tests ---");
        for (Test test : tests) {
            System.out.println("\nID: " + test.getId());
            System.out.println("Title: " + test.getTitle());
            System.out.println("Description: " + test.getDescription());
            System.out.println("Created by: " + test.getCreatedBy());
            System.out.println("Time limit: " + (test.getTimeLimit() != null ? test.getTimeLimit() + " min" : "No limit"));
            System.out.println("Max attempts: " + (test.getMaxAttempts() != null ? test.getMaxAttempts() : "Unlimited"));
            System.out.println("Active: " + (test.isActive() ? "Yes" : "No"));
            if (test.getStartTime() != null) {
                System.out.println("Start time: " + test.getStartTime().format(DATE_FORMAT));
            }
            if (test.getEndTime() != null) {
                System.out.println("End time: " + test.getEndTime().format(DATE_FORMAT));
            }
            System.out.println("---");
        }
    }

    public void showQuestion(Question question, List<AnswerOption> options, int questionNumber, int totalQuestions) {
        System.out.println("\n===========================================");
        System.out.println("Question " + questionNumber + " of " + totalQuestions);
        System.out.println("===========================================");
        System.out.println(question.getText());
        System.out.println("Max points: " + question.getMaxPoints());
        System.out.println("\nAnswer options:");

        for (int i = 0; i < options.size(); i++) {
            AnswerOption option = options.get(i);
            System.out.println((i + 1) + ". " + option.getOptionText());
        }
    }

    public void showTestResults(List<TestAttempt> attempts, List<Test> tests) {
        if (attempts.isEmpty()) {
            System.out.println("\nNo test attempts found.");
            return;
        }

        System.out.println("\n--- Your Test Results ---");
        for (TestAttempt attempt : attempts) {
            Test test = tests.stream()
                    .filter(t -> t.getId().equals(attempt.getTestId()))
                    .findFirst()
                    .orElse(null);

            System.out.println("\nAttempt ID: " + attempt.getId());
            System.out.println("Test: " + (test != null ? test.getTitle() : "Unknown"));
            System.out.println("Attempt #" + attempt.getAttemptNumber());
            System.out.println("Score: " + attempt.getScore());
            System.out.println("Started: " + attempt.getStartTime().format(DATE_FORMAT));
            if (attempt.getEndTime() != null) {
                System.out.println("Finished: " + attempt.getEndTime().format(DATE_FORMAT));
            } else {
                System.out.println("Status: In progress");
            }
            System.out.println("---");
        }
    }

    public void showMessage(String message) {
        System.out.println("\n" + message);
    }

    public void showError(String error) {
        System.out.println("\n[ERROR] " + error);
    }

    public void showSuccess(String message) {
        System.out.println("\n[SUCCESS] " + message);
    }
}
