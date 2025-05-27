package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorListResponseDTO {
    private List<DoctorDTO> data;
    private long total;
} 