package com.example.patientmanagementsystem.config;

import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.UserRepository;
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
 * 紧急修复管理员账号配置
 * 直接通过SQL创建管理员账号，确保绕过可能存在问题的业务逻辑
 */
@Configuration
public class EmergencyAdminFixConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyAdminFixConfig.class);

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
     * 紧急修复管理员账号
     */
    @Bean
    @Transactional
    public CommandLineRunner emergencyFixAdmin() {
        return args -> {
            logger.info("开始紧急修复管理员账号...");
            
            try {
                // 检查管理员账号是否存在
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM USERS WHERE PHONE = ?", 
                    Integer.class, 
                    adminPhone
                );
                
                if (count != null && count > 0) {
                    logger.info("管理员账号已存在，尝试更新角色标志");
                    
                    // 直接更新管理员角色标志
                    int updated = jdbcTemplate.update(
                        "UPDATE USERS SET IS_ADMIN = TRUE, IS_DOCTOR = FALSE, IS_PATIENT = FALSE WHERE PHONE = ?",
                        adminPhone
                    );
                    
                    logger.info("更新管理员角色标志: {} 行受影响", updated);
                    
                    // 验证更新结果
                    Integer adminFlag = jdbcTemplate.queryForObject(
                        "SELECT IS_ADMIN FROM USERS WHERE PHONE = ?", 
                        Integer.class, 
                        adminPhone
                    );
                    
                    logger.info("更新后管理员角色标志: {}", adminFlag);
                } else {
                    logger.info("管理员账号不存在，创建新账号");
                    
                    // 创建新的管理员账号
                    String userId = UUID.randomUUID().toString();
                    String encodedPassword = passwordEncoder.encode(adminPassword);
                    
                    int inserted = jdbcTemplate.update(
                        "INSERT INTO USERS (ID, NAME, PHONE, PASSWORD, IS_ADMIN, IS_DOCTOR, IS_PATIENT) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        userId, adminName, adminPhone, encodedPassword, true, false, false
                    );
                    
                    logger.info("创建管理员账号: {} 行受影响", inserted);
                    
                    // 验证创建结果
                    Integer adminFlag = jdbcTemplate.queryForObject(
                        "SELECT IS_ADMIN FROM USERS WHERE PHONE = ?", 
                        Integer.class, 
                        adminPhone
                    );
                    
                    logger.info("新建管理员角色标志: {}", adminFlag);
                }
                
                // 确保管理员密码正确
                String storedPassword = jdbcTemplate.queryForObject(
                    "SELECT PASSWORD FROM USERS WHERE PHONE = ?", 
                    String.class, 
                    adminPhone
                );
                
                boolean passwordMatches = passwordEncoder.matches(adminPassword, storedPassword);
                
                if (!passwordMatches) {
                    logger.info("管理员密码不匹配，更新密码");
                    
                    String encodedPassword = passwordEncoder.encode(adminPassword);
                    int updated = jdbcTemplate.update(
                        "UPDATE USERS SET PASSWORD = ? WHERE PHONE = ?",
                        encodedPassword, adminPhone
                    );
                    
                    logger.info("更新管理员密码: {} 行受影响", updated);
                } else {
                    logger.info("管理员密码匹配，无需更新");
                }
                
                logger.info("紧急修复管理员账号完成");
            } catch (Exception e) {
                logger.error("紧急修复管理员账号失败: {}", e.getMessage(), e);
            }
        };
    }
}
