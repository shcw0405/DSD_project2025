package com.example.patientmanagementsystem.config;

import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;

/**
 * 管理员账号初始化器
 * 确保系统启动时管理员账号存在且可用
 */
@Component
@Order(1)
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.phone:13000000000}")
    private String adminPhone;

    @Value("${app.admin.password:123456}")
    private String adminPassword;

    @Value("${app.admin.name:管理员}")
    private String adminName;

    @PostConstruct
    public void init() {
        logger.info("AdminAccountInitializer初始化，管理员账号: {}", adminPhone);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        logger.info("开始初始化管理员账号...");
        
        try {
            // 检查管理员账号是否存在
            Optional<User> adminOpt = userRepository.findByPhone(adminPhone);
            
            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();
                logger.info("管理员账号已存在，ID: {}, isAdmin: {}", admin.getId(), admin.isAdmin());
                
                // 确保管理员角色标志正确
                if (!admin.isAdmin()) {
                    admin.setAdmin(true);
                    admin.setDoctor(false);
                    admin.setPatient(false);
                    admin = userRepository.save(admin);
                    logger.info("更新管理员角色标志: isAdmin={}", admin.isAdmin());
                }
                
                // 更新管理员密码
                String encodedPassword = passwordEncoder.encode(adminPassword);
                admin.setPassword(encodedPassword);
                admin.setName(adminName);
                userRepository.save(admin);
                logger.info("更新管理员密码和信息完成");
            } else {
                logger.info("管理员账号不存在，创建新账号");
                
                // 创建新的管理员账号
                User admin = new User();
                admin.setId(UUID.randomUUID().toString());
                admin.setName(adminName);
                admin.setPhone(adminPhone);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setAdmin(true);
                admin.setDoctor(false);
                admin.setPatient(false);
                
                User savedAdmin = userRepository.save(admin);
                logger.info("创建管理员账号成功，ID: {}, isAdmin: {}", savedAdmin.getId(), savedAdmin.isAdmin());
            }
            
            // 验证管理员账号
            Optional<User> verifyAdmin = userRepository.findByPhone(adminPhone);
            if (verifyAdmin.isPresent()) {
                User admin = verifyAdmin.get();
                boolean passwordMatches = passwordEncoder.matches(adminPassword, admin.getPassword());
                logger.info("管理员账号验证: ID={}, isAdmin={}, passwordMatches={}", 
                    admin.getId(), admin.isAdmin(), passwordMatches);
            } else {
                logger.error("管理员账号验证失败: 账号不存在");
            }
            
            logger.info("管理员账号初始化完成");
        } catch (Exception e) {
            logger.error("管理员账号初始化失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}
