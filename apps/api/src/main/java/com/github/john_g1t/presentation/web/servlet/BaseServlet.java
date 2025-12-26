package com.github.john_g1t.presentation.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.john_g1t.infrastructure.ApplicationContext;
import com.github.john_g1t.presentation.web.listener.AppContextListener;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {
    protected static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    protected ApplicationContext getAppContext() {
        return AppContextListener.getApplicationContext(getServletContext());
    }

    protected void sendJson(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", status >= 200 && status < 300);
        result.put("data", data);

        objectMapper.writeValue(response.getWriter(), result);
    }

    protected void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        sendJson(response, HttpServletResponse.SC_OK, data);
    }

    protected void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", sanitizeInput(message));

        objectMapper.writeValue(response.getWriter(), result);
    }

    protected <T> T readJson(HttpServletRequest request, Class<T> clazz) throws IOException {
        return objectMapper.readValue(request.getReader(), clazz);
    }

    protected Integer getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getSession(false) != null ?
                request.getSession(false).getAttribute("userId") : null;
        return userId != null ? (Integer) userId : null;
    }

    protected boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUserId(request) != null;
    }

    protected String sanitizeInput(String input) {
        if (input == null) return null;
        return input.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    protected Integer parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.isEmpty()) {
            return null;
        }
        String[] parts = pathInfo.split("/");
        if (parts.length > 1) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    protected void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    protected int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
