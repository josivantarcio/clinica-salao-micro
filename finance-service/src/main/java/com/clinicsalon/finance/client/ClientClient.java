package com.clinicsalon.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "client-service", path = "/client-service/api/v1/clients", fallback = ClientClientFallback.class)
public interface ClientClient {

    @GetMapping("/{id}")
    Map<String, Object> getClientById(@PathVariable("id") UUID id);
    
    @GetMapping("/{id}/basic-info")
    Map<String, Object> getClientBasicInfo(@PathVariable("id") UUID id);
}
