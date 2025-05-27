package com.example.patientmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Record {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    @Column(name = "patient_id")
    private String patientId;
    
    @Column(name = "date", nullable = false)
    @NotNull(message = "日期不能为空")
    private LocalDate date;
    
    @Column(name = "time", nullable = false)
    @NotNull(message = "时间不能为空")
    private LocalDateTime time;
    
    @Column(name = "username", nullable = false)
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Column(name = "raw_file_path")
    private String rawFilePath;
    
    @Column(name = "raw_size_kb")
    private Integer rawSizeKb;
    
    @Lob
    @Column(name = "raw_file", columnDefinition = "LONGBLOB")
    private byte[] rawFile;
    
    @Column(name = "format_file_path")
    private String formatFilePath;
    
    @Column(name = "format_size")
    private Integer formatSize;
    
    @Lob
    @Column(name = "format_file", columnDefinition = "LONGBLOB")
    private byte[] formatFile;
    
    @Column(name = "report_file_path")
    private String reportFilePath;
    
    @Column(name = "report_size")
    private Integer reportSize;
    
    @Lob
    @Column(name = "report_file", columnDefinition = "LONGBLOB")
    private byte[] reportFile;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 添加缺失的字段和方法
    @Column(name = "processed")
    private boolean processed;
    
    @Column(name = "processed_data")
    private String processedData;
    
    // 添加上传时间字段
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
    
    // 添加文件名字段
    @Column(name = "file_name")
    private String fileName;
    
    // 添加文件路径字段
    @Column(name = "file_path")
    private String filePath;
    
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
    
    public void setProcessedData(String processedData) {
        this.processedData = processedData;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
