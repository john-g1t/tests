package com.github.john_g1t.presentation.web.servlet;

import com.github.john_g1t.domain.service.test.TestService;
import com.github.john_g1t.domain.service.user.UserService;
import com.github.john_g1t.domain.service.attempt.TestAttemptService;
import com.github.john_g1t.infrastructure.ApplicationContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@WebServlet(name = "StatisticsServlet", urlPatterns = {"/statistics/*"})
public class StatisticsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCorsHeaders(response);
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/global")) {
                handleGetGlobalStatistics(request, response);
            } else if (pathInfo.startsWith("/user/")) {
                Integer userId = parseIdFromPath(pathInfo, "/user/");
                handleGetUserStatistics(request, response, userId);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    private void handleGetGlobalStatistics(HttpServletRequest request,
                                           HttpServletResponse response) throws IOException {
        ApplicationContext ctx = getAppContext();
        UserService userService = ctx.getUserService();
        TestService testService = ctx.getTestService();

        int totalUsers = userService.getAllUsers().size();
        int totalTests = testService.getAllTests().size();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("totalTests", totalTests);

        sendSuccess(response, statistics);
    }

    private void handleGetUserStatistics(HttpServletRequest request,
                                         HttpServletResponse response, Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        UserService userService = ctx.getUserService();
        TestAttemptService attemptService = ctx.getAttemptService();

        if (!userService.existsById(userId)) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        var attempts = attemptService.getUserAttempts(userId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("userId", userId);
        statistics.put("totalAttempts", attempts.size());

        sendSuccess(response, statistics);
    }

    private Integer parseIdFromPath(String pathInfo, String prefix) {
        if (pathInfo == null || !pathInfo.startsWith(prefix)) {
            return null;
        }
        String idStr = pathInfo.substring(prefix.length());
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
