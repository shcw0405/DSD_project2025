package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 登录数据传输对象
 * 用于接收用户登录请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}
