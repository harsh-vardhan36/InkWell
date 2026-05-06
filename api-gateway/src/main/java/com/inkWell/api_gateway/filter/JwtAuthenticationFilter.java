package com.inkWell.api_gateway.filter;

import com.inkWell.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Reactive GlobalFilter for Spring Cloud Gateway (WebFlux).
 * This filter is responsible for:
 * <ul>
 *     <li>Validating JWT tokens for protected routes.</li>
 *     <li>Allowing anonymous access to public routes (auth, public posts, etc.).</li>
 *     <li>Injecting an internal secret header (X-Internal-Secret) to verify gateway origin for downstream services.</li>
 * </ul>
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Shared secret key used to identify requests originating from this gateway.
     */
    @Value("${internal.secret:change-me-in-env}")
    private String internalSecret;

    /**
     * List of path prefixes that do not require authentication for any HTTP method.
     */
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/auth/login",
            "/auth/login",
            "/api/auth/register",
            "/auth/register",
            "/api/auth/verify",
            "/auth/verify",
            "/api/auth/resend-otp",
            "/auth/resend-otp",
            "/api/auth/refresh",
            "/auth/refresh",
            "/api/auth/forgot-password",
            "/auth/forgot-password",
            "/api/auth/reset-password",
            "/auth/reset-password",
            "/api/auth/become-author",
            "/auth/become-author",
            "/login",
            "/error",
            "/oauth2/",
            "/actuator/",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/v3/api-docs/",
            "/webjars/");

    /**
     * List of path prefixes that do not require authentication for GET requests.
     */
    private static final List<String> PUBLIC_GET_PREFIXES = List.of(
            "/api/posts",
            "/posts",
            "/api/comments",
            "/comments",
            "/api/categories",
            "/categories");

    /**
     * Main filtering logic for incoming requests.
     * 
     * @param exchange The current server web exchange.
     * @param chain The gateway filter chain.
     * @return A {@link Mono<Void>} representing the completion of the request.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Inject X-Internal-Secret into every forwarded request
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Internal-Secret", internalSecret)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // Skip JWT validation for public paths
        boolean isPublic = PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
        
        // Allow GET requests for posts and comments to be public
        if (!isPublic && request.getMethod() == HttpMethod.GET) {
            isPublic = PUBLIC_GET_PREFIXES.stream().anyMatch(path::startsWith);
        }

        if (isPublic) {
            return chain.filter(mutatedExchange);
        }

        // Validate JWT for all protected paths
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            jwtUtil.validateToken(token);
            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}