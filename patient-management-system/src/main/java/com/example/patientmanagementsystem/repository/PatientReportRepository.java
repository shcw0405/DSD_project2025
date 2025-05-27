package com.example.patientmanagementsystem.repository;

import com.example.patientmanagementsystem.model.PatientReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PatientReportRepository extends JpaRepository<PatientReport, String> {
    
    // 修正属性映射，使用正确的关联属性路径
    List<PatientReport> findByPatient_IdOrderByDateDesc(String patientId);
    
    @Modifying
    @Transactional
    @Query("UPDATE PatientReport r SET r.type = :type, r.summary = :summary WHERE r.id = :reportId")
    int updateReportTypeAndSummary(
            @Param("reportId") String reportId,
            @Param("type") String type,
            @Param("summary") String summary);
            
    void deleteAllByPatient_Id(String patientId);
}
