package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.dto.LoginDTO;
import com.example.patientmanagementsystem.dto.RegisterDTO;
import com.example.patientmanagementsystem.dto.UserDTO;
import com.example.patientmanagementsystem.dto.LoginResponseDTO;
import com.example.patientmanagementsystem.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证控制器测试
 */
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // 准备测试数据
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setId("1");
        loginResponseDTO.setName("测试用户");
        loginResponseDTO.setAdmin(true);
        loginResponseDTO.setDoctor(false);
        loginResponseDTO.setPatient(false);
        loginResponseDTO.setToken("test-token");

        // 模拟服务层行为
        when(authService.login(any(LoginDTO.class))).thenReturn(loginResponseDTO);

        // 构建请求参数
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "13800138000");
        loginRequest.put("password", "password123");

        // 执行请求并验证结果
        mockMvc.perform(post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.name").value("测试用户"))
                .andExpect(jsonPath("$.data.isAdmin").value(true))
                .andExpect(jsonPath("$.data.isDoctor").value(false))
                .andExpect(jsonPath("$.data.isPatient").value(false))
                .andExpect(jsonPath("$.data.token").value("test-token"))
                .andExpect(jsonPath("$.message").value("登录成功"));
    }

    @Test
    public void testLoginFailure() throws Exception {
        // 模拟服务层抛出异常
        when(authService.login(any(LoginDTO.class))).thenThrow(new BadCredentialsException("用户名或密码错误"));

        // 构建请求参数
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "13800138000");
        loginRequest.put("password", "wrong-password");

        // 执行请求并验证结果
        mockMvc.perform(post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    public void testLoginMissingParameters() throws Exception {
        // 构建请求参数（缺少密码）
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "13800138000");

        // 执行请求并验证结果
        mockMvc.perform(post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("缺少 username 或 password 参数"));
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        // 准备测试数据
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        userDTO.setName("新用户");
        userDTO.setPhone("13800138000");
        userDTO.setGender("男");
        userDTO.setBirthDate("1990-01-01");
        userDTO.setIdType("身份证");
        userDTO.setIdNumber("110101199001011234");

        // 模拟服务层行为
        when(authService.register(any(RegisterDTO.class))).thenReturn(userDTO);

        // 构建请求参数
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("name", "新用户");
        registerRequest.put("phone", "13800138000");
        registerRequest.put("gender", "男");
        registerRequest.put("birthDate", "1990-01-01");
        registerRequest.put("idType", "身份证");
        registerRequest.put("idNumber", "110101199001011234");
        registerRequest.put("password", "password123");

        // 执行请求并验证结果
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.name").value("新用户"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.message").value("注册成功"));
    }

    @Test
    public void testRegisterMissingParameters() throws Exception {
        // 构建请求参数（缺少必填字段）
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("name", "新用户");
        registerRequest.put("phone", "13800138000");
        // 缺少其他必填字段

        // 执行请求并验证结果
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("缺少必填字段"));
    }
}
