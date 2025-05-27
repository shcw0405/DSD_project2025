package com.example.patientmanagementsystem.config;

import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.UserRepository;
import com.example.patientmanagementsystem.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;

/**
 * 全面修复管理员账号和权限配置
 * 确保管理员账号拥有正确的角色标识并能被系统正确识别
 */
@Configuration
public class AdminComprehensiveFixConfig {

    private static final Logger logger = LoggerFactory.getLogger(AdminComprehensiveFixConfig.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    /**
     * 全面修复管理员账号和权限
     */
    @Bean
    @Transactional
    public CommandLineRunner comprehensiveFixAdminRole() {
        return args -> {
            logger.info("开始全面修复管理员账号和权限...");
            
            try {
                // 1. 确保数据库中管理员账号存在且角色正确
                User adminUser = userRepository.findByPhone(adminPhone).orElse(null);
                
                if (adminUser == null) {
                    // 创建新的管理员账号
                    adminUser = new User();
                    adminUser.setId(UUID.randomUUID().toString());
                    adminUser.setName(adminName);
                    adminUser.setPhone(adminPhone);
                    adminUser.setPassword(passwordEncoder.encode(adminPassword));
                    adminUser.setAdmin(true);
                    adminUser.setDoctor(false);
                    adminUser.setPatient(false);
                    
                    adminUser = userRepository.save(adminUser);
                    logger.info("创建新管理员账号: {} (ID: {})", adminPhone, adminUser.getId());
                } else {
                    // 更新现有管理员账号
                    boolean needsUpdate = false;
                    
                    if (!adminUser.isAdmin()) {
                        adminUser.setAdmin(true);
                        needsUpdate = true;
                        logger.info("更新管理员角色: isAdmin = true");
                    }
                    
                    if (adminUser.isDoctor()) {
                        adminUser.setDoctor(false);
                        needsUpdate = true;
                        logger.info("更新管理员角色: isDoctor = false");
                    }
                    
                    if (adminUser.isPatient()) {
                        adminUser.setPatient(false);
                        needsUpdate = true;
                        logger.info("更新管理员角色: isPatient = false");
                    }
                    
                    if (needsUpdate) {
                        adminUser = userRepository.save(adminUser);
                        logger.info("更新管理员账号: {} (ID: {})", adminPhone, adminUser.getId());
                    }
                }
                
                // 2. 直接通过JDBC验证数据库中的角色设置
                Integer adminFlag = jdbcTemplate.queryForObject(
                    "SELECT IS_ADMIN FROM USERS WHERE PHONE = ?", 
                    Integer.class, 
                    adminPhone
                );
                
                logger.info("数据库中管理员角色标志: {}", adminFlag);
                
                // 3. 验证管理员密码是否正确
                String storedPassword = jdbcTemplate.queryForObject(
                    "SELECT PASSWORD FROM USERS WHERE PHONE = ?", 
                    String.class, 
                    adminPhone
                );
                
                boolean passwordMatches = passwordEncoder.matches(adminPassword, storedPassword);
                logger.info("管理员密码匹配: {}", passwordMatches);
                
                // 4. 生成管理员token并验证
                // 创建带有ROLE_ADMIN权限的认证对象
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        adminPhone, 
                        null, 
                        Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    );
                
                // 设置认证上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 生成token
                String token = tokenProvider.generateToken(authentication);
                logger.info("生成管理员token: {}", token);
                
                // 验证token中的角色
                String roles = tokenProvider.getRolesFromToken(token);
                logger.info("Token中的角色: {}", roles);
                
                logger.info("管理员账号和权限修复完成");
            } catch (Exception e) {
                logger.error("修复管理员账号和权限失败: {}", e.getMessage(), e);
            }
        };
    }
}
