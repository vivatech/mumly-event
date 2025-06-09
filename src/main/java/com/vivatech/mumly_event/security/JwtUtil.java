package com.vivatech.mumly_event.security;


import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.helper.MumlyUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    private final String SECRET_KEY = "vivatechrnd_vivatech@123456789_uygeudyweg"; // Use a strong, secret key in production

    private Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, int minutes) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(MumlyUtils.addMinutesToJavaUtilDate(new Date(), minutes)) // 10 hours
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            // First, try to parse the token to check for format and signature
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            if (e instanceof io.jsonwebtoken.security.SignatureException) {
                log.error("Invalid JWT signature: {}", e.getMessage());
                return false;
            } else if (e instanceof io.jsonwebtoken.ExpiredJwtException) {
                log.error("JWT token has expired: {}", e.getMessage());
                return false;
            } else if (e instanceof io.jsonwebtoken.MalformedJwtException) {
                log.error("Invalid JWT token: {}", e.getMessage());
                return false;
            } else if (e instanceof io.jsonwebtoken.UnsupportedJwtException) {
                log.error("Unsupported JWT token: {}", e.getMessage());
                return false;
            } else {
                log.error("Unexpected error validating token: {}", e.getMessage());
                return false;
            }
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        if (token == null || token.isEmpty()) {
            throw new CustomExceptionHandler("Token cannot be null or empty");
        }
        
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateCredentials(String username, String password) {
        return username.equalsIgnoreCase("admin") && password.equals("admin");
    }
}
