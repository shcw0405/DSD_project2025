package com.example.patientmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    @Column(name = "name", nullable = false)
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;
    
    @Column(name = "phone", nullable = false, unique = true)
    @NotBlank(message = "电话号码不能为空")
    @Size(max = 20, message = "电话号码长度不能超过20个字符")
    @Pattern(regexp = "^\\+?\\d{1,20}$", message = "电话号码格式不正确")
    private String phone;
    
    @Column(name = "hospital", nullable = false)
    @NotBlank(message = "医院名称不能为空")
    @Size(max = 100, message = "医院名称长度不能超过100个字符")
    private String hospital;
    
    @Column(name = "department", nullable = false)
    @NotBlank(message = "科室名称不能为空")
    @Size(max = 50, message = "科室名称长度不能超过50个字符")
    private String department;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 添加getUserId方法，解决构建错误
    public String getUserId() {
        return user != null ? user.getId() : null;
    }
    
    // 添加setUserId方法，解决构建错误
    public void setUserId(String userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }
}
