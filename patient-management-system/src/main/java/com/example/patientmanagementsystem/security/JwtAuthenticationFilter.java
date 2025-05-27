package com.example.patientmanagementsystem.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${app.jwt.header}")
    private String tokenHeader;

    @Value("${app.jwt.prefix}")
    private String tokenPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            logger.info("处理请求: {} {}", request.getMethod(), request.getRequestURI());
            
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                logger.info("请求包含有效JWT令牌");
                
                String username = tokenProvider.getUsernameFromToken(jwt);
                logger.info("从JWT令牌获取用户名: {}", username);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("加载用户详情: username={}, authorities={}", 
                    userDetails.getUsername(), 
                    userDetails.getAuthorities());
                
                // 使用tokenProvider创建认证令牌，确保权限正确传递
                UsernamePasswordAuthenticationToken authentication = 
                    tokenProvider.getAuthenticationToken(jwt, null, userDetails);
                    
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                logger.info("设置认证上下文: principal={}, authorities={}", 
                    authentication.getPrincipal(), 
                    authentication.getAuthorities());
                    
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.info("请求不包含有效JWT令牌");
            }
        } catch (Exception ex) {
            logger.error("无法设置用户认证: {}", ex.getMessage(), ex);
            // 记录异常但不阻止请求继续处理
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(tokenHeader);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length());
        }
        return null;
    }
}
