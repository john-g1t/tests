package com.github.john_g1t.presentation.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthFilter implements Filter {
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/users/register",
            "/users/login",
            "/tests" // GET only
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(
                httpRequest.getContextPath().length());
        String method = httpRequest.getMethod();

        if ("OPTIONS".equals(method)) {
            chain.doFilter(request, response);
            return;
        }

        boolean isPublic = PUBLIC_PATHS.stream()
                .anyMatch(publicPath -> path.startsWith("/api" + publicPath));

        if (isPublic && "GET".equals(method) && path.equals("/api/tests")) {
            chain.doFilter(request, response);
            return;
        }

        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        // Check authentication
        Object userId = httpRequest.getSession(false) != null ?
                httpRequest.getSession(false).getAttribute("userId") : null;

        if (userId == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                    "{\"success\":false,\"error\":\"Authentication required\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
