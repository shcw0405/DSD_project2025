package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRegistrationResponseDataDTO {
    private String id; // 这个id应该是Doctor实体的id，还是User实体的id？通常是User的id作为账户id
    private String name;
    private String phone;
    private String hospital;
    private String department;
} 