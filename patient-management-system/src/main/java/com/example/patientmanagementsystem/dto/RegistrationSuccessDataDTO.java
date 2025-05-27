package com.example.patientmanagementsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationSuccessDataDTO {
    private String id;
    private String name;
    private String phone;
    private String gender;
    private String birthDate;
    private String idType;
    private String idNumber;
} 