package com.example.patientmanagementsystem.security;

import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.DoctorPatientRelationRepository;
import com.example.patientmanagementsystem.repository.PatientRepository;
import com.example.patientmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service("patientSecurityService")
public class PatientSecurityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository; // 用于根据 patientId 找到关联的 userId

    @Autowired
    private DoctorPatientRelationRepository doctorPatientRelationRepository;

    public boolean canAccessPatientData(String patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        String currentUsername = null; // This will be the phone number
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

        // 1. Check if Admin
        if (authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"))) {
            return true;
        }

        // 2. Check if Patient self
        // We need to get the User ID associated with the patientId
        com.example.patientmanagementsystem.model.Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient != null && patient.getUser() != null && patient.getUser().getId().equals(currentUser.getId())) {
            return true;
        }
        
        // 3. Check if current user is a doctor related to this patient
        // currentUser.getId() is the doctor's user ID (which is also their doctor ID in Doctor table)
        if (currentUser.isDoctor() && patient != null) {
             if (doctorPatientRelationRepository.existsByDoctorIdAndPatientId(currentUser.getId(), patientId)){
                 return true;
             }
        }

        return false;
    }
} 