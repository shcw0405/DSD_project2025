package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.dto.LoginDTO;
import com.example.patientmanagementsystem.dto.RegisterDTO;
import com.example.patientmanagementsystem.dto.UserDTO;
import com.example.patientmanagementsystem.dto.LoginResponseDTO;
import com.example.patientmanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.DoctorRepository;
import com.example.patientmanagementsystem.repository.PatientRepository;
import com.example.patientmanagementsystem.repository.UserRepository;
import com.example.patientmanagementsystem.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Authentication authentication;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // 设置测试用户
        testUser = new User();
        testUser.setId("1");
        testUser.setPhone("13800000001");
        testUser.setPassword("encodedPassword");
        testUser.setDoctor(true);
        testUser.setPatient(false);
        testUser.setAdmin(false);
        testUser.setName("测试用户");
        testUser.setGender("男");
        testUser.setBirthDate("1990-01-01");
        testUser.setIdType("身份证");
        testUser.setIdNumber("110101199001011234");

        // 设置认证对象
        userDetails = new org.springframework.security.core.userdetails.User(
                testUser.getPhone(),
                testUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );
        authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void testLogin_Success() {
        // 准备
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("13800000001");
        loginDTO.setPassword("password");
        String token = "jwt.token.string";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn(token);
        when(userRepository.findByPhone(loginDTO.getUsername())).thenReturn(Optional.of(testUser));

        // 执行
        LoginResponseDTO result = authService.login(loginDTO);

        // 验证
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.isDoctor(), result.isDoctor());
        assertEquals(testUser.isPatient(), result.isPatient());
        assertEquals(testUser.isAdmin(), result.isAdmin());
        assertEquals(token, result.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
        verify(userRepository).findByPhone(loginDTO.getUsername());
    }

    @Test
    void testRegister_Success() {
        // 准备
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setName("新用户");
        registerDTO.setPhone("13800000002");
        registerDTO.setGender("男");
        registerDTO.setBirthDate("1990-01-01");
        registerDTO.setIdType("身份证");
        registerDTO.setIdNumber("110101199001011234");
        registerDTO.setPassword("password");
        
        User newUser = new User();
        newUser.setId("2");
        newUser.setName(registerDTO.getName());
        newUser.setPhone(registerDTO.getPhone());
        newUser.setGender(registerDTO.getGender());
        newUser.setBirthDate(registerDTO.getBirthDate());
        newUser.setIdType(registerDTO.getIdType());
        newUser.setIdNumber(registerDTO.getIdNumber());
        newUser.setPassword("encodedPassword");
        newUser.setAdmin(false);
        newUser.setDoctor(false);
        newUser.setPatient(true);

        when(userRepository.existsByPhone(registerDTO.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // 执行
        UserDTO result = authService.register(registerDTO);

        // 验证
        assertNotNull(result);
        assertEquals(newUser.getId(), result.getId());
        assertEquals(newUser.getName(), result.getName());
        assertEquals(newUser.getPhone(), result.getPhone());
        assertEquals(newUser.getGender(), result.getGender());
        assertEquals(newUser.getBirthDate(), result.getBirthDate());
        assertEquals(newUser.getIdType(), result.getIdType());
        assertEquals(newUser.getIdNumber(), result.getIdNumber());
        verify(userRepository).existsByPhone(registerDTO.getPhone());
        verify(passwordEncoder).encode(registerDTO.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_PhoneAlreadyExists() {
        // 准备
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setName("新用户");
        registerDTO.setPhone("13800000001");
        registerDTO.setGender("男");
        registerDTO.setBirthDate("1990-01-01");
        registerDTO.setIdType("身份证");
        registerDTO.setIdNumber("110101199001011234");
        registerDTO.setPassword("password");

        when(userRepository.existsByPhone(registerDTO.getPhone())).thenReturn(true);

        // 执行和验证
        Exception exception = assertThrows(ResourceAlreadyExistsException.class, () -> {
            authService.register(registerDTO);
        });

        assertEquals("该手机号已被注册", exception.getMessage());
        verify(userRepository).existsByPhone(registerDTO.getPhone());
        verify(userRepository, never()).save(any(User.class));
    }
}
