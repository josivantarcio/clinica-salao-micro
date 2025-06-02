package com.clinicsalon.appointment.service;

import com.clinicsalon.appointment.client.LoyaltyServiceClient;
import com.clinicsalon.appointment.model.Appointment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço responsável por integrar o appointment-service com o loyalty-service
 * para gerenciar pontos de fidelidade dos clientes
 */
@Service
@RequiredArgsConstructor
public class LoyaltyIntegrationService {
    
    private static final Logger log = LoggerFactory.getLogger(LoyaltyIntegrationService.class);
    
    private final LoyaltyServiceClient loyaltyServiceClient;
    
    /**
     * Adiciona pontos de fidelidade para o cliente baseado no agendamento concluído
     * A regra padrão é 1 ponto para cada R$ 1,00 gasto
     * 
     * @param appointment Agendamento concluído
     * @return Mapa contendo o status da operação e informações dos pontos adicionados
     */
    @CircuitBreaker(name = "loyaltyService", fallbackMethod = "addLoyaltyPointsFallback")
    @Retry(name = "loyaltyService")
    public Map<String, Object> addLoyaltyPoints(Appointment appointment) {
        log.info("Adicionando pontos de fidelidade para cliente ID: {} pelo agendamento ID: {}", 
                appointment.getClientId(), appointment.getId());
        
        // Calcula os pontos baseado no valor gasto (1 ponto para cada real)
        int points = appointment.getPrice().setScale(0, RoundingMode.DOWN).intValue();
        
        Map<String, Object> pointsRequest = new HashMap<>();
        pointsRequest.put("clientId", appointment.getClientId());
        pointsRequest.put("points", points);
        pointsRequest.put("description", "Pontos pelo agendamento #" + appointment.getId());
        pointsRequest.put("sourceId", appointment.getId().toString());
        pointsRequest.put("sourceType", "APPOINTMENT");
        
        ResponseEntity<Map<String, Object>> response = loyaltyServiceClient.addPoints(pointsRequest);
        
        if (response != null && response.hasBody()) {
            Map<String, Object> body = response.getBody();
            if (body != null && "SUCCESS".equals(body.get("status"))) {
                log.info("Pontos de fidelidade adicionados com sucesso: {} pontos para cliente ID: {}", 
                        points, appointment.getClientId());
                return body;
            }
        }
        
        log.error("Erro ao adicionar pontos de fidelidade para cliente ID: {}", appointment.getClientId());
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", "Não foi possível adicionar os pontos de fidelidade");
        return errorResponse;
    }
    
    /**
     * Verifica o saldo de pontos de fidelidade do cliente
     * 
     * @param clientId ID do cliente
     * @return Mapa contendo o saldo de pontos e informações da conta de fidelidade
     */
    @CircuitBreaker(name = "loyaltyService", fallbackMethod = "getPointsBalanceFallback")
    @Retry(name = "loyaltyService")
    public Map<String, Object> getPointsBalance(Long clientId) {
        log.info("Verificando saldo de pontos de fidelidade para cliente ID: {}", clientId);
        
        ResponseEntity<Map<String, Object>> response = loyaltyServiceClient.getPointsBalance(clientId);
        
        if (response != null && response.hasBody()) {
            Map<String, Object> body = response.getBody();
            if (body != null) {
                Object points = body.get("points");
                log.info("Saldo de pontos recuperado com sucesso para cliente ID: {}: {} pontos", 
                        clientId, points != null ? points : 0);
                return body;
            }
        }
        
        log.error("Erro ao verificar saldo de pontos para cliente ID: {}", clientId);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", "Não foi possível verificar o saldo de pontos");
        errorResponse.put("points", 0);
        return errorResponse;
    }
    
    /**
     * Verifica se o cliente tem uma conta de fidelidade e cria uma se não existir
     * 
     * @param clientId ID do cliente
     * @return Mapa contendo informações da conta de fidelidade
     */
    @CircuitBreaker(name = "loyaltyService", fallbackMethod = "getLoyaltyAccountFallback")
    @Retry(name = "loyaltyService")
    public Map<String, Object> ensureLoyaltyAccount(Long clientId) {
        log.info("Verificando conta de fidelidade para cliente ID: {}", clientId);
        
        ResponseEntity<Map<String, Object>> response = loyaltyServiceClient.getLoyaltyAccount(clientId);
        
        if (response.getBody() != null) {
            log.info("Conta de fidelidade encontrada/criada para cliente ID: {}", clientId);
            return response.getBody();
        } else {
            log.error("Erro ao verificar conta de fidelidade para cliente ID: {}", clientId);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Não foi possível verificar a conta de fidelidade");
            return errorResponse;
        }
    }
    
    // Métodos de fallback
    
    public Map<String, Object> addLoyaltyPointsFallback(Appointment appointment, Throwable t) {
        log.error("Fallback para adição de pontos de fidelidade ativado: {}", t.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("status", "ERROR");
        fallbackResponse.put("message", "Serviço de fidelidade temporariamente indisponível");
        fallbackResponse.put("pointsAdded", 0);
        return fallbackResponse;
    }
    
    public Map<String, Object> getPointsBalanceFallback(Long clientId, Throwable t) {
        log.error("Fallback para verificação de saldo de pontos ativado: {}", t.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("status", "ERROR");
        fallbackResponse.put("message", "Serviço de fidelidade temporariamente indisponível");
        fallbackResponse.put("points", 0);
        return fallbackResponse;
    }
    
    public Map<String, Object> getLoyaltyAccountFallback(Long clientId, Throwable t) {
        log.error("Fallback para verificação de conta de fidelidade ativado: {}", t.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("status", "ERROR");
        fallbackResponse.put("message", "Serviço de fidelidade temporariamente indisponível");
        return fallbackResponse;
    }
}
