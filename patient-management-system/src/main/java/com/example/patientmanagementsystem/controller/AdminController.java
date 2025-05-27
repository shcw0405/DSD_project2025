package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.dto.DoctorListResponseDTO;
import com.example.patientmanagementsystem.dto.DoctorRegistrationRequestDTO;
import com.example.patientmanagementsystem.dto.DoctorRegistrationResponseDataDTO;
import com.example.patientmanagementsystem.dto.PatientListResponseDTO;
import com.example.patientmanagementsystem.dto.RelationListResponseDTO;
import com.example.patientmanagementsystem.dto.UpdateDoctorRequestDTO;
import com.example.patientmanagementsystem.service.DoctorService;
import com.example.patientmanagementsystem.service.PatientService;
import com.example.patientmanagementsystem.service.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 医生管理控制器
 * 处理医生相关的CRUD操作
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final RelationService relationService;

    @Autowired
    public AdminController(DoctorService doctorService, PatientService patientService, RelationService relationService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.relationService = relationService;
    }

    /**
     * 获取医生列表（分页与搜索）
     * @param page 页码
     * @param pageSize 每页数量
     * @param name 医生姓名搜索关键词
     * @param phone 医生电话搜索关键词
     * @param hospital 医生所属医院搜索关键词
     * @param department 医生所属科室搜索关键词
     * @return ApiResponse 包含医生列表和总数
     */
    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<DoctorListResponseDTO>> getDoctors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String hospital,
            @RequestParam(required = false) String department) {
        
        DoctorListResponseDTO result = doctorService.getDoctors(page, pageSize, name, phone, hospital, department);
        return ResponseEntity.ok(ApiResponse.success(result, "获取医生列表成功"));
    }

    /**
     * 注册新医生
     * @param requestDTO 医生注册信息 DTO
     * @return 创建成功的医生信息
     */
    @PostMapping("/doctors")
    public ResponseEntity<ApiResponse<DoctorRegistrationResponseDataDTO>> createDoctor(
            @Valid @RequestBody DoctorRegistrationRequestDTO requestDTO) {
        
        DoctorRegistrationResponseDataDTO responseData = doctorService.createDoctor(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(responseData, "医生注册成功"));
    }

    /**
     * 更新医生信息
     * @param id 医生ID (Path Variable)
     * @param requestDTO 更新的医生信息 (Request Body)
     * @return 更新结果
     */
    @PutMapping("/doctors/{id}")
    public ResponseEntity<ApiResponse<Void>> updateDoctor(
            @PathVariable String id,
            @Valid @RequestBody UpdateDoctorRequestDTO requestDTO) {
        
        // The check for at least one field is implicitly handled: 
        // if all fields in DTO are null, service layer will do nothing.
        // If request body is empty JSON {}, DTO fields will be null.
        // Validation annotations on DTO will handle format errors if fields are provided but incorrect.

        doctorService.updateDoctor(
            id, 
            requestDTO.getName(), 
            requestDTO.getPhone(), 
            requestDTO.getHospital(), 
            requestDTO.getDepartment(), 
            requestDTO.getPassword()
        );
        return ResponseEntity.ok(ApiResponse.success("医生信息更新成功"));
    }

    /**
     * 删除医生
     * @param id 医生ID
     * @return 删除结果
     */
    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable String id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.deleted("医生删除成功"));
    }

    /**
     * 搜索医生（用于下拉选择）
     * @param query 搜索关键词
     * @return 医生简略信息列表
     */
    @GetMapping("/doctors/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchDoctors(
            @RequestParam(required = false, defaultValue = "") String query) {
        
        List<Map<String, Object>> doctors = doctorService.searchDoctors(query);
        return ResponseEntity.ok(ApiResponse.success(doctors, "搜索医生成功"));
    }
}
