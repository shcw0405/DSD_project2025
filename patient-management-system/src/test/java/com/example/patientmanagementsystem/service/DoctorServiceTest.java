package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.dto.DoctorDTO;
import com.example.patientmanagementsystem.dto.DoctorListResponseDTO;
import com.example.patientmanagementsystem.dto.DoctorRegistrationRequestDTO;
import com.example.patientmanagementsystem.dto.DoctorRegistrationResponseDataDTO;
import com.example.patientmanagementsystem.model.Doctor;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.DoctorRepository;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor testDoctor;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 设置测试用户
        testUser = new User();
        testUser.setId("user1");
        testUser.setPhone("13800000001");
        testUser.setPassword("encodedPassword");
        testUser.setDoctor(true);

        // 设置测试医生
        testDoctor = new Doctor();
        testDoctor.setId("doctor1");
        testDoctor.setName("张医生");
        testDoctor.setPhone("13800000001");
        testDoctor.setHospital("北京协和医院");
        testDoctor.setDepartment("神经内科");
        testDoctor.setUserId(testUser.getId());
    }

    @Test
    void testGetDoctors() {
        // 准备
        int page = 1;
        int pageSize = 10;
        String name = "张";
        String phone = null;
        String hospital = null;
        String department = null;

        Doctor doctor2 = new Doctor();
        doctor2.setId("d2");
        doctor2.setName("李医生");
        doctor2.setPhone("13800000002");
        doctor2.setHospital("北京协和医院");
        doctor2.setDepartment("骨科");
        doctor2.setUserId(testUser.getId());

        List<Doctor> doctors = Arrays.asList(testDoctor, doctor2);
        Page<Doctor> doctorPage = new PageImpl<>(doctors, PageRequest.of(page - 1, pageSize), doctors.size());
        
        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(doctorPage);

        // 执行
        DoctorListResponseDTO result = doctorService.getDoctors(page, pageSize, name, phone, hospital, department);

        // 验证
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals(doctors.size(), result.getTotal());

        DoctorDTO firstDoctorDTO = result.getData().get(0);
        assertEquals(testDoctor.getId(), firstDoctorDTO.getId());
        assertEquals(testDoctor.getName(), firstDoctorDTO.getName());
        assertEquals(testDoctor.getPhone(), firstDoctorDTO.getPhone());
        assertEquals(testDoctor.getHospital(), firstDoctorDTO.getHospital());
        assertEquals(testDoctor.getDepartment(), firstDoctorDTO.getDepartment());

        verify(doctorRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testCreateDoctor_Success() {
        // 准备
        DoctorRegistrationRequestDTO requestDTO = new DoctorRegistrationRequestDTO();
        requestDTO.setPassword("password");
        requestDTO.setName("王医生");
        requestDTO.setPhone("13800000003");
        requestDTO.setHospital("北京协和医院");
        requestDTO.setDepartment("内科");

        String encodedPassword = "encodedPasswordForNewDoctor";
        User newUser = new User();
        newUser.setId("userNewId");
        newUser.setName(requestDTO.getName());
        newUser.setPhone(requestDTO.getPhone());
        newUser.setPassword(encodedPassword);
        newUser.setDoctor(true);

        Doctor newDoctorEntity = new Doctor();
        newDoctorEntity.setUserId(newUser.getId());

        when(userRepository.existsByPhone(requestDTO.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(requestDTO.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(newDoctorEntity);

        // 执行
        DoctorRegistrationResponseDataDTO result = doctorService.createDoctor(requestDTO);

        // 验证
        assertNotNull(result);
        assertEquals(newUser.getId(), result.getId());
        assertEquals(requestDTO.getName(), result.getName());
        assertEquals(requestDTO.getPhone(), result.getPhone());
        assertEquals(requestDTO.getHospital(), result.getHospital());
        assertEquals(requestDTO.getDepartment(), result.getDepartment());
        verify(userRepository).existsByPhone(requestDTO.getPhone());
        verify(passwordEncoder).encode(requestDTO.getPassword());
        verify(userRepository).save(any(User.class));
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void testCreateDoctor_PhoneAlreadyExists() {
        // 准备
        DoctorRegistrationRequestDTO requestDTO = new DoctorRegistrationRequestDTO();
        requestDTO.setPassword("password");
        requestDTO.setName("王医生");
        requestDTO.setPhone("13800000001");
        requestDTO.setHospital("北京协和医院");
        requestDTO.setDepartment("内科");

        when(userRepository.existsByPhone(requestDTO.getPhone())).thenReturn(true);

        // 执行和验证
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorService.createDoctor(requestDTO);
        });

        assertEquals("手机号或姓名已被注册", exception.getMessage());
        verify(userRepository).existsByPhone(requestDTO.getPhone());
        verify(userRepository, never()).save(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void testUpdateDoctor_Success() {
        // 准备
        String id = "d1";
        String name = "张医生更新";
        String phone = "13800000001";
        String hospital = "北京协和医院更新";
        String department = "神经外科";
        String password = "newpassword";

        when(doctorRepository.findById(id)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(testDoctor.getUserId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(password)).thenReturn("encodedNewPassword");

        // 执行
        doctorService.updateDoctor(id, name, phone, hospital, department, password);

        // 验证
        verify(doctorRepository).findById(id);
        verify(doctorRepository).save(any(Doctor.class));
        verify(userRepository).findById(testDoctor.getUserId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateDoctor_NotFound() {
        // 准备
        String id = "nonexistent";
        String name = "张医生更新";
        String phone = "13800000001";
        String hospital = "北京协和医院更新";
        String department = "神经外科";
        String password = "newpassword";

        when(doctorRepository.findById(id)).thenReturn(Optional.empty());

        // 执行和验证
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            doctorService.updateDoctor(id, name, phone, hospital, department, password);
        });

        assertEquals("未找到该医生", exception.getMessage());
        verify(doctorRepository).findById(id);
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void testDeleteDoctor_Success() {
        // 准备
        String id = "d1";
        when(doctorRepository.findById(id)).thenReturn(Optional.of(testDoctor));

        // 执行
        doctorService.deleteDoctor(id);

        // 验证
        verify(doctorRepository).findById(id);
        verify(userRepository).deleteById(testDoctor.getUserId());
        verify(doctorRepository).delete(testDoctor);
    }

    @Test
    void testDeleteDoctor_NotFound() {
        // 准备
        String id = "nonexistent";
        when(doctorRepository.findById(id)).thenReturn(Optional.empty());

        // 执行和验证
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            doctorService.deleteDoctor(id);
        });

        assertEquals("未找到该医生", exception.getMessage());
        verify(doctorRepository).findById(id);
        verify(userRepository, never()).deleteById(any(String.class));
        verify(doctorRepository, never()).delete(any(Doctor.class));
    }

    @Test
    void testSearchDoctors() {
        // 准备
        String query = "张";
        List<Doctor> doctors = Arrays.asList(testDoctor);
        
        when(doctorRepository.findByNameContainingOrPhoneContainingOrHospitalContainingOrDepartmentContaining(
                eq(query), eq(query), eq(query), eq(query), any(Pageable.class))).thenReturn(doctors);

        // 执行
        List<Map<String, Object>> result = doctorService.searchDoctors(query);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDoctor.getId(), result.get(0).get("id"));
        assertEquals(testDoctor.getName(), result.get(0).get("name"));
        
        verify(doctorRepository).findByNameContainingOrPhoneContainingOrHospitalContainingOrDepartmentContaining(
                eq(query), eq(query), eq(query), eq(query), any(Pageable.class));
    }
}
