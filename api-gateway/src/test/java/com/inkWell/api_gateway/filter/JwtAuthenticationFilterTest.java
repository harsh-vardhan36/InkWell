package com.inkWell.api_gateway.filter;

import com.inkWell.api_gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        lenient().when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void filter_PublicPath_Success() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth/login")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        verify(filterChain).filter(any(ServerWebExchange.class));
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void filter_ProtectedPath_NoHeader_ReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/posts")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_ProtectedPath_ValidToken_Success() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/posts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        doNothing().when(jwtUtil).validateToken("valid-token");

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_ProtectedPath_InvalidToken_ReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/posts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        doThrow(new RuntimeException("Invalid token")).when(jwtUtil).validateToken("invalid-token");

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_PublicGetPath_Success() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, "/posts")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        verify(filterChain).filter(any(ServerWebExchange.class));
    }
}
