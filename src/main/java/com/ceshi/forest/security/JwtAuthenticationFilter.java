package com.ceshi.forest.security;

import com.ceshi.forest.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        if (!jwtUtil.validateToken(jwt)) {
            log.warn("无效的JWT令牌");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String username = jwtUtil.getUsernameFromToken(jwt);
            final String role = jwtUtil.getRoleFromToken(jwt);

            // 关键修复：避免重复添加 ROLE_ 前缀
            final String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

            log.info("Token解析 - 用户: {}, 角色: {}, 权限: {}", username, role, authority);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(authority)
                );

                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                        username, "", authorities
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("用户 {} 认证成功，权限: {}", username, authorities);
            }

        } catch (Exception e) {
            log.error("认证失败: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}