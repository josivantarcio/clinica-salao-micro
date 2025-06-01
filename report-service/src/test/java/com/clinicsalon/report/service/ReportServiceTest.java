package com.clinicsalon.report.service;

import com.clinicsalon.report.dto.ReportRequest;
import com.clinicsalon.report.dto.ReportResponse;
import com.clinicsalon.report.dto.ReportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    
    @Mock
    private ClientDataService clientDataService;
    
    @Mock
    private AppointmentDataService appointmentDataService;
    
    @Mock
    private LoyaltyDataService loyaltyDataService;
    
    @InjectMocks
    private ReportService reportService;
    
    private ReportRequest clientAppointmentsRequest;
    private ReportRequest professionalScheduleRequest;
    private ReportRequest revenueSummaryRequest;
    private ReportRequest loyaltyPointsSummaryRequest;
    
    @BeforeEach
    void setUp() {
        // Configuração comum
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        // Requisição para relatório de agendamentos de cliente
        clientAppointmentsRequest = ReportRequest.builder()
                .reportType(ReportType.CLIENT_APPOINTMENTS)
                .startDate(startDate)
                .endDate(endDate)
                .clientId(1L)
                .format("PDF")
                .build();
        
        // Requisição para relatório de agenda de profissional
        professionalScheduleRequest = ReportRequest.builder()
                .reportType(ReportType.PROFESSIONAL_SCHEDULE)
                .startDate(startDate)
                .endDate(endDate)
                .professionalId(1L)
                .format("EXCEL")
                .build();
        
        // Requisição para relatório de resumo de receitas
        revenueSummaryRequest = ReportRequest.builder()
                .reportType(ReportType.REVENUE_SUMMARY)
                .startDate(startDate)
                .endDate(endDate)
                .format("PDF")
                .build();
        
        // Requisição para relatório de resumo de pontos de fidelidade
        loyaltyPointsSummaryRequest = ReportRequest.builder()
                .reportType(ReportType.LOYALTY_POINTS_SUMMARY)
                .startDate(startDate)
                .endDate(endDate)
                .format("CSV")
                .build();
    }
    
    @Test
    void generateClientAppointmentsReport_Success() {
        // Act
        ReportResponse response = reportService.generateReport(clientAppointmentsRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(ReportType.CLIENT_APPOINTMENTS, response.getReportType());
        assertEquals("Agendamentos do Cliente", response.getReportName());
        assertEquals("application/pdf", response.getContentType());
        assertNotNull(response.getReportId());
        assertNotNull(response.getGeneratedAt());
        assertEquals("COMPLETED", response.getStatus());
        assertTrue(response.getFileSize() > 0);
    }
    
    @Test
    void generateProfessionalScheduleReport_Success() {
        // Act
        ReportResponse response = reportService.generateReport(professionalScheduleRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(ReportType.PROFESSIONAL_SCHEDULE, response.getReportType());
        assertEquals("Agenda do Profissional", response.getReportName());
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertNotNull(response.getReportId());
        assertNotNull(response.getGeneratedAt());
        assertEquals("COMPLETED", response.getStatus());
        assertTrue(response.getFileSize() > 0);
    }
    
    @Test
    void generateRevenueSummaryReport_Success() {
        // Act
        ReportResponse response = reportService.generateReport(revenueSummaryRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(ReportType.REVENUE_SUMMARY, response.getReportType());
        assertEquals("Resumo de Receitas", response.getReportName());
        assertEquals("application/pdf", response.getContentType());
        assertNotNull(response.getReportId());
        assertNotNull(response.getGeneratedAt());
        assertEquals("COMPLETED", response.getStatus());
        assertTrue(response.getFileSize() > 0);
    }
    
    @Test
    void generateLoyaltyPointsSummaryReport_Success() {
        // Act
        ReportResponse response = reportService.generateReport(loyaltyPointsSummaryRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(ReportType.LOYALTY_POINTS_SUMMARY, response.getReportType());
        assertEquals("Resumo de Pontos de Fidelidade", response.getReportName());
        assertEquals("text/csv", response.getContentType());
        assertNotNull(response.getReportId());
        assertNotNull(response.getGeneratedAt());
        assertEquals("COMPLETED", response.getStatus());
        assertTrue(response.getFileSize() > 0);
    }
    
    @Test
    void generateReport_UnsupportedType_ThrowsException() {
        // Arrange
        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("testParam", "testValue");
        
        ReportRequest invalidRequest = ReportRequest.builder()
                .reportType(null) // Tipo de relatório inválido
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .additionalParams(additionalParams)
                .build();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reportService.generateReport(invalidRequest);
        });
    }
    
    @Test
    void determineContentType_DefaultToPdf() {
        // Arrange
        ReportRequest request = ReportRequest.builder()
                .reportType(ReportType.CLIENT_APPOINTMENTS)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .format(null) // Formato não especificado
                .build();
        
        // Act
        ReportResponse response = reportService.generateReport(request);
        
        // Assert
        assertEquals("application/pdf", response.getContentType());
    }
}
