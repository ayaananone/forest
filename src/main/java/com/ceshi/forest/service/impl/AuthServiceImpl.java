package com.ceshi.forest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ceshi.forest.dto.auth.LoginRequest;
import com.ceshi.forest.dto.auth.LoginResponse;
import com.ceshi.forest.dto.auth.RegisterRequest;
import com.ceshi.forest.entity.User;
import com.ceshi.forest.mapper.RoleMapper;
import com.ceshi.forest.mapper.UserMapper;
import com.ceshi.forest.service.AuthService;
import com.ceshi.forest.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userDetails.getUsername()));

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        List<String> roles = roleMapper.selectRolesByUserId(user.getUserId());

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getUserId(),
                roles.isEmpty() ? "USER" : roles.get(0)
        );

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()))) {
            throw new RuntimeException("用户名已存在");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        roleMapper.insertUserRole(user.getUserId(), 2);
    }

    @Override
    public LoginResponse refreshToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("无效的令牌");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null || user.getStatus() == 0) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        List<String> roles = roleMapper.selectRolesByUserId(user.getUserId());
        String newToken = jwtUtil.refreshToken(token);

        return LoginResponse.builder()
                .token(newToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public Object getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }

        String username = authentication.getName();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        List<String> userRoles = roleMapper.selectRolesByUserId(user.getUserId());

        // 修复：使用单独的变量名，避免自引用
        return new UserInfoVO(user, userRoles);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    // 内部类或单独创建 VO 类
    public static class UserInfoVO {
        public final Integer userId;
        public final String username;
        public final String realName;
        public final String email;
        public final String phone;
        public final String avatar;
        public final List<String> roles;

        public UserInfoVO(User user, List<String> roleList) {
            this.userId = user.getUserId();
            this.username = user.getUsername();
            this.realName = user.getRealName();
            this.email = user.getEmail();
            this.phone = user.getPhone();
            this.avatar = user.getAvatar();
            this.roles = roleList;
        }
    }
}