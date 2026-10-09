package za.ac.cput.marketplace.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;

    private final long expirationMs = 1000 * 60 * 60 * 10; // 10 hours

    public JwtUtil(@Value("${marketplace.jwt.secret:}") String configuredSecret) {
        this.key = configuredSecret.isBlank()
                ? Jwts.SIG.HS256.key().build()
                : Keys.hmacShaKeyFor(configuredSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Create a token containing the user's email, database id, and their current mode (BUYER/SELLER/ADMIN)
    public String generateToken(String email, Long userId, String role, String loginAs) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("loginAs", loginAs)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    // Read and validate a token, returning its contents if valid
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
