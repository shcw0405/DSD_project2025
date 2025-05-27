package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.dto.DoctorDTO;
import com.example.patientmanagementsystem.dto.DoctorListResponseDTO;
import com.example.patientmanagementsystem.dto.DoctorRegistrationRequestDTO;
import com.example.patientmanagementsystem.dto.DoctorRegistrationResponseDataDTO;
import com.example.patientmanagementsystem.dto.PatientListResponseDTO;
import com.example.patientmanagementsystem.exception.BusinessException;
import com.example.patientmanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.patientmanagementsystem.mapper.PatientMapper;
import com.example.patientmanagementsystem.model.Doctor;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.DoctorPatientRelationRepository;
import com.example.patientmanagementsystem.repository.DoctorRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 医生服务
 * 处理医生相关的业务逻辑
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorPatientRelationRepository doctorPatientRelationRepository;
    private final PatientMapper patientMapper;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, DoctorPatientRelationRepository doctorPatientRelationRepository, PatientMapper patientMapper) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorPatientRelationRepository = doctorPatientRelationRepository;
        this.patientMapper = patientMapper;
    }

    /**
     * 获取医生列表（分页与搜索）
     * @param page 页码
     * @param pageSize 每页数量
     * @param name 医生姓名搜索关键词
     * @param phone 医生电话搜索关键词
     * @param hospital 医生所属医院搜索关键词
     * @param department 医生所属科室搜索关键词
     * @return DoctorListResponseDTO 包含医生列表和总数
     */
    public DoctorListResponseDTO getDoctors(int page, int pageSize, String name, String phone, String hospital, String department) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        Specification<Doctor> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (phone != null && !phone.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("phone"), "%" + phone + "%"));
            }
            if (hospital != null && !hospital.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("hospital"), "%" + hospital + "%"));
            }
            if (department != null && !department.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("department"), "%" + department + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Doctor> doctorsPage = doctorRepository.findAll(spec, pageable);
        
        List<DoctorDTO> doctorsList = doctorsPage.getContent().stream()
                .map(doctor -> new DoctorDTO(
                        doctor.getId(), 
                        doctor.getName(), 
                        doctor.getPhone(), 
                        doctor.getHospital(), 
                        doctor.getDepartment()
                ))
                .collect(Collectors.toList());
        
        return new DoctorListResponseDTO(doctorsList, doctorsPage.getTotalElements());
    }

    /**
     * 创建医生 (使用 DTO)
     * @param requestDTO 包含医生注册信息的 DTO
     * @return 创建成功的医生信息 (DoctorRegistrationResponseDataDTO)
     */
    @Transactional
    public DoctorRegistrationResponseDataDTO createDoctor(DoctorRegistrationRequestDTO requestDTO) {
        // 检查手机号是否已存在
        if (userRepository.existsByPhone(requestDTO.getPhone())) {
            // 根据API文档，提示信息包含姓名和手机号
            throw new ResourceAlreadyExistsException("手机号或姓名已被注册"); 
        }
        
        // 手机号格式校验已在DTO的@Pattern中处理，此处可移除或保留作为双重保险
        // if (!requestDTO.getPhone().matches("^1[3-9]\\d{9}$")) {
        //     throw new BusinessException("请求参数错误：电话号码格式不正确");
        // }
        
        // 创建用户
        User user = new User();
        user.setName(requestDTO.getName());
        user.setPhone(requestDTO.getPhone());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setAdmin(false);
        user.setDoctor(true); // 标记为医生
        user.setPatient(false);
        // 对于医生，通常也需要设置基础的用户信息如性别、出生日期等，但DoctorRegistrationRequestDTO没有这些字段
        // 如果需要，可以从默认值或额外参数设置，或修改DTO
        
        User savedUser = userRepository.save(user);
        
        // 创建医生
        Doctor doctor = new Doctor();
        doctor.setUserId(savedUser.getId()); // 关联 User ID
        doctor.setName(requestDTO.getName()); // Doctor 表也存了 name 和 phone
        doctor.setPhone(requestDTO.getPhone());
        doctor.setHospital(requestDTO.getHospital());
        doctor.setDepartment(requestDTO.getDepartment());
        
        /* Doctor savedDoctor = */ doctorRepository.save(doctor); // 保存医生实体，其ID可能不需要直接返回
        
        // 构建返回DTO
        return new DoctorRegistrationResponseDataDTO(
            savedUser.getId(), // 返回 User 的 ID 作为账户 ID
            savedUser.getName(),
            savedUser.getPhone(),
            requestDTO.getHospital(),
            requestDTO.getDepartment()
        );
    }

    /**
     * 更新医生信息
     * @param doctorId 医生的ID (来自 Doctor 表)
     * @param name 新姓名
     * @param phone 新电话
     * @param hospital 新医院
     * @param department 新科室
     * @param password 新密码 (明文)
     */
    @Transactional
    public void updateDoctor(String doctorId, String name, String phone, String hospital, String department, String password) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("未找到该医生，ID: " + doctorId));
        
        User user = userRepository.findById(doctor.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("未找到关联用户，用户ID: " + doctor.getUserId()));

        boolean doctorChanged = false;
        boolean userChanged = false;

        if (name != null && !name.isEmpty() && !name.equals(user.getName())) {
            doctor.setName(name);
            user.setName(name);  
            doctorChanged = true;
            userChanged = true;
        }

        if (phone != null && !phone.isEmpty() && !phone.equals(user.getPhone())) {
            // 检查新电话号码是否已被其他用户占用
            if (userRepository.existsByPhone(phone)) {
                User existingUserWithPhone = userRepository.findByPhone(phone).orElse(null);
                if (existingUserWithPhone != null && !existingUserWithPhone.getId().equals(user.getId())) {
                    throw new ResourceAlreadyExistsException("电话号码 '" + phone + "' 已被其他用户注册");
                }
            }
            doctor.setPhone(phone);
            user.setPhone(phone);   
            doctorChanged = true;
            userChanged = true;
        }

        if (hospital != null && !hospital.isEmpty() && !hospital.equals(doctor.getHospital())) {
            doctor.setHospital(hospital);
            doctorChanged = true;
        }

        if (department != null && !department.isEmpty() && !department.equals(doctor.getDepartment())) {
            doctor.setDepartment(department);
            doctorChanged = true;
        }

        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
            userChanged = true;
        }

        if (doctorChanged) {
            doctorRepository.save(doctor);
        }
        if (userChanged) {
            userRepository.save(user);
        }
    }

    /**
     * 删除医生
     * @param id 医生ID
     */
    @Transactional
    public void deleteDoctor(String id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到该医生，ID: " + id));
        
        String userId = doctor.getUserId();

        // 1. Delete doctor-patient relations
        doctorPatientRelationRepository.deleteAllById_DoctorId(id);
        
        // 2. Delete the doctor
        doctorRepository.delete(doctor);
        
        // 3. Delete the associated user
        userRepository.findById(userId).ifPresent(userRepository::delete);
    }

    /**
     * 搜索医生（用于下拉选择）
     * @param query 搜索关键词
     * @return 医生简略信息列表
     */
    public List<Map<String, Object>> searchDoctors(String query) {
        List<Doctor> doctors;
        
        if (query == null || query.isEmpty()) {
            // 如果没有搜索关键词，返回前50条记录
            Pageable pageable = PageRequest.of(0, 50);
            doctors = doctorRepository.findAll(pageable).getContent();
        } else {
            // 如果有搜索关键词，按姓名、电话、医院、科室搜索
            doctors = doctorRepository.findByNameContainingOrPhoneContainingOrHospitalContainingOrDepartmentContaining(
                    query, query, query, query, PageRequest.of(0, 50));
        }
        
        return doctors.stream()
                .map(doctor -> {
                    Map<String, Object> doctorMap = new HashMap<>();
                    doctorMap.put("id", doctor.getId());
                    doctorMap.put("name", doctor.getName());
                    return doctorMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取某位医生的患者列表 (分页与搜索)
     * @param doctorId 医生ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param name 患者姓名
     * @param phone 患者电话
     * @param gender 患者性别 (字符串: "男", "女", "" or null)
     * @param idNumber 患者身份证号
     * @return PatientListResponseDTO 包含患者列表和总数
     */
    public PatientListResponseDTO getPatientsForDoctor(String doctorId, int page, int pageSize, String name, String phone, String gender, String idNumber) {
        // 1. 校验医生是否存在
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("未找到指定的医生, ID: " + doctorId));

        // 2. 转换 gender 字符串为 Patient.Gender 枚举
        Patient.Gender genderEnum = Patient.Gender.fromString(gender);

        // 3. 构建分页请求 (JPA Page页码从0开始)
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 4. 调用 DoctorPatientRelationRepository 中的方法获取患者分页数据
        Page<Patient> patientPage = doctorPatientRelationRepository.findPatientsByDoctorIdAndFilters(
                doctorId, name, phone, genderEnum, idNumber, pageable
        );

        // 5. 使用 PatientMapper 转换为 DTO
        return patientMapper.toListResponseDTO(patientPage);
    }
}
