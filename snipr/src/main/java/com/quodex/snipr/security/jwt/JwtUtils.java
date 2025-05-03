package com.quodex.snipr.security.jwt;

import com.quodex.snipr.service.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    // 🔐 JWT secret key (Base64 encoded) from application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    // ⏰ JWT token expiration time in milliseconds from application.properties
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    // 📥 Extract JWT token from Authorization header
    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        // Check if the token starts with "Bearer "
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Remove "Bearer " prefix to extract the token
            return bearerToken.substring(7);
        }
        return null;
    }

    // 🔧 Generate a JWT token using user details
    public String generateToken(UserDetailsImpl userDetails){
        String username = userDetails.getUsername();

        // Get user role(s) as comma-separated string
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Build and return the JWT token
        return Jwts.builder()
                .subject(username)                      // Username is the subject
                .claim("roles", role)                   // Add custom claim (role)
                .issuedAt(new Date())                   // Issue time: now
                .expiration(new Date(new Date().getTime() + jwtExpirationMs)) // Expiry time
                .signWith(key())                        // Sign with secret key
                .compact();                             // Finalize and return token
    }

    // 📤 Extract username from JWT token
    public String getUserNameFromJWTToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())          // Validate with secret key
                .build().parseSignedClaims(token)       // Parse the token
                .getPayload().getSubject();             // Get the subject (username)
    }

    // 🔑 Generate signing key from Base64 encoded secret
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // ✅ Check if the token is valid (not expired or tampered)
    public boolean validateToken(String authToken){
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build().parseSignedClaims(authToken); // Try parsing the token
            return true;
        } catch (Exception e){
            // Token is invalid or expired
            throw new RuntimeException(e);
        }
    }
}
