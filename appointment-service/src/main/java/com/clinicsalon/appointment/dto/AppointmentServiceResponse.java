package com.clinicsalon.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentServiceResponse {
    
    private Long id;
    private Long serviceId;
    private String serviceName;
    private BigDecimal price;
    private String notes;
}
