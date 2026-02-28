package com.ceshi.forest.service;

import com.ceshi.forest.dto.auth.LoginRequest;
import com.ceshi.forest.dto.auth.LoginResponse;
import com.ceshi.forest.dto.auth.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    LoginResponse refreshToken(String token);

    Object getCurrentUserInfo();

    void changePassword(String oldPassword, String newPassword);
}