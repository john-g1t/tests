package com.github.john_g1t.presentation.web.servlet;

import com.github.john_g1t.app.dto.TestDto;
import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.test.CreateTestRequest;
import com.github.john_g1t.domain.model.Test;
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

        List<TestDto> tests = allTests.subList(start, end).stream()
                .map(this::convertToDto)
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
            sendSuccess(response, convertToDto(test.get()));
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

        if (!existingTest.get().getCreatedBy().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Only test creator can update the test");
            return;
        }

        UpdateTestRequestDto updateRequest = readJson(request, UpdateTestRequestDto.class);
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

        Optional<Test> test = testService.getTest(testId);
        if (test.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
            return;
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("testId", testId);
        statistics.put("totalAttempts", 0);
        statistics.put("completedAttempts", 0);
        statistics.put("averageScore", 0.0);
        statistics.put("maxScore", 100);
        statistics.put("minScore", 0);
        statistics.put("passRate", 0.0);

        sendSuccess(response, statistics);
    }

    private TestDto convertToDto(Test test) {
        return new TestDto(
                test.getId(),
                test.getTitle(),
                test.getDescription(),
                test.getCreatedBy(),
                test.getTimeLimit(),
                test.getMaxAttempts(),
                test.isActive(),
                test.getStartTime(),
                test.getEndTime()
        );
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
