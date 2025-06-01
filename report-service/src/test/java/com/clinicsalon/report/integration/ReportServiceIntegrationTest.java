package com.clinicsalon.report.integration;

import com.clinicsalon.report.client.ProfessionalClient;
import com.clinicsalon.report.client.ProfessionalDto;
import com.clinicsalon.report.client.LoyaltyClient;
import com.clinicsalon.report.client.ClientClient;
import com.clinicsalon.report.client.AppointmentClient;
import com.clinicsalon.report.dto.ReportRequest;
import com.clinicsalon.report.dto.ReportResponse;
import com.clinicsalon.report.dto.ReportType;
import com.clinicsalon.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Teste de integração para verificar a comunicação entre o ReportService e outros microsserviços
 * através dos clientes Feign. Utiliza mocks para simular as respostas dos outros serviços.
 */
@SpringBootTest
@ActiveProfiles("test")
public class ReportServiceIntegrationTest {

    @Autowired
    private ReportService reportService;

    @MockBean
    private ProfessionalClient professionalClient;

    @MockBean
    private ClientClient clientClient;

    @MockBean
    private LoyaltyClient loyaltyClient;

    @MockBean
    private AppointmentClient appointmentClient;

    @Test
    void generateProfessionalScheduleReport_Success() {
        // Arrange
        Long professionalId = 1L;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        // Mock para o profissional
        ProfessionalDto professionalDto = ProfessionalDto.builder()
                .id(professionalId)
                .name("João Silva")
                .specialization("Cabeleireiro")
                .email("joao.silva@example.com")
                .phone("11987654321")
                .active(true)
                .build();
        
        when(professionalClient.getProfessionalById(professionalId)).thenReturn(Optional.of(professionalDto));
        
        // Mock para os agendamentos
        List<Map<String, Object>> appointments = new ArrayList<>();
        Map<String, Object> appointment = new HashMap<>();
        appointment.put("appointmentId", 1L);
        appointment.put("serviceDate", LocalDateTime.now());
        appointment.put("serviceName", "Corte Masculino");
        appointment.put("clientName", "Cliente Teste");
        appointment.put("status", "CONFIRMADO");
        appointment.put("price", 50.0);
        appointments.add(appointment);
        
        when(appointmentClient.getProfessionalAppointments(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(appointments);
        
        ReportRequest request = ReportRequest.builder()
                .reportType(ReportType.PROFESSIONAL_SCHEDULE)
                .startDate(startDate)
                .endDate(endDate)
                .professionalId(professionalId)
                .format("PDF")
                .build();
        
        // Act
        ReportResponse response = reportService.generateReport(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(ReportType.PROFESSIONAL_SCHEDULE, response.getReportType());
        assertEquals("Agenda do Profissional", response.getReportName());
        assertEquals("COMPLETED", response.getStatus());
        assertNotNull(response.getReportContent());
        assertTrue(response.getReportContent().length > 0);
    }

    @Test
    void generateLoyaltyPointsSummaryReport_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        // Mock para os dados de fidelidade
        Map<Long, Integer> clientPointsMap = new HashMap<>();
        clientPointsMap.put(1L, 500); // OURO
        clientPointsMap.put(2L, 100); // BRONZE
        clientPointsMap.put(3L, 300); // PRATA
        
        when(loyaltyClient.getAllClientsTotalPoints()).thenReturn(clientPointsMap);
        when(clientClient.getClientName(1L)).thenReturn("João Silva");
        when(clientClient.getClientName(2L)).thenReturn("Maria Oliveira");
        when(clientClient.getClientName(3L)).thenReturn("Pedro Santos");
        
        ReportRequest request = ReportRequest.builder()
                .reportType(ReportType.LOYALTY_POINTS_SUMMARY)
                .startDate(startDate)
                .endDate(endDate)
                .format("PDF")
                .build();
        
        // Act
        ReportResponse response = reportService.generateReport(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(ReportType.LOYALTY_POINTS_SUMMARY, response.getReportType());
        assertEquals("Resumo do Programa de Fidelidade", response.getReportName());
        assertEquals("COMPLETED", response.getStatus());
        assertNotNull(response.getReportContent());
        assertTrue(response.getReportContent().length > 0);
    }

    @Test
    void generateRevenueSummaryReport_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        // Mock para os dados de receita
        List<Map<String, Object>> revenueData = new ArrayList<>();
        Map<String, Object> serviceRevenue1 = new HashMap<>();
        serviceRevenue1.put("serviceName", "Corte Masculino");
        serviceRevenue1.put("count", 25);
        serviceRevenue1.put("percentage", 50.0);
        serviceRevenue1.put("revenue", 1250.0);
        revenueData.add(serviceRevenue1);
        
        Map<String, Object> serviceRevenue2 = new HashMap<>();
        serviceRevenue2.put("serviceName", "Corte Feminino");
        serviceRevenue2.put("count", 20);
        serviceRevenue2.put("percentage", 40.0);
        serviceRevenue2.put("revenue", 2000.0);
        revenueData.add(serviceRevenue2);
        
        Map<String, Object> revenueSummary = new HashMap<>();
        revenueSummary.put("totalRevenue", 3250.0);
        revenueSummary.put("appointmentsCount", 45);
        revenueSummary.put("averageTicket", 72.22);
        revenueSummary.put("mostPopularService", "Corte Masculino");
        revenueSummary.put("mostRevenueService", "Corte Feminino");
        
        when(appointmentClient.getRevenueSummary(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(revenueSummary);
        
        when(appointmentClient.getServiceRevenueBreakdown(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(revenueData);
        
        ReportRequest request = ReportRequest.builder()
                .reportType(ReportType.REVENUE_SUMMARY)
                .startDate(startDate)
                .endDate(endDate)
                .format("PDF")
                .build();
        
        // Act
        ReportResponse response = reportService.generateReport(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(ReportType.REVENUE_SUMMARY, response.getReportType());
        assertEquals("Resumo de Receitas", response.getReportName());
        assertEquals("COMPLETED", response.getStatus());
        assertNotNull(response.getReportContent());
        assertTrue(response.getReportContent().length > 0);
    }
}
