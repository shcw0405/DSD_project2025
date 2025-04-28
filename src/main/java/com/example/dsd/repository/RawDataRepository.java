package com.example.dsd.repository;

import com.example.dsd.model.RawData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawDataRepository extends JpaRepository<RawData, String> { // 主键是 hashPk (String)
}
