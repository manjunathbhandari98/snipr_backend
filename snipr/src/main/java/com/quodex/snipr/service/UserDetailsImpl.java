package com.quodex.snipr.service;

import com.quodex.snipr.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    // Serial version ID for serialization compatibility
    private static final long serialVersionUID = 1L;

    // Custom user fields
    private Long id;
    private String username;
    private String email;
    private String password;

    // Spring Security authorities (roles/permissions)
    private Collection<? extends GrantedAuthority> authorities;

    // Custom constructor to set all fields
    public UserDetailsImpl(Long id, String username, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    // Static method to convert your custom User object into UserDetailsImpl
    public static UserDetailsImpl build(User user) {
        // Wrap user's role into a SimpleGrantedAuthority object
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());

        // Return a UserDetailsImpl object containing the user's data
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority) // Only one role
        );
    }

    // Return user's authorities (roles/permissions)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // Return password (used for authentication)
    @Override
    public String getPassword() {
        return password;
    }

    // Return username (used for authentication)
    @Override
    public String getUsername() {
        return username;
    }

    // Account status methods (for now, return true for all = account is always valid)

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
