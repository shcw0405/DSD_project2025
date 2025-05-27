package com.example.patientmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_patient_relations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPatientRelation {
    
    @EmbeddedId
    private DoctorPatientRelationId id;
    
    @ManyToOne
    @MapsId("doctorId")
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    
    @ManyToOne
    @MapsId("patientId")
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorPatientRelationId implements Serializable {
        
        @Column(name = "doctor_id")
        private String doctorId;
        
        @Column(name = "patient_id")
        private String patientId;
    }
    
    // 显式添加getNotes和setNotes方法，以防Lombok注解失效
    public String getNotes() {
        return this.notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
