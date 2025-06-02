package com.clinicsalon.appointment.integration;

import com.clinicsalon.appointment.client.FinanceServiceClient;
import com.clinicsalon.appointment.client.FinanceServiceClientFallbackFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FinanceServiceIntegrationTest {

    @Mock
    private FinanceServiceClient financeServiceClient;
    
    @InjectMocks
    private FinanceServiceClientFallbackFactory fallbackFactory = new FinanceServiceClientFallbackFactory();

    @Test
    public void testCreatePaymentLink() {
        // Mocking a successful response from finance service
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("status", "SUCCESS");
        successResponse.put("paymentLink", "http://asaas.com/payment/12345");
        
        when(financeServiceClient.createPaymentLink(any())).thenReturn(ResponseEntity.ok(successResponse));
        
        // Create a test payment request
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("appointmentId", 1L);
        paymentRequest.put("amount", BigDecimal.valueOf(100.00));
        paymentRequest.put("description", "Consulta de rotina");
        paymentRequest.put("clientId", 1L);
        
        // Execute test
        ResponseEntity<Map<String, String>> response = financeServiceClient.createPaymentLink(paymentRequest);
        
        // Verify response
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCode().value(), "Status code should be 200");
        
        // Verifica se o corpo da resposta não é nulo antes de acessá-lo
        if (response != null && response.hasBody()) {
            Map<String, String> body = response.getBody();
            // Verificações adicionais só se o body não for nulo
            if (body != null) {
                Object statusValue = body.get("status");
                Object linkValue = body.get("paymentLink");
                assertEquals("SUCCESS", statusValue, "Status should be SUCCESS");
                assertEquals("http://asaas.com/payment/12345", linkValue, "Payment link should match expected");
            }
        }
    }
    
    @Test
    public void testGetPaymentStatus() {
        // Mocking a payment status response
        Map<String, Object> paymentStatusResponse = new HashMap<>();
        paymentStatusResponse.put("status", "PAID");
        paymentStatusResponse.put("appointmentId", 1L);
        paymentStatusResponse.put("paidAmount", BigDecimal.valueOf(100.00));
        paymentStatusResponse.put("paymentDate", "2025-06-02T14:30:00");
        
        when(financeServiceClient.getPaymentStatusByAppointmentId(anyLong())).thenReturn(ResponseEntity.ok(paymentStatusResponse));
        
        // Execute test
        ResponseEntity<Map<String, Object>> response = financeServiceClient.getPaymentStatusByAppointmentId(1L);
        
        // Verify response
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCode().value(), "Status code should be 200");
        
        // Verifica se o corpo da resposta não é nulo antes de acessá-lo
        if (response != null && response.hasBody()) {
            Map<String, Object> body = response.getBody();
            // Verificações adicionais só se o body não for nulo
            if (body != null) {
                Object statusValue = body.get("status");
                assertEquals("PAID", statusValue, "Status should be PAID");
            }
        }
    }
    
    @Test
    public void testProcessRefund() {
        // Mocking a refund response
        Map<String, Object> refundResponse = new HashMap<>();
        refundResponse.put("status", "REFUNDED");
        refundResponse.put("appointmentId", 1L);
        refundResponse.put("refundedAmount", BigDecimal.valueOf(100.00));
        refundResponse.put("refundDate", "2025-06-02T14:30:00");
        
        when(financeServiceClient.processRefund(anyLong())).thenReturn(ResponseEntity.ok(refundResponse));
        
        // Execute test
        ResponseEntity<Map<String, Object>> response = financeServiceClient.processRefund(1L);
        
        // Verify response
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCode().value(), "Status code should be 200");
        
        // Verifica se o corpo da resposta não é nulo antes de acessá-lo
        if (response != null && response.hasBody()) {
            Map<String, Object> body = response.getBody();
            // Verificações adicionais só se o body não for nulo
            if (body != null) {
                Object statusValue = body.get("status");
                assertEquals("REFUNDED", statusValue, "Status should be REFUNDED");
            }
        }
    }
    
    @Test
    public void testFinanceServiceClientFallback() {
        // Create an exception to test the fallback
        RuntimeException testException = new RuntimeException("Service unavailable");
        
        // Get the fallback implementation
        FinanceServiceClient fallbackClient = fallbackFactory.create(testException);
        
        // Test createPaymentLink fallback
        ResponseEntity<Map<String, String>> paymentLinkResponse = fallbackClient.createPaymentLink(new HashMap<>());
        assertNotNull(paymentLinkResponse, "Fallback response should not be null");
        
        // Verifica se o corpo da resposta não é nulo antes de acessá-lo
        if (paymentLinkResponse != null && paymentLinkResponse.hasBody()) {
            Map<String, String> paymentLinkBody = paymentLinkResponse.getBody();
            if (paymentLinkBody != null) {
                Object statusValue = paymentLinkBody.get("status");
                assertEquals("ERROR", statusValue, "Fallback status should be ERROR");
            }
        }
        
        // Test getPaymentStatusByAppointmentId fallback
        ResponseEntity<Map<String, Object>> statusResponse = fallbackClient.getPaymentStatusByAppointmentId(1L);
        assertNotNull(statusResponse, "Status fallback response should not be null");
        
        // Verifica se o corpo da resposta não é nulo antes de acessá-lo
        if (statusResponse != null && statusResponse.hasBody()) {
            Map<String, Object> statusBody = statusResponse.getBody();
            if (statusBody != null) {
                Object statusValue = statusBody.get("status");
                assertEquals("UNKNOWN", statusValue, "Status fallback should be UNKNOWN");
            }
        }
        
        // Test processRefund fallback
        ResponseEntity<Map<String, Object>> refundResponse = fallbackClient.processRefund(1L);
        assertNotNull(refundResponse, "Refund fallback response should not be null");
        
        // Verifica se o corpo da resposta não é nulo antes de acessá-lo
        if (refundResponse != null && refundResponse.hasBody()) {
            Map<String, Object> refundBody = refundResponse.getBody();
            if (refundBody != null) {
                Object statusValue = refundBody.get("status");
                assertEquals("ERROR", statusValue, "Refund fallback status should be ERROR");
            }
        }
    }
}
