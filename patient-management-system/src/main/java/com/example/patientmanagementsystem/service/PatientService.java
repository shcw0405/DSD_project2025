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
import com.example.patientmanagementsystem.mapper.PatientMapper;
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
    private final PatientMapper patientMapper;

    @Autowired
    public PatientService(PatientRepository patientRepository, UserRepository userRepository, PasswordEncoder passwordEncoder,
                         DoctorPatientRelationRepository doctorPatientRelationRepository, PatientReportRepository patientReportRepository,
                         PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorPatientRelationRepository = doctorPatientRelationRepository;
        this.patientReportRepository = patientReportRepository;
        this.patientMapper = patientMapper;
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
                Patient.Gender genderEnum = Patient.Gender.fromString(genderStr);
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
        
        return patientMapper.toListResponseDTO(patientsPage);
    }

    /**
     * 搜索患者（用于下拉选择）
     * @param query 搜索关键词
     * @return 患者简略信息列表
     */
    public List<Map<String, Object>> searchPatients(String query) {
        List<Patient> patients;
        Pageable pageable = PageRequest.of(0, 50); // Limit to 50 results for dropdowns

        if (query == null || query.isEmpty()) {
            // If no query, fetch the first 50 patients
            patients = patientRepository.findAll(pageable).getContent();
        } else {
            // Search by name, phone, or ID number
            // Ensure that the query fetches associated User objects if not already eager-loaded
            // or handle patient.getUser() potentially being null if User is LAZY fetched.
            patients = patientRepository.findByNameContainingOrPhoneContainingOrIdNumberContaining(
                    query, query, query, pageable);
        }
        
        return patients.stream()
                .map(patient -> {
                    Map<String, Object> patientMap = new HashMap<>();
                    User user = patient.getUser();
                    if (user != null) {
                        patientMap.put("id", user.getId()); // Use User ID
                        patientMap.put("name", user.getName()); // Prefer User.name for consistency
                    } else {
                        // Fallback if user is null, though this ideally shouldn't happen for registered patients
                        // Or, if the search is intended to find patients not yet fully users (e.g. imported data)
                        // For consistency, we might want to filter out patients without a user or handle this case as an error.
                        // For now, providing patient table ID and name as a fallback if user is not available.
                        patientMap.put("id", patient.getId()); // Patient UUID as fallback
                        patientMap.put("name", patient.getName());
                         // It might be better to log a warning here or ensure patients always have a linked user.
                    }
                    // Optionally, add other fields needed for the search display
                    // patientMap.put("phone", patient.getPhone()); 
                    return patientMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新患者信息
     * @param userId 用户ID
     * @param dto 更新患者请求DTO
     */
    @Transactional
    public void updatePatient(String userId, UpdatePatientRequestDTO dto) {
        // Find Patient by userId
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new EntityNotFoundException("未找到与用户ID关联的患者记录, User ID: " + userId));
        
        // User entity is already linked to the patient, or can be fetched via patient.getUser() or directly by userId
        User user = userRepository.findById(userId) 
                .orElseThrow(() -> new EntityNotFoundException("未找到用户账户，用户ID: " + userId));

        boolean patientChanged = false;
        boolean userChanged = false;

        // Update Name (sync between User and Patient)
        if (dto.getName() != null && !dto.getName().isEmpty() && !dto.getName().equals(user.getName())) {
            user.setName(dto.getName());
            patient.setName(dto.getName()); // Assuming Patient.name should mirror User.name
            userChanged = true;
            patientChanged = true;
        }
        
        // Update Phone (sync between User and Patient)
        if (dto.getPhone() != null && !dto.getPhone().isEmpty() && !dto.getPhone().equals(user.getPhone())) {
            final String newPhone = dto.getPhone();
            // Check phone uniqueness against other users
            userRepository.findByPhone(newPhone).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new ResourceAlreadyExistsException("电话号码 '" + newPhone + "' 已被其他用户注册");
                }
            });
            user.setPhone(newPhone);
            patient.setPhone(newPhone); // Assuming Patient.phone should mirror User.phone
            userChanged = true;
            patientChanged = true;
        }
        
        // Update Gender (sync between User and Patient if User has gender)
        if (dto.getGender() != null && !dto.getGender().isEmpty()) {
            try {
                Patient.Gender newGender = Patient.Gender.valueOf(dto.getGender().toUpperCase()); // toUpperCase for robustness
                if (patient.getGender() == null || !newGender.equals(patient.getGender())) {
                    patient.setGender(newGender);
                    patientChanged = true;
                }
                // If User entity has a gender field compatible with Patient.Gender.name() or similar string:
                if (user.getGender() == null || !dto.getGender().equals(user.getGender())){
                     user.setGender(dto.getGender()); // Assuming User.gender is String type e.g. "男", "女"
                     userChanged = true;
                }
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的性别参数: " + dto.getGender() + ". 支持的值: " + java.util.Arrays.toString(Patient.Gender.values()));
            }
        }
        
        // Update BirthDate (sync between User and Patient if User has birthDate)
        if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
            try {
                LocalDate newBirthDate = LocalDate.parse(dto.getBirthDate(), DateTimeFormatter.ISO_DATE);
                if (patient.getBirthDate() == null || !newBirthDate.equals(patient.getBirthDate())) {
                    patient.setBirthDate(newBirthDate);
                    patientChanged = true;
                }
                // If User entity has a birthDate field compatible with this format:
                if(user.getBirthDate() == null || !dto.getBirthDate().equals(user.getBirthDate())){
                    user.setBirthDate(dto.getBirthDate()); // Assuming User.birthDate is String type and matches ISO_DATE
                    userChanged = true;
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new BusinessException("出生日期格式不正确，期望格式: YYYY-MM-DD, 实际: " + dto.getBirthDate());
            }
        }
        
        // Update ID Type (Patient only)
        if (dto.getIdType() != null && !dto.getIdType().isEmpty()) {
            try {
                Patient.IdType newIdType = Patient.IdType.valueOf(dto.getIdType().toUpperCase());
                if (patient.getIdType() == null || !newIdType.equals(patient.getIdType())) {
                    patient.setIdType(newIdType);
                    patientChanged = true;
                }
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的证件类型参数: " + dto.getIdType() + ". 支持的值: " + java.util.Arrays.toString(Patient.IdType.values()));
            }
        }
        
        // Update ID Number (Patient only, but check uniqueness)
        if (dto.getIdNumber() != null && !dto.getIdNumber().isEmpty() && !dto.getIdNumber().equals(patient.getIdNumber())) {
            final String newIdNumber = dto.getIdNumber();
            // ID Number uniqueness check against other patients
            patientRepository.findByIdNumber(newIdNumber).ifPresent(existingPatient -> {
                if (!existingPatient.getId().equals(patient.getId())) { // Compare with Patient's own UUID
                    throw new ResourceAlreadyExistsException("身份证号码 '" + newIdNumber + "' 已被其他患者注册");
                }
            });
            patient.setIdNumber(newIdNumber);
            patientChanged = true;
        }
        
        // Update Password (User only)
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            userChanged = true;
        }
        
        if (patientChanged) {
            patientRepository.save(patient);
        }
        if (userChanged) {
            userRepository.save(user);
        }
    }

    /**
     * 删除患者
     * @param userId 用户ID
     */
    @Transactional
    public void deletePatient(String userId) {
        // 1. Find Patient entity by userId to get its UUID (patientUuid)
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new EntityNotFoundException("未找到与用户ID关联的患者记录, User ID: " + userId));
        
        String patientUuid = patient.getId(); // This is the Patient table's primary key (UUID)

        // 2. Delete all doctor-patient relations for this patient (using patientUuid)
        // DoctorPatientRelationId uses patientId, which is the patient's UUID
        if (doctorPatientRelationRepository.existsById_PatientId(patientUuid)) {
             doctorPatientRelationRepository.deleteAllById_PatientId(patientUuid);
        }

        // 3. Delete all reports for this patient (using patientUuid)
        // PatientReport refers to Patient via patient.id (UUID)
        patientReportRepository.deleteAllByPatient_Id(patientUuid);
        
        // 4. Delete the Patient entity itself
        patientRepository.delete(patient);
        
        // 5. Delete the associated User entity
        // This should be done after deleting entities that have a foreign key to User, 
        // or if User has dependent entities that aren't Patient (though unlikely in this context for a patient user).
        // Cascading could also handle this, but explicit deletion is clearer here.
        userRepository.findById(userId).ifPresent(userRepository::delete);
    }
}
