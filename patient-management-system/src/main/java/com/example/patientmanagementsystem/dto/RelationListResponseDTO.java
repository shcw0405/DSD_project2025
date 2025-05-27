package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationListResponseDTO {
    private List<DoctorPatientRelationDTO> data;
    private long total;
} 