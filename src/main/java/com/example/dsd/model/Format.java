package com.example.dsd.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.Basic;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 格式化数据实体类，映射到 'format' 表
 * 注意：hash_PK 是数据库生成的列，JPA 中通常将其标记为只读。
 */
@Entity
@Table(name = "format")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Format {

    @Id
    @Column(name = "hash_PK", length = 64, insertable = false, updatable = false) // 数据库生成，设为不可插入和更新
    private String hashPk;

    @Column(name = "time")
    private LocalDateTime time; // 使用 LocalDateTime 对应 datetime 类型

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "file_size")
    private Integer fileSize;

    @Lob // 映射到 TEXT, CLOB, BLOB 等大对象类型
    @Basic(fetch = FetchType.LAZY) // 大字段通常使用懒加载
    @Column(name = "file", columnDefinition = "MEDIUMTEXT") // 指定列定义
    private String file; // 注意：存储大文件内容为 String 可能不是最佳实践，取决于具体内容和大小
}