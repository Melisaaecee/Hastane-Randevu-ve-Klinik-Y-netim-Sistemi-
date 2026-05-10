package com.hospital.management.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // TOKEN ÜRET
    public String generateToken(Long userId, String tckn, String role) {
        return Jwts.builder()
                .setSubject(tckn) 
                .claim("userId", userId)
                .claim("role", role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // Bu aslında TCKN dönecektir
    }

    public Long extractUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // TOKEN DOĞRULA
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String tcknFromToken = extractUsername(token);
            boolean isUsernameValid = tcknFromToken.equals(userDetails.getUsername());
            boolean isExpired = isTokenExpired(token);

            if (!isUsernameValid) {
                System.out.println("DOĞRULAMA HATASI: Token TCKN (" + tcknFromToken + ") ile UserDetails TCKN ("
                        + userDetails.getUsername() + ") eşleşmiyor!");
            }
            return (isUsernameValid && !isExpired);
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = getClaims(token);
        return resolver.apply(claims);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}