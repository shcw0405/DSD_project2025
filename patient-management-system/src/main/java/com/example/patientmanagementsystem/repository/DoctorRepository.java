package com.example.patientmanagementsystem.repository;

import com.example.patientmanagementsystem.model.Doctor;
import com.example.patientmanagementsystem.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 医生数据访问接口
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, String>, JpaSpecificationExecutor<Doctor> {
    
    /**
     * 根据姓名、电话、医院或科室包含指定关键词查询医生
     * @param name 姓名关键词
     * @param phone 电话关键词
     * @param hospital 医院关键词
     * @param department 科室关键词
     * @param pageable 分页参数
     * @return 符合条件的医生列表
     */
    List<Doctor> findByNameContainingOrPhoneContainingOrHospitalContainingOrDepartmentContaining(
            String name, String phone, String hospital, String department, Pageable pageable);
    
    /**
     * 检查指定手机号的医生是否存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    Optional<Doctor> findByPhone(String phone);
    Optional<Doctor> findByUser_Id(String userId);

    @Query("SELECT d.user FROM Doctor d WHERE d.user.phone = :phone")
    User findUserByDoctorPhone(@Param("phone") String phone);
}
