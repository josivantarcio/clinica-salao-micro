package com.clinicsalon.appointment.service;

import com.clinicsalon.appointment.client.ClientServiceClient;
import com.clinicsalon.appointment.client.NotificationServiceClient;
import com.clinicsalon.appointment.client.ProfessionalServiceClient;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentNotificationServiceTest {

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Mock
    private ClientServiceClient clientServiceClient;

    @Mock
    private ProfessionalServiceClient professionalServiceClient;

    @InjectMocks
    private AppointmentNotificationService notificationService;

    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setClientId(1L);
        testAppointment.setProfessionalId(1L);
        testAppointment.setStartTime(LocalDateTime.now().plusDays(1));
        testAppointment.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        testAppointment.setPrice(new BigDecimal("100.00"));

        // Mock das respostas dos clientes Feign
        when(clientServiceClient.findNameById(1L)).thenReturn("João da Silva");
        when(professionalServiceClient.findNameById(1L)).thenReturn("Maria Oliveira");
        
        // Mock das respostas do serviço de notificação
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "SUCCESS");
        successResponse.put("messageId", "MSG123456");
        
        when(notificationServiceClient.sendEmail(any())).thenReturn(ResponseEntity.ok(successResponse));
        when(notificationServiceClient.sendSms(any())).thenReturn(ResponseEntity.ok(successResponse));
        when(notificationServiceClient.sendPushNotification(any())).thenReturn(ResponseEntity.ok(successResponse));
    }

    @Test
    void sendAppointmentConfirmationNotification_Success() throws Exception {
        // Act
        notificationService.sendAppointmentConfirmationNotification(testAppointment);
        
        // Wait for async task to complete
        Thread.sleep(100);
        
        // Assert
        verify(clientServiceClient, times(1)).findNameById(1L);
        verify(professionalServiceClient, times(1)).findNameById(1L);
        verify(notificationServiceClient, times(1)).sendEmail(any());
        verify(notificationServiceClient, times(1)).sendSms(any());
    }

    @Test
    void sendAppointmentCancellationNotification_Success() throws Exception {
        // Act
        notificationService.sendAppointmentCancellationNotification(testAppointment);
        
        // Wait for async task to complete
        Thread.sleep(100);
        
        // Assert
        verify(clientServiceClient, times(1)).findNameById(1L);
        verify(professionalServiceClient, times(1)).findNameById(1L);
        verify(notificationServiceClient, times(1)).sendEmail(any());
        verify(notificationServiceClient, times(1)).sendSms(any());
    }

    @Test
    void sendAppointmentReminderNotification_Success() throws Exception {
        // Act
        notificationService.sendAppointmentReminderNotification(testAppointment);
        
        // Wait for async task to complete
        Thread.sleep(100);
        
        // Assert
        verify(clientServiceClient, times(1)).findNameById(1L);
        verify(professionalServiceClient, times(1)).findNameById(1L);
        verify(notificationServiceClient, times(1)).sendEmail(any());
        verify(notificationServiceClient, times(1)).sendSms(any());
    }

    @Test
    void sendPaymentApprovedNotification_Success() throws Exception {
        // Act
        notificationService.sendPaymentApprovedNotification(testAppointment);
        
        // Wait for async task to complete
        Thread.sleep(100);
        
        // Assert
        verify(clientServiceClient, times(1)).findNameById(1L);
        verify(notificationServiceClient, times(1)).sendEmail(any());
        verify(notificationServiceClient, times(1)).sendSms(any());
    }

    @Test
    void sendRefundProcessedNotification_Success() throws Exception {
        // Act
        notificationService.sendRefundProcessedNotification(testAppointment);
        
        // Wait for async task to complete
        Thread.sleep(100);
        
        // Assert
        verify(clientServiceClient, times(1)).findNameById(1L);
        verify(notificationServiceClient, times(1)).sendEmail(any());
        verify(notificationServiceClient, times(1)).sendSms(any());
    }

    @Test
    void handleServiceUnavailableException() throws Exception {
        // Arrange
        when(clientServiceClient.findNameById(1L)).thenThrow(new RuntimeException("Service unavailable"));
        
        // Act
        notificationService.sendAppointmentConfirmationNotification(testAppointment);
        
        // Wait for async task to complete
        Thread.sleep(100);
        
        // Assert - verifica que o serviço não quebra quando um dos componentes falha
        verify(clientServiceClient, times(1)).findNameById(1L);
        verify(professionalServiceClient, times(1)).findNameById(1L);
        // Nenhuma notificação deveria ser enviada devido ao erro
        verify(notificationServiceClient, times(0)).sendEmail(any());
        verify(notificationServiceClient, times(0)).sendSms(any());
    }
}
