package com.example.dsd.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 患者实体类，映射到 'patient' 表
 */
@Entity
@Table(name = "patient")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    // 定义枚举类型，对应数据库中的 ENUM
    public enum IdType {
        passport, 
        @Column(name = "identity card") // 处理带空格的枚举值
        identity_card 
    }

    public enum Gender {
        male, female
    }

    @Id
    @Column(name = "username", nullable = false, length = 255)
    private String username; // 主键，与 User 表的 username 关联

    @Enumerated(EnumType.STRING) // 将枚举映射为字符串类型存储
    @Column(name = "id_type")
    private IdType idType;

    @Column(name = "realname", length = 45)
    private String realname;

    @Column(name = "birthyear", length = 45) // 注意：年份通常用 Integer 或 Date 类型更好
    private String birthyear;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "phonenumber", length = 45)
    private String phonenumber;

    @Column(name = "doc", length = 45) // 关联到 Doctor 的 username
    private String docUsername; // 建议命名清晰，表示这是关联医生的用户名
}