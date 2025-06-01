package com.clinicsalon.finance.gateway.impl;

import com.clinicsalon.finance.gateway.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementação simulada do gateway de pagamento Asaas
 * Em um ambiente de produção, esta classe se comunicaria com a API real do Asaas
 */
@Service
@Slf4j
public class AsaasPaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "ASAAS";
    private static final String PAYMENT_BASE_URL = "https://sandbox.asaas.com/payment/";
    
    @Override
    public Map<String, Object> generatePaymentLink(UUID transactionId, Double amount, String description) {
        log.info("Gerando link de pagamento para transação {} no valor de R$ {}", transactionId, amount);
        
        // Em produção, faria uma chamada HTTP para a API do Asaas
        // https://docs.asaas.com/reference/criar-link-de-pagamento
        
        String gatewayTransactionId = "asaas_" + UUID.randomUUID().toString().replace("-", "");
        String paymentLink = PAYMENT_BASE_URL + gatewayTransactionId;
        
        Map<String, Object> response = new HashMap<>();
        response.put("gatewayTransactionId", gatewayTransactionId);
        response.put("paymentLink", paymentLink);
        response.put("expiresAt", LocalDateTime.now().plusDays(3));
        response.put("status", "PENDING");
        response.put("gateway", GATEWAY_NAME);
        
        log.info("Link de pagamento gerado: {}", paymentLink);
        return response;
    }

    @Override
    public Map<String, Object> processPayment(Map<String, Object> paymentData) {
        String gatewayTransactionId = (String) paymentData.getOrDefault("gatewayTransactionId", "asaas_" + UUID.randomUUID().toString().replace("-", ""));
        Double amount = (Double) paymentData.getOrDefault("amount", 0.0);
        
        log.info("Processando pagamento {} no valor de R$ {}", gatewayTransactionId, amount);
        
        // Em produção, faria uma chamada HTTP para a API do Asaas
        // https://docs.asaas.com/reference/efetuar-pagamento
        
        Map<String, Object> response = new HashMap<>();
        response.put("gatewayTransactionId", gatewayTransactionId);
        response.put("status", "CONFIRMED");
        response.put("paidAt", LocalDateTime.now());
        response.put("paymentMethod", paymentData.getOrDefault("paymentMethod", "CREDIT_CARD"));
        response.put("gateway", GATEWAY_NAME);
        
        log.info("Pagamento processado com sucesso: {}", gatewayTransactionId);
        return response;
    }

    @Override
    public Map<String, Object> processRefund(String gatewayTransactionId, Double amount) {
        log.info("Processando reembolso para transação {} no valor de R$ {}", gatewayTransactionId, amount);
        
        // Em produção, faria uma chamada HTTP para a API do Asaas
        // https://docs.asaas.com/reference/reembolsar-pagamento
        
        String refundId = "refund_" + UUID.randomUUID().toString().replace("-", "");
        
        Map<String, Object> response = new HashMap<>();
        response.put("originalTransactionId", gatewayTransactionId);
        response.put("refundId", refundId);
        response.put("status", "REFUNDED");
        response.put("refundedAt", LocalDateTime.now());
        response.put("amount", amount);
        response.put("gateway", GATEWAY_NAME);
        
        log.info("Reembolso processado com sucesso: {}", refundId);
        return response;
    }

    @Override
    public Map<String, Object> checkPaymentStatus(String gatewayTransactionId) {
        log.info("Verificando status do pagamento {}", gatewayTransactionId);
        
        // Em produção, faria uma chamada HTTP para a API do Asaas
        // https://docs.asaas.com/reference/consultar-pagamento
        
        // Simulando uma resposta com status aleatório
        String[] possibleStatuses = {"PENDING", "CONFIRMED", "RECEIVED", "OVERDUE", "REFUNDED"};
        int randomIndex = (int) (Math.random() * possibleStatuses.length);
        String status = possibleStatuses[randomIndex];
        
        Map<String, Object> response = new HashMap<>();
        response.put("gatewayTransactionId", gatewayTransactionId);
        response.put("status", status);
        response.put("lastUpdate", LocalDateTime.now());
        response.put("gateway", GATEWAY_NAME);
        
        log.info("Status do pagamento {}: {}", gatewayTransactionId, status);
        return response;
    }

    @Override
    public String getGatewayName() {
        return GATEWAY_NAME;
    }
}
