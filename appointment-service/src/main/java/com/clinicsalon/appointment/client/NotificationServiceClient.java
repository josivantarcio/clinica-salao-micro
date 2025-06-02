package com.clinicsalon.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", fallbackFactory = NotificationServiceClientFallbackFactory.class)
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/send-email")
    ResponseEntity<Map<String, Object>> sendEmail(@RequestBody Map<String, Object> emailRequest);
    
    @PostMapping("/api/notifications/send-sms")
    ResponseEntity<Map<String, Object>> sendSms(@RequestBody Map<String, Object> smsRequest);
    
    @PostMapping("/api/notifications/send-push")
    ResponseEntity<Map<String, Object>> sendPushNotification(@RequestBody Map<String, Object> pushRequest);
}
