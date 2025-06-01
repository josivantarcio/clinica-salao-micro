package com.clinicsalon.loyalty.dto;

import com.clinicsalon.loyalty.model.LoyaltyTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransactionResponse {

    private Long id;
    private Long accountId;
    private Long clientId;
    private String clientName;
    private LoyaltyTransaction.TransactionType type;
    private Integer points;
    private String description;
    private String referenceId;
    private LocalDateTime transactionDate;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
}
