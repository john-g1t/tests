package com.github.john_g1t.presentation.web.servlet;

import com.github.john_g1t.app.dto.UserDto;
import com.github.john_g1t.app.usecase.UseCase;
import com.github.john_g1t.app.usecase.user.CreateUserRequest;
import com.github.john_g1t.domain.model.User;
import com.github.john_g1t.domain.service.user.UserService;
import com.github.john_g1t.infrastructure.ApplicationContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "UserServlet", urlPatterns = {"/users/*"})
public class UserServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setCorsHeaders(response);
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllUsers(request, response);
            } else if (pathInfo.equals("/me")) {
                handleGetCurrentUser(request, response);
            } else {
                Integer userId = parseId(pathInfo);
                if (userId != null) {
                    handleGetUserById(request, response, userId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
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
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/register")) {
                handleRegister(request, response);
            } else if (pathInfo != null && pathInfo.equals("/login")) {
                handleLogin(request, response);
            } else if (pathInfo != null && pathInfo.equals("/logout")) {
                handleLogout(request, response);
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
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/password")) {
                handleChangePassword(request, response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ApplicationContext ctx = getAppContext();
        UseCase<CreateUserRequest, Integer> createUserUseCase = ctx.getCreateUserUseCase();

        RegisterRequest registerRequest = readJson(request, RegisterRequest.class);

        if (registerRequest.email == null || registerRequest.email.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email is required");
            return;
        }

        if (registerRequest.password == null || registerRequest.password.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Password is required");
            return;
        }

        if (!isValidEmail(registerRequest.email)) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid email format");
            return;
        }

        try {
            CreateUserRequest createRequest = new CreateUserRequest(
                    sanitizeInput(registerRequest.email),
                    registerRequest.password,
                    sanitizeInput(registerRequest.firstName),
                    sanitizeInput(registerRequest.lastName)
            );

            Integer userId = createUserUseCase.execute(createRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            sendSuccess(response, result);
        } catch (IllegalStateException e) {
            // Email already exists
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ApplicationContext ctx = getAppContext();
        UserService userService = ctx.getUserService();

        LoginRequest loginRequest = readJson(request, LoginRequest.class);

        if (loginRequest.email == null || loginRequest.password == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Email and password are required");
            return;
        }

        Optional<User> user = userService.authenticateUser(
                sanitizeInput(loginRequest.email),
                loginRequest.password
        );

        if (user.isPresent()) {
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.get().getId());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            sendSuccess(response, convertToFullDto(user.get()));
        } else {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid email or password");
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        sendSuccess(response, null);
    }

    private void handleGetCurrentUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ApplicationContext ctx = getAppContext();
        UserService userService = ctx.getUserService();

        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            return;
        }

        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            sendSuccess(response, convertToFullDto(user.get()));
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }

    private void handleGetAllUsers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ApplicationContext ctx = getAppContext();
        UserService userService = ctx.getUserService();

        int page = getIntParameter(request, "page", 1);
        int limit = getIntParameter(request, "limit", 20);
        String search = request.getParameter("search");

        List<User> allUsers = userService.getAllUsers();

        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            allUsers = allUsers.stream()
                    .filter(u -> u.getEmail().toLowerCase().contains(searchLower) ||
                            (u.getFirstName() != null &&
                                    u.getFirstName().toLowerCase().contains(searchLower)) ||
                            (u.getLastName() != null &&
                                    u.getLastName().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        int total = allUsers.size();
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);

        List<Map<String, Object>> users = allUsers.subList(start, end).stream()
                .map(this::convertToFullDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("total", total);
        result.put("page", page);
        result.put("limit", limit);
        result.put("totalPages", (int) Math.ceil((double) total / limit));

        sendSuccess(response, result);
    }

    private void handleGetUserById(HttpServletRequest request, HttpServletResponse response,
                                   Integer userId) throws IOException {
        ApplicationContext ctx = getAppContext();
        UserService userService = ctx.getUserService();

        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            sendSuccess(response, convertToFullDto(user.get()));
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ApplicationContext ctx = getAppContext();
        UserService userService = ctx.getUserService();

        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            return;
        }

        ChangePasswordRequest changeRequest = readJson(request, ChangePasswordRequest.class);

        if (changeRequest.oldPassword == null || changeRequest.newPassword == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Old and new passwords are required");
            return;
        }

        try {
            userService.changePassword(userId, changeRequest.oldPassword,
                    changeRequest.newPassword);
            sendSuccess(response, null);
        } catch (IllegalStateException | IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private Map<String, Object> convertToFullDto(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("email", user.getEmail());
        dto.put("firstName", user.getFirstName());
        dto.put("lastName", user.getLastName());
        return dto;
    }

    private boolean isValidEmail(String email) {
        return true;
//        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static class RegisterRequest {
        public String email;
        public String password;
        public String firstName;
        public String lastName;
    }

    private static class LoginRequest {
        public String email;
        public String password;
    }

    private static class ChangePasswordRequest {
        public String oldPassword;
        public String newPassword;
    }
}