package com.clinicsalon.loyalty.dto;

import com.clinicsalon.loyalty.model.LoyaltyTransaction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransactionRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    @NotNull(message = "Transaction type is required")
    private LoyaltyTransaction.TransactionType type;
    
    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;
    
    private String description;
    
    private String referenceId;
    
    private LocalDateTime expiryDate;
}
