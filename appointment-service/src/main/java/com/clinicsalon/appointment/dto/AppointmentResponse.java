package com.clinicsalon.appointment.dto;

import com.clinicsalon.appointment.model.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    
    private Long id;
    private Long clientId;
    private String clientName; // Será preenchido via Feign Client
    private Long professionalId;
    private String professionalName; // Será preenchido via Feign Client
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;
    
    private AppointmentStatus status;
    private BigDecimal totalPrice;
    private String notes;
    private List<AppointmentServiceResponse> services;
    
    // Métodos explícitos para garantir compatibilidade
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public void setProfessionalName(String professionalName) {
        this.professionalName = professionalName;
    }
    
    public void setServices(List<AppointmentServiceResponse> services) {
        this.services = services;
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updatedAt;
}
