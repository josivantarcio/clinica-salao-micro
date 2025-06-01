package com.clinicsalon.professional.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fábrica de fallback para o cliente Feign do serviço de profissionais.
 * Implementa estratégias de fallback quando o serviço está indisponível.
 */
@Component
public class ProfessionalClientFallbackFactory implements FallbackFactory<ProfessionalClient> {

    private static final Logger logger = LoggerFactory.getLogger(ProfessionalClientFallbackFactory.class);

    @Override
    public ProfessionalClient create(Throwable cause) {
        return new ProfessionalClientFallback(cause);
    }

    /**
     * Implementação de fallback para o cliente de profissionais.
     * Retorna respostas padrão quando o serviço está indisponível.
     */
    static class ProfessionalClientFallback implements ProfessionalClient {
        
        private final Throwable cause;
        
        ProfessionalClientFallback(Throwable cause) {
            this.cause = cause;
        }
        
        @Override
        public ResponseEntity<ProfessionalResponse> findById(Long id) {
            logger.error("Falha ao buscar profissional por ID: {}, causa: {}", id, cause.getMessage());
            // Retorna um profissional com dados mínimos
            ProfessionalResponse fallbackResponse = ProfessionalResponse.builder()
                    .id(id)
                    .name("Indisponível")
                    .role("Indisponível")
                    .specialization("Indisponível")
                    .active(false)
                    .build();
            return ResponseEntity.ok(fallbackResponse);
        }
        
        @Override
        public ResponseEntity<String> findNameById(Long id) {
            logger.error("Falha ao buscar nome do profissional por ID: {}, causa: {}", id, cause.getMessage());
            return ResponseEntity.ok("Profissional Indisponível");
        }
        
        @Override
        public ResponseEntity<List<ProfessionalResponse>> findAllActive() {
            logger.error("Falha ao buscar profissionais ativos, causa: {}", cause.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        @Override
        public ResponseEntity<List<ProfessionalResponse>> findBySpecialization(String specialization) {
            logger.error("Falha ao buscar profissionais por especialização: {}, causa: {}", specialization, cause.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}
