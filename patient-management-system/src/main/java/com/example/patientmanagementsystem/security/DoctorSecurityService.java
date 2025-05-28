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

    public boolean isDoctorSelf(String userIdFromPath) {
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
        if (currentUser == null || !currentUser.isDoctor()) {
            return false;
        }

        return currentUser.getId().equals(userIdFromPath);
    }
} 