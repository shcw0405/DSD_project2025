package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.dto.PatientDTO;
import com.example.patientmanagementsystem.dto.PatientListResponseDTO;
import com.example.patientmanagementsystem.dto.UpdatePatientRequestDTO;
import com.example.patientmanagementsystem.exception.BusinessException;
import com.example.patientmanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.DoctorPatientRelationRepository;
import com.example.patientmanagementsystem.repository.PatientReportRepository;
import com.example.patientmanagementsystem.repository.PatientRepository;
import com.example.patientmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 患者服务
 * 处理患者相关的业务逻辑
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorPatientRelationRepository doctorPatientRelationRepository;
    private final PatientReportRepository patientReportRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository, UserRepository userRepository, PasswordEncoder passwordEncoder,
                         DoctorPatientRelationRepository doctorPatientRelationRepository, PatientReportRepository patientReportRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorPatientRelationRepository = doctorPatientRelationRepository;
        this.patientReportRepository = patientReportRepository;
    }

    /**
     * 获取患者列表（分页与搜索）
     * @param page 页码
     * @param pageSize 每页数量
     * @param name 患者姓名搜索关键词
     * @param phone 患者电话搜索关键词
     * @param genderStr 患者性别搜索关键词 (String)
     * @param idNumber 患者身份证号搜索关键词
     * @return PatientListResponseDTO 包含患者列表和总数
     */
    public PatientListResponseDTO getPatients(int page, int pageSize, String name, String phone, String genderStr, String idNumber) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        Specification<Patient> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            
            if (phone != null && !phone.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("phone"), "%" + phone + "%"));
            }
            
            if (genderStr != null && !genderStr.isEmpty()) {
                Patient.Gender genderEnum = Patient.Gender.fromString(genderStr); // Use existing fromString
                if (genderEnum != null) {
                    predicates.add(criteriaBuilder.equal(root.get("gender"), genderEnum));
                } else {
                    // Log or handle invalid gender string if necessary, current behavior is to ignore
                    // System.err.println("Invalid gender value: " + genderStr + ". Ignoring filter.");
                    // Consider adding logger.warn("Invalid gender value: {}", genderStr);
                }
            }
            
            if (idNumber != null && !idNumber.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("idNumber"), "%" + idNumber + "%"));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Patient> patientsPage = patientRepository.findAll(spec, pageable);
        
        List<PatientDTO> patientsList = patientsPage.getContent().stream()
                .map(patient -> new PatientDTO(
                        patient.getId(),
                        patient.getName(),
                        patient.getPhone(),
                        patient.getGender() != null ? patient.getGender().name() : null, // Use .name() for "男", "女"
                        patient.getBirthDate(),
                        patient.getIdNumber()
                ))
                .collect(Collectors.toList());
        
        return new PatientListResponseDTO(patientsList, patientsPage.getTotalElements());
    }

    /**
     * 搜索患者（用于下拉选择）
     * @param query 搜索关键词
     * @return 患者简略信息列表
     */
    public List<Map<String, Object>> searchPatients(String query) {
        List<Patient> patients;
        
        if (query == null || query.isEmpty()) {
            // 如果没有搜索关键词，返回前50条记录
            Pageable pageable = PageRequest.of(0, 50);
            patients = patientRepository.findAll(pageable).getContent();
        } else {
            // 如果有搜索关键词，按姓名、电话、性别、身份证号搜索
            patients = patientRepository.findByNameContainingOrPhoneContainingOrIdNumberContaining(
                    query, query, query, PageRequest.of(0, 50));
        }
        
        return patients.stream()
                .map(patient -> {
                    Map<String, Object> patientMap = new HashMap<>();
                    patientMap.put("id", patient.getId());
                    patientMap.put("name", patient.getName());
                    return patientMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新患者信息
     * @param id 患者ID
     * @param dto 更新患者请求DTO
     */
    @Transactional
    public void updatePatient(String id, UpdatePatientRequestDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到该患者，ID: " + id));
        
        User user = null;
        // Only fetch user if there are fields that need user table update, or if password is being updated
        // And if patient is actually linked to a user.
        if (patient.getUserId() != null && 
            (dto.getName() != null || dto.getPhone() != null || dto.getGender() != null || 
             dto.getBirthDate() != null || dto.getIdType() != null || dto.getIdNumber() != null || dto.getPassword() != null)) {
            user = userRepository.findById(patient.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("未找到患者关联的用户账户，用户ID: " + patient.getUserId()));
        }

        boolean patientChanged = false;
        boolean userChanged = false;

        // Update Patient entity fields
        if (dto.getName() != null && !dto.getName().equals(patient.getName())) {
            patient.setName(dto.getName());
            if (user != null) user.setName(dto.getName());
            patientChanged = true;
            if (user != null) userChanged = true;
        }
        
        if (dto.getPhone() != null && !dto.getPhone().equals(patient.getPhone())) {
            // Phone uniqueness check (against other users)
            final String newPhone = dto.getPhone();
            userRepository.findByPhone(newPhone).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(patient.getUserId())) {
                    throw new ResourceAlreadyExistsException("电话号码 '" + newPhone + "' 已被其他用户注册");
                }
            });
            // Phone format validation is expected to be handled by DTO @Pattern
            patient.setPhone(newPhone);
            if (user != null) user.setPhone(newPhone);
            patientChanged = true;
            if (user != null) userChanged = true;
        }
        
        if (dto.getGender() != null) {
            try {
                Patient.Gender newGender = Patient.Gender.valueOf(dto.getGender());
                if (!newGender.equals(patient.getGender())) {
                    patient.setGender(newGender);
                    if (user != null) user.setGender(dto.getGender()); // User.gender might be String
                    patientChanged = true;
                    if (user != null) userChanged = true;
                }
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的性别参数: " + dto.getGender());
            }
        }
        
        if (dto.getBirthDate() != null) {
            try {
                LocalDate newBirthDate = LocalDate.parse(dto.getBirthDate(), DateTimeFormatter.ISO_DATE);
                if (!newBirthDate.equals(patient.getBirthDate())) {
                    patient.setBirthDate(newBirthDate);
                    if (user != null) user.setBirthDate(dto.getBirthDate()); // User.birthDate might be String
                    patientChanged = true;
                    if (user != null) userChanged = true;
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new BusinessException("出生日期格式不正确，期望格式: YYYY-MM-DD, 实际: " + dto.getBirthDate());
            }
        }
        
        if (dto.getIdType() != null) {
            try {
                Patient.IdType newIdType = Patient.IdType.valueOf(dto.getIdType());
                if (!newIdType.equals(patient.getIdType())) {
                    patient.setIdType(newIdType);
                    if (user != null) user.setIdType(dto.getIdType()); // User.idType might be String
                    patientChanged = true;
                    if (user != null) userChanged = true;
                }
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的证件类型参数: " + dto.getIdType());
            }
        }
        
        if (dto.getIdNumber() != null && !dto.getIdNumber().equals(patient.getIdNumber())) {
            // ID Number uniqueness check (against other patients)
            final String newIdNumber = dto.getIdNumber();
            patientRepository.findByIdNumber(newIdNumber).ifPresent(existingPatient -> {
                if (!existingPatient.getId().equals(id)) {
                    throw new ResourceAlreadyExistsException("身份证号码 '" + newIdNumber + "' 已被其他患者注册");
                }
            });
            // ID Number format validation for 身份证 is expected to be handled by DTO or more specific logic if needed
            // if (patient.getIdType() == Patient.IdType.身份证 && !newIdNumber.matches("^\\d{17}[0-9X]$")) {
            //     throw new BusinessException("请求参数错误：身份证号格式不正确");
            // }
            patient.setIdNumber(newIdNumber);
            if (user != null) user.setIdNumber(newIdNumber);
            patientChanged = true;
            if (user != null) userChanged = true;
        }
        
        if (patientChanged) {
            patientRepository.save(patient);
        }
        
        // Update User entity password if provided
        if (user != null && dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            userChanged = true;
        }
        
        if (user != null && userChanged) {
            userRepository.save(user);
        }
    }

    /**
     * 删除患者
     * @param id 患者ID
     */
    @Transactional
    public void deletePatient(String id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到该患者，ID: " + id));
        
        String userId = patient.getUserId();

        // 1. Delete related DoctorPatientRelations
        doctorPatientRelationRepository.deleteAllById_PatientId(id);

        // 2. Delete related PatientReports
        patientReportRepository.deleteAllByPatient_Id(id);
        
        // 3. Delete the Patient entity
        patientRepository.delete(patient);
        
        // 4. Delete the associated User entity, if exists
        if (userId != null) {
            userRepository.findById(userId).ifPresent(userRepository::delete);
        }
    }
}
