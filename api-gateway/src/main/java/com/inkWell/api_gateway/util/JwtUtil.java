package com.inkWell.api_gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public void validateToken(final String token) {
        Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }

    private SecretKey getSigningKey() {
        try {
            // Try decoding as Hex manually
            byte[] keyBytes = decodeHex(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            try {
                // Fallback to Base64
                byte[] keyBytes = Decoders.BASE64.decode(secret);
                return Keys.hmacShaKeyFor(keyBytes);
            } catch (Exception e2) {
                // Final fallback: use raw bytes of the string
                return Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        }
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
