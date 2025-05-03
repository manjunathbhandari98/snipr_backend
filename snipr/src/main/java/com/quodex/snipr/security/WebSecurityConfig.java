/**
 * WebSecurityConfig.java
 *
 * This class is the central security configuration for the Spring Boot application.
 * It sets up how authentication and authorization should be handled.
 *
 * Key responsibilities:
 * - Disables CSRF protection (common in stateless APIs using JWT)
 * - Allows unauthenticated access to certain endpoints (e.g., /api/auth/**)
 * - Protects other endpoints and requires valid JWT tokens
 * - Configures how users are loaded (via UserDetailsServiceImpl)
 * - Adds a custom JWT filter before the default username-password filter
 *
 * This setup enables stateless JWT-based authentication for REST APIs.
 */

package com.quodex.snipr.security;

import com.quodex.snipr.security.jwt.JwtAuthenticationFilter;
import com.quodex.snipr.service.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration                      // Marks this class as a configuration class
@EnableWebSecurity                  // Enables Spring Security for the application
@EnableMethodSecurity               // Allows method-level security using annotations like @PreAuthorize
@AllArgsConstructor
public class WebSecurityConfig {

    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private CustomAuthEntryPoint customAuthEntryPoint;

    // Registers the custom JWT authentication filter as a bean
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    // Registers a password encoder bean using BCrypt (used to hash passwords securely)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Configures the authentication provider using our custom UserDetailsService and password encoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // How Spring loads users
        authProvider.setPasswordEncoder(passwordEncoder());     // How passwords are verified
        return authProvider;
    }

    // Configures the main security filter chain for HTTP requests
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF because we're using stateless JWTs
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthEntryPoint))
                // Define which endpoints are public and which require authentication
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()      // Allow access to auth endpoints like login/signup
                        .requestMatchers("/api/urls/**").authenticated()  // Protect /api/urls/** endpoints
                        .requestMatchers("/{shortUrl}").permitAll()        // Allow public access to shortened URLs
                        .anyRequest().authenticated()                    // Any other requests require authentication
                );

        // Register our custom auth provider
        http.authenticationProvider(authenticationProvider());

        // Add our custom JWT filter before Spring's default UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // Finalize and return the configured security filter chain
        return http.build();
    }
}

