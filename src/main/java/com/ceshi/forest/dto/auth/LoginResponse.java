package com.ceshi.forest.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String realName;
    private List<String> roles;
    private String avatar;
}