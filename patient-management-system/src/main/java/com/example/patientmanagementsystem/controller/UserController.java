package com.example.patientmanagementsystem.controller;

import com.example.patientmanagementsystem.dto.ApiResponse;
import com.example.patientmanagementsystem.dto.RegisterDTO;
import com.example.patientmanagementsystem.dto.RegistrationSuccessDataDTO;
import com.example.patientmanagementsystem.dto.UserDTO;
import com.example.patientmanagementsystem.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600) // 保持与AuthController一致的跨域配置
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<RegistrationSuccessDataDTO>> registerUser(@Valid @RequestBody RegisterDTO registerDTO) {
        logger.info("收到用户注册请求: phone={}", registerDTO.getPhone());
        UserDTO registeredUser = authService.register(registerDTO);

        // 将 UserDTO 转换为 RegistrationSuccessDataDTO
        RegistrationSuccessDataDTO responseData = new RegistrationSuccessDataDTO(
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getPhone(),
                registeredUser.getGender(),
                registeredUser.getBirthDate(),
                registeredUser.getIdType(),
                registeredUser.getIdNumber()
        );
        logger.info("用户注册成功: userId={}", responseData.getId());
        return new ResponseEntity<>(ApiResponse.success(responseData, "注册成功", 201), HttpStatus.CREATED);
    }
} 