package com.example.patientmanagementsystem.security;

import com.example.patientmanagementsystem.model.Doctor;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.DoctorPatientRelationRepository;
import com.example.patientmanagementsystem.repository.DoctorRepository;
import com.example.patientmanagementsystem.repository.PatientRepository;
import com.example.patientmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service("patientSecurityService")
public class PatientSecurityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository; // 用于根据 patientId 找到关联的 userId

    @Autowired
    private DoctorPatientRelationRepository doctorPatientRelationRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    public boolean canAccessPatientData(String userIdFromPath) {
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

        // 2. Check if Patient self (userIdFromPath is the User ID of the patient whose data is being accessed)
        if (currentUser.getId().equals(userIdFromPath)) {
            return true;
        }
        
        // 3. Check if current user is a doctor related to this patient
        if (currentUser.isDoctor()) {
            // Find the Doctor entity linked to the current (doctor) User
            Optional<Doctor> currentDoctorOpt = doctorRepository.findByUser_Id(currentUser.getId());
            if (currentDoctorOpt.isPresent()) {
                String actualDoctorUuid = currentDoctorOpt.get().getId(); // This is the Doctor table's primary key (UUID)

                // Find the Patient entity linked to the userIdFromPath (User ID of the patient)
                Optional<Patient> patientOpt = patientRepository.findByUser_Id(userIdFromPath);
                if (patientOpt.isPresent()) {
                    String actualPatientUuid = patientOpt.get().getId(); // This is the Patient table's primary key (UUID)
                    if (doctorPatientRelationRepository.existsByDoctorIdAndPatientId(actualDoctorUuid, actualPatientUuid)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
} 