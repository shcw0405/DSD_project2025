package com.example.patientmanagementsystem.security;

import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service("doctorSecurityService")
public class DoctorSecurityService {

    @Autowired
    private UserRepository userRepository;

    public boolean isDoctorSelf(String doctorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        String currentUsername = null;
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            currentUsername = (String) principal;
        }

        if (currentUsername == null) {
            return false;
        }

        User currentUser = userRepository.findByPhone(currentUsername).orElse(null);
        if (currentUser == null) {
            return false;
        }
        // 假设 Doctor 的 User ID 就是 Doctor 实体自身的 ID，或者 Doctor 有一个 userId 字段关联到 User 表
        // 这里我们先假设 doctorId 就是 User 表的 ID
        return currentUser.getId().equals(doctorId);
    }
} 