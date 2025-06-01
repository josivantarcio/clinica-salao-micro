package com.clinicsalon.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "client-service", fallbackFactory = ClientClientFallbackFactory.class)
public interface ClientClient {

    @GetMapping("/api/clients/{clientId}")
    Optional<ClientDto> getClientById(@PathVariable Long clientId);

    @GetMapping("/api/clients")
    List<ClientDto> getAllClients();
    
    @GetMapping("/api/clients/{clientId}/name")
    String getClientName(@PathVariable Long clientId);
}
