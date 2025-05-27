package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.LoginDTO;
import com.example.patientmanagementsystem.dto.UserDTO;
import com.example.patientmanagementsystem.dto.LoginResponseDTO;
import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/session")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
        logger.info("收到登录请求: username={}", loginDTO.getUsername());
        
        // 检查是否是管理员登录
        if ("13000000000".equals(loginDTO.getUsername())) {
            logger.info("检测到管理员登录请求");
        }
        
        LoginResponseDTO loginResponse = authService.login(loginDTO);
        
        logger.info("登录成功: userId={}, isAdmin={}, token={}", 
                loginResponse.getId(), loginResponse.isAdmin(), loginResponse.getToken());
        
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "登录成功"));
    }
}
