package com.clinicsalon.report.service;

import com.clinicsalon.report.client.ProfessionalDto;
import com.clinicsalon.report.dto.ReportRequest;
import com.clinicsalon.report.dto.ReportResponse;
import com.clinicsalon.report.dto.ReportType;
import com.clinicsalon.report.util.JasperReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.clinicsalon.monitoring.aspect.MonitorPerformance;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ClientDataService clientDataService;
    private final AppointmentDataService appointmentDataService;
    private final LoyaltyDataService loyaltyDataService;
    private final ProfessionalDataService professionalDataService;
    private final JasperReportGenerator reportGenerator;
    
    /**
     * Gera um relatório baseado nos parâmetros fornecidos
     */
    @MonitorPerformance(description = "Gerar relatório", thresholdMillis = 2000, alertOnError = true)
    public ReportResponse generateReport(ReportRequest request) {
        log.info("Generating report: {}", request.getReportType());
        
        byte[] reportContent;
        String contentType;
        String reportName;
        
        switch (request.getReportType()) {
            case CLIENT_APPOINTMENTS:
                reportContent = generateClientAppointmentsReport(request);
                reportName = "Agendamentos do Cliente";
                contentType = determineContentType(request.getFormat());
                break;
            case PROFESSIONAL_SCHEDULE:
                reportContent = generateProfessionalScheduleReport(request);
                reportName = "Agenda do Profissional";
                contentType = determineContentType(request.getFormat());
                break;
            case REVENUE_SUMMARY:
                reportContent = generateRevenueSummaryReport(request);
                reportName = "Resumo de Receitas";
                contentType = determineContentType(request.getFormat());
                break;
            case LOYALTY_POINTS_SUMMARY:
                reportContent = generateLoyaltyPointsSummaryReport(request);
                reportName = "Resumo do Programa de Fidelidade";
                contentType = determineContentType(request.getFormat());
                break;
            case CLIENT_HISTORY:
                reportContent = generateClientHistoryReport(request);
                reportName = "Histórico de Cliente";
                contentType = determineContentType(request.getFormat());
                break;
            case SERVICES_POPULARITY:
                reportContent = generateServicesPopularityReport(request);
                reportName = "Popularidade de Serviços";
                contentType = determineContentType(request.getFormat());
                break;
            default:
                throw new IllegalArgumentException("Tipo de relatório não suportado: " + request.getReportType());
        }
        
        return ReportResponse.builder()
                .reportId(UUID.randomUUID().toString())
                .reportType(request.getReportType())
                .reportName(reportName)
                .contentType(contentType)
                .reportContent(reportContent)
                .generatedAt(LocalDateTime.now())
                .status("COMPLETED")
                .fileSize((long) reportContent.length)
                .build();
    }
    
    private String determineContentType(String format) {
        if (format == null) {
            return "application/pdf";
        }
        
        return switch (format.toUpperCase()) {
            case "PDF" -> "application/pdf";
            case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "CSV" -> "text/csv";
            default -> "application/pdf";
        };
    }
    
    @MonitorPerformance(description = "Gerar relatório de agendamentos do cliente", thresholdMillis = 1500, alertOnError = true)
    private byte[] generateClientAppointmentsReport(ReportRequest request) {
        log.info("Generating client appointments report for client ID: {}", request.getClientId());
        
        // Validação do cliente
        Long clientId = request.getClientId();
        if (clientId == null) {
            throw new IllegalArgumentException("ID do cliente é obrigatório para este relatório");
        }
        
        // Busca dados do cliente
        String clientName = clientDataService.getClientName(clientId);
        if (clientName == null || clientName.isEmpty()) {
            throw new IllegalArgumentException("Cliente não encontrado: " + clientId);
        }
        
        // Busca agendamentos do cliente
        List<Map<String, Object>> appointments = appointmentDataService.getClientAppointments(
                clientId, 
                request.getStartDate(), 
                request.getEndDate());
        
        // Preparação dos parâmetros para o relatório
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("CLIENT_NAME", clientName);
        parameters.put("CLIENT_ID", clientId);
        parameters.put("START_DATE", request.getStartDate().toString());
        parameters.put("END_DATE", request.getEndDate().toString());
        parameters.put("REPORT_TITLE", "Agendamentos do Cliente");
        parameters.put("TOTAL_APPOINTMENTS", appointments.size());
        
        // Gera o relatório usando o template JasperReports
        return reportGenerator.generatePdfReport(
                "reports/client_appointments.jrxml",
                parameters,
                appointments);
    }
    
    @MonitorPerformance(description = "Gerar relatório de agenda do profissional", thresholdMillis = 1500, alertOnError = true)
    private byte[] generateProfessionalScheduleReport(ReportRequest request) {
        log.info("Generating professional schedule report for professional ID: {}", request.getProfessionalId());
        
        // Validação do profissional
        Long professionalId = request.getProfessionalId();
        if (professionalId == null) {
            throw new IllegalArgumentException("ID do profissional é obrigatório para este relatório");
        }
        
        // Busca dados do profissional
        ProfessionalDto professional = professionalDataService.getProfessionalById(professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado: " + professionalId));
        
        // Busca agendamentos do profissional
        List<Map<String, Object>> appointments = appointmentDataService.getProfessionalSchedule(
                professionalId, 
                request.getStartDate(), 
                request.getEndDate());
        
        // Preparação dos parâmetros para o relatório
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("PROFESSIONAL_NAME", professional.getName());
        parameters.put("PROFESSIONAL_ID", professional.getId());
        parameters.put("PROFESSIONAL_SPECIALIZATION", professional.getSpecialization());
        parameters.put("START_DATE", request.getStartDate().toString());
        parameters.put("END_DATE", request.getEndDate().toString());
        parameters.put("REPORT_TITLE", "Agenda do Profissional");
        parameters.put("TOTAL_APPOINTMENTS", appointments.size());
        
        // Gera o relatório usando o template JasperReports
        return reportGenerator.generatePdfReport(
                "reports/professional_schedule.jrxml",
                parameters,
                appointments);
    }
    
    @MonitorPerformance(description = "Gerar relatório de resumo de receitas", thresholdMillis = 1500, alertOnError = true)
    private byte[] generateRevenueSummaryReport(ReportRequest request) {
        log.info("Generating revenue summary report from {} to {}", request.getStartDate(), request.getEndDate());
        
        // Busca resumo de receitas
        Map<String, Object> revenueSummary = appointmentDataService.getRevenueSummary(
                request.getStartDate(), 
                request.getEndDate());
        
        // Busca detalhamento por serviço
        List<Map<String, Object>> serviceRevenue = appointmentDataService.getServiceRevenueBreakdown(
                request.getStartDate(), 
                request.getEndDate());
        
        // Preparação dos parâmetros para o relatório
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("START_DATE", request.getStartDate().toString());
        parameters.put("END_DATE", request.getEndDate().toString());
        parameters.put("REPORT_TITLE", "Resumo de Receitas");
        parameters.put("TOTAL_REVENUE", revenueSummary.get("totalRevenue"));
        parameters.put("APPOINTMENTS_COUNT", revenueSummary.get("appointmentsCount"));
        parameters.put("AVERAGE_TICKET", revenueSummary.get("averageTicket"));
        parameters.put("MOST_POPULAR_SERVICE", revenueSummary.get("mostPopularService"));
        parameters.put("MOST_REVENUE_SERVICE", revenueSummary.get("mostRevenueService"));
        
        // Gera o relatório usando o template JasperReports
        return reportGenerator.generatePdfReport(
                "reports/revenue_summary.jrxml",
                parameters,
                serviceRevenue);
    }
    
    private byte[] generateLoyaltyPointsSummaryReport(ReportRequest request) {
        log.info("Generating loyalty points summary report");
        
        // Busca resumo de pontos de fidelidade por cliente
        Map<Long, Integer> clientPointsMap = loyaltyDataService.getAllClientsTotalPoints();
        
        // Converte para uma lista de mapas para o relatório
        List<Map<String, Object>> loyaltyData = new ArrayList<>();
        clientPointsMap.forEach((clientId, points) -> {
            Map<String, Object> clientData = new HashMap<>();
            clientData.put("clientId", clientId);
            clientData.put("clientName", clientDataService.getClientName(clientId));
            clientData.put("points", points);
            clientData.put("tier", loyaltyDataService.determineClientTier(points));
            clientData.put("nextTier", loyaltyDataService.determineNextTier(points));
            clientData.put("pointsToNextTier", loyaltyDataService.calculatePointsToNextTier(points));
            loyaltyData.add(clientData);
        });
        
        // Preparação dos parâmetros para o relatório
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "Resumo do Programa de Fidelidade");
        parameters.put("START_DATE", request.getStartDate().toString());
        parameters.put("END_DATE", request.getEndDate().toString());
        parameters.put("TOTAL_CLIENTS", loyaltyData.size());
        
        // Estatísticas por tier
        long bronzeCount = loyaltyData.stream().filter(d -> "BRONZE".equals(d.get("tier"))).count();
        long silverCount = loyaltyData.stream().filter(d -> "PRATA".equals(d.get("tier"))).count();
        long goldCount = loyaltyData.stream().filter(d -> "OURO".equals(d.get("tier"))).count();
        
        parameters.put("BRONZE_COUNT", bronzeCount);
        parameters.put("SILVER_COUNT", silverCount);
        parameters.put("GOLD_COUNT", goldCount);
        
        // Gera o relatório usando o template JasperReports
        return reportGenerator.generatePdfReport(
                "reports/loyalty_summary.jrxml",
                parameters,
                loyaltyData);
    }
    
    private byte[] generateClientHistoryReport(ReportRequest request) {
        log.info("Generating client history report for client ID: {}", request.getClientId());
        
        // Validação do cliente
        Long clientId = request.getClientId();
        if (clientId == null) {
            throw new IllegalArgumentException("ID do cliente é obrigatório para este relatório");
        }
        
        // Busca dados do cliente
        String clientName = clientDataService.getClientName(clientId);
        if (clientName == null || clientName.isEmpty()) {
            throw new IllegalArgumentException("Cliente não encontrado: " + clientId);
        }
        
        // Busca histórico completo do cliente
        List<Map<String, Object>> appointments = appointmentDataService.getClientAppointments(clientId, null, null);
        
        // Busca pontos de fidelidade
        int loyaltyPoints = loyaltyDataService.getClientPoints(clientId);
        String tier = loyaltyDataService.determineClientTier(loyaltyPoints);
        
        // Busca profissionais preferidos do cliente
        Map<Long, Integer> professionalVisits = appointments.stream()
                .filter(appt -> appt.get("professionalId") != null)
                .collect(Collectors.groupingBy(
                        appt -> Long.parseLong(appt.get("professionalId").toString()),
                        Collectors.summingInt(appt -> 1)));
        
        // Pega os 3 profissionais mais visitados pelo cliente
        List<Long> favoriteProIds = professionalVisits.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // Busca dados completos dos profissionais preferidos
        List<Map<String, Object>> favoriteProfessionals = new ArrayList<>();
        for (Long proId : favoriteProIds) {
            try {
                ProfessionalDto professional = professionalDataService.getProfessionalById(proId);
                if (professional != null) {
                    Map<String, Object> proData = new HashMap<>();
                    proData.put("id", professional.getId());
                    proData.put("name", professional.getName());
                    proData.put("specialization", professional.getSpecialization());
                    proData.put("visits", professionalVisits.get(proId));
                    favoriteProfessionals.add(proData);
                }
            } catch (Exception e) {
                log.warn("Não foi possível obter dados do profissional ID: {}", proId, e);
            }
        }
        
        // Calcula estatísticas do cliente
        double totalSpent = appointments.stream()
                .mapToDouble(appt -> Double.parseDouble(appt.get("price").toString()))
                .sum();
        
        Map<String, Long> serviceCount = appointments.stream()
                .collect(Collectors.groupingBy(
                        appt -> appt.get("serviceName").toString(),
                        Collectors.counting()));
        
        String favoriteService = serviceCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        
        // Preparação dos parâmetros para o relatório
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("CLIENT_NAME", clientName);
        parameters.put("CLIENT_ID", clientId);
        parameters.put("REPORT_TITLE", "Histórico do Cliente");
        parameters.put("TOTAL_APPOINTMENTS", appointments.size());
        parameters.put("TOTAL_SPENT", totalSpent);
        parameters.put("LOYALTY_POINTS", loyaltyPoints);
        parameters.put("LOYALTY_TIER", tier);
        parameters.put("FAVORITE_SERVICE", favoriteService);
        
        // Dados dos 3 profissionais preferidos para o subrelatório
        parameters.put("FAVORITE_PROFESSIONALS", favoriteProfessionals);
        
        // Gera o relatório usando o template JasperReports
        return reportGenerator.generatePdfReport(
                "reports/client_history.jrxml",
                parameters,
                appointments);
    }
    
    private byte[] generateServicesPopularityReport(ReportRequest request) {
        log.info("Generating services popularity report from {} to {}", request.getStartDate(), request.getEndDate());
        
        // Busca dados de popularidade de serviços
        List<Map<String, Object>> servicesPopularity = appointmentDataService.getServicesPopularity(
                request.getStartDate(), 
                request.getEndDate());
        
        // Calcula total de agendamentos para estatísticas percentuais
        int totalAppointments = servicesPopularity.stream()
                .mapToInt(service -> Integer.parseInt(service.get("count").toString()))
                .sum();
        
        // Calcula o serviço mais popular
        String mostPopularService = servicesPopularity.stream()
                .max((s1, s2) -> Integer.parseInt(s1.get("count").toString()) - Integer.parseInt(s2.get("count").toString()))
                .map(service -> service.get("serviceName").toString())
                .orElse("N/A");
                
        // Busca profissionais ativos que podem oferecer os serviços
        List<ProfessionalDto> activeProfessionals = professionalDataService.getActiveProfessionals();
        
        // Enriquece os dados de serviços com os profissionais que os oferecem
        for (Map<String, Object> serviceData : servicesPopularity) {
            String serviceName = serviceData.get("serviceName").toString();
            String serviceCategory = serviceData.get("category").toString();
            
            // Filtra profissionais por especialização relacionada à categoria do serviço
            List<String> professionalNames = activeProfessionals.stream()
                    .filter(p -> p.getSpecialization() != null && 
                            (p.getSpecialization().equalsIgnoreCase(serviceCategory) || 
                             p.getSpecialization().contains(serviceName)))
                    .map(ProfessionalDto::getName)
                    .collect(Collectors.toList());
            
            // Adiciona a lista de profissionais para cada serviço
            serviceData.put("availableProfessionals", String.join(", ", professionalNames));
        }
        
        // Preparação dos parâmetros para o relatório
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("START_DATE", request.getStartDate().toString());
        parameters.put("END_DATE", request.getEndDate().toString());
        parameters.put("REPORT_TITLE", "Popularidade de Serviços");
        parameters.put("TOTAL_APPOINTMENTS", totalAppointments);
        parameters.put("MOST_POPULAR_SERVICE", mostPopularService);
        parameters.put("TOTAL_PROFESSIONALS", activeProfessionals.size());
        
        // Gera o relatório usando o template JasperReports
        return reportGenerator.generatePdfReport(
                "reports/services_popularity.jrxml",
                parameters,
                servicesPopularity);
    }
}
