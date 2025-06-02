package com.clinicsalon.appointment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalServiceClientFallbackFactory implements FallbackFactory<ProfessionalServiceClient> {
    
    private static final Logger log = LoggerFactory.getLogger(ProfessionalServiceClientFallbackFactory.class);
    
    @Override
    public ProfessionalServiceClient create(Throwable cause) {
        log.error("Fallback para ProfessionalServiceClient ativado devido a: {}", cause.getMessage());
        
        return new ProfessionalServiceClient() {
            @Override
            public Object findById(Long id) {
                log.warn("Usando fallback para findById do profissional com ID: {}", id);
                return null;
            }
            
            @Override
            public String getProfessionalName(Long id) {
                log.warn("Usando fallback para getProfessionalName do profissional com ID: {}", id);
                return "Profissional " + id;
            }
            
            @Override
            public String findNameById(Long id) {
                log.warn("Usando fallback para findNameById do profissional com ID: {}", id);
                return "Profissional " + id;
            }
        };
    }
}
