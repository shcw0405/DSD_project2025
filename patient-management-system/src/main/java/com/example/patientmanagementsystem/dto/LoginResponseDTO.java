package com.example.patientmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String id;
    private String name;
    private String token;

    @JsonProperty("isAdmin")
    private boolean isAdmin;

    @JsonProperty("isDoctor")
    private boolean isDoctor;

    @JsonProperty("isPatient")
    private boolean isPatient;
} 