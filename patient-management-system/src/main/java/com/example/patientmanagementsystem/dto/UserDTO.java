package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户数据传输对象
 * 用于在API层传输用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String patientEntityId;
    private String name;
    private String phone;
    private String gender;
    private String birthDate;
    private String idType;
    private String idNumber;
    private boolean isAdmin;
    private boolean isDoctor;
    private boolean isPatient;
    private String token;
}
