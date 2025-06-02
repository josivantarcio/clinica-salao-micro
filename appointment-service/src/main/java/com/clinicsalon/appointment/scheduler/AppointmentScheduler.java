package com.clinicsalon.appointment.scheduler;

import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import com.clinicsalon.appointment.repository.AppointmentRepository;
import com.clinicsalon.appointment.service.AppointmentNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agendador de tarefas para gerenciar automaticamente ações relacionadas a agendamentos
 */
@Component
@RequiredArgsConstructor
public class AppointmentScheduler {

    private static final Logger log = LoggerFactory.getLogger(AppointmentScheduler.class);
    
    private final AppointmentRepository appointmentRepository;
    private final AppointmentNotificationService notificationService;
    
    /**
     * Envia lembretes para agendamentos que ocorrerão no dia seguinte
     * Executa todos os dias às 18:00
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void sendAppointmentReminders() {
        log.info("Iniciando envio de lembretes para agendamentos de amanhã");
        
        LocalDateTime tomorrowStart = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1).minusSeconds(1);
        
        List<Appointment> appointments = appointmentRepository.findByStatusAndStartTimeBetween(
                AppointmentStatus.CONFIRMED, tomorrowStart, tomorrowEnd);
        
        log.info("Encontrados {} agendamentos para enviar lembretes", appointments.size());
        
        for (Appointment appointment : appointments) {
            try {
                notificationService.sendAppointmentReminderNotification(appointment);
                log.info("Lembrete enviado para agendamento ID: {}", appointment.getId());
            } catch (Exception e) {
                log.error("Erro ao enviar lembrete para agendamento ID {}: {}", 
                        appointment.getId(), e.getMessage());
            }
        }
    }
    
    /**
     * Marca agendamentos como "No Show" quando passaram da hora e não foram concluídos
     * Executa a cada 30 minutos
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void markMissedAppointments() {
        log.info("Verificando agendamentos não comparecidos");
        
        // Agendamentos confirmados que já passaram da hora de término e não foram atualizados
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        
        List<Appointment> missedAppointments = appointmentRepository.findByStatusAndEndTimeBetween(
                AppointmentStatus.CONFIRMED, oneHourAgo, now);
        
        log.info("Encontrados {} agendamentos não comparecidos", missedAppointments.size());
        
        for (Appointment appointment : missedAppointments) {
            appointment.setStatus(AppointmentStatus.NO_SHOW);
            appointmentRepository.save(appointment);
            log.info("Agendamento ID: {} marcado como NO_SHOW", appointment.getId());
        }
    }
    
    /**
     * Cancela agendamentos pendentes que não foram confirmados em 24 horas
     * Executa todos os dias à meia-noite
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cancelUnconfirmedAppointments() {
        log.info("Cancelando agendamentos não confirmados");
        
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        List<Appointment> unconfirmedAppointments = appointmentRepository.findByStatusAndCreatedAtBefore(
                AppointmentStatus.PENDING, oneDayAgo);
        
        log.info("Encontrados {} agendamentos não confirmados para cancelar", unconfirmedAppointments.size());
        
        for (Appointment appointment : unconfirmedAppointments) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            log.info("Agendamento ID: {} cancelado automaticamente por falta de confirmação", appointment.getId());
            
            try {
                notificationService.sendAppointmentCancellationNotification(appointment);
            } catch (Exception e) {
                log.error("Erro ao enviar notificação de cancelamento para agendamento ID {}: {}", 
                        appointment.getId(), e.getMessage());
            }
        }
    }
}
