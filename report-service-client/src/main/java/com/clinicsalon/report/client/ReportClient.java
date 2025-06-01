package com.clinicsalon.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "report-service", fallbackFactory = ReportClientFallbackFactory.class)
public interface ReportClient {

    /**
     * Solicita a geração de um relatório
     * @param request Detalhes do relatório a ser gerado
     * @return Resposta contendo o relatório gerado
     */
    @PostMapping("/api/reports")
    ResponseEntity<ReportResponse> generateReport(@RequestBody ReportRequest request);
    
    /**
     * Download de um relatório com conteúdo em bytes
     * @param request Detalhes do relatório a ser gerado e baixado
     * @return Conteúdo do relatório em bytes
     */
    @PostMapping("/api/reports/download")
    ResponseEntity<byte[]> downloadReport(@RequestBody ReportRequest request);
    
    /**
     * Verifica o status de um relatório por ID
     * @param reportId ID do relatório
     * @return Resposta contendo o status do relatório
     */
    @GetMapping("/api/reports/{reportId}")
    ResponseEntity<ReportResponse> getReportStatus(@PathVariable String reportId);
}
