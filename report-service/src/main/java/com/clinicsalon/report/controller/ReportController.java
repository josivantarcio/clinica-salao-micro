package com.clinicsalon.report.controller;

import com.clinicsalon.report.dto.ReportRequest;
import com.clinicsalon.report.dto.ReportResponse;
import com.clinicsalon.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    /**
     * Endpoint para solicitar a geração de um relatório
     */
    @PostMapping
    public ResponseEntity<ReportResponse> generateReport(@Valid @RequestBody ReportRequest request) {
        log.info("Report generation request received: {}", request.getReportType());
        return new ResponseEntity<>(reportService.generateReport(request), HttpStatus.CREATED);
    }

    /**
     * Endpoint para download de um relatório com o conteúdo em bytes
     * Retorna o relatório como um arquivo para download
     */
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadReport(@Valid @RequestBody ReportRequest request) {
        log.info("Report download request received: {}", request.getReportType());
        
        ReportResponse report = reportService.generateReport(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(report.getContentType()));
        headers.setContentDispositionFormData(
                "attachment", 
                generateFileName(report));
        headers.setContentLength(report.getFileSize());
        
        return new ResponseEntity<>(report.getReportContent(), headers, HttpStatus.OK);
    }

    /**
     * Endpoint para verificar o status de um relatório por ID
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReportStatus(@PathVariable String reportId) {
        log.info("Report status request received for ID: {}", reportId);
        
        // Implementação simples - em um cenário real, buscaríamos o status no banco de dados
        return ResponseEntity.ok(ReportResponse.builder()
                .reportId(reportId)
                .status("COMPLETED")
                .build());
    }

    /**
     * Gera um nome de arquivo baseado no tipo de relatório e formato
     */
    private String generateFileName(ReportResponse report) {
        String extension = switch (report.getContentType()) {
            case "application/pdf" -> "pdf";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx";
            case "text/csv" -> "csv";
            default -> "pdf";
        };
        
        return report.getReportName().replaceAll("\\s+", "_") + "_" + 
               report.getGeneratedAt().toString().replaceAll("[^0-9]", "") + "." + extension;
    }
}
