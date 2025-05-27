package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.dto.DoctorPatientRelationDTO;
import com.example.patientmanagementsystem.dto.RelationListResponseDTO;
import com.example.patientmanagementsystem.dto.UpdateRelationRequestDTO;
import com.example.patientmanagementsystem.dto.PatientDTO;
import com.example.patientmanagementsystem.exception.BusinessException;
import com.example.patientmanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.patientmanagementsystem.model.Doctor;
import com.example.patientmanagementsystem.model.DoctorPatientRelation;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.repository.DoctorPatientRelationRepository;
import com.example.patientmanagementsystem.repository.DoctorRepository;
import com.example.patientmanagementsystem.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 医患关系服务
 * 处理医患关系相关的业务逻辑
 */
@Service
public class RelationService {

    private final DoctorPatientRelationRepository relationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public RelationService(DoctorPatientRelationRepository relationRepository,
                          DoctorRepository doctorRepository,
                          PatientRepository patientRepository) {
        this.relationRepository = relationRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
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
    public RelationListResponseDTO getRelations(int page, int pageSize, String doctorName, String doctorPhone,
                                          String patientName, String patientPhone) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // The repository method findRelationsWithDetailsByFilters returns Page<Object[]>
        // where Object[] elements are in the order: doctorId, doctorName, patientId, patientName
        Page<Object[]> relationsPage = relationRepository.findRelationsWithDetailsByFilters(
                doctorName, doctorPhone, patientName, patientPhone, pageable);

        List<DoctorPatientRelationDTO> dtoList = relationsPage.getContent().stream()
                .map(objArray -> new DoctorPatientRelationDTO(
                        (String) objArray[0], // doctorId
                        (String) objArray[1], // doctorName
                        (String) objArray[2], // patientId
                        (String) objArray[3]  // patientName
                ))
                .collect(Collectors.toList());

        return new RelationListResponseDTO(dtoList, relationsPage.getTotalElements());
    }

    /**
     * 添加医患关系
     * @param doctorId 医生ID
     * @param patientId 患者ID
     * @return 添加的医患关系
     */
    @Transactional
    public Map<String, Object> addRelation(String doctorId, String patientId) {
        // 检查医生是否存在
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException("请求参数错误：医生 ID '" + doctorId + "' 不存在"));
        
