package com.clinicsalon.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ProfessionalClientFallbackFactory implements FallbackFactory<ProfessionalClient> {

    @Override
    public ProfessionalClient create(Throwable cause) {
        return new ProfessionalClientFallback(cause);
    }

    @Slf4j
    static class ProfessionalClientFallback implements ProfessionalClient {
        private final Throwable cause;

        ProfessionalClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public Optional<ProfessionalDto> getProfessionalById(Long professionalId) {
            log.error("Fallback para getProfessionalById. ProfessionalId: {}, erro: {}", professionalId, cause.getMessage());
            return Optional.empty();
        }

        @Override
        public List<ProfessionalDto> getAllProfessionals() {
            log.error("Fallback para getAllProfessionals. Erro: {}", cause.getMessage());
            return Collections.emptyList();
        }

        @Override
        public String getProfessionalName(Long professionalId) {
            log.error("Fallback para getProfessionalName. ProfessionalId: {}, erro: {}", professionalId, cause.getMessage());
            return "Profissional #" + professionalId;
        }

        @Override
        public List<ProfessionalDto> getProfessionalsBySpecialization(String specialization) {
            log.error("Fallback para getProfessionalsBySpecialization. Specialization: {}, erro: {}", specialization, cause.getMessage());
            return Collections.emptyList();
        }
        
        @Override
        public List<ProfessionalDto> getActiveProfessionals() {
            log.error("Fallback para getActiveProfessionals. Erro: {}", cause.getMessage());
            return Collections.emptyList();
        }
    }
}
