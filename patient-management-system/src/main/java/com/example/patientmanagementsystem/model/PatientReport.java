package com.example.patientmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "patient_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientReport {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "患者不能为空")
    private Patient patient;
    
    @Column(name = "record_id")
    private String recordId;
    
    @Column(name = "date", nullable = false)
    @NotNull(message = "报告日期不能为空")
    private LocalDate date;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    
    // 修改为H2兼容的TEXT类型，移除@Type注解
    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportDataJson;

    @Lob
    @Column(name = "raw_csv_content1", columnDefinition = "LONGTEXT")
    private String rawCsvContent1;

    @Lob
    @Column(name = "raw_csv_content2", columnDefinition = "LONGTEXT")
    private String rawCsvContent2;

    @Lob
    @Column(name = "raw_csv_content3", columnDefinition = "LONGTEXT")
    private String rawCsvContent3;

    @Lob
    @Column(name = "raw_csv_content4", columnDefinition = "LONGTEXT")
    private String rawCsvContent4;

    @Lob
    @Column(name = "cleaned_csv_content1", columnDefinition = "LONGTEXT")
    private String cleanedCsvContent1;

    @Lob
    @Column(name = "cleaned_csv_content2", columnDefinition = "LONGTEXT")
    private String cleanedCsvContent2;

    @Lob
    @Column(name = "cleaned_csv_content3", columnDefinition = "LONGTEXT")
    private String cleanedCsvContent3;

    @Lob
    @Column(name = "cleaned_csv_content4", columnDefinition = "LONGTEXT")
    private String cleanedCsvContent4;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 添加缺失的方法
    public String getPatientId() {
        return patient != null ? patient.getId() : null;
    }
    
    public String getRecordId() {
        return recordId;
    }
    
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
    
    public String getReportType() {
        return type;
    }
    
    // 添加reportData的getter和setter，使用JSON序列化和反序列化
    @Transient
    private Map<String, Object> reportData = new HashMap<>();
    
    public Map<String, Object> getReportData() {
        return reportData;
    }
    
    public void setReportData(Map<String, Object> reportData) {
        this.reportData = reportData;
    }
    
    // 在保存前将Map转换为JSON字符串
    @PrePersist
    @PreUpdate
    public void beforeSave() {
        try {
            if (reportData != null) {
                // 使用Jackson或其他JSON库进行序列化
                reportDataJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(reportData);
            }
        } catch (Exception e) {
            reportDataJson = "{}";
        }
    }
    
    // 在加载后将JSON字符串转换为Map
    @PostLoad
    public void afterLoad() {
        try {
            if (reportDataJson != null && !reportDataJson.isEmpty()) {
                // 使用Jackson或其他JSON库进行反序列化
                reportData = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    reportDataJson, 
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
            }
        } catch (Exception e) {
            reportData = new HashMap<>();
        }
    }
}
