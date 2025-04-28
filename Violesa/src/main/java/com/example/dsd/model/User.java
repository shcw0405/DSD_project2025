package com.example.dsd.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data; // 使用 Lombok 简化 getter/setter 等代码
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id // 标记为主键
    @Column(name = "username", nullable = false, length = 255) // 映射到 'username' 列
    private String username;

    @Column(name = "password", length = 20) // 映射到 'password' 列
    private String password;
}
