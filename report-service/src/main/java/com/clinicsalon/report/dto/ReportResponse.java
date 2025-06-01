package com.clinicsalon.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    
    private String reportId;
    private ReportType reportType;
    private String reportName;
    private String contentType;
    private byte[] reportContent;
    private LocalDateTime generatedAt;
    private String status;
    private Long fileSize;
}
