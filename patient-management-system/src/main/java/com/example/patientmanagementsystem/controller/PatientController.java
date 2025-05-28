package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.dto.PatientDTO;
import com.example.patientmanagementsystem.dto.PatientListResponseDTO;
import com.example.patientmanagementsystem.dto.PatientReportDTO;
import com.example.patientmanagementsystem.dto.UpdatePatientRequestDTO;
import com.example.patientmanagementsystem.service.PatientService;
import com.example.patientmanagementsystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 患者管理控制器
 * 处理患者相关的CRUD操作以及患者报告获取
 */
@RestController
public class PatientController {

    private final PatientService patientService;
    private final ReportService reportService;

    @Autowired
    public PatientController(PatientService patientService, ReportService reportService) {
        this.patientService = patientService;
        this.reportService = reportService;
    }

    /**
     * 获取患者列表（分页与搜索） - 管理员接口
     */
    @GetMapping("/admin/patients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getPatients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String idNumber) {
        
        PatientListResponseDTO result = patientService.getPatients(page, pageSize, name, phone, gender, idNumber);
        return ResponseEntity.ok(ApiResponse.success(result.getData(), result.getTotal(), "获取患者列表成功"));
    }

    /**
     * 搜索患者（用于下拉选择）- 管理员接口
     */
    @GetMapping("/admin/patients/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchPatients(
            @RequestParam(required = false, defaultValue = "") String query) {
        
        List<Map<String, Object>> patients = patientService.searchPatients(query);
        return ResponseEntity.ok(ApiResponse.success(patients, "搜索患者成功"));
    }

    /**
     * 更新患者信息 - 管理员接口
     */
    @PutMapping("/admin/patients/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updatePatient(
            @PathVariable String userId,
            @Valid @RequestBody UpdatePatientRequestDTO requestDTO) {
        
        patientService.updatePatient(userId, requestDTO);
        return ResponseEntity.ok(ApiResponse.success("患者信息更新成功"));
    }

    /**
     * 删除患者 - 管理员接口
     */
    @DeleteMapping("/admin/patients/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable String userId) {
        patientService.deletePatient(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.deleted("患者删除成功"));
    }

    /**
     * 获取某位患者的步态评估报告列表
     * 接口路径: /api/patient/{userId}/reports  (Singular 'patient', path param is User ID)
     */
    @GetMapping("/patient/{userId}/reports")
    @PreAuthorize("@patientSecurityService.canAccessPatientData(#userId)")
    public ResponseEntity<ApiResponse<List<PatientReportDTO>>> getPatientReports(
            @PathVariable String userId) {
        List<PatientReportDTO> reports = reportService.getReportsForPatientByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(reports, "查询成功"));
    }
}
