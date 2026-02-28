package com.ceshi.forest.security;

import com.ceshi.forest.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 获取 JWT 令牌
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 检查 Authorization 头
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取令牌
        jwt = authHeader.substring(7);

        // 验证令牌
        if (!jwtUtil.validateToken(jwt)) {
            log.warn("无效的JWT令牌");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            username = jwtUtil.getUsernameFromToken(jwt);
        } catch (Exception e) {
            log.error("从令牌解析用户名失败: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 检查是否已认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 验证令牌是否有效且未过期
            if (jwtUtil.validateToken(jwt) && !jwtUtil.isTokenExpired(jwt)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置安全上下文
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("用户 {} 认证成功", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}