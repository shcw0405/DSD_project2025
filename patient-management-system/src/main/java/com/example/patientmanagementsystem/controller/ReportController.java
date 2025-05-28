package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.dto.PatientReportDTO;
import com.example.patientmanagementsystem.dto.PatientReportDetailDTO;
import com.example.patientmanagementsystem.dto.ReportDownloadDTO;
import com.example.patientmanagementsystem.dto.UpdateReportRequestDTO;
import com.example.patientmanagementsystem.dto.UpdateReportResponseDataDTO;
import com.example.patientmanagementsystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 报告管理控制器
 * 处理患者报告相关的CRUD操作
 */
@RestController
@RequestMapping("/report")
@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 获取患者报告列表
     * @param patientId 患者ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 报告列表和总数
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPatientReports(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        Map<String, Object> result = reportService.getPatientReports(patientId, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result, "获取患者报告列表成功"));
    }

    /**
     * 获取报告详情
     * @param reportId 报告ID
     * @return 报告详情
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<PatientReportDetailDTO>> getReportDetail(@PathVariable String reportId) {
        PatientReportDetailDTO report = reportService.getReportDetail(reportId);
        return ResponseEntity.ok(ApiResponse.success(report, "获取报告详情成功"));
    }

    /**
     * 根据提供的 reportId 更新指定报告的类型 (type) 和概要 (summary) 信息.
     * API Path: PUT /reports/{reportId}
     * @param reportId 报告的唯一标识符
     * @param updateReportRequestDTO 请求体，包含 type 和 summary
     * @return 包含更新后详细信息的响应
     */
    @PutMapping("/{reportId}")
    public ResponseEntity<ApiResponse<UpdateReportResponseDataDTO>> updateReportDetails(
            @PathVariable String reportId,
            @Valid @RequestBody UpdateReportRequestDTO updateReportRequestDTO) {
        
        UpdateReportResponseDataDTO responseData = reportService.updateReportDetails(
                reportId, 
                updateReportRequestDTO.getType(), 
                updateReportRequestDTO.getSummary()
        );
        return ResponseEntity.ok(ApiResponse.success(responseData, "报告更新成功"));
    }
    
    /**
     * 上传患者报告
     * @param file 报告文件
     * @param patientId 患者ID
     * @param type 报告类型
     * @param summary 报告摘要
     * @return 上传的报告信息
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<PatientReportDetailDTO>> uploadReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("patientId") String patientId,
            @RequestParam("type") String type,
            @RequestParam(value = "summary", required = false) String summary) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("请选择要上传的文件"));
        }
        
        PatientReportDetailDTO report = reportService.uploadReportFileAndCreateRecord(file, patientId, type, summary);
        return ResponseEntity.ok(ApiResponse.success(report, "报告上传成功"));
    }
    
    /**
     * 下载患者报告
     * @param reportId 报告ID
     * @return 报告文件
     */
    @GetMapping("/{reportId}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable String reportId) {
        try {
            ReportDownloadDTO downloadInfo = reportService.getReportDownloadInfo(reportId);
            
            Path filePath = Paths.get(downloadInfo.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadInfo.getFileName() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("文件不存在或无法读取");
            }
        } catch (Exception e) {
            throw new RuntimeException("下载文件失败: " + e.getMessage());
        }
    }
}
