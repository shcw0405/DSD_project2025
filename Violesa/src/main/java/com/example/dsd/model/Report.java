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
 * 报告实体类，映射到 'report' 表
 */
@Entity
@Table(name = "report")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @Column(name = "hash_PK", length = 64, insertable = false, updatable = false)
    private String hashPk;

    @Column(name = "time")
    private LocalDateTime time;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "file_size")
    private Integer fileSize;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file", columnDefinition = "MEDIUMTEXT")
    private String file;
}