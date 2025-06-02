package com.clinicsalon.appointment.client;

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
public class ServiceDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
