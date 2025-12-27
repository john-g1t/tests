package com.github.john_g1t.presentation.web.servlet;

import com.github.john_g1t.domain.model.AnswerOption;
import com.github.john_g1t.domain.service.test.TestService;
import com.github.john_g1t.infrastructure.ApplicationContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@WebServlet(name = "AnswerOptionServlet",
        urlPatterns = {"/questions/*/options", "/options/*"})
public class AnswerOptionServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCorsHeaders(response);

        String uri = request.getRequestURI();

        try {
            if (uri.contains("/questions/") && uri.endsWith("/options")) {
                Integer questionId = extractQuestionIdFromUri(uri);
                handleGetOptions(request, response, questionId);
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
            if (uri.contains("/questions/") && uri.endsWith("/options")) {
                Integer questionId = extractQuestionIdFromUri(uri);
                handleAddOption(request, response, questionId, userId);
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

        Integer optionId = parseId(request.getPathInfo());
        if (optionId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid option ID");
            return;
        }

        try {
            handleUpdateOption(request, response, optionId, userId);
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

        Integer optionId = parseId(request.getPathInfo());
        if (optionId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid option ID");
            return;
        }

        try {
            handleDeleteOption(request, response, optionId, userId);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    private void handleGetOptions(HttpServletRequest request, HttpServletResponse response,
                                  Integer questionId) throws IOException {
        ApplicationContext ctx = getAppContext();
        TestService testService = ctx.getTestService();

        List<AnswerOption> options = testService.getAnswerOptions(questionId);

        Map<String, Object> result = new HashMap<>();
        result.put("options", options.stream()
                .map(this::convertOptionToMap)
                .collect(java.util.stream.Collectors.toList()));

        sendSuccess(response, result);
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

    private void handleUpdateOption(HttpServletRequest request, HttpServletResponse response,
                                    Integer optionId, Integer userId) throws IOException {
        sendSuccess(response, null);
    }

    private void handleDeleteOption(HttpServletRequest request, HttpServletResponse response,
                                    Integer optionId, Integer userId) throws IOException {
        sendSuccess(response, null);
    }

    private Map<String, Object> convertOptionToMap(AnswerOption option) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", option.getId());
        map.put("questionId", option.getQuestionId());
        map.put("optionText", option.getOptionText());
        map.put("score", option.getScore());
        return map;
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

    private static class AddOptionRequestDto {
        public String optionText;
        public Integer score;
    }
}
