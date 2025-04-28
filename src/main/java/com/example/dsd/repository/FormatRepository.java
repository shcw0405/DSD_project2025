package com.example.dsd.repository;

import com.example.dsd.model.Format;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormatRepository extends JpaRepository<Format, String> { // 主键是 hashPk (String)
}
