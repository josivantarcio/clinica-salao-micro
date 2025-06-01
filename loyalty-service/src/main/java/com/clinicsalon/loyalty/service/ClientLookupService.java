package com.clinicsalon.loyalty.service;

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

    // Aqui seria injetado o cliente Feign para comunicação com o client-service
    // private final ClientServiceClient clientServiceClient;
    
    /**
     * Busca o nome do cliente pelo ID
     * Utiliza circuit breaker para lidar com falhas no serviço de clientes
     */
    @CircuitBreaker(name = "clientService", fallbackMethod = "getClientNameFallback")
    public String getClientName(Long clientId) {
        log.info("Looking up client name for ID: {}", clientId);
        
        // Aqui seria a chamada real ao cliente Feign
        // return clientServiceClient.getClientName(clientId);
        
        // Mock temporário até que o client-service-client seja implementado
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
