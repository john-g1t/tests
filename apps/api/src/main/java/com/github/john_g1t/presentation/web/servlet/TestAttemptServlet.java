package com.github.john_g1t.presentation.web.servlet;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.attempt.*;
import com.github.john_g1t.domain.model.*;
import com.github.john_g1t.domain.service.attempt.TestAttemptService;
import com.github.john_g1t.domain.service.test.TestService;
import com.github.john_g1t.infrastructure.ApplicationContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "TestAttemptServlet", urlPatterns = {"/attempts/*"})
public class TestAttemptServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCorsHeaders(response);

        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.startsWith("/user/")) {
                String userIdStr = pathInfo.substring(6);
                if (userIdStr.contains("/")) {
                    userIdStr = userIdStr.substring(0, userIdStr.indexOf("/"));
                }
                Integer requestedUserId = Integer.parseInt(userIdStr);
                handleGetUserAttempts(request, response, requestedUserId);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/details")) {
                Integer attemptId = parseIdFromPath(pathInfo, "/details");
                handleGetAttemptDetails(request, response, attemptId, userId);
            } else if (pathInfo != null) {
                Integer attemptId = parseId(pathInfo);
                if (attemptId != null) {
                    handleGetAttemptProgress(request, response, attemptId, userId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid attempt ID");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            }
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCorsHeaders(response);

        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/start")) {
                handleStartAttempt(request, response, userId);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/answers")) {
                Integer attemptId = parseIdFromPath(pathInfo, "/answers");
                handleSubmitAnswer(request, response, attemptId, userId);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/finish")) {
                Integer attemptId = parseIdFromPath(pathInfo, "/finish");
                handleFinishAttempt(request, response, attemptId, userId);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    private void handleStartAttempt(HttpServletRequest request, HttpServletResponse response,
                                    Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        UseCase<StartTestAttemptRequest, Integer> startTestAttemptUseCase =
                ctx.getStartTestAttemptUseCase();
        TestAttemptService attemptService = ctx.getAttemptService();
        TestService testService = ctx.getTestService();

        StartAttemptRequestDto startRequest = readJson(request, StartAttemptRequestDto.class);

        if (startRequest.testId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Test ID is required");
            return;
        }

        try {
            StartTestAttemptRequest useCaseRequest = new StartTestAttemptRequest(
                    userId,
                    startRequest.testId
            );
            Integer attemptId = startTestAttemptUseCase.execute(useCaseRequest);

            Optional<TestAttempt> attempt = attemptService.getAttempt(attemptId);
            Optional<Test> test = testService.getTest(startRequest.testId);

            if (attempt.isPresent()) {
                Map<String, Object> result = new HashMap<>();
                result.put("attemptId", attemptId);
                result.put("startTime", attempt.get().getStartTime());
                result.put("timeLimit", test.map(Test::getTimeLimit).orElse(null));

                sendSuccess(response, result);
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to retrieve attempt");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleSubmitAnswer(HttpServletRequest request, HttpServletResponse response,
                                    Integer attemptId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        UseCase<SubmitAnswerRequest, Void> submitAnswerUseCase = ctx.getSubmitAnswerUseCase();
        TestAttemptService attemptService = ctx.getAttemptService();

        Optional<TestAttempt> attempt = attemptService.getAttempt(attemptId);
        if (!attempt.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Attempt not found");
            return;
        }

        if (!attempt.get().getUserId().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Not your attempt");
            return;
        }

        if (attempt.get().getEndTime() != null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Attempt already finished");
            return;
        }

        SubmitAnswerRequestDto answerRequest = readJson(request, SubmitAnswerRequestDto.class);

        if (answerRequest.questionId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Question ID is required");
            return;
        }

        try {
            SubmitAnswerRequest useCaseRequest = new SubmitAnswerRequest(
                    attemptId,
                    answerRequest.questionId,
                    answerRequest.answerId,
                    sanitizeInput(answerRequest.answerText)
            );

            submitAnswerUseCase.execute(useCaseRequest);
            sendSuccess(response, null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleFinishAttempt(HttpServletRequest request, HttpServletResponse response,
                                     Integer attemptId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        UseCase<FinishTestAttemptRequest, Integer> finishTestAttemptUseCase =
                ctx.getFinishTestAttemptUseCase();
        TestAttemptService attemptService = ctx.getAttemptService();
        TestService testService = ctx.getTestService();

        Optional<TestAttempt> attempt = attemptService.getAttempt(attemptId);
        if (!attempt.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Attempt not found");
            return;
        }

        TestAttempt att = attempt.get();
        if (!att.getUserId().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Not your attempt");
            return;
        }

        if (att.getEndTime() != null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Attempt already finished");
            return;
        }

        try {
            FinishTestAttemptRequest useCaseRequest = new FinishTestAttemptRequest(attemptId);
            Integer score = finishTestAttemptUseCase.execute(useCaseRequest);

            // Calculate max score from test questions
            Optional<Test> test = testService.getTest(att.getTestId());
            int maxScore = 0;
            if (test.isPresent()) {
                List<Question> questions = testService.getQuestions(test.get().getId());
                maxScore = questions.stream()
                        .mapToInt(q -> q.getMaxPoints() != null ? q.getMaxPoints() : 0)
                        .sum();
            }

            if (maxScore == 0) {
                maxScore = 100; // fallback
            }

            double percentage = maxScore > 0 ? ((double) score / maxScore) * 100 : 0;
            boolean passed = percentage >= 60; // Configure pass threshold

            Map<String, Object> result = new HashMap<>();
            result.put("score", score);
            result.put("maxScore", maxScore);
            result.put("percentage", percentage);
            result.put("passed", passed);

            sendSuccess(response, result);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleGetAttemptProgress(HttpServletRequest request, HttpServletResponse response,
                                          Integer attemptId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestAttemptService attemptService = ctx.getAttemptService();
        TestService testService = ctx.getTestService();

        Optional<TestAttempt> attempt = attemptService.getAttempt(attemptId);
        if (!attempt.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Attempt not found");
            return;
        }

        if (!attempt.get().getUserId().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Not your attempt");
            return;
        }

        TestAttempt att = attempt.get();
        List<UserAnswer> answers = attemptService.getAttemptAnswers(attemptId);

        Set<Integer> answeredQuestions = answers.stream()
                .map(UserAnswer::getQuestionId)
                .collect(Collectors.toSet());

        Optional<Test> test = testService.getTest(att.getTestId());
        int totalQuestions = test.map(t -> testService.getQuestions(t.getId()).size()).orElse(0);

        Map<String, Object> result = new HashMap<>();
        result.put("id", att.getId());
        result.put("userId", att.getUserId());
        result.put("testId", att.getTestId());
        result.put("startTime", att.getStartTime());
        result.put("endTime", att.getEndTime());
        result.put("score", att.getScore());
        result.put("isFinished", att.getEndTime() != null);
        result.put("answeredQuestions", answeredQuestions);
        result.put("totalQuestions", totalQuestions);

        if (att.getEndTime() == null && att.getStartTime() != null && test.isPresent()) {
            Integer timeLimit = test.get().getTimeLimit();
            if (timeLimit != null) {
                long elapsed = Duration.between(att.getStartTime(), ZonedDateTime.now()).getSeconds();
                result.put("timeRemaining", Math.max(0, (timeLimit * 60L) - elapsed));
            }
        }

        sendSuccess(response, result);
    }

    private void handleGetUserAttempts(HttpServletRequest request, HttpServletResponse response,
                                       Integer requestedUserId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestAttemptService attemptService = ctx.getAttemptService();
        TestService testService = ctx.getTestService();

        int page = getIntParameter(request, "page", 1);
        int limit = getIntParameter(request, "limit", 20);
        String testIdParam = request.getParameter("testId");

        List<TestAttempt> allAttempts = attemptService.getUserAttempts(requestedUserId);

        // Filter by testId if provided
        if (testIdParam != null) {
            try {
                Integer testId = Integer.parseInt(testIdParam);
                allAttempts = allAttempts.stream()
                        .filter(a -> a.getTestId().equals(testId))
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                return;
            }
        }

        int total = allAttempts.size();
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);

        List<Map<String, Object>> attempts = allAttempts.subList(start, end).stream()
                .map(a -> convertAttemptToMap(a, testService))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("attempts", attempts);
        result.put("total", total);
        result.put("page", page);
        result.put("limit", limit);
        result.put("totalPages", (int) Math.ceil((double) total / limit));

        sendSuccess(response, result);
    }

    private void handleGetAttemptDetails(HttpServletRequest request, HttpServletResponse response,
                                         Integer attemptId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestAttemptService attemptService = ctx.getAttemptService();
        TestService testService = ctx.getTestService();

        Optional<TestAttempt> attempt = attemptService.getAttempt(attemptId);
        if (!attempt.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Attempt not found");
            return;
        }

        if (!attempt.get().getUserId().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Not authorized to view this attempt");
            return;
        }

        if (attempt.get().getEndTime() == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Attempt not finished yet");
            return;
        }

        TestAttempt att = attempt.get();
        List<UserAnswer> userAnswers = attemptService.getAttemptAnswers(attemptId);

        // Load all questions for the test
        List<Question> questions = testService.getQuestions(att.getTestId());
        Map<Integer, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // Load all answer options for each question
        Map<Integer, List<AnswerOption>> answerOptionsMap = new HashMap<>();
        for (Question question : questions) {
            List<AnswerOption> options = testService.getAnswerOptions(question.getId());
            answerOptionsMap.put(question.getId(), options);
        }

        // Build detailed answer information
        List<Map<String, Object>> detailedAnswers = userAnswers.stream()
                .map(ua -> convertAnswerToDetailedMap(ua, questionMap, answerOptionsMap))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("attempt", convertAttemptToMap(att, testService));
        result.put("answers", detailedAnswers);

        sendSuccess(response, result);
    }

    private Map<String, Object> convertAttemptToMap(TestAttempt attempt, TestService testService) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", attempt.getId());
        map.put("userId", attempt.getUserId());
        map.put("testId", attempt.getTestId());

        Optional<Test> test = testService.getTest(attempt.getTestId());
        map.put("testTitle", test.map(Test::getTitle).orElse("Unknown Test"));

        map.put("startTime", attempt.getStartTime());
        map.put("endTime", attempt.getEndTime());
        map.put("score", attempt.getScore());
        map.put("isFinished", attempt.getEndTime() != null);
        return map;
    }

    private Map<String, Object> convertAnswerToDetailedMap(UserAnswer answer,
                                                           Map<Integer, Question> questionMap,
                                                           Map<Integer, List<AnswerOption>> answerOptionsMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("questionId", answer.getQuestionId());

        // Get question details
        Question question = questionMap.get(answer.getQuestionId());
        if (question != null) {
            map.put("questionText", question.getText());
            map.put("maxScore", question.getMaxPoints());
        } else {
            map.put("questionText", "Unknown Question");
            map.put("maxScore", 0);
        }

        // Get user's answer
        map.put("userAnswerId", answer.getAnswerId());
        map.put("userAnswerText", answer.getAnswerText());

        // Get score earned and correct answer info
        Integer scoreEarned = 0;
        String correctAnswerText = null;

        if (answer.getAnswerId() != null) {
            List<AnswerOption> options = answerOptionsMap.get(answer.getQuestionId());
            if (options != null) {
                // Find the selected option
                Optional<AnswerOption> selectedOption = options.stream()
                        .filter(opt -> opt.getId().equals(answer.getAnswerId()))
                        .findFirst();

                if (selectedOption.isPresent()) {
                    scoreEarned = selectedOption.get().getScore();
                }

                // Find the correct answer (highest score)
                Optional<AnswerOption> correctOption = options.stream()
                        .max(Comparator.comparing(opt -> opt.getScore() != null ? opt.getScore() : 0));

                if (correctOption.isPresent()) {
                    correctAnswerText = correctOption.get().getOptionText();
                }
            }
        }

        map.put("scoreEarned", scoreEarned);
        map.put("correctAnswerText", correctAnswerText);
        map.put("isCorrect", scoreEarned != null && scoreEarned > 0);

        return map;
    }

    private Integer parseIdFromPath(String pathInfo, String suffix) {
        if (pathInfo == null) return null;
        String idPart = pathInfo.replace(suffix, "");
        return parseId(idPart);
    }

    // Request DTOs
    private static class StartAttemptRequestDto {
        public Integer testId;
    }

    private static class SubmitAnswerRequestDto {
        public Integer questionId;
        public Integer answerId;
        public String answerText;
    }
}
