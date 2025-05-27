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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * 修复版管理员账号初始化器
 * 确保管理员账号正确创建并可以登录
 */
@Configuration
public class AdminUserFixConfig {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserFixConfig.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.idNumber}")
    private String adminIdNumber;

    @Value("${app.admin.gender}")
    private String adminGender;

    @Value("${app.admin.birthDate}")
    private String adminBirthDate;

    @Value("${app.admin.idType}")
    private String adminIdType;

    /**
     * 强制重新创建管理员账号，确保密码加密正确
     */
    @Bean
    @Transactional
    public CommandLineRunner forceCreateAdminUser() {
        return args -> {
            logger.info("开始强制重新创建管理员账号...");
            
            // 先删除可能存在的管理员账号
            User existingAdmin = userRepository.findByPhone(adminPhone).orElse(null);
            if (existingAdmin != null) {
                logger.info("找到现有管理员账号，准备删除重建: {}", adminPhone);
                userRepository.delete(existingAdmin);
                userRepository.flush(); // 确保删除操作立即生效
            }
            
            // 创建新的管理员账号
            User adminUser = new User();
            adminUser.setName(adminName);
            adminUser.setPhone(adminPhone);
            // 确保使用正确的密码加密方式
            String encodedPassword = passwordEncoder.encode(adminPassword);
            adminUser.setPassword(encodedPassword);
            adminUser.setGender(adminGender);
            adminUser.setBirthDate(adminBirthDate);
            adminUser.setIdType(adminIdType);
            adminUser.setIdNumber(adminIdNumber);
            
            // 设置管理员角色
            adminUser.setAdmin(true);
            adminUser.setDoctor(false);
            adminUser.setPatient(false);
            
            User savedAdmin = userRepository.save(adminUser);
            userRepository.flush(); // 确保保存操作立即生效
            
            logger.info("管理员账号强制重建完成: {} (ID: {})", adminPhone, savedAdmin.getId());
            logger.info("管理员密码已加密: {}", encodedPassword);
            
            // 验证密码匹配
            boolean passwordMatches = passwordEncoder.matches(adminPassword, savedAdmin.getPassword());
            logger.info("密码匹配验证: {}", passwordMatches ? "成功" : "失败");
        };
    }
}
