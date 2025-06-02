package com.clinicsalon.appointment.service;

import com.clinicsalon.appointment.client.LoyaltyServiceClient;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyIntegrationServiceTest {

    @Mock
    private LoyaltyServiceClient loyaltyServiceClient;

    @InjectMocks
    private LoyaltyIntegrationService loyaltyService;

    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setClientId(1L);
        testAppointment.setProfessionalId(1L);
        testAppointment.setStartTime(LocalDateTime.now().plusDays(1));
        testAppointment.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        testAppointment.setStatus(AppointmentStatus.COMPLETED);
        testAppointment.setPrice(new BigDecimal("150.00"));
    }

    @Test
    void addLoyaltyPoints_Success() {
        // Arrange
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "SUCCESS");
        successResponse.put("pointsAdded", 150);
        successResponse.put("newBalance", 300);
        
        when(loyaltyServiceClient.addPoints(any())).thenReturn(ResponseEntity.ok(successResponse));
        
        // Act
        Map<String, Object> result = loyaltyService.addLoyaltyPoints(testAppointment);
        
        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals(150, result.get("pointsAdded"));
        verify(loyaltyServiceClient, times(1)).addPoints(any());
    }
    
    @Test
    void addLoyaltyPoints_Failure() {
        // Arrange
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", "Cliente não encontrado");
        
        when(loyaltyServiceClient.addPoints(any())).thenReturn(ResponseEntity.ok(errorResponse));
        
        // Act
        Map<String, Object> result = loyaltyService.addLoyaltyPoints(testAppointment);
        
        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.get("status"));
        verify(loyaltyServiceClient, times(1)).addPoints(any());
    }
    
    @Test
    void getPointsBalance_Success() {
        // Arrange
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "SUCCESS");
        successResponse.put("points", 200);
        successResponse.put("clientId", 1L);
        
        when(loyaltyServiceClient.getPointsBalance(anyLong())).thenReturn(ResponseEntity.ok(successResponse));
        
        // Act
        Map<String, Object> result = loyaltyService.getPointsBalance(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals(200, result.get("points"));
        verify(loyaltyServiceClient, times(1)).getPointsBalance(anyLong());
    }
    
    @Test
    void ensureLoyaltyAccount_Success() {
        // Arrange
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "SUCCESS");
        successResponse.put("accountId", "ACC123");
        successResponse.put("clientId", 1L);
        successResponse.put("message", "Conta encontrada");
        
        when(loyaltyServiceClient.getLoyaltyAccount(anyLong())).thenReturn(ResponseEntity.ok(successResponse));
        
        // Act
        Map<String, Object> result = loyaltyService.ensureLoyaltyAccount(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals("ACC123", result.get("accountId"));
        verify(loyaltyServiceClient, times(1)).getLoyaltyAccount(anyLong());
    }
    
    @Test
    void handleServiceUnavailableException() {
        // Arrange
        when(loyaltyServiceClient.addPoints(any())).thenThrow(new RuntimeException("Service unavailable"));
        
        // Act
        Map<String, Object> result = loyaltyService.addLoyaltyPoints(testAppointment);
        
        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.get("status"));
        assertTrue(result.containsKey("message"));
        verify(loyaltyServiceClient, times(1)).addPoints(any());
    }
}
