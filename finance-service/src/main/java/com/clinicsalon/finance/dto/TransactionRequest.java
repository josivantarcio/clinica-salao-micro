package com.clinicsalon.finance.dto;

import com.clinicsalon.finance.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Appointment ID is required")
    private UUID appointmentId;
    
    @NotNull(message = "Client ID is required")
    private UUID clientId;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private String description;
}
