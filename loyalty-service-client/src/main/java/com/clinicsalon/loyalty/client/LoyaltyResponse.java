package com.clinicsalon.loyalty.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respostas do serviço de fidelidade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyResponse {

    private Long id;
    private Long clientId;
    private Integer pointsBalance;
    private Integer lifetimePoints;
    private String tier; // BRONZE, SILVER, GOLD, PLATINUM
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
