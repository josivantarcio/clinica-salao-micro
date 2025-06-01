package com.clinicsalon.finance.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class ClientClientFallback implements ClientClient {

    @Override
    public Map<String, Object> getClientById(UUID id) {
        log.warn("Fallback executed for getClientById with id: {}", id);
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("id", id);
        fallbackResponse.put("name", "Cliente indisponível");
        fallbackResponse.put("email", "indisponivel@exemplo.com");
        fallbackResponse.put("phone", "N/A");
        fallbackResponse.put("fallback", true);
        return fallbackResponse;
    }

    @Override
    public Map<String, Object> getClientBasicInfo(UUID id) {
        log.warn("Fallback executed for getClientBasicInfo with id: {}", id);
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("id", id);
        fallbackResponse.put("name", "Cliente indisponível");
        fallbackResponse.put("fallback", true);
        return fallbackResponse;
    }
}
