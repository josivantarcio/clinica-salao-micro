package com.clinicsalon.appointment.controller;

import com.clinicsalon.appointment.service.LoyaltyIntegrationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador para expor endpoints relacionados ao sistema de fidelidade
 * dentro do contexto do appointment-service
 */
@RestController
@RequestMapping("/api/appointments/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyController.class);
    
    private final LoyaltyIntegrationService loyaltyService;
    
    /**
     * Retorna o saldo de pontos de fidelidade de um cliente
     * 
     * @param clientId ID do cliente
     * @return Saldo e informações da conta de fidelidade
     */
    @GetMapping("/points/balance/{clientId}")
    public ResponseEntity<Map<String, Object>> getPointsBalance(@PathVariable Long clientId) {
        log.info("Obtendo saldo de pontos de fidelidade para cliente ID: {}", clientId);
        Map<String, Object> result = loyaltyService.getPointsBalance(clientId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Verifica ou cria uma conta de fidelidade para um cliente
     * 
     * @param clientId ID do cliente
     * @return Informações da conta de fidelidade
     */
    @GetMapping("/account/{clientId}")
    public ResponseEntity<Map<String, Object>> getLoyaltyAccount(@PathVariable Long clientId) {
        log.info("Verificando conta de fidelidade para cliente ID: {}", clientId);
        Map<String, Object> result = loyaltyService.ensureLoyaltyAccount(clientId);
        return ResponseEntity.ok(result);
    }
}
