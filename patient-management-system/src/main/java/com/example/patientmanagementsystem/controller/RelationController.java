package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.AddRelationRequestDTO;
import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.dto.RelationListResponseDTO;
import com.example.patientmanagementsystem.dto.UpdateRelationRequestDTO;
import com.example.patientmanagementsystem.service.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 医患关系管理控制器
 * 处理医患关系相关的CRUD操作
 */
@RestController
@RequestMapping("/admin/relations")
@PreAuthorize("hasRole('ADMIN')")
public class RelationController {

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
    public ResponseEntity<ApiResponse<RelationListResponseDTO>> getRelations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String doctorPhone,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String patientPhone) {
        
        RelationListResponseDTO result = relationService.getRelations(page, pageSize, doctorName, doctorPhone, patientName, patientPhone);
        return ResponseEntity.ok(ApiResponse.success(result, "获取医患关系列表成功"));
    }

    /**
     * 添加医患关系
     * @param requestDTO 医患关系信息
     * @return 添加结果
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> addRelation(
            @Valid @RequestBody AddRelationRequestDTO requestDTO) {
        
        Map<String, Object> relation = relationService.addRelation(requestDTO.getDoctorId(), requestDTO.getPatientId());
        return ResponseEntity.status(201)
                .body(ApiResponse.created(relation, "关系添加成功"));
    }

    /**
     * 更新医患关系 (替换医生/患者)
     * @param requestDTO 包含旧ID对和新ID对的请求体
     * @return 更新结果，包含新关系的信息
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateRelation(
            @Valid @RequestBody UpdateRelationRequestDTO requestDTO) {
        
        Map<String, Object> updatedRelation = relationService.replaceRelation(requestDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedRelation, "医患关系更新成功"));
    }

    /**
     * 删除医患关系
     * @param doctorId 医生ID (Path Variable)
     * @param patientId 患者ID (Path Variable)
     * @return 删除结果
     */
    @DeleteMapping("/{doctorId}/{patientId}")
    public ResponseEntity<ApiResponse<Void>> deleteRelation(
            @PathVariable String doctorId,
            @PathVariable String patientId) {
        
        relationService.deleteRelation(doctorId, patientId);
        return ResponseEntity.ok(ApiResponse.deleted("医患关系删除成功"));
    }
}
