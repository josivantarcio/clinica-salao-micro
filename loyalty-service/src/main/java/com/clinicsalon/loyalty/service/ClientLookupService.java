package com.clinicsalon.loyalty.service;

import com.clinicsalon.client.client.ClientClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * Serviço para buscar informações de clientes no microsserviço client-service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientLookupService {

    private final ClientClient clientClient;
    
    /**
     * Busca o nome do cliente pelo ID
     * Utiliza circuit breaker para lidar com falhas no serviço de clientes
     */
    @CircuitBreaker(name = "clientService", fallbackMethod = "getClientNameFallback")
    public String getClientName(Long clientId) {
        log.info("Looking up client name for ID: {}", clientId);
        
        // Chamada real ao cliente Feign para buscar o nome do cliente
        ResponseEntity<?> response = clientClient.getClientById(clientId);
        if (response != null && response.getBody() != null) {
            // Assumindo que o response body é um Map<String, Object>
            @SuppressWarnings("unchecked")
            Map<String, Object> clientData = (Map<String, Object>) response.getBody();
            if (clientData.containsKey("name")) {
                return (String) clientData.get("name");
            }
        }
        // Retornar um nome genérico se o response ou body for null ou não tiver nome
        return "Cliente " + clientId;
    }
    
    /**
     * Método de fallback para quando o serviço de clientes está indisponível
     */
    public String getClientNameFallback(Long clientId, Exception ex) {
        log.warn("Fallback for client lookup. Client ID: {}, Error: {}", clientId, ex.getMessage());
        return "Cliente #" + clientId;
    }
}
