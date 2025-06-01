package com.clinicsalon.report.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyPointsDto {
    private Long id;
    private Long clientId;
    private String clientName;
    private Integer points;
    private String operation; // ADD, SUBTRACT
    private String source; // APPOINTMENT, PROMOTION, MANUAL
    private Long referenceId; // ID do agendamento, se for o caso
    private LocalDateTime createdAt;
}
