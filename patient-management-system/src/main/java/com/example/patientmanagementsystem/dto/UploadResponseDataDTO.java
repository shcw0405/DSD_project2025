package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDataDTO {
    private String receivedAt; // ISO 格式时间戳
    private String reportId;
    private ReportDataDTO reportData;
} 