package com.example.patientmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDataDetailsDTO {

    @JsonProperty("标准幅度")
    private List<Double> standardAmplitude;

    @JsonProperty("运动幅度")
    private List<Double> motionAmplitude;

    @JsonProperty("差异")
    private List<Double> difference;
} 