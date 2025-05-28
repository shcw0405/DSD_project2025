package com.example.patientmanagementsystem.repository;

import com.example.patientmanagementsystem.model.DoctorPatientRelation;
import com.example.patientmanagementsystem.model.DoctorPatientRelation.DoctorPatientRelationId;
import com.example.patientmanagementsystem.model.Patient;
import com.example.patientmanagementsystem.dto.DoctorPatientRelationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorPatientRelationRepository extends JpaRepository<DoctorPatientRelation, DoctorPatientRelationId>, 
                                                        JpaSpecificationExecutor<DoctorPatientRelation> {
    
    @Query("SELECT r.patient FROM DoctorPatientRelation r WHERE r.doctor.id = :doctorId")
    List<Patient> findPatientsByDoctorId(@Param("doctorId") String doctorId);
    
    @Query("SELECT r.patient FROM DoctorPatientRelation r WHERE r.doctor.id = :doctorId " +
           "AND (:name IS NULL OR r.patient.name LIKE %:name%) " +
           "AND (:phone IS NULL OR r.patient.phone LIKE %:phone%) " +
           "AND (:gender IS NULL OR r.patient.gender = :gender) " +
           "AND (:idNumber IS NULL OR r.patient.idNumber LIKE %:idNumber%)")
    Page<Patient> findPatientsByDoctorIdAndFilters(
            @Param("doctorId") String doctorId,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("gender") Patient.Gender gender,
            @Param("idNumber") String idNumber,
            Pageable pageable);
    
    @Query("SELECT new com.example.patientmanagementsystem.dto.DoctorPatientRelationDTO(" +
           "r.doctor.id, r.doctor.name, r.patient.id, r.patient.name) " +
           "FROM DoctorPatientRelation r")
    List<Object[]> findAllRelationsWithDetails();
    
    @Query("SELECT new com.example.patientmanagementsystem.dto.DoctorPatientRelationDTO(" +
           "du.id, d.name, pu.id, p.name) " +
           "FROM DoctorPatientRelation r " +
           "JOIN r.doctor d JOIN d.user du " +
           "JOIN r.patient p JOIN p.user pu " +
           "WHERE (:doctorName IS NULL OR d.name LIKE %:doctorName%) " +
           "AND (:doctorPhone IS NULL OR d.phone LIKE %:doctorPhone%) " +
           "AND (:patientName IS NULL OR p.name LIKE %:patientName%) " +
           "AND (:patientPhone IS NULL OR p.phone LIKE %:patientPhone%) " +
           "AND (:requestingDoctorUserId IS NULL OR du.id = :requestingDoctorUserId)")
    Page<DoctorPatientRelationDTO> findRelationsWithDetailsByFilters(
            @Param("doctorName") String doctorName,
            @Param("doctorPhone") String doctorPhone,
            @Param("patientName") String patientName,
            @Param("patientPhone") String patientPhone,
            @Param("requestingDoctorUserId") String requestingDoctorUserId,
            Pageable pageable);
    
    boolean existsByDoctorIdAndPatientId(String doctorId, String patientId);
    
    void deleteByDoctorIdAndPatientId(String doctorId, String patientId);
    
    List<DoctorPatientRelation> findByDoctorId(String doctorId);
    
    @Query("SELECT r FROM DoctorPatientRelation r WHERE r.doctor.id = :doctorId AND r.patient.id = :patientId")
    Optional<DoctorPatientRelation> findByDoctorIdAndPatientId(@Param("doctorId") String doctorId, @Param("patientId") String patientId);
    
    void deleteAllById_DoctorId(String doctorId);
    
    void deleteAllById_PatientId(String patientId);

    boolean existsById_PatientId(String patientId);
}
