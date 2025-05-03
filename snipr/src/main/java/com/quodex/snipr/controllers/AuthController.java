package com.quodex.snipr.controllers;

import com.quodex.snipr.dto.LoginRequest;
import com.quodex.snipr.dto.RegisterRequest;
import com.quodex.snipr.models.User;
import com.quodex.snipr.repositories.UserRepository;
import com.quodex.snipr.security.jwt.JwtAuthenticationResponse;
import com.quodex.snipr.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@CrossOrigin
public class AuthController {

    private final UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setRole("ROLE_USER");
        userService.registerUser(user);
        return ResponseEntity.ok("User registered Successfully");
    }

    @PostMapping("/public/login")
    public JwtAuthenticationResponse loginUser(@RequestBody LoginRequest loginRequest){
        return userService.authenticateUser(loginRequest);
    }
}
