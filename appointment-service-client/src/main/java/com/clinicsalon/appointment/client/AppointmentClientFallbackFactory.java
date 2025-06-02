package com.clinicsalon.appointment.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppointmentClientFallbackFactory implements FallbackFactory<AppointmentClient> {

    @Override
    public AppointmentClient create(Throwable cause) {
        return new AppointmentClientFallback(cause);
    }
    
    @Slf4j
    static class AppointmentClientFallback implements AppointmentClient {
        private final Throwable cause;
        
        public AppointmentClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public ResponseEntity<AppointmentResponse> getAppointmentById(String id) {
            log.error("Fallback para getAppointmentById. Id: {}, Erro: {}", id, cause.getMessage());
            return ResponseEntity.ok(
                AppointmentResponse.builder()
                    .id(id)
                    .status("UNAVAILABLE")
                    .build()
            );
        }

        @Override
        public ResponseEntity<PagedAppointmentsResponse> getAppointments(String clientId, String professionalId,
                LocalDate startDate, LocalDate endDate, String status, int page, int size) {
            log.error("Fallback para getAppointments. ClientId: {}, ProfessionalId: {}, Erro: {}", 
                    clientId, professionalId, cause.getMessage());
            
            return ResponseEntity.ok(
                PagedAppointmentsResponse.builder()
                    .content(new ArrayList<>())
                    .pageNumber(page)
                    .pageSize(size)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build()
            );
        }

        @Override
        public ResponseEntity<List<AppointmentResponse>> getAppointmentsByClientId(String clientId) {
            log.error("Fallback para getAppointmentsByClientId. ClientId: {}, Erro: {}", 
                    clientId, cause.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }

        @Override
        public ResponseEntity<List<AppointmentResponse>> getAppointmentsByProfessionalId(String professionalId) {
            log.error("Fallback para getAppointmentsByProfessionalId. ProfessionalId: {}, Erro: {}", 
                    professionalId, cause.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }

        @Override
        public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDate(LocalDate date) {
            log.error("Fallback para getAppointmentsByDate. Date: {}, Erro: {}", 
                    date, cause.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }

        @Override
        public ResponseEntity<AppointmentResponse> createAppointment(AppointmentRequest request) {
            log.error("Fallback para createAppointment. ClientId: {}, ProfessionalId: {}, Erro: {}", 
                    request.getClientId(), request.getProfessionalId(), cause.getMessage());
            
            return ResponseEntity.ok(
                AppointmentResponse.builder()
                    .id("UNAVAILABLE")
                    .clientId(request.getClientId())
                    .professionalId(request.getProfessionalId())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .status("ERROR")
                    .services(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .build()
            );
        }
    }
}
