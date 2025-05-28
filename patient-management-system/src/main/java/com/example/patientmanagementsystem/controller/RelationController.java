package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.AddRelationRequestDTO;
import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.dto.DoctorPatientRelationDTO;
import com.example.patientmanagementsystem.dto.RelationListResponseDTO;
import com.example.patientmanagementsystem.dto.UpdateRelationRequestDTO;
import com.example.patientmanagementsystem.service.RelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 医患关系管理控制器
 * 处理医患关系相关的CRUD操作
 */
@RestController
@RequestMapping("/admin/relations")
public class RelationController {

    private static final Logger logger = LoggerFactory.getLogger(RelationController.class);
    private final RelationService relationService;

    @Autowired
    public RelationController(RelationService relationService) {
        this.relationService = relationService;
    }

    /**
     * 获取医患关系列表（分页与搜索）
     * @param page 页码
     * @param pageSize 每页数量
     * @param doctorName 医生姓名搜索关键词
     * @param doctorPhone 医生电话搜索关键词
     * @param patientName 患者姓名搜索关键词
     * @param patientPhone 患者电话搜索关键词
     * @return 医患关系列表和总数
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<List<DoctorPatientRelationDTO>>> getRelations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String doctorPhone,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String patientPhone) {
        logger.info("GET /admin/relations called with page: {}, pageSize: {}, doctorName: {}, doctorPhone: {}, patientName: {}, patientPhone: {}",
                page, pageSize, doctorName, doctorPhone, patientName, patientPhone);
        try {
            RelationListResponseDTO result = relationService.getRelations(page, pageSize, doctorName, doctorPhone, patientName, patientPhone);
            return ResponseEntity.ok(ApiResponse.success(result.getData(), result.getTotal(), "获取医患关系列表成功"));
        } catch (Exception e) {
            logger.error("Error in getRelations: ", e);
            throw e; // Re-throw to be handled by GlobalExceptionHandler or Spring
        }
    }

    /**
     * 添加医患关系
     * @param requestDTO 医患关系信息
     * @return 添加结果
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addRelation(@Valid @RequestBody AddRelationRequestDTO requestDTO) {
        logger.info("POST /admin/relations invoked. Request DTO: {}", requestDTO);
        try {
            logger.debug("Attempting to call relationService.addRelation");
            Map<String, Object> newRelation = relationService.addRelation(requestDTO.getDoctorId(), requestDTO.getPatientId());
            logger.info("Successfully added relation: {}", newRelation);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(newRelation, "关系添加成功"));
        } catch (Exception e) {
            logger.error("Exception in RelationController.addRelation: doctorId={}, patientId={}. Error: {}", 
                         requestDTO.getDoctorId(), requestDTO.getPatientId(), e.getMessage(), e);
            // Re-throw the exception to be handled by GlobalExceptionHandler
            // This ensures that the response format is consistent and handled by GlobalExceptionHandler
            throw e;
        }
    }

    /**
     * 更新医患关系 (替换医生/患者)
     * @param requestDTO 包含旧ID对和新ID对的请求体
     * @return 更新结果，包含新关系的信息
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateRelation(@Valid @RequestBody UpdateRelationRequestDTO requestDTO) {
        logger.info("PUT /admin/relations invoked with DTO: {}", requestDTO);
        try {
            Map<String, Object> updatedRelation = relationService.replaceRelation(requestDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedRelation, "医患关系更新成功"));
        } catch (Exception e) {
            logger.error("Error in updateRelation: ", e);
            throw e;
        }
    }

    /**
     * 删除医患关系
     * @param doctorId 医生ID (Path Variable)
     * @param patientId 患者ID (Path Variable)
     * @return 删除结果
     */
    @DeleteMapping("/{doctorId}/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRelation(
            @PathVariable String doctorId,
            @PathVariable String patientId) {
        
        relationService.deleteRelation(doctorId, patientId);
        return ResponseEntity.ok(ApiResponse.deleted("医患关系删除成功"));
    }
}
