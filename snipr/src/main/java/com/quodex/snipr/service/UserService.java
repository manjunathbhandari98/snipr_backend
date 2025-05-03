// This class handles user-related operations like registration and authentication (login).
// It is marked as a @Service, meaning it's a Spring-managed service component that contains business logic.

package com.quodex.snipr.service;

import com.quodex.snipr.dto.LoginRequest; // DTO class that holds username and password from login request
import com.quodex.snipr.models.User; // User model representing a user in the system
import com.quodex.snipr.repositories.UserRepository; // JPA repository interface for User database operations
import com.quodex.snipr.security.jwt.JwtAuthenticationResponse; // Class used to return the JWT token after successful login
import com.quodex.snipr.security.jwt.JwtUtils; // Utility class for generating and validating JWT tokens
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager; // Authenticates the login credentials
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Represents login credentials (username + password)
import org.springframework.security.core.Authentication; // Represents the result of an authentication attempt
import org.springframework.security.core.context.SecurityContextHolder; // Stores authentication information in the current context (thread)
import org.springframework.security.crypto.password.PasswordEncoder; // Used to hash passwords securely
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    // Used to hash and verify passwords (e.g., using BCrypt)
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Repository for saving and fetching user data from the database
    @Autowired
    private UserRepository userRepository;

    // Used to authenticate login credentials
    @Autowired
    private AuthenticationManager authenticationManager;

    // Utility to generate and validate JWT tokens
    @Autowired
    private JwtUtils jwtUtils;

    // Registers a new user by hashing their password and saving them to the database
    public User registerUser(User user) {
        // Encode (hash) the raw password before saving to the database for security
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save the user to the database using JPA repository
        return userRepository.save(user);
    }

    // Authenticates a user and returns a JWT token on successful login
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        // Create an authentication token using the username and password from the login request
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Store authentication information in the current security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get the authenticated user's details (our custom implementation)
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate a JWT token for the authenticated user
        String jwt = jwtUtils.generateToken(userDetails);

        // Return the token in a response object
        return new JwtAuthenticationResponse(jwt);
    }

    public User findByUserName(String name) {
        return userRepository.findByUsername(name).orElseThrow(() -> new RuntimeException("User Name Not Found with username: "+name));
    }
}
