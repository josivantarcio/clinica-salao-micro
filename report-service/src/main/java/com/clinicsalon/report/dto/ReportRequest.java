package com.clinicsalon.report.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    @NotNull(message = "Report type is required")
    private ReportType reportType;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private Long clientId;
    
    private Long professionalId;
    
    private String format; // PDF, EXCEL, CSV
    
    private Map<String, String> additionalParams;
}
