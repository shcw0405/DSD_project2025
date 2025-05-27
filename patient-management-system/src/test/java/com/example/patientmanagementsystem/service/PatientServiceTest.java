package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.dto.PatientDTO;
import com.example.patientmanagementsystem.dto.PatientListResponseDTO;
import com.example.patientmanagementsystem.dto.UpdatePatientRequestDTO;
import com.example.patientmanagementsystem.exception.BusinessException;
import com.example.patientmanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.model.PatientReport;
import com.example.patientmanagementsystem.repository.DoctorPatientRelationRepository;
import com.example.patientmanagementsystem.repository.PatientReportRepository;
import com.example.patientmanagementsystem.repository.PatientRepository;
import com.example.patientmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DoctorPatientRelationRepository doctorPatientRelationRepository;

    @Mock
    private PatientReportRepository patientReportRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient1;
    private User testUser1;
    private Patient testPatient2;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new User();
        testUser1.setId("user1");
        testUser1.setPhone("13900000001");
        testUser1.setName("患者甲用户");
        // ... 其他 User 属性根据需要设置

        testPatient1 = new Patient();
        testPatient1.setId("patient1");
        testPatient1.setName("患者甲");
        testPatient1.setPhone("13900000001");
        testPatient1.setGender(Patient.Gender.男);
        testPatient1.setBirthDate(LocalDate.of(1990, 1, 1));
        testPatient1.setIdNumber("110101199001010001");
        testPatient1.setUser(testUser1); // 关联 User 对象
        testPatient1.setIdType(Patient.IdType.身份证); // 补充 IdType


        testUser2 = new User();
        testUser2.setId("user2");
        testUser2.setPhone("13900000002");
        testUser2.setName("患者乙用户");
        // ... 其他 User 属性根据需要设置

        testPatient2 = new Patient();
        testPatient2.setId("patient2");
        testPatient2.setName("患者乙");
        testPatient2.setPhone("13900000002");
        testPatient2.setGender(Patient.Gender.女);
        testPatient2.setBirthDate(LocalDate.of(1985, 5, 5));
        testPatient2.setIdNumber("110101198505050002");
        testPatient2.setUser(testUser2); // 关联 User 对象
        testPatient2.setIdType(Patient.IdType.身份证); // 补充 IdType
    }

    @Test
    void testGetPatients_Success() {
        // 准备
        int page = 1;
        int pageSize = 10;
        String name = "患者";
        String phone = null;
        String genderStr = "男";
        String idNumber = null;

        List<Patient> patients = Arrays.asList(testPatient1); // 假设根据条件只匹配到 testPatient1
        Page<Patient> patientPage = new PageImpl<>(patients, PageRequest.of(page - 1, pageSize), patients.size());

        when(patientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(patientPage);

        // 执行
        PatientListResponseDTO result = patientService.getPatients(page, pageSize, name, phone, genderStr, idNumber);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(patients.size(), result.getTotal());

        PatientDTO firstPatientDTO = result.getData().get(0);
        assertEquals(testPatient1.getId(), firstPatientDTO.getId());
        assertEquals(testPatient1.getName(), firstPatientDTO.getName());
        assertEquals(testPatient1.getPhone(), firstPatientDTO.getPhone());
        assertEquals(testPatient1.getGender().toString(), firstPatientDTO.getGender());
        assertEquals(testPatient1.getBirthDate(), firstPatientDTO.getBirthDate());
        assertEquals(testPatient1.getIdNumber(), firstPatientDTO.getIdNumber());

        verify(patientRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetPatients_EmptyResult() {
        // 准备
        int page = 1;
        int pageSize = 10;
        String name = "不存在的患者";
        String phone = null;
        String genderStr = null;
        String idNumber = null;

        Page<Patient> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, pageSize), 0);

        when(patientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        // 执行
        PatientListResponseDTO result = patientService.getPatients(page, pageSize, name, phone, genderStr, idNumber);

        // 验证
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotal());

        verify(patientRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testSearchPatients_WithQuery() {
        // 准备
        String query = "甲";
        List<Patient> patients = Arrays.asList(testPatient1);
        Pageable pageable = PageRequest.of(0, 50);

        when(patientRepository.findByNameContainingOrPhoneContainingOrIdNumberContaining(query, query, query, pageable))
                .thenReturn(patients);

        // 执行
        List<Map<String, Object>> result = patientService.searchPatients(query);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPatient1.getId(), result.get(0).get("id"));
        assertEquals(testPatient1.getName(), result.get(0).get("name"));

        verify(patientRepository).findByNameContainingOrPhoneContainingOrIdNumberContaining(query, query, query, pageable);
    }

    @Test
    void testSearchPatients_EmptyQuery() {
        // 准备
        String query = ""; // 或者 null
        List<Patient> patients = Arrays.asList(testPatient1, testPatient2);
        Page<Patient> patientPage = new PageImpl<>(patients, PageRequest.of(0, 50), patients.size());
        Pageable pageable = PageRequest.of(0, 50);

        when(patientRepository.findAll(pageable)).thenReturn(patientPage);

        // 执行
        List<Map<String, Object>> result = patientService.searchPatients(query);

        // 验证
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testPatient1.getId(), result.get(0).get("id"));
        assertEquals(testPatient1.getName(), result.get(0).get("name"));
        assertEquals(testPatient2.getId(), result.get(1).get("id"));
        assertEquals(testPatient2.getName(), result.get(1).get("name"));

        verify(patientRepository).findAll(pageable);
    }

    @Test
    void testSearchPatients_NoResults() {
        // 准备
        String query = "不存在的患者";
        Pageable pageable = PageRequest.of(0, 50);

        when(patientRepository.findByNameContainingOrPhoneContainingOrIdNumberContaining(query, query, query, pageable))
                .thenReturn(Collections.emptyList());

        // 执行
        List<Map<String, Object>> result = patientService.searchPatients(query);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(patientRepository).findByNameContainingOrPhoneContainingOrIdNumberContaining(query, query, query, pageable);
    }

    @Test
    void testDeletePatient_Success() {
        // 准备
        String patientId = testPatient1.getId();
        String userId = testPatient1.getUserId(); // 从 testPatient1 获取 userId

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient1));
        // 假设 PatientService 的 deletePatient 会先检查并删除关联报告和关系
        when(patientReportRepository.findByPatient_IdOrderByDateDesc(patientId)).thenReturn(Collections.emptyList()); // 模拟没有关联报告
        when(doctorPatientRelationRepository.existsById_PatientId(patientId)).thenReturn(false); // 使用新方法，模拟没有关联医患关系

        // 执行
        assertDoesNotThrow(() -> patientService.deletePatient(patientId));

        // 验证
        verify(patientRepository).findById(patientId);
        verify(patientReportRepository).deleteAllByPatient_Id(patientId); // 验证报告被删除
        verify(doctorPatientRelationRepository).deleteAllById_PatientId(patientId); // 使用正确的方法名
        verify(patientRepository).delete(testPatient1);
        if (userId != null) { 
            verify(userRepository).deleteById(userId);
        }
    }

    @Test
    void testDeletePatient_NotFound() {
        // 准备
        String patientId = "nonExistentId";
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // 执行和验证
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            patientService.deletePatient(patientId);
        });
        assertTrue(exception.getMessage().contains("未找到该患者"));

        verify(patientRepository).findById(patientId);
        verify(patientRepository, never()).delete(any(Patient.class));
        verify(userRepository, never()).deleteById(anyString());
        // verify(patientReportRepository, never()).deleteAllByPatientId(anyString());
        // verify(doctorPatientRelationRepository, never()).deleteAllByPatientId(anyString());
    }

    @Test
    void testDeletePatient_WithActiveRelations() {
        // 准备
        String patientId = testPatient1.getId();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient1));
        // 模拟患者仍有关联的报告
        PatientReport mockReport = new PatientReport(); // 创建一个模拟报告对象
        when(patientReportRepository.findByPatient_IdOrderByDateDesc(patientId)).thenReturn(Arrays.asList(mockReport));
        // 假设此时不检查医患关系，或者医患关系不存在 (如果业务逻辑是先检查报告，再检查关系)
        // 如果需要同时检查医患关系，可以取消下面的注释并调整mock
        // when(doctorPatientRelationRepository.existsById_PatientId(patientId)).thenReturn(false); 

        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            patientService.deletePatient(patientId);
        });
        assertTrue(exception.getMessage().contains("患者尚有关联的报告或医患关系，无法删除"));

        verify(patientRepository).findById(patientId);
        verify(patientReportRepository).findByPatient_IdOrderByDateDesc(patientId);
        // verify(doctorPatientRelationRepository.existsById_PatientId(patientId)); // 如果业务逻辑也检查这个
        verify(patientRepository, never()).delete(any(Patient.class));
        verify(userRepository, never()).deleteById(anyString());
        verify(patientReportRepository, never()).deleteAllByPatient_Id(anyString()); // 不应该删除报告
        verify(doctorPatientRelationRepository, never()).deleteAllById_PatientId(anyString()); // 不应该删除关系
    }
} 