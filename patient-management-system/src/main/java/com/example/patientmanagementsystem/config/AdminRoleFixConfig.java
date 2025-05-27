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
 * 修复管理员角色配置
 * 确保管理员账号拥有正确的角色标识
 */
@Configuration
public class AdminRoleFixConfig {

    private static final Logger logger = LoggerFactory.getLogger(AdminRoleFixConfig.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    /**
     * 修复管理员角色
     */
    @Bean
    @Transactional
    public CommandLineRunner fixAdminRole() {
        return args -> {
            logger.info("开始修复管理员角色...");
            
            try {
                // 查找管理员账号
                User adminUser = userRepository.findByPhone(adminPhone).orElse(null);
                
                if (adminUser != null) {
                    logger.info("找到管理员账号: {} (ID: {})", adminPhone, adminUser.getId());
                    
                    // 确保管理员角色正确设置
                    boolean needsUpdate = false;
                    
                    if (!adminUser.isAdmin()) {
                        adminUser.setAdmin(true);
                        needsUpdate = true;
                        logger.info("设置管理员角色: isAdmin = true");
                    }
                    
                    // 确保其他角色正确设置
                    if (adminUser.isDoctor()) {
                        adminUser.setDoctor(false);
                        needsUpdate = true;
                        logger.info("设置管理员角色: isDoctor = false");
                    }
                    
                    if (adminUser.isPatient()) {
                        adminUser.setPatient(false);
                        needsUpdate = true;
                        logger.info("设置管理员角色: isPatient = false");
                    }
                    
                    // 如果需要更新，保存用户
                    if (needsUpdate) {
                        userRepository.save(adminUser);
                        logger.info("管理员角色已更新");
                    } else {
                        logger.info("管理员角色已正确设置，无需更新");
                    }
                    
                    // 直接通过JDBC验证角色设置
                    Integer adminFlag = jdbcTemplate.queryForObject(
                        "SELECT IS_ADMIN FROM USERS WHERE PHONE = ?", 
                        Integer.class, 
                        adminPhone
                    );
                    
                    logger.info("数据库中管理员角色标志: {}", adminFlag);
                } else {
                    logger.error("未找到管理员账号，无法修复角色");
                    
                    // 创建新的管理员账号
                    String userId = UUID.randomUUID().toString();
                    String encodedPassword = passwordEncoder.encode(adminPassword);
                    
                    jdbcTemplate.update(
                        "INSERT INTO USERS (ID, NAME, PHONE, PASSWORD, IS_ADMIN, IS_DOCTOR, IS_PATIENT) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        userId, "Emergency Admin", adminPhone, encodedPassword, true, false, false
                    );
                    
                    logger.info("已创建紧急管理员账号: {} (ID: {})", adminPhone, userId);
                }
            } catch (Exception e) {
                logger.error("修复管理员角色失败: {}", e.getMessage(), e);
            }
        };
    }
}
