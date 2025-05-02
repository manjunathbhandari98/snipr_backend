package com.quodex.snipr.security.jwt;

import lombok.Data;

// ✅ This class represents the response sent back to the client after successful login
@Data
public class JwtAuthenticationResponse {

    // 🔐 The JWT token that will be returned to the client after successful authentication
    private String token;
}
