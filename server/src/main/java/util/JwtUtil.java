package util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final SecretKey KEY =
            Keys.hmacShaKeyFor("kino-system-secret-key-min-256-bits!!".getBytes());
    private static final long EXPIRY_MS = 8 * 60 * 60 * 1000L;

    public static String generate(int userId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
                .signWith(KEY)
                .compact();
    }

    public static Claims verify(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static int getUserId(String token) {
        return Integer.parseInt(verify(token).getSubject());
    }

    public static String getRole(String token) {
        return verify(token).get("role", String.class);
    }
}