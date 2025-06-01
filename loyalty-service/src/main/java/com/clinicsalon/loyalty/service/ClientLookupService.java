package com.clinicsalon.loyalty.service;

import com.clinicsalon.client.client.ClientClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return clientClient.getClientById(clientId).getBody().getName();
    }
    
    /**
     * Método de fallback para quando o serviço de clientes está indisponível
     */
    public String getClientNameFallback(Long clientId, Exception ex) {
        log.warn("Fallback for client lookup. Client ID: {}, Error: {}", clientId, ex.getMessage());
        return "Cliente #" + clientId;
    }
}
