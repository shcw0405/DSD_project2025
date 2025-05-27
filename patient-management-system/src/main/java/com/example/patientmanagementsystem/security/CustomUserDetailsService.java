package com.example.patientmanagementsystem.security;

import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("加载用户详情: username={}", username);
        
        User user = userRepository.findByPhone(username)
                .orElseThrow(() -> {
                    logger.error("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });

        logger.info("找到用户: id={}, name={}, isAdmin={}, isDoctor={}, isPatient={}", 
                user.getId(), user.getName(), user.isAdmin(), user.isDoctor(), user.isPatient());

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 添加用户角色
        if (user.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            logger.info("用户拥有管理员角色");
        }
        if (user.isDoctor()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_DOCTOR"));
            logger.info("用户拥有医生角色");
        }
        if (user.isPatient()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PATIENT"));
            logger.info("用户拥有患者角色");
        }
        
        // 特殊处理管理员账号
        if ("13000000000".equals(username)) {
            // 检查是否已有管理员角色
            boolean hasAdminRole = authorities.stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            
            // 如果没有管理员角色，添加并更新数据库
            if (!hasAdminRole) {
                logger.warn("管理员账号 {} 缺少管理员角色，尝试添加", username);
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                
                // 更新数据库中的管理员标志
                user.setAdmin(true);
                user.setDoctor(false);
                user.setPatient(false);
                userRepository.save(user);
                
                logger.info("已更新管理员账号权限: isAdmin={}", user.isAdmin());
            }
        }

        logger.info("用户 {} 的权限: {}", username, authorities);
        
        return new org.springframework.security.core.userdetails.User(
                user.getPhone(), 
                user.getPassword(), 
                authorities);
    }
}
