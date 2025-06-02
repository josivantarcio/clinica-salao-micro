package com.clinicsalon.appointment.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private String id;
    private String clientId;
    private String clientName;
    private String professionalId;
    private String professionalName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private BigDecimal totalPrice;
    private String notes;
    private String paymentStatus;
    private String paymentId;
    private List<ServiceDTO> services;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
