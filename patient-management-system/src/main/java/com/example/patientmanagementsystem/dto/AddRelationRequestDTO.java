package com.example.patientmanagementsystem.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AddRelationRequestDTO {

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;
} 