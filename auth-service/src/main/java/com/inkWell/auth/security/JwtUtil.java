package com.inkWell.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;
    
    @Value("${jwt.refreshExpiration:604800000}") // default 7 days
    private long refreshExpiration;

    public String generateToken(String email, String role) {
        return buildToken(email, role, expiration);
    }
    
    public String generateRefreshToken(String email) {
        return buildToken(email, null, refreshExpiration);
    }
    
    private String buildToken(String email, String role, long expirationTime) {
        var jwtBuilder = Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey());
                
        if (role != null) {
            jwtBuilder.claim("role", role);
        }
        
        return jwtBuilder.compact();
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
    
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getRemainingExpiration(String token) {
        Date expirationDate = extractExpiration(token);
        return Math.max(0, expirationDate.getTime() - System.currentTimeMillis());
    }
}

