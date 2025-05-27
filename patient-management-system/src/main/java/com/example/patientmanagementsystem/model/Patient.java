package com.example.patientmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    
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
    
    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "性别不能为空")
    private Gender gender;
    
    @Column(name = "birth_date", nullable = false)
    @NotNull(message = "出生日期不能为空")
    private LocalDate birthDate;
    
    @Column(name = "id_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "证件类型不能为空")
    private IdType idType;
    
    @Column(name = "id_number", nullable = false, unique = true)
    @NotBlank(message = "证件号码不能为空")
    @Size(max = 50, message = "证件号码长度不能超过50个字符")
    private String idNumber;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum Gender {
        男, 女;

        public static Gender fromString(String text) {
            if (text != null) {
                for (Gender g : Gender.values()) {
                    if (text.equalsIgnoreCase(g.name())) {
                        return g;
                    }
                }
            }
            return null; // 或者抛出异常，取决于需求
        }
    }
    
    public enum IdType {
        身份证, 护照
    }
    
    // 添加getUserId方法，解决构建错误
    public String getUserId() {
        return user != null ? user.getId() : null;
    }
}
