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
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.DoctorPatientRelationRepository;
import com.example.patientmanagementsystem.repository.DoctorRepository;
import com.example.patientmanagementsystem.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
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

    private static final Logger logger = LoggerFactory.getLogger(RelationService.class);

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
        String requestingDoctorUserId = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String username = null;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            }

            boolean isDoctor = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_DOCTOR"));
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (isDoctor && !isAdmin && username != null) { 
                User currentUser = doctorRepository.findUserByDoctorPhone(username);
                if (currentUser != null) {
                    requestingDoctorUserId = currentUser.getId();
                }
            }
        }

        Page<DoctorPatientRelationDTO> relationsPage = relationRepository.findRelationsWithDetailsByFilters(
                doctorName, doctorPhone, patientName, patientPhone, requestingDoctorUserId, pageable);

        return new RelationListResponseDTO(relationsPage.getContent(), relationsPage.getTotalElements());
    }

    /**
     * 添加医患关系
     * @param doctorUserIdFromRequest 医生ID
     * @param patientUserIdFromRequest 患者ID
     * @return 添加的医患关系
     */
    @Transactional
    public Map<String, Object> addRelation(String doctorUserIdFromRequest, String patientUserIdFromRequest) {
        logger.info("Attempting to add relation. Requested doctorUserId: {}, patientUserId: {}", doctorUserIdFromRequest, patientUserIdFromRequest);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Validate doctor based on role
        Doctor doctor;
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String username = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : null; // This is phone number

            boolean isDoctorRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_DOCTOR"));
            boolean isAdminRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (isDoctorRole && !isAdminRole && username != null) {
                User currentDoctorUser = doctorRepository.findUserByDoctorPhone(username);
                if (currentDoctorUser == null) {
                    logger.error("Access Denied: Doctor user not found by phone: {}", username);
                    throw new AccessDeniedException("权限不足：无法验证您的医生身份。");
                }
                if (!doctorUserIdFromRequest.equals(currentDoctorUser.getId())) {
                    logger.error("Access Denied: Doctor user {} trying to add relation for a different doctor user {}", currentDoctorUser.getId(), doctorUserIdFromRequest);
                    throw new AccessDeniedException("权限不足：您只能添加与自己账户相关的医患关系。");
                }
                // If doctor is adding for themselves, doctorUserIdFromRequest is their own User ID.
                doctor = doctorRepository.findByUser_Id(doctorUserIdFromRequest)
                    .orElseThrow(() -> {
                        logger.error("Doctor entity not found for current doctor User ID: {}", doctorUserIdFromRequest);
                        return new BusinessException("请求参数错误：您的医生账户未正确关联医生记录 (User ID: " + doctorUserIdFromRequest + ")");
                    });
                 logger.info("Current doctor (User ID: {}) is adding relation for themselves.", doctorUserIdFromRequest);
            } else if (isAdminRole) {
                // Admin can specify any doctorUserId
                doctor = doctorRepository.findByUser_Id(doctorUserIdFromRequest)
                    .orElseThrow(() -> {
                        logger.error("Doctor entity not found for specified User ID (admin request): {}", doctorUserIdFromRequest);
                        return new BusinessException("请求参数错误：指定的医生用户 ID '" + doctorUserIdFromRequest + "' 不存在或未关联医生记录");
                    });
                logger.info("Admin request. Using doctor User ID for relation: {}", doctorUserIdFromRequest);
            } else {
                logger.error("Access Denied: User is not a recognized doctor or admin, or username is null.");
                throw new AccessDeniedException("权限不足：无法执行此操作。");
            }
        } else {
            logger.error("Access Denied: User not authenticated.");
            throw new AccessDeniedException("用户未认证。");
        }

        // Fetch Patient entity using patientUserIdFromRequest
        final String finalPatientUserId = patientUserIdFromRequest; // for lambda
        Patient patient = patientRepository.findByUser_Id(finalPatientUserId)
                .orElseThrow(() -> {
                     logger.error("Patient entity not found for User ID: {}", finalPatientUserId);
                    return new BusinessException("请求参数错误：患者用户 ID '" + finalPatientUserId + "' 不存在或未关联患者记录");
                });
        
        String doctorUuid = doctor.getId(); // Doctor Table UUID
        String patientUuid = patient.getId(); // Patient Table UUID

        if (relationRepository.existsByDoctorIdAndPatientId(doctorUuid, patientUuid)) {
            logger.warn("Relation already exists for doctor UUID: {} and patient UUID: {}", doctorUuid, patientUuid);
            throw new ResourceAlreadyExistsException("该医患关系已存在");
        }
        
        DoctorPatientRelation relation = new DoctorPatientRelation();
        DoctorPatientRelation.DoctorPatientRelationId relationId = new DoctorPatientRelation.DoctorPatientRelationId(doctorUuid, patientUuid);
        relation.setId(relationId);
        relation.setDoctor(doctor);
        relation.setPatient(patient);
        
        relationRepository.save(relation);
        logger.info("Successfully added relation for doctor UUID: {} and patient UUID: {}", doctorUuid, patientUuid);
        
        Map<String, Object> result = new HashMap<>();
        result.put("doctorId", doctor.getUserId()); // Return User ID of the doctor
        result.put("patientId", patient.getUserId()); // Return User ID of the patient
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
     * @param doctorUserId 医生ID
     * @param patientUserId 患者ID
     */
    @Transactional
    public void deleteRelation(String doctorUserId, String patientUserId) {
        logger.info("Attempting to delete relation for doctorUserId: {}, patientUserId: {}", doctorUserId, patientUserId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Determine the Doctor UUID to use for deletion based on authorization
        String doctorUuidToDeleteRelationFor;

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String username = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : null; // phone

            boolean isDoctorRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_DOCTOR"));
            boolean isAdminRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            Doctor doctorEntityForDeletion;
            if (isDoctorRole && !isAdminRole && username != null) {
                User currentDoctorUser = doctorRepository.findUserByDoctorPhone(username);
                if (currentDoctorUser == null) {
                    logger.error("Access Denied (deleteRelation): Doctor user not found by phone: {}", username);
                    throw new AccessDeniedException("权限不足：无法验证您的医生身份以删除关系。");
                }
                // Doctor can only delete relations linked to their own User ID
                if (!doctorUserId.equals(currentDoctorUser.getId())) {
                    logger.error("Access Denied (deleteRelation): Doctor user {} trying to delete relation for a different doctor user {}", 
                                 currentDoctorUser.getId(), doctorUserId);
                    throw new AccessDeniedException("权限不足：您只能删除与自己账户相关的医患关系。");
                }
                doctorEntityForDeletion = doctorRepository.findByUser_Id(doctorUserId)
                    .orElseThrow(() -> {
                        logger.error("Doctor entity not found for current doctor User ID (deleteRelation): {}", doctorUserId);
                        return new EntityNotFoundException("您的医生账户未正确关联医生记录 (User ID: " + doctorUserId + ")，无法删除关系");
                    });
                doctorUuidToDeleteRelationFor = doctorEntityForDeletion.getId();
                logger.info("Doctor user {} deleting relation for themselves (Doctor UUID: {}).", doctorUserId, doctorUuidToDeleteRelationFor);
            } else if (isAdminRole) {
                // Admin can specify any doctorUserId to delete a relation for
                doctorEntityForDeletion = doctorRepository.findByUser_Id(doctorUserId)
                    .orElseThrow(() -> {
                        logger.error("Doctor entity not found for specified User ID (admin deleteRelation): {}", doctorUserId);
                        return new EntityNotFoundException("指定的医生用户 ID '" + doctorUserId + "' 不存在或未关联医生记录，无法删除关系");
                    });
                doctorUuidToDeleteRelationFor = doctorEntityForDeletion.getId();
                logger.info("Admin deleting relation for doctor user {} (Doctor UUID: {}).", doctorUserId, doctorUuidToDeleteRelationFor);
            } else {
                logger.error("Access Denied (deleteRelation): User is not a recognized doctor or admin, or username is null.");
                throw new AccessDeniedException("权限不足：无法执行删除医患关系操作。");
            }
        } else {
            logger.error("Access Denied (deleteRelation): User not authenticated.");
            throw new AccessDeniedException("用户未认证，无法删除医患关系。");
        }

        // Fetch Patient entity using patientUserId to get its UUID
        final String finalPatientUserId = patientUserId; // for lambda
        Patient patient = patientRepository.findByUser_Id(finalPatientUserId)
                .orElseThrow(() -> {
                    logger.error("Patient entity for Patient User ID '" + finalPatientUserId + "' not found (deleteRelation).");
                    return new EntityNotFoundException("患者用户 ID '" + finalPatientUserId + "' 不存在或未关联患者记录，无法删除关系");
                });
        String patientUuid = patient.getId();

        // Construct the relation ID using Doctor UUID and Patient UUID
        DoctorPatientRelation.DoctorPatientRelationId relationId = new DoctorPatientRelation.DoctorPatientRelationId(doctorUuidToDeleteRelationFor, patientUuid);
        
        // Find the relation by its composite ID (Doctor UUID, Patient UUID)
        DoctorPatientRelation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> {
                    logger.warn("Relation not found for Doctor UUID {} and Patient UUID {} (deleteRelation).", doctorUuidToDeleteRelationFor, patientUuid);
                    return new EntityNotFoundException("未找到要删除的医患关系 (Doctor UserID: " + doctorUserId + ", Patient UserID: " + patientUserId + ")");
                 });
        
        relationRepository.delete(relation);
        logger.info("Successfully deleted relation for Doctor UUID {} and Patient UUID {}", doctorUuidToDeleteRelationFor, patientUuid);
    }

    @Transactional
    public Map<String, Object> replaceRelation(UpdateRelationRequestDTO dto) {
        String oldDoctorUserId = dto.getOldDoctorId();
        String oldPatientUserId = dto.getOldPatientId();
        String newDoctorUserId = dto.getNewDoctorId();
        String newPatientUserId = dto.getNewPatientId();

        logger.info("Attempting to replace relation: oldDoctorUserId={}, oldPatientUserId={}, newDoctorUserId={}, newPatientUserId={}",
                    oldDoctorUserId, oldPatientUserId, newDoctorUserId, newPatientUserId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String username = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : null; // phone

            boolean isDoctorRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_DOCTOR"));
            boolean isAdminRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (isDoctorRole && !isAdminRole && username != null) {
                User currentDoctorUser = doctorRepository.findUserByDoctorPhone(username)
                        .orElseThrow(() -> {
                            logger.error("Access Denied (replaceRelation): Doctor user not found by phone: {}", username);
                            return new AccessDeniedException("权限不足：无法验证您的医生身份以更新关系。");
                        });

                // Doctor can only update relations linked to their own User ID,
                // and cannot change the doctor in the relation to someone else.
                if (!oldDoctorUserId.equals(currentDoctorUser.getId())) {
                    logger.error("Access Denied (replaceRelation): Doctor user {} trying to update relation for a different old doctor user {}",
                                 currentDoctorUser.getId(), oldDoctorUserId);
                    throw new AccessDeniedException("权限不足：您只能更新与自己账户相关的医患关系。");
                }
                if (!newDoctorUserId.equals(currentDoctorUser.getId())) {
                    logger.error("Access Denied (replaceRelation): Doctor user {} trying to change doctor in relation to {}",
                                 currentDoctorUser.getId(), newDoctorUserId);
                    throw new AccessDeniedException("权限不足：您不能将医患关系中的医生更改为其他医生。");
                }
                logger.info("Doctor user {} is updating their own relation (Old Doctor User ID: {}, New Doctor User ID: {}).", 
                            currentDoctorUser.getId(), oldDoctorUserId, newDoctorUserId);
            } else if (!isAdminRole && !isDoctorRole) { // Neither admin nor doctor (or not authenticated properly for this check)
                 logger.error("Access Denied (replaceRelation): User is not a recognized doctor or admin, or username is null for role check.");
                 throw new AccessDeniedException("权限不足：无法执行更新医患关系操作。");
            }
             // Admins can proceed without these specific checks.
             // If user is admin, or if user is a doctor and passed the checks, continue.
        } else {
            logger.error("Access Denied (replaceRelation): User not authenticated.");
            throw new AccessDeniedException("用户未认证，无法更新医患关系。");
        }

        // 1. Find Doctor and Patient entities for the OLD relation using User IDs to get their UUIDs
        Doctor oldDoctor = doctorRepository.findByUser_Id(oldDoctorUserId)
            .orElseThrow(() -> new EntityNotFoundException("旧关系中的医生用户 ID '" + oldDoctorUserId + "' 不存在或未关联医生记录"));
        Patient oldPatient = patientRepository.findByUser_Id(oldPatientUserId)
            .orElseThrow(() -> new EntityNotFoundException("旧关系中的患者用户 ID '" + oldPatientUserId + "' 不存在或未关联患者记录"));
        
        String oldDoctorUuid = oldDoctor.getId();
        String oldPatientUuid = oldPatient.getId();

        // 2. Verify old relation exists using UUIDs
        DoctorPatientRelation.DoctorPatientRelationId oldRelationId = new DoctorPatientRelation.DoctorPatientRelationId(oldDoctorUuid, oldPatientUuid);
        relationRepository.findById(oldRelationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("要替换的旧医患关系 (Doctor UUID: %s, Patient UUID: %s) 不存在", oldDoctorUuid, oldPatientUuid)));

        // 3. Find Doctor and Patient entities for the NEW relation using User IDs
        Doctor newDoctor = doctorRepository.findByUser_Id(newDoctorUserId)
                .orElseThrow(() -> new BusinessException("请求参数错误：新医生用户 ID '" + newDoctorUserId + "' 不存在或未关联医生记录"));
        Patient newPatient = patientRepository.findByUser_Id(newPatientUserId)
                .orElseThrow(() -> new BusinessException("请求参数错误：新患者用户 ID '" + newPatientUserId + "' 不存在或未关联患者记录"));

        String newDoctorUuid = newDoctor.getId();
        String newPatientUuid = newPatient.getId();

        // 4. If old and new UUIDs are the same, no action needed.
        if (oldDoctorUuid.equals(newDoctorUuid) && oldPatientUuid.equals(newPatientUuid)) {
            logger.info("Old and new relation IDs (based on UUIDs) are identical. No change needed.");
            Map<String, Object> result = new HashMap<>();
            result.put("doctorId", newDoctor.getUserId()); // newDoctorUserId
            result.put("patientId", newPatient.getUserId()); // newPatientUserId
            result.put("doctorName", newDoctor.getName());
            result.put("patientName", newPatient.getName());
            return result; 
        }

        // 5. Check if the new relation (newDoctorUuid, newPatientUuid) already exists
        if (relationRepository.existsByDoctorIdAndPatientId(newDoctorUuid, newPatientUuid)) {
            throw new ResourceAlreadyExistsException(
                    String.format("更新后的新医患关系 (Doctor UUID: %s, Patient UUID: %s) 已存在", newDoctorUuid, newPatientUuid));
        }

        // 6. Delete the old relation (using UUIDs)
        relationRepository.deleteById(oldRelationId);
        logger.info("Deleted old relation (Doctor UUID: {}, Patient UUID: {})", oldDoctorUuid, oldPatientUuid);

        // 7. Add the new relation (using UUIDs)
        DoctorPatientRelation newRelation = new DoctorPatientRelation();
        DoctorPatientRelation.DoctorPatientRelationId newRelationIdObject = new DoctorPatientRelation.DoctorPatientRelationId(newDoctorUuid, newPatientUuid);
        newRelation.setId(newRelationIdObject);
        newRelation.setDoctor(newDoctor);
        newRelation.setPatient(newPatient);
        relationRepository.save(newRelation);
        logger.info("Added new relation (Doctor UUID: {}, Patient UUID: {})", newDoctorUuid, newPatientUuid);

        // 8. Build and return response data for the new relation using User IDs
        Map<String, Object> result = new HashMap<>();
        result.put("doctorId", newDoctor.getUserId()); // newDoctorUserId
        result.put("patientId", newPatient.getUserId()); // newPatientUserId
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
        if (patient == null) return null;
        return new PatientDTO(
                patient.getId(),
                patient.getName(),
                patient.getPhone(),
                patient.getGender() != null ? patient.getGender().name() : null,
                patient.getBirthDate(),
                patient.getIdNumber()
        );
    }
}
