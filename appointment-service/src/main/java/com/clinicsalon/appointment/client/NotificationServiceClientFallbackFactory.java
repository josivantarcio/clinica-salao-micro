package com.clinicsalon.appointment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationServiceClientFallbackFactory implements FallbackFactory<NotificationServiceClient> {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClientFallbackFactory.class);
    
    @Override
    public NotificationServiceClient create(Throwable cause) {
        log.error("Fallback para NotificationServiceClient ativado devido a: {}", cause.getMessage());
        
        return new NotificationServiceClient() {
            @Override
            public ResponseEntity<Map<String, Object>> sendEmail(Map<String, Object> emailRequest) {
                log.warn("Usando fallback para sendEmail: {}", emailRequest.get("subject"));
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Serviço de notificações indisponível no momento");
                return ResponseEntity.ok(response);
            }
            
            @Override
            public ResponseEntity<Map<String, Object>> sendSms(Map<String, Object> smsRequest) {
                log.warn("Usando fallback para sendSms: {}", smsRequest.get("phoneNumber"));
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Serviço de notificações indisponível no momento");
                return ResponseEntity.ok(response);
            }
            
            @Override
            public ResponseEntity<Map<String, Object>> sendPushNotification(Map<String, Object> pushRequest) {
                log.warn("Usando fallback para sendPushNotification: {}", pushRequest.get("title"));
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Serviço de notificações indisponível no momento");
                return ResponseEntity.ok(response);
            }
        };
    }
}
