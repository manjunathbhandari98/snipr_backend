package com.quodex.snipr.service;

import com.quodex.snipr.models.User;
import com.quodex.snipr.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // Marks this class as a Spring service so it can be injected where needed
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired // Injects the UserRepository to access user data from the database
    private UserRepository userRepository;

    @Override
    @Transactional // Ensures the database operations are wrapped in a transaction
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 🔍 Tries to find a user in the database by username
        // ❌ If not found, throws a UsernameNotFoundException
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // ✅ Converts the User object into a Spring Security-compatible UserDetails object
        return UserDetailsImpl.build(user);
    }
}
