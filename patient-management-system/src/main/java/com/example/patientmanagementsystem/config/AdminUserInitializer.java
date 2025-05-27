package com.example.patientmanagementsystem.config;

import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

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

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!userRepository.existsByPhone(adminPhone)) {
            User adminUser = new User();
            adminUser.setName(adminName);
            adminUser.setPhone(adminPhone);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setGender(adminGender);
            adminUser.setBirthDate(adminBirthDate);
            adminUser.setIdType(adminIdType);
            adminUser.setIdNumber(adminIdNumber);
            
            adminUser.setAdmin(true);
            adminUser.setDoctor(false); // Explicitly set other roles if necessary
            adminUser.setPatient(false);
            
            userRepository.save(adminUser);
            logger.info("Default admin user created with phone: {}", adminPhone);
        } else {
            logger.info("Default admin user with phone: {} already exists.", adminPhone);
            // Optionally, update existing admin user's password or details if needed
            // For example, to ensure the password matches the properties file:
            User existingAdmin = userRepository.findByPhone(adminPhone).orElse(null);
            if (existingAdmin != null && !passwordEncoder.matches(adminPassword, existingAdmin.getPassword())) {
                existingAdmin.setPassword(passwordEncoder.encode(adminPassword));
                userRepository.save(existingAdmin);
                logger.info("Updated password for default admin user: {}", adminPhone);
            }
        }
    }
} 