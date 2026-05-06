package com.inkWell.api_gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
    }

    @Test
    void validateToken_ValidToken_Success() {
        SecretKey key = Keys.hmacShaKeyFor(decodeHex(secret));
        String token = Jwts.builder()
                .subject("testUser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(key)
                .compact();

        assertDoesNotThrow(() -> jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken_ThrowsException() {
        assertThrows(Exception.class, () -> jwtUtil.validateToken("invalidToken"));
    }

    @Test
    void testSigningKeyFallback_Base64() {
        String base64Secret = "YmFzZTY0c2VjcmV0dmFsdWV0aGF0aXNsb25nZW5vdWdoZm9yc2hhMjU2";
        ReflectionTestUtils.setField(jwtUtil, "secret", base64Secret);
        
        // This will test the fallback logic
        assertNotNull(ReflectionTestUtils.invokeMethod(jwtUtil, "getSigningKey"));
    }

    @Test
    void testSigningKeyFallback_Raw() {
        String rawSecret = "too-short-secret-but-we-use-raw-bytes-fallback-logic-here";
        ReflectionTestUtils.setField(jwtUtil, "secret", rawSecret);
        
        assertNotNull(ReflectionTestUtils.invokeMethod(jwtUtil, "getSigningKey"));
    }

    private byte[] decodeHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
