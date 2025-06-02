package com.clinicsalon.appointment.service;

import com.clinicsalon.appointment.client.FinanceServiceClient;
import com.clinicsalon.appointment.exception.BusinessException;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import com.clinicsalon.appointment.repository.AppointmentRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentPaymentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private FinanceServiceClient financeServiceClient;

    @InjectMocks
    private AppointmentPaymentService paymentService;

    private Appointment testAppointment;
    private final Long appointmentId = 1L;

    @BeforeEach
    void setUp() {
        testAppointment = new Appointment();
        testAppointment.setId(appointmentId);
        testAppointment.setClientId(1L);
        testAppointment.setProfessionalId(1L);
        testAppointment.setStartTime(LocalDateTime.now().plusDays(1));
        testAppointment.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        testAppointment.setPrice(new BigDecimal("100.00"));
    }

    @Test
    void createPaymentLink_Success() {
        // Arrange
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("status", "SUCCESS");
        successResponse.put("paymentLink", "https://payment.example.com/123456");
        
        when(financeServiceClient.createPaymentLink(any()))
                .thenReturn(ResponseEntity.ok(successResponse));

        // Act
        Map<String, String> result = paymentService.createPaymentLink(appointmentId);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals("https://payment.example.com/123456", result.get("paymentLink"));
    }

    @Test
    void createPaymentLink_InvalidStatus() {
        // Arrange
        testAppointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.createPaymentLink(appointmentId);
        });
        
        assertTrue(exception.getMessage().contains("Não é possível gerar link de pagamento"));
    }

    @Test
    void getPaymentStatus_Success() {
        // Arrange
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        
        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "PAID");
        statusResponse.put("paidAt", "2025-06-01T10:15:30");
        statusResponse.put("amount", "100.00");
        
        when(financeServiceClient.getPaymentStatusByAppointmentId(appointmentId))
                .thenReturn(ResponseEntity.ok(statusResponse));

        // Act
        Map<String, Object> result = paymentService.getPaymentStatus(appointmentId);

        // Assert
        assertNotNull(result);
        assertEquals("PAID", result.get("status"));
    }

    @Test
    void processRefund_Success() {
        // Arrange
        testAppointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        
        Map<String, Object> refundResponse = new HashMap<>();
        refundResponse.put("status", "SUCCESS");
        refundResponse.put("refundId", "REF123456");
        refundResponse.put("amount", "100.00");
        
        when(financeServiceClient.processRefund(appointmentId))
                .thenReturn(ResponseEntity.ok(refundResponse));

        // Act
        Map<String, Object> result = paymentService.processRefund(appointmentId);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals("REF123456", result.get("refundId"));
    }

    @Test
    void processRefund_InvalidStatus() {
        // Arrange
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.processRefund(appointmentId);
        });
        
        assertTrue(exception.getMessage().contains("Apenas agendamentos cancelados ou com ausência podem ser reembolsados"));
    }
}
