package com.clinicsalon.finance.gateway;

import java.util.Map;
import java.util.UUID;

/**
 * Interface para integração com gateways de pagamento
 */
public interface PaymentGateway {

    /**
     * Gera um link de pagamento para uma transação
     * @param transactionId ID da transação
     * @param amount Valor da transação
     * @param description Descrição da transação
     * @return Dados do link de pagamento gerado
     */
    Map<String, Object> generatePaymentLink(UUID transactionId, Double amount, String description);
    
    /**
     * Processa um pagamento
     * @param paymentData Dados do pagamento
     * @return Resultado do processamento
     */
    Map<String, Object> processPayment(Map<String, Object> paymentData);
    
    /**
     * Processa um reembolso
     * @param transactionId ID da transação original
     * @param amount Valor a ser reembolsado
     * @return Resultado do reembolso
     */
    Map<String, Object> processRefund(String transactionId, Double amount);
    
    /**
     * Verifica o status de um pagamento
     * @param gatewayTransactionId ID da transação no gateway
     * @return Status atual do pagamento
     */
    Map<String, Object> checkPaymentStatus(String gatewayTransactionId);
    
    /**
     * Retorna o nome do gateway
     * @return Nome do gateway
     */
    String getGatewayName();
}
