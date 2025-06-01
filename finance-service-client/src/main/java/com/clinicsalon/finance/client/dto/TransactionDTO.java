package com.clinicsalon.finance.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para representar uma transação financeira com os dados essenciais para integração com outros serviços
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    
    private UUID id;
    
    private UUID clientId;
    
    private UUID appointmentId;
    
    private String type;
    
    private Double amount;
    
    private String status;
    
    private String paymentMethod;
    
    private String paymentGatewayId;
    
    private String invoiceUrl;
    
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
