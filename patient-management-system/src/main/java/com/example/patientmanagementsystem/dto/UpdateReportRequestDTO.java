package com.example.patientmanagementsystem.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;

@Data
public class UpdateReportRequestDTO {
    @NotEmpty(message = "报告类型 type 不能为空")
    private String type;

    @NotEmpty(message = "报告概要 summary 不能为空")
    private String summary;
} 