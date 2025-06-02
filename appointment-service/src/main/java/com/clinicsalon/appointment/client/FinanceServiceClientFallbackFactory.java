package com.clinicsalon.appointment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class FinanceServiceClientFallbackFactory implements FallbackFactory<FinanceServiceClient> {
    
    private static final Logger log = LoggerFactory.getLogger(FinanceServiceClientFallbackFactory.class);
    
    @Override
    public FinanceServiceClient create(Throwable cause) {
        log.error("Fallback para FinanceServiceClient ativado devido a: {}", cause.getMessage());
        
        return new FinanceServiceClient() {
            @Override
            public ResponseEntity<Map<String, String>> createPaymentLink(Map<String, Object> paymentRequest) {
                log.warn("Usando fallback para createPaymentLink");
                Map<String, String> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Serviço de pagamento indisponível no momento");
                return ResponseEntity.ok(response);
            }
            
            @Override
            public ResponseEntity<Map<String, Object>> getPaymentStatusByAppointmentId(Long appointmentId) {
                log.warn("Usando fallback para getPaymentStatusByAppointmentId para agendamento ID: {}", appointmentId);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "UNKNOWN");
                response.put("message", "Serviço de pagamento indisponível no momento");
                return ResponseEntity.ok(response);
            }
            
            @Override
            public ResponseEntity<Map<String, Object>> processRefund(Long appointmentId) {
                log.warn("Usando fallback para processRefund para agendamento ID: {}", appointmentId);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Serviço de pagamento indisponível para processamento de reembolso");
                return ResponseEntity.ok(response);
            }
        };
    }
}
