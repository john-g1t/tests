package com.github.john_g1t.presentation.web.servlet;

import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.service.test.TestService;
import com.github.john_g1t.domain.repository.QuestionRepository;
import com.github.john_g1t.infrastructure.ApplicationContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@WebServlet(name = "QuestionServlet", urlPatterns = {"/tests/*/questions", "/questions/*"})
public class QuestionServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        System.out.println("request");
        setCorsHeaders(response);

        String pathInfo = request.getPathInfo();
        String uri = request.getRequestURI();
        System.out.println("aboba");
        try {
            if (uri.contains("/tests/") && uri.endsWith("/questions")) {
                System.out.println("extracting");
                Integer testId = extractTestIdFromUri(uri);
                System.out.println(testId);
                if (testId != null) {
                    handleGetQuestions(request, response, testId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                }
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
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

        String uri = request.getRequestURI();

        try {
            if (uri.contains("/tests/") && uri.endsWith("/questions")) {
                Integer testId = extractTestIdFromUri(uri);
                if (testId != null) {
                    handleAddQuestion(request, response, testId, userId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid test ID");
                }
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
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

        Integer questionId = parseId(request.getPathInfo());
        if (questionId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid question ID");
            return;
        }

        try {
            handleUpdateQuestion(request, response, questionId, userId);
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

        Integer questionId = parseId(request.getPathInfo());
        if (questionId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid question ID");
            return;
        }

        try {
            handleDeleteQuestion(request, response, questionId, userId);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    private void handleGetQuestions(HttpServletRequest request, HttpServletResponse response,
                                    Integer testId) throws IOException {
        System.out.println("get questions");
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
                .collect(java.util.stream.Collectors.toList()));

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

    private Map<String, Object> convertQuestionToMap(Question question) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", question.getId());
        map.put("testId", question.getTestId());
        map.put("text", question.getText());
        map.put("answerType", question.getAnswerType());
        map.put("maxPoints", question.getMaxPoints());
        return map;
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
