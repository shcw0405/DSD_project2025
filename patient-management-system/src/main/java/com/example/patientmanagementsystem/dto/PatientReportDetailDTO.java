package com.example.patientmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 患者报告详细信息数据传输对象
 * 用于API层传输单个患者报告的完整信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientReportDetailDTO {
    // Fields from the original PatientReportDTO that were removed
    private String id;
    private String patientId;
    private String patientName;
    private String recordId;
    private String fileName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Fields that are common with the list DTO (PatientReportDTO)
    private String date; // 报告日期 (YYYY-MM-DD)
    private String type; // 报告类型 (之前叫 reportType, API 规范统一为 type)
    private String summary; // 报告摘要

    @JsonProperty("reportData")
    private ReportDataDetailsDTO reportDataDetails;
} 