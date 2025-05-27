package com.example.patientmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 患者报告列表项数据传输对象 (用于 /api/patient/{patientId}/reports)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientReportDTO {
    private String date; // 报告日期 (YYYY-MM-DD)
    private String type; // 报告类型
    private String summary; // 报告摘要

    @JsonProperty("reportData")
    private ReportDataDetailsDTO reportDataDetails;
}
