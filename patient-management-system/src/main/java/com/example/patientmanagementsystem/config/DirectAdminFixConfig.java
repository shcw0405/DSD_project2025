package com.example.patientmanagementsystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 直接数据库操作修复管理员账号配置
 * 通过JDBC直接操作数据库，确保管理员账号存在且密码正确
 */
@Configuration
public class DirectAdminFixConfig {

    private static final Logger logger = LoggerFactory.getLogger(DirectAdminFixConfig.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    /**
     * 直接数据库操作修复管理员账号
     */
    @Bean
    @Transactional
    public CommandLineRunner directFixAdmin() {
        return args -> {
            logger.info("开始直接数据库操作修复管理员账号...");
            
            try {
                // 检查管理员账号是否存在
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM USERS WHERE PHONE = ?", 
                    Integer.class, 
                    adminPhone
                );
                
                String adminId;
                
                if (count != null && count > 0) {
                    logger.info("管理员账号已存在，更新账号信息");
                    
                    // 获取管理员ID
                    adminId = jdbcTemplate.queryForObject(
                        "SELECT ID FROM USERS WHERE PHONE = ?", 
                        String.class, 
                        adminPhone
                    );
                    
                    // 更新管理员信息
                    String encodedPassword = passwordEncoder.encode(adminPassword);
                    int updated = jdbcTemplate.update(
                        "UPDATE USERS SET PASSWORD = ?, IS_ADMIN = TRUE, IS_DOCTOR = FALSE, IS_PATIENT = FALSE, NAME = ? WHERE PHONE = ?",
                        encodedPassword, adminName, adminPhone
                    );
                    
                    logger.info("更新管理员账号: {} 行受影响", updated);
                } else {
                    logger.info("管理员账号不存在，创建新账号");
                    
                    // 创建新的管理员账号
                    adminId = UUID.randomUUID().toString();
                    String encodedPassword = passwordEncoder.encode(adminPassword);
                    
                    int inserted = jdbcTemplate.update(
                        "INSERT INTO USERS (ID, NAME, PHONE, PASSWORD, IS_ADMIN, IS_DOCTOR, IS_PATIENT) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        adminId, adminName, adminPhone, encodedPassword, true, false, false
                    );
                    
                    logger.info("创建管理员账号: {} 行受影响", inserted);
                }
                
                // 验证管理员账号
                String storedPassword = jdbcTemplate.queryForObject(
                    "SELECT PASSWORD FROM USERS WHERE PHONE = ?", 
                    String.class, 
                    adminPhone
                );
                
                Boolean isAdmin = jdbcTemplate.queryForObject(
                    "SELECT IS_ADMIN FROM USERS WHERE PHONE = ?", 
                    Boolean.class, 
                    adminPhone
                );
                
                logger.info("管理员账号验证: ID={}, isAdmin={}, passwordMatches={}", 
                    adminId, 
                    isAdmin, 
                    passwordEncoder.matches(adminPassword, storedPassword));
                
                logger.info("直接数据库操作修复管理员账号完成");
            } catch (Exception e) {
                logger.error("直接数据库操作修复管理员账号失败: {}", e.getMessage(), e);
            }
        };
    }
}
