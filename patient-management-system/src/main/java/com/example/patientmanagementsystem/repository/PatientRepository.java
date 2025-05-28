package com.example.patientmanagementsystem.repository;

import com.example.patientmanagementsystem.model.Patient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 患者数据访问接口
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, String>, JpaSpecificationExecutor<Patient> {
    
    /**
     * 根据姓名、电话或身份证号包含指定关键词查询患者
     * @param name 姓名关键词
     * @param phone 电话关键词
     * @param idNumber 身份证号关键词
     * @param pageable 分页参数
     * @return 符合条件的患者列表
     */
    List<Patient> findByNameContainingOrPhoneContainingOrIdNumberContaining(
            String name, String phone, String idNumber, Pageable pageable);
    
    /**
     * 根据用户ID查询患者
     * @param userId 用户ID
     * @return 患者信息 (Optional)
     */
    Optional<Patient> findByUser_Id(String userId);
    
    /**
     * 根据身份证号查询患者
     * @param idNumber 身份证号
     * @return 患者信息
     */
    Optional<Patient> findByIdNumber(String idNumber);
    
    /**
     * 检查指定手机号的患者是否存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 检查指定身份证号的患者是否存在
     * @param idNumber 身份证号
     * @return 是否存在
     */
    boolean existsByIdNumber(String idNumber);
}
