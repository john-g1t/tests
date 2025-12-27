package com.github.john_g1t.presentation.web.servlet;

import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.test.CreateTestRequest;
import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.model.TestAttempt;
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

@WebServlet(name = "TestServlet", urlPatterns = {"/tests/*", "/questions/*"})
public class TestServlet extends BaseServlet {

    // ==========================================
    // HTTP METHOD ENTRY POINTS
    // ==========================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCorsHeaders(response);
        String uri = request.getRequestURI();
        String pathInfo = request.getPathInfo();

        try {
            // Route: /questions/{id}/options
            if (uri.contains("/questions/") && uri.endsWith("/options")) {
                Integer questionId = extractIdFromUriSegment(uri, "questions");
                if (questionId != null) {
                    handleGetOptions(request, response, questionId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid question ID");
                }
                return;
            }

            // Route: /tests/{id}/questions
            if (uri.contains("/tests/") && uri.endsWith("/questions")) {
                Integer testId = extractTestIdFromUri(uri);
                if (testId != null) {
                    handleGetQuestions(request, response, testId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                }
                return;
            }

            // Route: /tests/{id}/statistics
            if (uri.endsWith("/statistics")) {
                Integer testId = parseIdFromPath(pathInfo, "/statistics");
                if (testId != null) {
                    handleGetTestStatistics(request, response, testId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                }
                return;
            }

            // Route: /tests or /tests/ (Get All)
            if (uri.endsWith("/tests") || uri.endsWith("/tests/")) {
                handleGetAllTests(request, response);
                return;
            }

            // Route: /tests/{id} (Get One)
            // We ensure we aren't mistakenly catching a /questions/{id} route here by checking the start
            if (uri.contains("/tests/")) {
                Integer testId = parseId(pathInfo);
                if (testId != null) {
                    handleGetTestById(request, response, testId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                }
                return;
            }

            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");

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

        String uri = request.getRequestURI();

        try {
            // Route: /questions/{id}/options
            if (uri.contains("/questions/") && uri.endsWith("/options")) {
                Integer questionId = extractQuestionIdFromUri(uri);
                handleAddOption(request, response, questionId, userId);
            }

            // Route: /tests/{id}/questions (Add Question)
            if (uri.contains("/tests/") && uri.endsWith("/questions")) {
                Integer testId = extractTestIdFromUri(uri);
                if (testId != null) {
                    handleAddQuestion(request, response, testId, userId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                }
            }
            // Route: /tests (Create Test)
            else if (uri.endsWith("/tests") || uri.endsWith("/tests/")) {
                handleCreateTest(request, response, userId);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    private Integer extractQuestionIdFromUri(String uri) {
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("questions".equals(parts[i])) {
                try {
                    return Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private void handleAddOption(HttpServletRequest request, HttpServletResponse response,
                                 Integer questionId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

       AddOptionRequestDto optionRequest = readJson(request, AddOptionRequestDto.class);

        if (optionRequest.optionText == null || optionRequest.optionText.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Option text is required");
            return;
        }

        try {
            System.out.println(questionId);
            Integer optionId = testService.addAnswerOption(
                    questionId,
                    sanitizeInput(optionRequest.optionText),
                    optionRequest.score
            );

            Map<String, Object> result = new HashMap<>();
            result.put("optionId", optionId);
            sendSuccess(response, result);
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private static class AddOptionRequestDto {
        public String optionText;
        public Integer score;
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

        String uri = request.getRequestURI();

        try {
            // Route: /questions/{id} (Update Question)
            if (uri.contains("/questions/")) {
                Integer questionId = parseId(request.getPathInfo());
                if (questionId == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid question ID");
                    return;
                }
                handleUpdateQuestion(request, response, questionId, userId);
            }
            // Route: /tests/{id} (Update Test)
            else if (uri.contains("/tests/")) {
                Integer testId = parseId(request.getPathInfo());
                if (testId == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                    return;
                }
                handleUpdateTest(request, response, testId, userId);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
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

        String uri = request.getRequestURI();

        try {
            // Route: /questions/{id} (Delete Question)
            if (uri.contains("/questions/")) {
                Integer questionId = parseId(request.getPathInfo());
                if (questionId == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid question ID");
                    return;
                }
                handleDeleteQuestion(request, response, questionId, userId);
            }
            // Route: /tests/{id} (Deactivate Test)
            else if (uri.contains("/tests/")) {
                Integer testId = parseId(request.getPathInfo());
                if (testId == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                    return;
                }
                handleDeactivateTest(request, response, testId, userId);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST HANDLERS
    // ==========================================

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

        List<Map<String, Object>> tests = new ArrayList<>();
        if (start < total) {
            tests = allTests.subList(start, end).stream()
                    .map(this::convertTestToMap)
                    .collect(Collectors.toList());
        }

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
            sendSuccess(response, convertTestToMap(test.get()));
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
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

    // ==========================================
    // QUESTION HANDLERS
    // ==========================================

    private void handleGetQuestions(HttpServletRequest request, HttpServletResponse response,
                                    Integer testId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

        Optional<Test> test = testService.getTest(testId);
        if (test.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
            return;
        }

        List<Question> questions = testService.getQuestions(testId);

        Map<String, Object> result = new HashMap<>();
        result.put("questions", questions.stream()
                .map(this::convertQuestionToMap)
                .collect(Collectors.toList()));

        sendSuccess(response, result);
    }

    private void handleAddQuestion(HttpServletRequest request, HttpServletResponse response,
                                   Integer testId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

        Optional<Test> test = testService.getTest(testId);
        if (!test.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
            return;
        }

        if (!test.get().getCreatedBy().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Only test creator can add questions");
            return;
        }

        AddQuestionRequestDto questionRequest = readJson(request, AddQuestionRequestDto.class);

        if (questionRequest.text == null || questionRequest.text.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Question text is required");
            return;
        }

        try {
            Integer questionId = testService.addQuestion(
                    testId,
                    sanitizeInput(questionRequest.text),
                    questionRequest.answerType,
                    questionRequest.maxPoints
            );

            Map<String, Object> result = new HashMap<>();
            result.put("questionId", questionId);
            sendSuccess(response, result);
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleUpdateQuestion(HttpServletRequest request, HttpServletResponse response,
                                      Integer questionId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

        Optional<Question> questionOpt = testService.getQuestionById(questionId);
        if (!questionOpt.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Question not found");
            return;
        }

        Question question = questionOpt.get();
        Optional<Test> test = testService.getTest(question.getTestId());

        if (!test.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
            return;
        }

        if (!test.get().getCreatedBy().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Only test creator can update questions");
            return;
        }

        UpdateQuestionRequestDto updateRequest = readJson(request, UpdateQuestionRequestDto.class);

        if (updateRequest.text != null) {
            question.setText(sanitizeInput(updateRequest.text));
        }
        if (updateRequest.answerType != null) {
            question.setAnswerType(updateRequest.answerType);
        }
        if (updateRequest.maxPoints != null) {
            question.setMaxPoints(updateRequest.maxPoints);
        }

        testService.saveQuestion(question);
        sendSuccess(response, null);
    }

    private void handleDeleteQuestion(HttpServletRequest request, HttpServletResponse response,
                                      Integer questionId, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

        Optional<Question> questionOpt = testService.getQuestionById(questionId);
        if (!questionOpt.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Question not found");
            return;
        }

        Question question = questionOpt.get();
        Optional<Test> test = testService.getTest(question.getTestId());

        if (!test.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Test not found");
            return;
        }

        if (!test.get().getCreatedBy().equals(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Only test creator can delete questions");
            return;
        }

        testService.deleteQuestionById(questionId);
        sendSuccess(response, null);
    }

    private void handleGetOptions(HttpServletRequest request, HttpServletResponse response,
                                  Integer questionId) throws IOException {
        ApplicationContext ctx = getAppContext();
        // Assuming TestService handles options or you have an OptionService
        TestService testService = ctx.getTestService();

        // Check if question exists
        Optional<Question> question = testService.getQuestionById(questionId);
        if (question.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Question not found");
            return;
        }

        // Fetch options (Update your service/domain model if getOptions doesn't exist yet)
        List<com.github.john_g1t.domain.model.AnswerOption> options = testService.getOptions(questionId);

        Map<String, Object> result = new HashMap<>();
        result.put("options", options.stream()
                .map(opt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", opt.getId());
                    map.put("questionId", opt.getQuestionId());
                    map.put("optionText", opt.getOptionText());
                    // We typically don't send 'isCorrect' to the client during a test
                    return map;
                })
                .collect(Collectors.toList()));

        sendSuccess(response, result);
    }

    // Add this helper for cleaner URI parsing
    private Integer extractIdFromUriSegment(String uri, String segment) {
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if (segment.equals(parts[i])) {
                try {
                    return Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    // ==========================================
    // UTILITIES
    // ==========================================

    private Map<String, Object> convertTestToMap(Test test) {
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

    private Map<String, Object> convertQuestionToMap(Question question) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", question.getId());
        map.put("testId", question.getTestId());
        map.put("text", question.getText());
        map.put("answerType", question.getAnswerType());
        map.put("maxPoints", question.getMaxPoints());
        return map;
    }

    private Integer parseIdFromPath(String pathInfo, String suffix) {
        if (pathInfo == null) return null;
        String idPart = pathInfo.replace(suffix, "");
        return parseId(idPart);
    }

    private Integer extractTestIdFromUri(String uri) {
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("tests".equals(parts[i])) {
                try {
                    return Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    // ==========================================
    // DTOs
    // ==========================================

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

    private static class AddQuestionRequestDto {
        public String text;
        public String answerType;
        public Integer maxPoints;
    }

    private static class UpdateQuestionRequestDto {
        public String text;
        public String answerType;
        public Integer maxPoints;
    }
}
