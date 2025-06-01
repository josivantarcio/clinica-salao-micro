package com.clinicsalon.report.service;

import com.clinicsalon.report.client.ClientClient;
import com.clinicsalon.report.client.ClientDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientDataService {

    private final ClientClient clientClient;

    /**
     * Busca informações de um cliente pelo ID
     */
    @CircuitBreaker(name = "clientService", fallbackMethod = "getClientByIdFallback")
    public ClientDto getClientById(Long clientId) {
        log.info("Fetching client data for ID: {}", clientId);
        Optional<ClientDto> clientOptional = clientClient.getClientById(clientId);
        return clientOptional.orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + clientId));
    }

    /**
     * Busca todos os clientes
     */
    @CircuitBreaker(name = "clientService", fallbackMethod = "getAllClientsFallback")
    public List<ClientDto> getAllClients() {
        log.info("Fetching all clients data");
        return clientClient.getAllClients();
    }
    
    /**
     * Busca o nome de um cliente pelo ID
     */
    @CircuitBreaker(name = "clientService", fallbackMethod = "getClientNameFallback")
    public String getClientName(Long clientId) {
        log.info("Fetching client name for ID: {}", clientId);
        return clientClient.getClientName(clientId);
    }

    /**
     * Método de fallback para getClientById
     */
    public ClientDto getClientByIdFallback(Long clientId, Exception ex) {
        log.warn("Fallback for client lookup. Client ID: {}, Error: {}", clientId, ex.getMessage());
        return ClientDto.builder()
                .id(clientId)
                .name("Cliente Indisponível #" + clientId)
                .email("indisponivel@exemplo.com")
                .build();
    }

    /**
     * Método de fallback para getAllClients
     */
    public List<ClientDto> getAllClientsFallback(Exception ex) {
        log.warn("Fallback for all clients lookup. Error: {}", ex.getMessage());
        return Collections.emptyList();
    }
    
    /**
     * Método de fallback para getClientName
     */
    public String getClientNameFallback(Long clientId, Exception ex) {
        log.warn("Fallback for client name lookup. Client ID: {}, Error: {}", clientId, ex.getMessage());
        return "Cliente #" + clientId;
    }
}
