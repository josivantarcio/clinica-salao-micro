package com.clinicsalon.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class AppointmentClientFallbackFactory implements FallbackFactory<AppointmentClient> {

    @Override
    public AppointmentClient create(Throwable cause) {
        return new AppointmentClientFallback(cause);
    }

    @Slf4j
    static class AppointmentClientFallback implements AppointmentClient {
        private final Throwable cause;

        AppointmentClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public List<AppointmentDto> getAppointmentsByClientId(Long clientId, LocalDate startDate, LocalDate endDate) {
            log.error("Fallback para getAppointmentsByClientId. ClientId: {}, erro: {}", clientId, cause.getMessage());
            return Collections.emptyList();
        }

        @Override
        public List<AppointmentDto> getAppointmentsByProfessionalId(Long professionalId, LocalDate startDate, LocalDate endDate) {
            log.error("Fallback para getAppointmentsByProfessionalId. ProfessionalId: {}, erro: {}", professionalId, cause.getMessage());
            return Collections.emptyList();
        }
    }
}
