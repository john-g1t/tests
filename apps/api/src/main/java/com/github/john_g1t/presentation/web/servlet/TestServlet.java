package com.github.john_g1t.presentation.web.servlet;

import com.github.john_g1t.app.dto.TestDto;
import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.test.CreateTestRequest;
import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.model.TestAttempt;
import com.github.john_g1t.domain.repository.TestAttemptRepository;
import com.github.john_g1t.domain.repository.TestRepository;
import com.github.john_g1t.domain.service.attempt.TestAttemptService;
import com.github.john_g1t.domain.service.test.TestService;
import com.github.john_g1t.infrastructure.ApplicationContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "TestServlet", urlPatterns = {"/tests/*"})
public class TestServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCorsHeaders(response);
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllTests(request, response);
            } else if (pathInfo.endsWith("/statistics")) {
                Integer testId = parseIdFromPath(pathInfo, "/statistics");
                if (testId != null) {
                    handleGetTestStatistics(request, response, testId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                }
            } else {
                Integer testId = parseId(pathInfo);
                if (testId != null) {
                    handleGetTestById(request, response, testId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                }
            }
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

        try {
            handleCreateTest(request, response, userId);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCorsHeaders(response);

        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        Integer testId = parseId(request.getPathInfo());
        if (testId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
            return;
        }

        try {
            handleUpdateTest(request, response, testId, userId);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCorsHeaders(response);

        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        Integer testId = parseId(request.getPathInfo());
        if (testId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
            return;
        }

        try {
            handleDeactivateTest(request, response, testId, userId);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    private void handleCreateTest(HttpServletRequest request, HttpServletResponse response,
                                  Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        UseCase<CreateTestRequest, Integer> createTestUseCase = ctx.getCreateTestUseCase();

        CreateTestRequestDto testRequest = readJson(request, CreateTestRequestDto.class);

        if (testRequest.title == null || testRequest.title.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Title is required");
            return;
        }

        try {
            CreateTestRequest createRequest = new CreateTestRequest(
                    userId,
                    sanitizeInput(testRequest.title),
                    sanitizeInput(testRequest.description),
                    testRequest.timeLimit,
                    testRequest.maxAttempts,
                    testRequest.startTime,
                    testRequest.endTime
            );

            Integer testId = createTestUseCase.execute(createRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("testId", testId);
            sendSuccess(response, result);
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleGetAllTests(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

        int page = getIntParameter(request, "page", 1);
        int limit = getIntParameter(request, "limit", 20);
        String activeParam = request.getParameter("active");
        String creatorIdParam = request.getParameter("creatorId");
        String search = request.getParameter("search");

        List<Test> allTests;

        if ("true".equalsIgnoreCase(activeParam)) {
            allTests = testService.getActiveTests();
        } else if (creatorIdParam != null) {
            try {
                Integer creatorId = Integer.parseInt(creatorIdParam);
                allTests = testService.getTestsByCreator(creatorId);
            } catch (NumberFormatException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid creator ID");
                return;
            }
        } else {
            allTests = testService.getAllTests();
        }

        // Filter by search if provided
        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            allTests = allTests.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(searchLower) ||
                            (t.getDescription() != null &&
                                    t.getDescription().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        int total = allTests.size();
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);

        List<Map<String, Object>> tests = allTests.subList(start, end).stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("tests", tests);
        result.put("total", total);
        result.put("page", page);
        result.put("limit", limit);
        result.put("totalPages", (int) Math.ceil((double) total / limit));

        sendSuccess(response, result);
    }

    private void handleGetTestById(HttpServletRequest request, HttpServletResponse response,
                                   Integer testId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

        Optional<Test> test = testService.getTest(testId);
        if (test.isPresent()) {
            sendSuccess(response, convertToMap(test.get()));
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
        }
    }

    private void handleUpdateTest(HttpServletRequest request, HttpServletResponse response,
                                  Integer testId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

        Optional<Test> existingTest = testService.getTest(testId);
        if (!existingTest.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
            return;
        }

        Test test = existingTest.get();
        if (!test.getCreatedBy().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Only test creator can update the test");
            return;
        }

        UpdateTestRequestDto updateRequest = readJson(request, UpdateTestRequestDto.class);

        if (updateRequest.title != null) {
            test.setTitle(sanitizeInput(updateRequest.title));
        }
        if (updateRequest.description != null) {
            test.setDescription(sanitizeInput(updateRequest.description));
        }
        if (updateRequest.timeLimit != null) {
            test.setTimeLimit(updateRequest.timeLimit);
        }
        if (updateRequest.maxAttempts != null) {
            test.setMaxAttempts(updateRequest.maxAttempts);
        }
        if (updateRequest.startTime != null) {
            test.setStartTime(updateRequest.startTime);
        }
        if (updateRequest.endTime != null) {
            test.setEndTime(updateRequest.endTime);
        }

        testService.saveTest(test);
        sendSuccess(response, null);
    }

    private void handleDeactivateTest(HttpServletRequest request, HttpServletResponse response,
                                      Integer testId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

        Optional<Test> existingTest = testService.getTest(testId);
        if (!existingTest.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
            return;
        }

        if (!existingTest.get().getCreatedBy().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Only test creator can deactivate the test");
            return;
        }

        testService.deactivateTest(testId);
        sendSuccess(response, null);
    }

    private void handleGetTestStatistics(HttpServletRequest request, HttpServletResponse response,
                                         Integer testId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();
        TestAttemptService attemptService = ctx.getAttemptService();

        Optional<Test> test = testService.getTest(testId);
        if (test.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
            return;
        }

        // Get all attempts for this test
        List<TestAttempt> attempts = attemptService.getByTestId(testId);

        int totalAttempts = attempts.size();
        int completedAttempts = 0;
        double totalScore = 0;
        int scoredAttempts = 0;
        Integer maxScoreAchieved = null;
        Integer minScoreAchieved = null;
        int passedCount = 0;

        // Calculate max possible score from questions
        List<Question> questions = testService.getQuestions(testId);
        int maxPossibleScore = questions.stream()
                .mapToInt(q -> q.getMaxPoints() != null ? q.getMaxPoints() : 0)
                .sum();

        if (maxPossibleScore == 0) {
            maxPossibleScore = 100; // fallback
        }

        final int passThreshold = (int) (maxPossibleScore * 0.6); // 60% to pass

        for (TestAttempt attempt : attempts) {
            if (attempt.getEndTime() != null) {
                completedAttempts++;

                if (attempt.getScore() != null) {
                    int score = attempt.getScore();
                    totalScore += score;
                    scoredAttempts++;

                    if (maxScoreAchieved == null || score > maxScoreAchieved) {
                        maxScoreAchieved = score;
                    }
                    if (minScoreAchieved == null || score < minScoreAchieved) {
                        minScoreAchieved = score;
                    }

                    if (score >= passThreshold) {
                        passedCount++;
                    }
                }
            }
        }

        double averageScore = scoredAttempts > 0 ? totalScore / scoredAttempts : 0.0;
        double passRate = completedAttempts > 0 ? (passedCount * 100.0 / completedAttempts) : 0.0;

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("testId", testId);
        statistics.put("totalAttempts", totalAttempts);
        statistics.put("completedAttempts", completedAttempts);
        statistics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        statistics.put("maxScore", maxScoreAchieved != null ? maxScoreAchieved : 0);
        statistics.put("minScore", minScoreAchieved != null ? minScoreAchieved : 0);
        statistics.put("passRate", Math.round(passRate * 100.0) / 100.0);

        sendSuccess(response, statistics);
    }

    private Map<String, Object> convertToMap(Test test) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", test.getId());
        map.put("creatorId", test.getCreatedBy());
        map.put("title", test.getTitle());
        map.put("description", test.getDescription());
        map.put("timeLimit", test.getTimeLimit());
        map.put("maxAttempts", test.getMaxAttempts());
        map.put("isActive", test.isActive());
        map.put("startTime", test.getStartTime());
        map.put("endTime", test.getEndTime());
        return map;
    }

    private Integer parseIdFromPath(String pathInfo, String suffix) {
        if (pathInfo == null) return null;
        String idPart = pathInfo.replace(suffix, "");
        return parseId(idPart);
    }

    // Request DTOs
    private static class CreateTestRequestDto {
        public String title;
        public String description;
        public Integer timeLimit;
        public Integer maxAttempts;
        public ZonedDateTime startTime;
        public ZonedDateTime endTime;
    }

    private static class UpdateTestRequestDto {
        public String title;
        public String description;
        public Integer timeLimit;
        public Integer maxAttempts;
        public ZonedDateTime startTime;
        public ZonedDateTime endTime;
    }
}
