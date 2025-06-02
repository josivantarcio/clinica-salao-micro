package com.clinicsalon.appointment.service;

import com.clinicsalon.appointment.client.FinanceServiceClient;
import com.clinicsalon.appointment.exception.BusinessException;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import com.clinicsalon.appointment.repository.AppointmentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentPaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(AppointmentPaymentService.class);
    
    private final AppointmentRepository appointmentRepository;
    private final FinanceServiceClient financeServiceClient;
    
    /**
     * Gera um link de pagamento para um agendamento
     * @param appointmentId ID do agendamento
     * @return Mapa contendo o link de pagamento e outras informações
     */
    @CircuitBreaker(name = "financeService", fallbackMethod = "createPaymentLinkFallback")
    @Retry(name = "financeService")
    @Transactional(readOnly = true)
    public Map<String, String> createPaymentLink(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        // Verifica se o agendamento pode receber um link de pagamento
        if (appointment.getStatus() != AppointmentStatus.PENDING && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Não é possível gerar link de pagamento para agendamentos " + 
                                       "que não estejam com status PENDING ou CONFIRMED");
        }
        
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("appointmentId", appointmentId);
        paymentRequest.put("clientId", appointment.getClientId());
        paymentRequest.put("amount", appointment.getPrice());
        paymentRequest.put("description", "Pagamento de agendamento - ID: " + appointmentId);
        
        // Chamar o serviço financeiro para criar o link de pagamento
        ResponseEntity<Map<String, String>> response = financeServiceClient.createPaymentLink(paymentRequest);
        
        if (response != null && response.hasBody()) {
            Map<String, String> body = response.getBody();
            if (body != null && "SUCCESS".equals(body.get("status"))) {
                log.info("Link de pagamento gerado com sucesso para o agendamento ID: {}", appointmentId);
                return body;
            }
        }
        
        log.error("Erro ao gerar link de pagamento para o agendamento ID: {}", appointmentId);
        throw new BusinessException("Não foi possível gerar o link de pagamento");
    }
    
    /**
     * Verifica o status de pagamento de um agendamento
     * @param appointmentId ID do agendamento
     * @return Mapa contendo o status do pagamento e outras informações
     */
    @CircuitBreaker(name = "financeService", fallbackMethod = "getPaymentStatusFallback")
    @Retry(name = "financeService")
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStatus(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        ResponseEntity<Map<String, Object>> response = 
            financeServiceClient.getPaymentStatusByAppointmentId(appointmentId);
        
        if (response != null && response.hasBody()) {
            Map<String, Object> body = response.getBody();
            if (body != null) {
                Object status = body.get("status");
                log.info("Status de pagamento obtido para o agendamento ID: {}: {}", 
                        appointmentId, status);
                return body;
            }
        }
        
        log.error("Erro ao obter status de pagamento para o agendamento ID: {}", appointmentId);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", "Não foi possível obter o status do pagamento");
        return errorResponse;
    }
    
    /**
     * Processa o reembolso de um agendamento
     * @param appointmentId ID do agendamento
     * @return Mapa contendo o status do reembolso e outras informações
     */
    @CircuitBreaker(name = "financeService", fallbackMethod = "processRefundFallback")
    @Retry(name = "financeService")
    @Transactional
    public Map<String, Object> processRefund(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        
        // Verifica se o agendamento pode ser reembolsado
        if (appointment.getStatus() != AppointmentStatus.CANCELLED && 
            appointment.getStatus() != AppointmentStatus.NO_SHOW) {
            throw new BusinessException("Apenas agendamentos cancelados ou com ausência podem ser reembolsados");
        }
        
        ResponseEntity<Map<String, Object>> response = financeServiceClient.processRefund(appointmentId);
        
        if (response != null && response.hasBody()) {
            Map<String, Object> body = response.getBody();
            if (body != null && "SUCCESS".equals(body.get("status"))) {
                log.info("Reembolso processado com sucesso para o agendamento ID: {}", appointmentId);
                return body;
            }
        }
        
        log.error("Erro ao processar reembolso para o agendamento ID: {}", appointmentId);
        throw new BusinessException("Não foi possível processar o reembolso");
    }
    
    // Métodos de fallback para o Circuit Breaker
    
    public Map<String, String> createPaymentLinkFallback(Long appointmentId, Throwable t) {
        log.error("Fallback para criação de link de pagamento ativado: {}", t.getMessage());
        Map<String, String> fallbackResponse = new HashMap<>();
        fallbackResponse.put("status", "ERROR");
        fallbackResponse.put("message", "Serviço de pagamento temporariamente indisponível");
        return fallbackResponse;
    }
    
    public Map<String, Object> getPaymentStatusFallback(Long appointmentId, Throwable t) {
        log.error("Fallback para verificação de status de pagamento ativado: {}", t.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("status", "UNKNOWN");
        fallbackResponse.put("message", "Serviço de pagamento temporariamente indisponível");
        return fallbackResponse;
    }
    
    public Map<String, Object> processRefundFallback(Long appointmentId, Throwable t) {
        log.error("Fallback para processamento de reembolso ativado: {}", t.getMessage());
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("status", "ERROR");
        fallbackResponse.put("message", "Serviço de pagamento temporariamente indisponível para processamento de reembolso");
        return fallbackResponse;
    }
    
    private Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Agendamento não encontrado com ID: " + id));
    }
}
