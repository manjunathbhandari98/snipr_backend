package com.quodex.snipr.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private Set<String> role;
    private String password;
}
