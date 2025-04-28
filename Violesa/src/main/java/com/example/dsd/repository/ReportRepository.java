package com.example.dsd.repository;


import com.example.dsd.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> { // 主键是 hashPk (String)
}
