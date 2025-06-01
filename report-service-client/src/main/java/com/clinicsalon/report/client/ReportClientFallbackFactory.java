package com.clinicsalon.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class ReportClientFallbackFactory implements FallbackFactory<ReportClient> {

    @Override
    public ReportClient create(Throwable cause) {
        return new ReportClientFallback(cause);
    }

    @Slf4j
    static class ReportClientFallback implements ReportClient {
        private final Throwable cause;

        ReportClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public ResponseEntity<ReportResponse> generateReport(ReportRequest request) {
            log.error("Fallback para generateReport. ReportType: {}, Erro: {}", 
                    request.getReportType(), cause.getMessage());
            
            return ResponseEntity.ok(ReportResponse.builder()
                    .reportId(UUID.randomUUID().toString())
                    .reportType(request.getReportType())
                    .reportName("Indisponível")
                    .status("ERROR")
                    .message("Serviço de relatórios indisponível. Tente novamente mais tarde.")
                    .generatedAt(LocalDateTime.now())
                    .build());
        }

        @Override
        public ResponseEntity<byte[]> downloadReport(ReportRequest request) {
            log.error("Fallback para downloadReport. ReportType: {}, Erro: {}", 
                    request.getReportType(), cause.getMessage());
            
            // Retorna um array vazio em caso de falha
            return ResponseEntity.ok(new byte[0]);
        }

        @Override
        public ResponseEntity<ReportResponse> getReportStatus(String reportId) {
            log.error("Fallback para getReportStatus. ReportId: {}, Erro: {}", 
                    reportId, cause.getMessage());
            
            return ResponseEntity.ok(ReportResponse.builder()
                    .reportId(reportId)
                    .status("ERROR")
                    .message("Serviço de relatórios indisponível. Tente novamente mais tarde.")
                    .build());
        }
    }
}
