package com.example.patientmanagementsystem.repository;

import com.example.patientmanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByPhone(String phone);
    
    List<User> findByIsAdmin(boolean isAdmin);
    
    List<User> findByIsDoctor(boolean isDoctor);
    
    List<User> findByIsPatient(boolean isPatient);
    
    boolean existsByPhone(String phone);
    
    void deleteByPhone(String phone);
}
