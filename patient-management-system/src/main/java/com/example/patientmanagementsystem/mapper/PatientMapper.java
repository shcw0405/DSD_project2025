package com.example.patientmanagementsystem.mapper;

import com.example.patientmanagementsystem.dto.PatientDTO;
import com.example.patientmanagementsystem.dto.PatientListResponseDTO;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.model.Patient.Gender;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PatientMapper {

    public PatientDTO toDTO(Patient patient) {
        if (patient == null) {
            return null;
        }
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        User user = patient.getUser();
        if (user != null) {
            dto.setName(user.getName());
            dto.setPhone(user.getPhone());
        }
        dto.setGender(patient.getGender() != null ? patient.getGender().name() : null);
        dto.setBirthDate(patient.getBirthDate());
        dto.setIdNumber(patient.getIdNumber());
        return dto;
    }

    public PatientListResponseDTO toListResponseDTO(Page<Patient> patientPage) {
        List<PatientDTO> dtos = patientPage.getContent().stream()
                                           .map(this::toDTO)
                                           .collect(Collectors.toList());
        return new PatientListResponseDTO(dtos, patientPage.getTotalElements());
    }
} 