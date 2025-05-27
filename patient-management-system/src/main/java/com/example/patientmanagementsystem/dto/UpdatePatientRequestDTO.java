package com.example.patientmanagementsystem.dto;

import com.example.patientmanagementsystem.validation.AtLeastOneNotNull;
import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
// Consider adding custom validator for date format if not using @JsonFormat or standard parsing
// For gender and idType, consider custom validator if specific values are required beyond string

@Data
@AtLeastOneNotNull(fields = {"name", "phone", "gender", "birthDate", "idType", "idNumber", "password"}, message = "请求体缺少需要更新的字段或所有字段无效")
public class UpdatePatientRequestDTO {

    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;

    // 允许为空，但如果不为空，则校验格式
    // Regex allows + and digits, common for international numbers. Adjust if only Chinese format is needed.
    @Pattern(regexp = "^$|^\\+?[0-9\\s\\-()]{7,20}$", message = "电话号码格式不正确")
    private String phone;

    // Gender: "男", "女". Could use @Pattern(regexp="^(男|女)$") if no enum conversion is planned before validation.
    private String gender; 

    // BirthDate: "YYYY-MM-DD". Could use @Pattern(regexp="^\\d{4}-\\d{2}-\\d{2}$")
    // Or rely on parsing in service layer. For DTO validation, @Pattern is an option.
    @Pattern(regexp = "^$|^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$", message = "出生日期格式必须为 YYYY-MM-DD")
    private String birthDate;

    // IdType: e.g., "身份证", "护照". Could use @Pattern
    private String idType;

    @Size(max = 50, message = "证件号码长度不能超过50个字符")
    private String idNumber;

    // Password: 允许为空，但如果不为空，则校验长度
    @Size(min = 6, max = 20, message = "密码长度必须在6到20位之间")
    private String password;
} 