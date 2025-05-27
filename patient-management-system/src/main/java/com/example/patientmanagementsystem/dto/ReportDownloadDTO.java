package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报告下载信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDownloadDTO {
    private String fileName;
    private String filePath;
}
