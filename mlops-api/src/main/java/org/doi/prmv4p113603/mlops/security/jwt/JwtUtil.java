package org.doi.prmv4p113603.mlops.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Utility class for generating and validating JWT tokens.
 * <p>
 * Uses HMAC-SHA signing to encode and verify token claims.
 */
@Component
public class JwtUtil {

    /*
     * TODO: A stronger key could be generated with:
     *
     *  private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
     *
     *  In this case, it will be necessary to persist or export the key, or a new one
     *  will be generated every time the app starts.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Generates a signed JWT token for the given user.
     *
     * @param userDetails the authenticated user's details
     * @return a JWT token string
     */
    public String generateToken(UserDetails userDetails) {

        long expiration = 1000 * 60 * 60; // 1 hour

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the username contained in the token
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the provided JWT token.
     *
     * @param token the JWT token
     * @param userDetails the user details to match the token's subject against
     * @return true if the token is valid and belongs to the user; false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.before(new Date());
    }

}
