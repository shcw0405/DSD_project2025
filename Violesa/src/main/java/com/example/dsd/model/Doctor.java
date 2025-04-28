package com.example.dsd.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "doctor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @Column(name = "username", nullable = false, length = 255)
    private String username; // 主键，与 User 表的 username 关联

    @Column(name = "docname", length = 45)
    private String docname;

    @Column(name = "hospital", length = 45)
    private String hospital;

    @Column(name = "Department", length = 45) // 注意 SQL 中的列名大小写
    private String department;

    @Column(name = "phonenumber", length = 45)
    private String phonenumber;
}
