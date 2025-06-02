package com.clinicsalon.appointment.service;

import com.clinicsalon.appointment.client.ClientServiceClient;
import com.clinicsalon.appointment.client.ProfessionalServiceClient;
import com.clinicsalon.appointment.dto.AppointmentRequest;
import com.clinicsalon.appointment.dto.AppointmentResponse;
import com.clinicsalon.appointment.dto.AppointmentServiceRequest;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import com.clinicsalon.appointment.repository.AppointmentRepository;
import com.clinicsalon.appointment.repository.AppointmentServiceRepository;
import com.clinicsalon.appointment.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceIntegrationTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private AppointmentServiceRepository appointmentServiceRepository;
    
    @Mock
    private ServiceRepository serviceRepository;
    
    @Mock
    private ClientServiceClient clientServiceClient;
    
    @Mock
    private ProfessionalServiceClient professionalServiceClient;
    
    @Mock
    private AppointmentPaymentService paymentService;
    
    @Mock
    private AppointmentNotificationService notificationService;
    
    @Mock
    private LoyaltyIntegrationService loyaltyService;
    
    @InjectMocks
    private AppointmentService appointmentService;
    
    private Appointment testAppointment;
    private AppointmentRequest testRequest;
    
    @BeforeEach
    void setUp() {
        // Configurar um agendamento de teste
        testAppointment = Appointment.builder()
                .id(1L)
                .clientId(101L)
                .professionalId(201L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(AppointmentStatus.PENDING)
                .price(BigDecimal.valueOf(120.00))
                .notes("Teste de agendamento")
                .build();
        
        // Configurar uma requisição de teste
        testRequest = new AppointmentRequest();
        testRequest.setClientId(101L);
        testRequest.setProfessionalId(201L);
        testRequest.setStartTime(LocalDateTime.now().plusDays(1));
        testRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        testRequest.setNotes("Teste de agendamento");
        
        // Adicionar serviços
        List<AppointmentServiceRequest> services = new ArrayList<>();
        AppointmentServiceRequest service = new AppointmentServiceRequest();
        service.setServiceId(1L);
        service.setQuantity(1);
        services.add(service);
        testRequest.setServices(services);
        
        // Configurar mocks
        when(clientServiceClient.findNameById(anyLong())).thenReturn("Cliente Teste");
        when(professionalServiceClient.findNameById(anyLong())).thenReturn("Profissional Teste");
    }
    
    @Test
    void testFindAll() {
        // Arrange
        List<Appointment> appointments = new ArrayList<>();
        appointments.add(testAppointment);
        Page<Appointment> appointmentPage = new PageImpl<>(appointments);
        when(appointmentRepository.findAll(any(Pageable.class))).thenReturn(appointmentPage);
        when(appointmentServiceRepository.findByAppointmentId(anyLong())).thenReturn(new ArrayList<>());
        
        // Act
        Page<AppointmentResponse> result = appointmentService.findAll(Pageable.unpaged());
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Cliente Teste", result.getContent().get(0).getClientName());
        assertEquals("Profissional Teste", result.getContent().get(0).getProfessionalName());
        verify(appointmentRepository).findAll(any(Pageable.class));
    }
    
    @Test
    void testFindById() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        when(appointmentServiceRepository.findByAppointmentId(anyLong())).thenReturn(new ArrayList<>());
        
        // Act
        AppointmentResponse result = appointmentService.findById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(testAppointment.getId(), result.getId());
        assertEquals("Cliente Teste", result.getClientName());
        assertEquals("Profissional Teste", result.getProfessionalName());
        verify(appointmentRepository).findById(anyLong());
    }
    
    @Test
    void testUpdateStatus() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        when(appointmentServiceRepository.findByAppointmentId(anyLong())).thenReturn(new ArrayList<>());
        
        // Act
        AppointmentResponse result = appointmentService.updateStatus(1L, AppointmentStatus.CONFIRMED);
        
        // Assert
        assertNotNull(result);
        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());
        verify(appointmentRepository).save(any(Appointment.class));
        verify(notificationService, times(1)).sendAppointmentConfirmationNotification(any(Appointment.class));
    }
}
