package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPatientRelationDTO {
    private String doctorId;
    private String doctorName;
    private String patientId;
    private String patientName;
}
