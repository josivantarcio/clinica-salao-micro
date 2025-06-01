package com.clinicsalon.loyalty.dto;

import com.clinicsalon.loyalty.model.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyAccountResponse {

    private Long id;
    private Long clientId;
    private String clientName; // Obtido via Feign Client
    private Integer pointsBalance;
    private Integer lifetimePoints;
    private LoyaltyTier tier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
