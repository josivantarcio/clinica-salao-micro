package com.clinicsalon.finance.dto;

import com.clinicsalon.finance.model.TransactionStatus;
import com.clinicsalon.finance.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private UUID appointmentId;
    private UUID clientId;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private String paymentMethod;
    private String paymentGatewayId;
    private String invoiceUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private String clientName;  // Enriquecido via client-service
    private String appointmentDate;  // Enriquecido via appointment-service
    private String serviceName;  // Enriquecido via appointment-service
}
