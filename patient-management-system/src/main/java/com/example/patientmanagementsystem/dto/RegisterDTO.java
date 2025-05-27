package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户注册数据传输对象
 * 用于接收用户注册请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "电话号码格式不正确")
    private String phone;
    
    @NotBlank(message = "性别不能为空")
    private String gender;
    
    @NotBlank(message = "出生日期不能为空")
    private String birthDate;
    
    @NotBlank(message = "证件类型不能为空")
    private String idType;
    
    @NotBlank(message = "证件号码不能为空")
    private String idNumber;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20位之间")
    private String password;
}
