package com.inkWell.auth.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Rejects any request that does NOT carry the X-Internal-Secret header.
 * The API Gateway adds this header to every forwarded request.
 * Direct calls to port 8081 won't have this header → 403 Forbidden.
 */
public class GatewayOnlyFilter implements Filter {

    private final String expectedSecret;

    public GatewayOnlyFilter(String expectedSecret) {
        this.expectedSecret = expectedSecret;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String secret = request.getHeader("X-Internal-Secret");

        if (expectedSecret == null || !expectedSecret.equals(secret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied\"}");
            return;
        }

        chain.doFilter(req, res);
    }
}