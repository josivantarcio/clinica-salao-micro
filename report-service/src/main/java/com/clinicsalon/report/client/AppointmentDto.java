package com.clinicsalon.report.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    
    private Long id;
    private Long clientId;
    private String clientName;
    private Long professionalId;
    private String professionalName;
    private LocalDateTime appointmentDate;
    private String serviceName;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
