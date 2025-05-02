package com.quodex.snipr.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 🔒 Custom filter that runs once per request to check JWT and set authentication
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 📦 Injects the utility class that helps handle JWT operations (generate, validate, parse)
    @Autowired
    private JwtUtils jwtUtils;

    // 👤 Used to load user data (username, password, roles) from DB or other source
    @Autowired
    private UserDetailsService userDetailsService;

    // 🚦 This method is called for every HTTP request before it reaches the controller
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 🔍 Extract JWT token from the Authorization header
            String jwt = jwtUtils.getJwtFromHeader(request);

            // ✅ If token exists and is valid
            if (jwt != null && jwtUtils.validateToken(jwt)) {

                // 👤 Extract username from the token
                String username = jwtUtils.getUserNameFromJWTToken(jwt);

                // 📄 Load user details from DB (or configured service)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 🔐 If user is found, build an authentication object
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,         // principal
                                    null,                // credentials (null since already authenticated)
                                    userDetails.getAuthorities() // user's roles/permissions
                            );

                    // 📌 Set additional authentication details from request (IP, session ID, etc.)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 🧠 Store authentication info in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (Exception e) {
            // ⚠️ If any error occurs (like invalid token), log and throw runtime error
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // 🔁 Continue with the rest of the filter chain
        filterChain.doFilter(request, response);
    }
}
