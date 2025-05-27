package com.example.patientmanagementsystem.service;

import com.example.patientmanagementsystem.dto.LoginDTO;
import com.example.patientmanagementsystem.dto.RegisterDTO;
import com.example.patientmanagementsystem.dto.UserDTO;
import com.example.patientmanagementsystem.dto.LoginResponseDTO;
import com.example.patientmanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.patientmanagementsystem.exception.BusinessException;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.model.Patient;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private static final String ADMIN_PHONE_CONST = "13000000000";
    private static final String ADMIN_PASSWORD_CONST = "123456";

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
    private User adminUserInstance;

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

        adminUserInstance = new User();
        adminUserInstance.setId("adminUserId");
        adminUserInstance.setName("管理员");
        adminUserInstance.setPhone(ADMIN_PHONE_CONST);
        adminUserInstance.setPassword("encodedAdminPassword"); 
        adminUserInstance.setAdmin(true);
        adminUserInstance.setDoctor(false);
        adminUserInstance.setPatient(false);
    }

    @Test
    void testLogin_Success() {
        // 准备
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password");

        User testUser = new User();
        testUser.setId("userId");
        testUser.setName("Test User");
        testUser.setPhone(loginDTO.getUsername());
        testUser.setAdmin(false);
        testUser.setDoctor(false);
        testUser.setPatient(true);

        Authentication authentication = mock(Authentication.class);
        // Simulating successful authentication by AuthenticationManager
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())))
                .thenReturn(authentication);
        
        when(userRepository.findByPhone(loginDTO.getUsername())).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(authentication)).thenReturn("test-jwt-token");

        // 执行
        LoginResponseDTO response = authService.login(loginDTO);

        // 验证
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals("test-jwt-token", response.getToken());
        assertFalse(response.isAdmin());
        assertFalse(response.isDoctor());
        assertTrue(response.isPatient());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByPhone(loginDTO.getUsername());
        verify(tokenProvider).generateToken(authentication);
    }

    @Test
    void testLogin_BadCredentials() {
        // 准备
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpassword");

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())))
                .thenThrow(new BadCredentialsException("用户名或密码错误"));

        // 执行和验证
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginDTO);
        });
        assertEquals("用户名或密码错误", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByPhone(anyString());
        verify(tokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testRegister_Success() {
        // 准备
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setName("新用户");
        registerDTO.setPhone("13912345678");
        registerDTO.setPassword("password123");
        registerDTO.setGender("男");
        registerDTO.setBirthDate("1995-06-15");
        registerDTO.setIdType("身份证");
        registerDTO.setIdNumber("33010119950615001X");

        String encodedPassword = "encodedPasswordForNewUser";
        User savedUser = new User();
        savedUser.setId("newUserId");
        savedUser.setName(registerDTO.getName());
        savedUser.setPhone(registerDTO.getPhone());
        savedUser.setPassword(encodedPassword);
        savedUser.setAdmin(false); savedUser.setDoctor(false); savedUser.setPatient(true);
        savedUser.setGender(registerDTO.getGender());
        savedUser.setBirthDate(registerDTO.getBirthDate());
        savedUser.setIdType(registerDTO.getIdType());
        savedUser.setIdNumber(registerDTO.getIdNumber());

        Patient savedPatient = new Patient();
        savedPatient.setId("newPatientId");
        savedPatient.setUser(savedUser);
        savedPatient.setName(registerDTO.getName());

        when(userRepository.existsByPhone(registerDTO.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // 执行
        UserDTO result = authService.register(registerDTO);

        // 验证
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(registerDTO.getName(), result.getName());
        assertEquals(registerDTO.getPhone(), result.getPhone());
        assertTrue(result.isPatient());
        assertFalse(result.isAdmin());
        assertFalse(result.isDoctor());
        verify(userRepository).existsByPhone(registerDTO.getPhone());
        verify(passwordEncoder).encode(registerDTO.getPassword());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(registerDTO.getName(), capturedUser.getName());
        assertEquals(registerDTO.getPhone(), capturedUser.getPhone());
        assertEquals(encodedPassword, capturedUser.getPassword());
        assertTrue(capturedUser.isPatient());

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(patientCaptor.capture());
        Patient capturedPatient = patientCaptor.getValue();
        assertEquals(savedUser, capturedPatient.getUser());
        assertEquals(registerDTO.getName(), capturedPatient.getName());
        assertEquals(Patient.Gender.valueOf(registerDTO.getGender()), capturedPatient.getGender());
        assertEquals(LocalDate.parse(registerDTO.getBirthDate()), capturedPatient.getBirthDate());
        assertEquals(Patient.IdType.valueOf(registerDTO.getIdType()), capturedPatient.getIdType());
    }

    @Test
    void testRegister_PhoneAlreadyExists() {
        // 准备
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setPhone("13000000000");
        registerDTO.setPassword("password123");

        when(userRepository.existsByPhone(registerDTO.getPhone())).thenReturn(true);

        // 执行和验证
        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> {
            authService.register(registerDTO);
        });
        assertEquals("该手机号已被注册", exception.getMessage());
        verify(userRepository).existsByPhone(registerDTO.getPhone());
        verify(userRepository, never()).save(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testRegister_InvalidDateFormat() {
        // 准备
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setName("新用户");
        registerDTO.setPhone("13987654321");
        registerDTO.setPassword("password123");
        registerDTO.setGender("男");
        registerDTO.setBirthDate("1995/06/15");
        registerDTO.setIdType("身份证");
        registerDTO.setIdNumber("33010119950615001X");

        User tempSavedUser = new User();
        tempSavedUser.setId("tempUserId");

        when(userRepository.existsByPhone(registerDTO.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(tempSavedUser);

        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerDTO);
        });
        assertTrue(exception.getMessage().contains("患者信息格式不正确"));
        assertTrue(exception.getMessage().contains("could not be parsed"));

        verify(userRepository).save(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testRegister_InvalidGender() {
        // 准备
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setName("新用户");
        registerDTO.setPhone("13911112222");
        registerDTO.setPassword("password");
        registerDTO.setGender("未知");
        registerDTO.setBirthDate("1990-01-01");

        User tempSavedUser = new User(); 
        tempSavedUser.setId("tempUserId2");

        when(userRepository.existsByPhone(registerDTO.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPass2");
        when(userRepository.save(any(User.class))).thenReturn(tempSavedUser); 

        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerDTO);
        });
        assertTrue(exception.getMessage().contains("患者信息格式不正确"));
        assertTrue(exception.getMessage().contains("No enum constant com.example.patientmanagementsystem.model.Patient.Gender.未知"));
        verify(userRepository).save(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testLogin_AdminLoginCallsLoginAdmin() {
        // 准备: 测试当 loginDTO 的 username 是 ADMIN_PHONE 时，会调用 loginAdmin 方法
        LoginDTO adminLoginDTO = new LoginDTO();
        adminLoginDTO.setUsername(ADMIN_PHONE_CONST);
        adminLoginDTO.setPassword(ADMIN_PASSWORD_CONST);

        when(userRepository.findByPhone(ADMIN_PHONE_CONST)).thenReturn(Optional.of(adminUserInstance));
        when(passwordEncoder.matches(eq(ADMIN_PASSWORD_CONST), eq(adminUserInstance.getPassword()))).thenReturn(true);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("admin-jwt-token");

        // 执行
        LoginResponseDTO response = authService.login(adminLoginDTO);

        // 验证
        assertNotNull(response);
        assertEquals(adminUserInstance.getId(), response.getId());
        assertEquals("admin-jwt-token", response.getToken());
        assertTrue(response.isAdmin());
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByPhone(ADMIN_PHONE_CONST);
        verify(passwordEncoder).matches(eq(ADMIN_PASSWORD_CONST), eq(adminUserInstance.getPassword()));
        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(tokenProvider).generateToken(authCaptor.capture());
        assertTrue(authCaptor.getValue().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testLoginAdmin_Success() {
        // 准备
        when(userRepository.findByPhone(ADMIN_PHONE_CONST)).thenReturn(Optional.of(adminUserInstance));
        when(passwordEncoder.matches(eq(ADMIN_PASSWORD_CONST), eq(adminUserInstance.getPassword()))).thenReturn(true);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("admin-test-token");

        // 执行
        LoginResponseDTO response = authService.loginAdmin(ADMIN_PHONE_CONST, ADMIN_PASSWORD_CONST);

        // 验证
        assertNotNull(response);
        assertEquals(adminUserInstance.getId(), response.getId());
        assertEquals(adminUserInstance.getName(), response.getName());
        assertEquals("admin-test-token", response.getToken());
        assertTrue(response.isAdmin());
        assertFalse(response.isDoctor());
        assertFalse(response.isPatient());
        verify(userRepository).findByPhone(ADMIN_PHONE_CONST);
        verify(passwordEncoder).matches(eq(ADMIN_PASSWORD_CONST), eq(adminUserInstance.getPassword()));
        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(tokenProvider).generateToken(authCaptor.capture());
        Authentication capturedAuth = authCaptor.getValue();
        assertEquals(ADMIN_PHONE_CONST, capturedAuth.getName());
        assertTrue(capturedAuth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testLoginAdmin_UserNotFound() {
        // 准备
        when(userRepository.findByPhone(ADMIN_PHONE_CONST)).thenReturn(Optional.empty());

        // 执行和验证
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.loginAdmin(ADMIN_PHONE_CONST, ADMIN_PASSWORD_CONST);
        });
        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userRepository).findByPhone(ADMIN_PHONE_CONST);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLoginAdmin_IncorrectPassword() {
        // 准备
        when(userRepository.findByPhone(ADMIN_PHONE_CONST)).thenReturn(Optional.of(adminUserInstance));
        when(passwordEncoder.matches(eq(ADMIN_PASSWORD_CONST), eq(adminUserInstance.getPassword()))).thenReturn(false);

        // 执行和验证
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.loginAdmin(ADMIN_PHONE_CONST, ADMIN_PASSWORD_CONST);
        });
        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userRepository).findByPhone(ADMIN_PHONE_CONST);
        verify(passwordEncoder).matches(eq(ADMIN_PASSWORD_CONST), eq(adminUserInstance.getPassword()));
        verify(tokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLoginAdmin_UserNotAdminButUpdated() {
        // 准备
        User adminUserNotMarkedAsAdmin = new User();
        adminUserNotMarkedAsAdmin.setId("adminUserId2");
        adminUserNotMarkedAsAdmin.setPhone(ADMIN_PHONE_CONST);
        adminUserNotMarkedAsAdmin.setPassword(adminUserInstance.getPassword());
        adminUserNotMarkedAsAdmin.setAdmin(false);

        when(userRepository.findByPhone(ADMIN_PHONE_CONST)).thenReturn(Optional.of(adminUserNotMarkedAsAdmin));
        when(passwordEncoder.matches(eq(ADMIN_PASSWORD_CONST), eq(adminUserNotMarkedAsAdmin.getPassword()))).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("admin-updated-token");

        // 执行
        LoginResponseDTO response = authService.loginAdmin(ADMIN_PHONE_CONST, ADMIN_PASSWORD_CONST);

        // 验证
        assertNotNull(response);
        assertTrue(response.isAdmin(), "Response DTO should indicate admin status");
        assertEquals("admin-updated-token", response.getToken());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().isAdmin(), "User in repository should be updated to admin");
        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(tokenProvider).generateToken(authCaptor.capture());
        assertTrue(authCaptor.getValue().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
