package com.demo.ecommerce.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private static final String SECRET = "mySecretKeyForJwtAuthenticationProject2026SpringBoot";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)              // Email = identity of user
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(key)
                .compact();
    }
}


// JWT has 3 parts: 1.Header 2.Payload 3.Signature

// hmacShaKeyFor converts String to Secret Key
// HMAC-SHA stands for Hash-based Message Authentication Code using a Secure Hash Algorithm