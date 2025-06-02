package com.clinicsalon.appointment.service;

import com.clinicsalon.appointment.client.ClientServiceClient;
import com.clinicsalon.appointment.client.NotificationServiceClient;
import com.clinicsalon.appointment.client.ProfessionalServiceClient;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AppointmentNotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(AppointmentNotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final NotificationServiceClient notificationServiceClient;
    private final ClientServiceClient clientServiceClient;
    private final ProfessionalServiceClient professionalServiceClient;
    
    /**
     * Envia notificação de confirmação de agendamento
     */
    public void sendAppointmentConfirmationNotification(Appointment appointment) {
        CompletableFuture.runAsync(() -> {
            try {
                String clientName = clientServiceClient.findNameById(appointment.getClientId());
                String professionalName = professionalServiceClient.findNameById(appointment.getProfessionalId());
                
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("to", "cliente@email.com"); // Em uma implementação real, obteria o email do cliente
                emailData.put("subject", "Confirmação de Agendamento - Clínica Salão");
                emailData.put("templateName", "appointment-confirmation");
                emailData.put("data", Map.of(
                    "clientName", clientName,
                    "professionalName", professionalName,
                    "date", appointment.getStartTime().format(DATE_FORMATTER),
                    "serviceName", "Serviços Selecionados", // Em uma implementação real, obteria os nomes dos serviços
                    "appointmentId", appointment.getId().toString()
                ));
                
                sendEmail(emailData);
                sendSmsNotification(appointment, "Seu agendamento na Clínica Salão foi confirmado para " + 
                                   appointment.getStartTime().format(DATE_FORMATTER) + 
                                   " com " + professionalName);
            } catch (Exception e) {
                log.error("Erro ao enviar notificação de confirmação: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Envia notificação de cancelamento de agendamento
     */
    public void sendAppointmentCancellationNotification(Appointment appointment) {
        CompletableFuture.runAsync(() -> {
            try {
                String clientName = clientServiceClient.findNameById(appointment.getClientId());
                String professionalName = professionalServiceClient.findNameById(appointment.getProfessionalId());
                
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("to", "cliente@email.com"); // Em uma implementação real, obteria o email do cliente
                emailData.put("subject", "Cancelamento de Agendamento - Clínica Salão");
                emailData.put("templateName", "appointment-cancellation");
                emailData.put("data", Map.of(
                    "clientName", clientName,
                    "date", appointment.getStartTime().format(DATE_FORMATTER),
                    "appointmentId", appointment.getId().toString()
                ));
                
                sendEmail(emailData);
                sendSmsNotification(appointment, "Seu agendamento na Clínica Salão para " + 
                                   appointment.getStartTime().format(DATE_FORMATTER) + 
                                   " foi cancelado.");
            } catch (Exception e) {
                log.error("Erro ao enviar notificação de cancelamento: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Envia notificação de lembrete de agendamento
     */
    public void sendAppointmentReminderNotification(Appointment appointment) {
        CompletableFuture.runAsync(() -> {
            try {
                String clientName = clientServiceClient.findNameById(appointment.getClientId());
                String professionalName = professionalServiceClient.findNameById(appointment.getProfessionalId());
                
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("to", "cliente@email.com"); // Em uma implementação real, obteria o email do cliente
                emailData.put("subject", "Lembrete de Agendamento - Clínica Salão");
                emailData.put("templateName", "appointment-reminder");
                emailData.put("data", Map.of(
                    "clientName", clientName,
                    "professionalName", professionalName,
                    "date", appointment.getStartTime().format(DATE_FORMATTER),
                    "appointmentId", appointment.getId().toString()
                ));
                
                sendEmail(emailData);
                sendSmsNotification(appointment, "Lembrete: Seu agendamento na Clínica Salão é amanhã às " + 
                                   appointment.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + 
                                   " com " + professionalName);
            } catch (Exception e) {
                log.error("Erro ao enviar notificação de lembrete: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Envia notificação de atualização de agendamento
     */
    public void sendAppointmentUpdateNotification(Appointment appointment) {
        CompletableFuture.runAsync(() -> {
            try {
                String clientName = clientServiceClient.findNameById(appointment.getClientId());
                String professionalName = professionalServiceClient.findNameById(appointment.getProfessionalId());
                
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("to", "cliente@email.com"); // Em uma implementação real, obteria o email do cliente
                emailData.put("subject", "Atualização de Agendamento - Clínica Salão");
                emailData.put("templateName", "appointment-update");
                emailData.put("data", Map.of(
                    "clientName", clientName,
                    "professionalName", professionalName,
                    "date", appointment.getStartTime().format(DATE_FORMATTER),
                    "appointmentId", appointment.getId().toString()
                ));
                
                sendEmail(emailData);
                sendSmsNotification(appointment, "Seu agendamento na Clínica Salão foi atualizado para " + 
                                   appointment.getStartTime().format(DATE_FORMATTER) + 
                                   " com " + professionalName);
            } catch (Exception e) {
                log.error("Erro ao enviar notificação de atualização: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Envia notificação de pagamento aprovado
     */
    public void sendPaymentApprovedNotification(Appointment appointment) {
        CompletableFuture.runAsync(() -> {
            try {
                String clientName = clientServiceClient.findNameById(appointment.getClientId());
                
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("to", "cliente@email.com"); // Em uma implementação real, obteria o email do cliente
                emailData.put("subject", "Pagamento Aprovado - Clínica Salão");
                emailData.put("templateName", "payment-approved");
                emailData.put("data", Map.of(
                    "clientName", clientName,
                    "date", appointment.getStartTime().format(DATE_FORMATTER),
                    "amount", appointment.getPrice().toString(),
                    "appointmentId", appointment.getId().toString()
                ));
                
                sendEmail(emailData);
                sendSmsNotification(appointment, "Seu pagamento para o agendamento na Clínica Salão foi aprovado. " +
                                   "Obrigado!");
            } catch (Exception e) {
                log.error("Erro ao enviar notificação de pagamento aprovado: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Envia notificação de reembolso processado
     */
    public void sendRefundProcessedNotification(Appointment appointment) {
        CompletableFuture.runAsync(() -> {
            try {
                String clientName = clientServiceClient.findNameById(appointment.getClientId());
                
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("to", "cliente@email.com"); // Em uma implementação real, obteria o email do cliente
                emailData.put("subject", "Reembolso Processado - Clínica Salão");
                emailData.put("templateName", "refund-processed");
                emailData.put("data", Map.of(
                    "clientName", clientName,
                    "date", appointment.getStartTime().format(DATE_FORMATTER),
                    "amount", appointment.getPrice().toString(),
                    "appointmentId", appointment.getId().toString()
                ));
                
                sendEmail(emailData);
                sendSmsNotification(appointment, "Seu reembolso para o agendamento cancelado na Clínica Salão " +
                                   "foi processado. O valor será creditado em breve.");
            } catch (Exception e) {
                log.error("Erro ao enviar notificação de reembolso: {}", e.getMessage());
            }
        });
    }
    
    // Métodos auxiliares com Circuit Breaker
    
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendEmailFallback")
    @Retry(name = "notificationService")
    private ResponseEntity<Map<String, Object>> sendEmail(Map<String, Object> emailData) {
        log.info("Enviando email: {}", emailData.get("subject"));
        return notificationServiceClient.sendEmail(emailData);
    }
    
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendSmsFallback")
    @Retry(name = "notificationService")
    private ResponseEntity<Map<String, Object>> sendSmsNotification(Appointment appointment, String message) {
        log.info("Enviando SMS para cliente ID: {}", appointment.getClientId());
        
        Map<String, Object> smsData = new HashMap<>();
        smsData.put("phoneNumber", "5511999999999"); // Em uma implementação real, obteria o telefone do cliente
        smsData.put("message", message);
        
        return notificationServiceClient.sendSms(smsData);
    }
    
    // Métodos de fallback
    
    private ResponseEntity<Map<String, Object>> sendEmailFallback(Map<String, Object> emailData, Throwable t) {
        log.error("Fallback para envio de email ativado: {}", t.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", "Não foi possível enviar o email. Serviço indisponível.");
        return ResponseEntity.ok(response);
    }
    
    private ResponseEntity<Map<String, Object>> sendSmsFallback(Appointment appointment, String message, Throwable t) {
        log.error("Fallback para envio de SMS ativado: {}", t.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", "Não foi possível enviar o SMS. Serviço indisponível.");
        return ResponseEntity.ok(response);
    }
}
