package com.clinicsalon.report.controller;

import com.clinicsalon.report.dto.ReportRequest;
import com.clinicsalon.report.dto.ReportResponse;
import com.clinicsalon.report.dto.ReportType;
import com.clinicsalon.report.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ReportService reportService;
    
    private ReportRequest reportRequest;
    private ReportResponse reportResponse;
    
    @BeforeEach
    void setUp() {
        // Configuração da requisição de teste
        reportRequest = ReportRequest.builder()
                .reportType(ReportType.CLIENT_APPOINTMENTS)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .clientId(1L)
                .format("PDF")
                .build();
        
        // Configuração da resposta mockada
        reportResponse = ReportResponse.builder()
                .reportId(UUID.randomUUID().toString())
                .reportType(ReportType.CLIENT_APPOINTMENTS)
                .reportName("Agendamentos do Cliente")
                .contentType("application/pdf")
                .reportContent("Relatório de Agendamentos do Cliente".getBytes())
                .generatedAt(LocalDateTime.now())
                .status("COMPLETED")
                .fileSize(36L)
                .build();
    }
    
    @Test
    void generateReport_Success() throws Exception {
        // Arrange
        when(reportService.generateReport(any(ReportRequest.class))).thenReturn(reportResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").isNotEmpty())
                .andExpect(jsonPath("$.reportType").value("CLIENT_APPOINTMENTS"))
                .andExpect(jsonPath("$.reportName").value("Agendamentos do Cliente"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    void downloadReport_Success() throws Exception {
        // Arrange
        when(reportService.generateReport(any(ReportRequest.class))).thenReturn(reportResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/reports/download")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", "application/pdf"));
    }
    
    @Test
    void getReportStatus_Success() throws Exception {
        // Arrange
        String reportId = UUID.randomUUID().toString();
        ReportResponse statusResponse = ReportResponse.builder()
                .reportId(reportId)
                .status("COMPLETED")
                .build();
                
        // Act & Assert
        mockMvc.perform(get("/api/reports/" + reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    void generateReport_InvalidRequest_BadRequest() throws Exception {
        // Arrange
        ReportRequest invalidRequest = ReportRequest.builder()
                .reportType(null) // Tipo de relatório ausente (obrigatório)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
