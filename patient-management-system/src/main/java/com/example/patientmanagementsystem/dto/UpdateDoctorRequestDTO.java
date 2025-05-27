package com.example.patientmanagementsystem.dto;

import lombok.Data;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateDoctorRequestDTO {

    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;

    // 允许为空，但如果不为空，则校验格式
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "电话号码格式不正确") 
    private String phone;

    @Size(max = 100, message = "医院名称长度不能超过100个字符")
    private String hospital;

    @Size(max = 50, message = "科室名称长度不能超过50个字符")
    private String department;

    // 允许为空，但如果不为空，则校验长度
    @Size(min = 6, max = 20, message = "密码长度必须在6到20位之间") 
    private String password;
} 