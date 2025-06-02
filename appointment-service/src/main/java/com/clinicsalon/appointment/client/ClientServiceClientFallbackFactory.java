package com.clinicsalon.appointment.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ClientServiceClientFallbackFactory implements FallbackFactory<ClientServiceClient> {
    
    private static final Logger log = LoggerFactory.getLogger(ClientServiceClientFallbackFactory.class);
    
    @Override
    public ClientServiceClient create(Throwable cause) {
        log.error("Fallback para ClientServiceClient ativado devido a: {}", cause.getMessage());
        
        return new ClientServiceClient() {
            @Override
            public Object findById(Long id) {
                log.warn("Usando fallback para findById do cliente com ID: {}", id);
                return null;
            }
            
            @Override
            public String getClientName(Long id) {
                log.warn("Usando fallback para getClientName do cliente com ID: {}", id);
                return "Cliente " + id;
            }
            
            @Override
            public String findNameById(Long id) {
                log.warn("Usando fallback para findNameById do cliente com ID: {}", id);
                return "Cliente " + id;
            }
        };
    }
}
