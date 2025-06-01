package com.clinicsalon.finance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Endpoints para verificação de saúde do serviço")
@Slf4j
public class HealthCheckController {
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${info.app.version:1.0.0}")
    private String appVersion;
    
    @GetMapping
    @Operation(summary = "Verifica a saúde do serviço")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Verificando saúde do serviço finance-service");
        
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", applicationName);
        healthInfo.put("version", appVersion);
        healthInfo.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(healthInfo);
    }
}