        // 检查患者是否存在
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException("请求参数错误：患者 ID '" + patientId + "' 不存在"));
        
        // 检查关系是否已存在
        if (relationRepository.existsByDoctorIdAndPatientId(doctorId, patientId)) {
            throw new ResourceAlreadyExistsException("该医患关系已存在");
        }
        
        // 创建新关系
        DoctorPatientRelation relation = new DoctorPatientRelation();
        DoctorPatientRelation.DoctorPatientRelationId relationId = new DoctorPatientRelation.DoctorPatientRelationId(doctorId, patientId);
        relation.setId(relationId);
        relation.setDoctor(doctor);
        relation.setPatient(patient);
        
        relationRepository.save(relation);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("doctorId", doctor.getId());
        result.put("patientId", patient.getId());
        result.put("doctorName", doctor.getName());
        result.put("patientName", patient.getName());
        
        return result;
    }
    
    /**
     * 更新医患关系
     * @param doctorId 医生ID
     * @param patientId 患者ID
     * @param notes 关系备注
     * @return 更新后的医患关系
     */
    @Transactional
    public Map<String, Object> updateRelation(String doctorId, String patientId, String notes) {
        // 检查关系是否存在
        DoctorPatientRelation relation = relationRepository.findByDoctorIdAndPatientId(doctorId, patientId)
                .orElseThrow(() -> new EntityNotFoundException("未找到该医患关系"));
        
        // 更新关系备注
        if (notes != null) {
            relation.setNotes(notes);
        }
        
        relationRepository.save(relation);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("doctorId", relation.getDoctor().getId());
        result.put("patientId", relation.getPatient().getId());
        result.put("doctorName", relation.getDoctor().getName());
        result.put("patientName", relation.getPatient().getName());
        result.put("notes", relation.getNotes());
        
        return result;
    }

    /**
     * 删除医患关系
     * @param doctorId 医生ID
     * @param patientId 患者ID
     */
    @Transactional
    public void deleteRelation(String doctorId, String patientId) {
        DoctorPatientRelation.DoctorPatientRelationId relationId = new DoctorPatientRelation.DoctorPatientRelationId(doctorId, patientId);
        DoctorPatientRelation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> new EntityNotFoundException("未找到该医患关系 (DoctorID: " + doctorId + ", PatientID: " + patientId + ")"));
        
        relationRepository.delete(relation);
    }

    @Transactional
    public Map<String, Object> replaceRelation(UpdateRelationRequestDTO dto) {
        String oldDoctorId = dto.getOldDoctorId();
        String oldPatientId = dto.getOldPatientId();
        String newDoctorId = dto.getNewDoctorId();
        String newPatientId = dto.getNewPatientId();

        // 1. Verify old relation exists and get doctor/patient entities
        DoctorPatientRelation.DoctorPatientRelationId oldRelationId = new DoctorPatientRelation.DoctorPatientRelationId(oldDoctorId, oldPatientId);
        relationRepository.findById(oldRelationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("要替换的旧医患关系 (DoctorID: %s, PatientID: %s) 不存在", oldDoctorId, oldPatientId)));

        // 2. Verify new doctor and patient exist
        Doctor newDoctor = doctorRepository.findById(newDoctorId)
                .orElseThrow(() -> new BusinessException("请求参数错误：新医生 ID '" + newDoctorId + "' 不存在"));
        Patient newPatient = patientRepository.findById(newPatientId)
                .orElseThrow(() -> new BusinessException("请求参数错误：新患者 ID '" + newPatientId + "' 不存在"));

        // 3. If old and new are the same, no action needed, or could return current state (but API implies change)
        if (oldDoctorId.equals(newDoctorId) && oldPatientId.equals(newPatientId)) {
            // Or throw BusinessException("新旧医患关系ID相同，无需更新")
            // For now, return the existing relation details as if it were "updated" to itself.
            Map<String, Object> result = new HashMap<>();
            result.put("doctorId", newDoctor.getId());
            result.put("patientId", newPatient.getId());
            result.put("doctorName", newDoctor.getName());
            result.put("patientName", newPatient.getName());
            return result; 
        }

        // 4. Check if the new relation (newDoctorId, newPatientId) already exists
        if (relationRepository.existsByDoctorIdAndPatientId(newDoctorId, newPatientId)) {
            throw new ResourceAlreadyExistsException(
                    String.format("更新后的新医患关系 (DoctorID: %s, PatientID: %s) 已存在", newDoctorId, newPatientId));
        }

        // 5. Delete the old relation
        // Note: The deleteRelation method uses findById, which is fine.
        // We can directly use relationRepository.deleteById for simplicity here if preferred.
        relationRepository.deleteById(oldRelationId);

        // 6. Add the new relation
        DoctorPatientRelation newRelation = new DoctorPatientRelation();
        DoctorPatientRelation.DoctorPatientRelationId newRelationIdObject = new DoctorPatientRelation.DoctorPatientRelationId(newDoctorId, newPatientId);
        newRelation.setId(newRelationIdObject);
        newRelation.setDoctor(newDoctor);
        newRelation.setPatient(newPatient);
        relationRepository.save(newRelation);

        // 7. Build and return response data for the new relation
        Map<String, Object> result = new HashMap<>();
        result.put("doctorId", newDoctor.getId());
        result.put("patientId", newPatient.getId());
        result.put("doctorName", newDoctor.getName());
        result.put("patientName", newPatient.getName());
        return result;
    }
    
    /**
     * 根据医生ID获取患者列表
     * @param doctorId 医生ID
     * @return 患者列表
     */
    public List<PatientDTO> getPatientsByDoctorId(String doctorId) {
        List<DoctorPatientRelation> relations = relationRepository.findByDoctorId(doctorId);
        return relations.stream()
                .map(DoctorPatientRelation::getPatient)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据医生ID和过滤条件获取患者列表
     * @param doctorId 医生ID
     * @param name 患者姓名搜索关键词
     * @param phone 患者电话搜索关键词
     * @param gender 患者性别
     * @param idNumber 患者身份证号搜索关键词
     * @param pageable 分页参数
     * @return 患者列表和总数
     */
    public Page<PatientDTO> getPatientsByDoctorIdAndFilters(String doctorId, String name, String phone, 
                                                        Patient.Gender gender, String idNumber, 
                                                        Pageable pageable) {
        // 实现根据医生ID和过滤条件获取患者列表的逻辑
        // 这里简化实现，实际应该使用复杂查询
        List<DoctorPatientRelation> relations = relationRepository.findByDoctorId(doctorId);
        List<Patient> patients = relations.stream()
                .map(DoctorPatientRelation::getPatient)
                .filter(patient -> {
                    boolean match = true;
                    if (name != null && !name.isEmpty()) {
                        match = match && patient.getName().contains(name);
                    }
                    if (phone != null && !phone.isEmpty()) {
                        match = match && patient.getPhone().contains(phone);
                    }
                    if (gender != null) {
                        match = match && patient.getGender() == gender;
                    }
                    if (idNumber != null && !idNumber.isEmpty()) {
                        match = match && patient.getIdNumber().contains(idNumber);
                    }
                    return match;
                })
                .collect(Collectors.toList());
        
        // 转换为DTO
        List<PatientDTO> patientDTOs = patients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 简化实现，实际应该使用数据库分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), patientDTOs.size());
        List<PatientDTO> pageContent = patientDTOs.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, patientDTOs.size());
    }
    
    /**
     * 将Patient实体转换为PatientDTO
     * @param patient 患者实体
     * @return 患者DTO
     */
    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setPhone(patient.getPhone());
        dto.setGender(patient.getGender().toString());
        dto.setBirthDate(patient.getBirthDate());
        dto.setIdNumber(patient.getIdNumber());
        return dto;
    }
}
