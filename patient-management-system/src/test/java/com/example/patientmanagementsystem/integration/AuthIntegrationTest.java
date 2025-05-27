package com.example.patientmanagementsystem.integration;

import com.example.patientmanagementsystem.dto.UserDTO;
import com.example.patientmanagementsystem.model.User;
import com.example.patientmanagementsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 清理测试用户
        userRepository.deleteAll();

        // 创建测试用户
        testUser = new User();
        testUser.setPhone("13800000001");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setDoctor(true);
        testUser.setPatient(false);
        testUser.setAdmin(false);
        userRepository.save(testUser);
    }

    @Test
    void testLogin_Success() throws Exception {
        // 执行
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .param("phone", "13800000001")
                .param("password", "password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();

        // 验证
        String content = result.getResponse().getContentAsString();
        UserDTO userDTO = objectMapper.readValue(content, UserDTO.class);

        assertNotNull(userDTO);
        assertEquals(testUser.getId(), userDTO.getId());
        assertEquals(testUser.isDoctor(), userDTO.isDoctor());
        assertEquals(testUser.isPatient(), userDTO.isPatient());
        assertEquals(testUser.isAdmin(), userDTO.isAdmin());
        assertNotNull(userDTO.getToken());
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // 执行
        mockMvc.perform(post("/api/auth/login")
                .param("phone", "13800000001")
                .param("password", "wrongpassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegister_Success() throws Exception {
        // 执行
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .param("phone", "13800000002")
                .param("password", "password")
                .param("isDoctor", "true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();

        // 验证
        String content = result.getResponse().getContentAsString();
        UserDTO userDTO = objectMapper.readValue(content, UserDTO.class);

        assertNotNull(userDTO);
        assertEquals(true, userDTO.isDoctor());
        assertEquals(false, userDTO.isPatient());
        assertEquals(false, userDTO.isAdmin());

        // 验证用户已保存到数据库
        User savedUser = userRepository.findByPhone("13800000002").orElse(null);
        assertNotNull(savedUser);
        assertEquals("13800000002", savedUser.getPhone());
        assertEquals(true, savedUser.isDoctor());
    }

    @Test
    void testRegister_PhoneAlreadyExists() throws Exception {
        // 执行
        mockMvc.perform(post("/api/auth/register")
                .param("phone", "13800000001")
                .param("password", "password")
                .param("isDoctor", "true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());
    }
}
