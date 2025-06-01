package com.clinicsalon.report.service;

import com.clinicsalon.report.client.AppointmentClient;
import com.clinicsalon.report.client.AppointmentDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço para buscar dados de agendamentos
 * Será implementado completamente quando o appointment-service-client estiver disponível
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentDataService {

    private final AppointmentClient appointmentClient;

    /**
     * Busca agendamentos de um cliente em um período
     */
    @CircuitBreaker(name = "appointmentService", fallbackMethod = "getClientAppointmentsFallback")
    public List<Map<String, Object>> getClientAppointments(Long clientId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching appointments for client ID: {} from {} to {}", clientId, startDate, endDate);
        
        // Usa o Feign client para buscar os dados reais de agendamentos
        List<AppointmentDto> appointments = appointmentClient.getAppointmentsByClientId(clientId, startDate, endDate);
        return appointments.stream()
                .map(this::mapAppointmentToReportData)
                .collect(Collectors.toList());
    }

    /**
     * Busca agenda de um profissional em um período
     */
    @CircuitBreaker(name = "appointmentService", fallbackMethod = "getProfessionalScheduleFallback")
    public List<Map<String, Object>> getProfessionalSchedule(Long professionalId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching schedule for professional ID: {} from {} to {}", professionalId, startDate, endDate);
        
        // Usa o Feign client para buscar os dados reais da agenda do profissional
        List<AppointmentDto> appointments = appointmentClient.getAppointmentsByProfessionalId(professionalId, startDate, endDate);
        return appointments.stream()
                .map(this::mapAppointmentToProfessionalSchedule)
                .collect(Collectors.toList());
    }

    /**
     * Busca resumo de receitas em um período
     */
    @CircuitBreaker(name = "appointmentService", fallbackMethod = "getRevenueSummaryFallback")
    public Map<String, Object> getRevenueSummary(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching revenue summary from {} to {}", startDate, endDate);
        
        // Implementação temporária - será substituída por chamada real ao client Feign
        return mockRevenueSummary();
    }

    /**
     * Busca popularidade de serviços em um período
     */
    @CircuitBreaker(name = "appointmentService", fallbackMethod = "getServicesPopularityFallback")
    public List<Map<String, Object>> getServicesPopularity(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching services popularity from {} to {}", startDate, endDate);
        
        // Implementação temporária - será substituída por chamada real ao client Feign
        return mockServicesPopularity();
    }

    // Métodos de fallback

    public List<Map<String, Object>> getClientAppointmentsFallback(Long clientId, LocalDate startDate, LocalDate endDate, Exception ex) {
        log.warn("Fallback for client appointments. Client ID: {}, Error: {}", clientId, ex.getMessage());
        return Collections.emptyList();
    }

    public List<Map<String, Object>> getProfessionalScheduleFallback(Long professionalId, LocalDate startDate, LocalDate endDate, Exception ex) {
        log.warn("Fallback for professional schedule. Professional ID: {}, Error: {}", professionalId, ex.getMessage());
        return Collections.emptyList();
    }

    public Map<String, Object> getRevenueSummaryFallback(LocalDate startDate, LocalDate endDate, Exception ex) {
        log.warn("Fallback for revenue summary. Error: {}", ex.getMessage());
        return new HashMap<>();
    }

    public List<Map<String, Object>> getServicesPopularityFallback(LocalDate startDate, LocalDate endDate, Exception ex) {
        log.warn("Fallback for services popularity. Error: {}", ex.getMessage());
        return Collections.emptyList();
    }

    // Métodos auxiliares para mapear DTOs para formato de relatório

    /**
     * Mapeia um AppointmentDto para o formato usado nos relatórios de cliente
     */
    private Map<String, Object> mapAppointmentToReportData(AppointmentDto appointment) {
        Map<String, Object> data = new HashMap<>();
        data.put("appointmentId", appointment.getId());
        data.put("clientId", appointment.getClientId());
        data.put("clientName", appointment.getClientName());
        data.put("serviceDate", appointment.getAppointmentDate());
        data.put("serviceName", appointment.getServiceName());
        data.put("professionalName", appointment.getProfessionalName());
        data.put("status", appointment.getStatus());
        data.put("price", appointment.getPrice());
        return data;
    }

    /**
     * Mapeia um AppointmentDto para o formato usado nos relatórios de agenda de profissional
     */
    private Map<String, Object> mapAppointmentToProfessionalSchedule(AppointmentDto appointment) {
        Map<String, Object> data = new HashMap<>();
        data.put("appointmentId", appointment.getId());
        data.put("professionalId", appointment.getProfessionalId());
        data.put("professionalName", appointment.getProfessionalName());
        data.put("clientId", appointment.getClientId());
        data.put("clientName", appointment.getClientName());
        data.put("serviceDate", appointment.getAppointmentDate());
        data.put("serviceName", appointment.getServiceName());
        data.put("status", appointment.getStatus());
        data.put("price", appointment.getPrice());
        return data;
    }

    private Map<String, Object> mockRevenueSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", 8750.0);
        summary.put("appointmentsCount", 125);
        summary.put("averageTicket", 70.0);
        summary.put("mostPopularService", "Corte de Cabelo");
        summary.put("mostRevenueService", "Coloração");
        
        return summary;
    }

    private List<Map<String, Object>> mockServicesPopularity() {
        Map<String, Object> service1 = new HashMap<>();
        service1.put("serviceName", "Corte de Cabelo");
        service1.put("count", 45);
        service1.put("percentage", 36.0);
        service1.put("revenue", 3600.0);

        Map<String, Object> service2 = new HashMap<>();
        service2.put("serviceName", "Coloração");
        service2.put("count", 30);
        service2.put("percentage", 24.0);
        service2.put("revenue", 3000.0);

        Map<String, Object> service3 = new HashMap<>();
        service3.put("serviceName", "Manicure");
        service3.put("count", 25);
        service3.put("percentage", 20.0);
        service3.put("revenue", 1250.0);

        Map<String, Object> service4 = new HashMap<>();
        service4.put("serviceName", "Outros");
        service4.put("count", 25);
        service4.put("percentage", 20.0);
        service4.put("revenue", 900.0);

        return List.of(service1, service2, service3, service4);
    }
}
