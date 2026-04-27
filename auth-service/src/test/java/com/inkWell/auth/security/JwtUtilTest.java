package com.inkWell.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = Base64.getEncoder().encodeToString("mySecretKeyWithAtLeast256BitsOfLengthForHS256Algorithm".getBytes());
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L);
    }

    @Test
    void shouldGenerateTokenAndExtractEmail() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, "READER");
        
        assertNotNull(token);
        String extractedEmail = jwtUtil.extractEmail(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldValidateToken() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, "READER");
        
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void shouldNotBeValidIfExpired() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // Already expired
        String token = jwtUtil.generateToken("test@example.com", "READER");
        
        assertFalse(jwtUtil.isTokenValid(token));
    }
}
