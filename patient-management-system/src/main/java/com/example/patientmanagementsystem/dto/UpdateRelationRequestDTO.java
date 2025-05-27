package com.example.patientmanagementsystem.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UpdateRelationRequestDTO {

    @NotBlank(message = "旧医生ID不能为空")
    private String oldDoctorId;

    @NotBlank(message = "旧患者ID不能为空")
    private String oldPatientId;

    @NotBlank(message = "新医生ID不能为空")
    private String newDoctorId;

    @NotBlank(message = "新患者ID不能为空")
    private String newPatientId;
} 