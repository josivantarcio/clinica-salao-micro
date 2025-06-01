package com.clinicsalon.finance.gateway;

import com.clinicsalon.finance.gateway.impl.AsaasPaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AsaasPaymentGatewayTest {

    private PaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        paymentGateway = new AsaasPaymentGateway();
    }

    @Test
    void shouldGeneratePaymentLink() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        Double amount = 100.0;
        String description = "Teste de pagamento";

        // Act
        Map<String, Object> result = paymentGateway.generatePaymentLink(transactionId, amount, description);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("paymentLink"));
        assertTrue(result.containsKey("gatewayTransactionId"));
        assertTrue(result.containsKey("status"));
        assertEquals("ASAAS", result.get("gateway"));
        assertTrue(((String) result.get("paymentLink")).startsWith("https://sandbox.asaas.com/payment/"));
    }

    @Test
    void shouldProcessPayment() {
        // Arrange
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", 50.0);
        paymentData.put("paymentMethod", "CREDIT_CARD");
        String gatewayTransactionId = "asaas_" + UUID.randomUUID().toString().replace("-", "");
        paymentData.put("gatewayTransactionId", gatewayTransactionId);

        // Act
        Map<String, Object> result = paymentGateway.processPayment(paymentData);

        // Assert
        assertNotNull(result);
        assertEquals("CONFIRMED", result.get("status"));
        assertEquals(gatewayTransactionId, result.get("gatewayTransactionId"));
        assertEquals("ASAAS", result.get("gateway"));
        assertTrue(result.containsKey("paidAt"));
    }

    @Test
    void shouldProcessRefund() {
        // Arrange
        String gatewayTransactionId = "asaas_" + UUID.randomUUID().toString().replace("-", "");
        Double amount = 25.0;

        // Act
        Map<String, Object> result = paymentGateway.processRefund(gatewayTransactionId, amount);

        // Assert
        assertNotNull(result);
        assertEquals("REFUNDED", result.get("status"));
        assertEquals(gatewayTransactionId, result.get("originalTransactionId"));
        assertEquals(amount, result.get("amount"));
        assertEquals("ASAAS", result.get("gateway"));
        assertTrue(result.containsKey("refundId"));
        assertTrue(result.containsKey("refundedAt"));
    }

    @Test
    void shouldCheckPaymentStatus() {
        // Arrange
        String gatewayTransactionId = "asaas_" + UUID.randomUUID().toString().replace("-", "");
        
        // Act
        Map<String, Object> result = paymentGateway.checkPaymentStatus(gatewayTransactionId);
        
        // Assert
        assertNotNull(result);
        assertEquals(gatewayTransactionId, result.get("gatewayTransactionId"));
        assertNotNull(result.get("status"));
        assertEquals("ASAAS", result.get("gateway"));
        assertTrue(result.containsKey("lastUpdate"));
    }
    
    @Test
    void shouldReturnCorrectGatewayName() {
        assertEquals("ASAAS", paymentGateway.getGatewayName());
    }
}
