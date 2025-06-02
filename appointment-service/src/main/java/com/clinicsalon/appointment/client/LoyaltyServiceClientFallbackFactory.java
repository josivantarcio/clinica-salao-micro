package com.clinicsalon.appointment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoyaltyServiceClientFallbackFactory implements FallbackFactory<LoyaltyServiceClient> {
    
    private static final Logger log = LoggerFactory.getLogger(LoyaltyServiceClientFallbackFactory.class);
    
    @Override
    public LoyaltyServiceClient create(Throwable cause) {
        log.error("Fallback para LoyaltyServiceClient ativado devido a: {}", cause.getMessage());
        
        return new LoyaltyServiceClient() {
            @Override
            public ResponseEntity<Map<String, Object>> getLoyaltyAccount(Long clientId) {
                log.warn("Usando fallback para getLoyaltyAccount para cliente ID: {}", clientId);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Serviço de fidelidade indisponível no momento");
                return ResponseEntity.ok(response);
            }
            
            @Override
            public ResponseEntity<Map<String, Object>> addPoints(Map<String, Object> pointsRequest) {
                log.warn("Usando fallback para addPoints: {}", pointsRequest.get("clientId"));
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Serviço de fidelidade indisponível no momento");
                return ResponseEntity.ok(response);
            }
            
            @Override
            public ResponseEntity<Map<String, Object>> getPointsBalance(Long clientId) {
                log.warn("Usando fallback para getPointsBalance para cliente ID: {}", clientId);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Serviço de fidelidade indisponível no momento");
                response.put("points", 0);
                return ResponseEntity.ok(response);
            }
        };
    }
}
