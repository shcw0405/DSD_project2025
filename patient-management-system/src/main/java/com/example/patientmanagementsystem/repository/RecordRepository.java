package com.example.patientmanagementsystem.repository;

import com.example.patientmanagementsystem.model.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record, String> {
    
    List<Record> findByUsernameAndTime(String username, LocalDateTime time);
    
    @Query("SELECT r FROM Record r WHERE r.username = :username AND DATE(r.time) = :date")
    List<Record> findByUsernameAndDate(@Param("username") String username, @Param("date") LocalDate date);
    
    List<Record> findByUsername(String username);
    
    @Query("SELECT r FROM Record r WHERE DATE(r.time) = :date")
    List<Record> findByDate(@Param("date") LocalDate date);
    
    /**
     * 根据患者ID和处理状态查询记录（分页）
     * @param patientId 患者ID
     * @param pageable 分页参数
     * @return 分页记录列表
     */
    Page<Record> findByPatientIdAndProcessedTrue(String patientId, Pageable pageable);
}
