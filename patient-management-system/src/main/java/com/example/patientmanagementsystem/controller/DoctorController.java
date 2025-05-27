package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.dto.PatientListResponseDTO;
import com.example.patientmanagementsystem.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctors") // 统一使用复数形式
public class DoctorController {

    private final DoctorService doctorService;

    @Autowired
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /**
     * 获取某位医生的患者列表 (分页与搜索)
     * 接口路径: /api/doctors/{doctorId}/patients
     */
    @GetMapping("/{doctorId}/patients")
    @PreAuthorize("hasRole('ADMIN') or @doctorSecurityService.isDoctorSelf(#doctorId)")
    public ResponseEntity<ApiResponse<PatientListResponseDTO>> getPatientsForDoctor(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String idNumber) {

        PatientListResponseDTO result = doctorService.getPatientsForDoctor(doctorId, page, pageSize, name, phone, gender, idNumber);
        return ResponseEntity.ok(ApiResponse.success(result, "获取医生关联患者列表成功"));
    }
}
