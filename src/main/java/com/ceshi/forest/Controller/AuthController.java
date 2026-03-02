package com.ceshi.forest.controller;

import com.ceshi.forest.dto.ResultDTO;
import com.ceshi.forest.dto.auth.LoginRequest;
import com.ceshi.forest.dto.auth.LoginResponse;
import com.ceshi.forest.dto.auth.RegisterRequest;
import com.ceshi.forest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResultDTO<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResultDTO.ok(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResultDTO<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResultDTO.okMsg("注册成功");
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ResultDTO<LoginResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResultDTO.fail(401, "无效的认证头");
        }
        String token = authHeader.substring(7);
        LoginResponse response = authService.refreshToken(token);
        return ResultDTO.ok(response);
    }

    /**
     * 获取当前用户信息（修复 204 问题）
     */
    @GetMapping("/me")
    public ResultDTO<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 检查是否已认证
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("获取用户信息失败：用户未认证");
            return ResultDTO.fail(401, "用户未登录");
        }

        try {
            Object userInfo = authService.getCurrentUserInfo();
            return ResultDTO.ok(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResultDTO.fail(500, "获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResultDTO<Void> changePassword(@RequestParam String oldPassword,
                                          @RequestParam String newPassword) {
        authService.changePassword(oldPassword, newPassword);
        return ResultDTO.okMsg("密码修改成功");
    }
}