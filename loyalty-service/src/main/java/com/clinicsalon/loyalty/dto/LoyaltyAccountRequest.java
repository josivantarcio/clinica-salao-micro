package com.clinicsalon.loyalty.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyAccountRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Integer initialPoints;
}
