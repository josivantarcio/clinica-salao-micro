package com.clinicsalon.finance.util;

import com.clinicsalon.finance.dto.TransactionRequest;
import com.clinicsalon.finance.dto.TransactionResponse;
import com.clinicsalon.finance.model.Transaction;
import org.springframework.stereotype.Component;

/**
 * Utilitário para conversão entre DTOs e entidades
 */
@Component
public class DtoConverter {
    
    /**
     * Converte uma requisição de transação para uma entidade
     * @param request Requisição de transação
     * @return Entidade Transaction
     */
    public Transaction toEntity(TransactionRequest request) {
        if (request == null) {
            return null;
        }
        
        return Transaction.builder()
                .appointmentId(request.getAppointmentId())
                .clientId(request.getClientId())
                .type(request.getType())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .build();
    }
    
    /**
     * Converte uma entidade para uma resposta de transação
     * @param transaction Entidade de transação
     * @return Resposta de transação
     */
    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return TransactionResponse.builder()
                .id(transaction.getId())
                .appointmentId(transaction.getAppointmentId())
                .clientId(transaction.getClientId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentGatewayId(transaction.getPaymentGatewayId())
                .invoiceUrl(transaction.getInvoiceUrl())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .description(transaction.getDescription())
                .build();
    }
}
