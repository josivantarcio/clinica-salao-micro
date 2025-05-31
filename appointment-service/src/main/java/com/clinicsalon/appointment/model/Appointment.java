package com.clinicsalon.appointment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long clientId;
    
    @Column(nullable = false)
    private Long professionalId;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Getters explícitos para resolver problemas de compilação
    public Long getClientId() {
        return clientId;
    }
    
    public Long getProfessionalId() {
        return professionalId;
    }
    
    public Long getId() {
        return id;
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public void setProfessionalId(Long professionalId) {
        this.professionalId = professionalId;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
