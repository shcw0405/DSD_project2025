package com.example.patientmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 用户实体类
 * 存储用户基本信息和认证信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    private String name;
    
    @Column(unique = true)
    private String phone;
    
    private String gender;
    
    private String birthDate;
    
    private String idType;
    
    private String idNumber;
    
    private String password;
    
    private boolean isAdmin;
    
    private boolean isDoctor;
    
    private boolean isPatient;
}
