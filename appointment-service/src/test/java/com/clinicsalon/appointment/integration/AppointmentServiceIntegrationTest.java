package com.clinicsalon.appointment.integration;

import com.clinicsalon.appointment.client.ClientServiceClient;
import com.clinicsalon.appointment.client.FinanceServiceClient;
import com.clinicsalon.appointment.client.LoyaltyServiceClient;
import com.clinicsalon.appointment.client.ProfessionalServiceClient;
import com.clinicsalon.appointment.dto.AppointmentDto;
import com.clinicsalon.appointment.dto.AppointmentSearchParams;
import com.clinicsalon.appointment.dto.ClientDto;
import com.clinicsalon.appointment.dto.ProfessionalDto;
import com.clinicsalon.appointment.dto.ServiceDto;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import com.clinicsalon.appointment.model.PaymentStatus;
import com.clinicsalon.appointment.repository.AppointmentRepository;
import com.clinicsalon.appointment.service.AppointmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes de integração para o serviço de agendamentos
 * Testa os fluxos completos de gerenciamento de agendamentos
 * incluindo criação, busca, atualização e cancelamento
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
public class AppointmentServiceIntegrationTest {

    @Autowired
    private AppointmentService appointmentService;

    @MockBean
    private AppointmentRepository appointmentRepository;

    @MockBean
    private ClientServiceClient clientServiceClient;

    @MockBean
    private ProfessionalServiceClient professionalServiceClient;

    @MockBean
    private FinanceServiceClient financeServiceClient;

    @MockBean
    private LoyaltyServiceClient loyaltyServiceClient;

    private List<Appointment> testAppointments;

    @BeforeEach
    public void setup() {
        // Configurar dados de teste
        testAppointments = new ArrayList<>();
        
        // Criar agendamentos de exemplo
        for (int i = 1; i <= 10; i++) {
            Appointment appointment = new Appointment();
            appointment.setId((long) i);
            appointment.setClientId(100L + i);
            appointment.setProfessionalId(200L + (i % 3));
            appointment.setServiceId(300L + (i % 5));
            appointment.setDateTime(LocalDateTime.now().plusDays(i));
            appointment.setDuration(60);
            appointment.setPrice(BigDecimal.valueOf(100.0 + (i * 10)));
            
            // Configurar status diferentes para testar diferentes fluxos
            if (i <= 3) {
                appointment.setStatus(AppointmentStatus.PENDING);
                appointment.setPaymentStatus(PaymentStatus.PENDING);
            } else if (i <= 6) {
                appointment.setStatus(AppointmentStatus.CONFIRMED);
                appointment.setPaymentStatus(PaymentStatus.PAID);
            } else if (i <= 8) {
                appointment.setStatus(AppointmentStatus.COMPLETED);
                appointment.setPaymentStatus(PaymentStatus.PAID);
            } else {
                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointment.setPaymentStatus(PaymentStatus.REFUNDED);
            }
            
            testAppointments.add(appointment);
        }
    }

    @AfterEach
    public void cleanup() {
        // Limpar mocks
        reset(appointmentRepository, clientServiceClient, professionalServiceClient, 
                financeServiceClient, loyaltyServiceClient);
    }

    /**
     * Testa o fluxo completo de criação de um agendamento
     */
    @Test
    public void testCreateAppointmentFlow() {
        // Configurar dados de entrada
        AppointmentDto appointmentDto = new AppointmentDto();
        appointmentDto.setClientId(101L);
        appointmentDto.setProfessionalId(201L);
        appointmentDto.setServiceId(301L);
        appointmentDto.setDateTime(LocalDateTime.now().plusDays(3));
        
        // Configurar mocks para dados externos
        // Simulação de cliente
        ClientDto clientDto = new ClientDto();
        clientDto.setId(101L);
        clientDto.setName("Maria Silva");
        clientDto.setEmail("maria@example.com");
        when(clientServiceClient.getClientById(101L)).thenReturn(Optional.of(clientDto));
        
        // Simulação de profissional
        ProfessionalDto professionalDto = new ProfessionalDto();
        professionalDto.setId(201L);
        professionalDto.setName("Carlos Oliveira");
        professionalDto.setSpecialization("Cabeleireiro");
        when(professionalServiceClient.getProfessionalById(201L)).thenReturn(Optional.of(professionalDto));
        
        // Simulação de serviço
        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId(301L);
        serviceDto.setName("Corte Feminino");
        serviceDto.setDuration(60);
        serviceDto.setPrice(BigDecimal.valueOf(150.0));
        when(professionalServiceClient.getServiceById(301L)).thenReturn(Optional.of(serviceDto));
        
        // Simulação de disponibilidade
        when(professionalServiceClient.checkAvailability(eq(201L), any(LocalDateTime.class), eq(60)))
                .thenReturn(true);
        
        // Configurar mock para salvar no repositório
        Appointment savedAppointment = new Appointment();
        savedAppointment.setId(11L);
        savedAppointment.setClientId(101L);
        savedAppointment.setProfessionalId(201L);
        savedAppointment.setServiceId(301L);
        savedAppointment.setDateTime(appointmentDto.getDateTime());
        savedAppointment.setDuration(60);
        savedAppointment.setPrice(BigDecimal.valueOf(150.0));
        savedAppointment.setStatus(AppointmentStatus.PENDING);
        savedAppointment.setPaymentStatus(PaymentStatus.PENDING);
        
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);
        
        // Executar método sob teste
        AppointmentDto result = appointmentService.createAppointment(appointmentDto);
        
        // Verificar resultado
        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals(101L, result.getClientId());
        assertEquals(201L, result.getProfessionalId());
        assertEquals(301L, result.getServiceId());
        assertEquals(60, result.getDuration());
        assertEquals(BigDecimal.valueOf(150.0), result.getPrice());
        assertEquals(AppointmentStatus.PENDING.name(), result.getStatus());
        assertEquals(PaymentStatus.PENDING.name(), result.getPaymentStatus());
        
        // Verificar interações
        verify(clientServiceClient).getClientById(101L);
        verify(professionalServiceClient).getProfessionalById(201L);
        verify(professionalServiceClient).getServiceById(301L);
        verify(professionalServiceClient).checkAvailability(eq(201L), any(LocalDateTime.class), eq(60));
        verify(appointmentRepository).save(any(Appointment.class));
    }

    /**
     * Testa o fluxo de busca de agendamentos com diferentes filtros
     */
    @Test
    public void testSearchAppointmentsFlow() {
        // Configurar mock do repositório para diferentes cenários de busca
        Page<Appointment> appointmentPage = new PageImpl<>(testAppointments.subList(0, 5));
        when(appointmentRepository.findAll(any(PageRequest.class))).thenReturn(appointmentPage);
        
        // Filtro por cliente
        when(appointmentRepository.findByClientId(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(testAppointments.subList(0, 2)));
        
        // Filtro por profissional
        when(appointmentRepository.findByProfessionalId(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(testAppointments.subList(3, 5)));
        
        // Filtro por data
        when(appointmentRepository.findByDateTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(testAppointments.subList(1, 4)));
        
        // Filtro por status
        when(appointmentRepository.findByStatus(any(AppointmentStatus.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(testAppointments.subList(0, 3)));
        
        // Teste 1: Busca sem filtros
        AppointmentSearchParams emptyParams = new AppointmentSearchParams();
        Page<AppointmentDto> resultAll = appointmentService.searchAppointments(emptyParams, PageRequest.of(0, 10));
        
        assertNotNull(resultAll);
        assertEquals(5, resultAll.getContent().size());
        
        // Teste 2: Filtro por cliente
        AppointmentSearchParams clientParams = new AppointmentSearchParams();
        clientParams.setClientId(101L);
        Page<AppointmentDto> resultByClient = appointmentService.searchAppointments(clientParams, PageRequest.of(0, 10));
        
        assertNotNull(resultByClient);
        assertEquals(2, resultByClient.getContent().size());
        
        // Teste 3: Filtro por profissional
        AppointmentSearchParams professionalParams = new AppointmentSearchParams();
        professionalParams.setProfessionalId(201L);
        Page<AppointmentDto> resultByProfessional = appointmentService.searchAppointments(professionalParams, PageRequest.of(0, 10));
        
        assertNotNull(resultByProfessional);
        assertEquals(2, resultByProfessional.getContent().size());
        
        // Teste 4: Filtro por data
        AppointmentSearchParams dateParams = new AppointmentSearchParams();
        dateParams.setStartDate(LocalDate.now());
        dateParams.setEndDate(LocalDate.now().plusDays(5));
        Page<AppointmentDto> resultByDate = appointmentService.searchAppointments(dateParams, PageRequest.of(0, 10));
        
        assertNotNull(resultByDate);
        assertEquals(3, resultByDate.getContent().size());
        
        // Teste 5: Filtro por status
        AppointmentSearchParams statusParams = new AppointmentSearchParams();
        statusParams.setStatus(AppointmentStatus.PENDING.name());
        Page<AppointmentDto> resultByStatus = appointmentService.searchAppointments(statusParams, PageRequest.of(0, 10));
        
        assertNotNull(resultByStatus);
        assertEquals(3, resultByStatus.getContent().size());
        
        // Verificar interações
        verify(appointmentRepository).findAll(any(PageRequest.class));
        verify(appointmentRepository).findByClientId(anyLong(), any(PageRequest.class));
        verify(appointmentRepository).findByProfessionalId(anyLong(), any(PageRequest.class));
        verify(appointmentRepository).findByDateTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
        verify(appointmentRepository).findByStatus(any(AppointmentStatus.class), any(PageRequest.class));
    }

    /**
     * Testa o fluxo de atualização de status de agendamento
     */
    @Test
    public void testUpdateAppointmentStatusFlow() {
        // Configurar mock para busca de agendamento
        Appointment appointment = testAppointments.get(0); // PENDING
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        
        // Configurar mock para salvar agendamento atualizado
        Appointment updatedAppointment = new Appointment();
        updatedAppointment.setId(1L);
        updatedAppointment.setClientId(appointment.getClientId());
        updatedAppointment.setProfessionalId(appointment.getProfessionalId());
        updatedAppointment.setServiceId(appointment.getServiceId());
        updatedAppointment.setDateTime(appointment.getDateTime());
        updatedAppointment.setDuration(appointment.getDuration());
        updatedAppointment.setPrice(appointment.getPrice());
        updatedAppointment.setStatus(AppointmentStatus.CONFIRMED);
        updatedAppointment.setPaymentStatus(PaymentStatus.PENDING);
        
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(updatedAppointment);
        
        // Executar método sob teste
        AppointmentDto result = appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED.name());
        
        // Verificar resultado
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(AppointmentStatus.CONFIRMED.name(), result.getStatus());
        
        // Verificar interações
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    /**
     * Testa o fluxo de cancelamento de agendamento
     */
    @Test
    public void testCancelAppointmentFlow() {
        // Configurar mock para busca de agendamento
        Appointment appointment = testAppointments.get(4); // CONFIRMED e PAID
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));
        
        // Configurar mock para reembolso via finance-service
        Map<String, String> refundResponse = new HashMap<>();
        refundResponse.put("status", "REFUNDED");
        refundResponse.put("refundId", "ref-123");
        when(financeServiceClient.processRefund(anyLong(), any())).thenReturn(refundResponse);
        
        // Configurar mock para salvar agendamento atualizado
        Appointment cancelledAppointment = new Appointment();
        cancelledAppointment.setId(5L);
        cancelledAppointment.setClientId(appointment.getClientId());
        cancelledAppointment.setProfessionalId(appointment.getProfessionalId());
        cancelledAppointment.setServiceId(appointment.getServiceId());
        cancelledAppointment.setDateTime(appointment.getDateTime());
        cancelledAppointment.setDuration(appointment.getDuration());
        cancelledAppointment.setPrice(appointment.getPrice());
        cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);
        cancelledAppointment.setPaymentStatus(PaymentStatus.REFUNDED);
        
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(cancelledAppointment);
        
        // Executar método sob teste
        AppointmentDto result = appointmentService.cancelAppointment(5L, "Cliente solicitou cancelamento");
        
        // Verificar resultado
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(AppointmentStatus.CANCELLED.name(), result.getStatus());
        assertEquals(PaymentStatus.REFUNDED.name(), result.getPaymentStatus());
        
        // Verificar interações
        verify(appointmentRepository).findById(5L);
        verify(financeServiceClient).processRefund(anyLong(), any());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    /**
     * Testa o fluxo de recuperação de relatórios de agendamento
     */
    @Test
    public void testGetAppointmentReportsFlow() {
        // Configurar mocks para diferentes relatórios
        
        // Relatório diário
        when(appointmentRepository.countByDateTimeBetween(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(15L);
        
        // Relatório de faturamento
        when(appointmentRepository.sumPriceByDateTimeBetweenAndStatus(
                any(LocalDateTime.class), any(LocalDateTime.class), eq(AppointmentStatus.COMPLETED)))
                .thenReturn(BigDecimal.valueOf(2500.0));
        
        // Relatório por profissional
        Map<Long, Long> appointmentsByProfessional = new HashMap<>();
        appointmentsByProfessional.put(201L, 8L);
        appointmentsByProfessional.put(202L, 5L);
        appointmentsByProfessional.put(203L, 2L);
        
        when(appointmentRepository.countByProfessionalIdAndDateTimeBetween(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Long professionalId = invocation.getArgument(0);
                    return appointmentsByProfessional.getOrDefault(professionalId, 0L);
                });
        
        // Relatório por serviço
        Map<Long, Long> appointmentsByService = new HashMap<>();
        appointmentsByService.put(301L, 6L);
        appointmentsByService.put(302L, 4L);
        appointmentsByService.put(303L, 3L);
        appointmentsByService.put(304L, 2L);
        
        when(appointmentRepository.countByServiceIdAndDateTimeBetween(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Long serviceId = invocation.getArgument(0);
                    return appointmentsByService.getOrDefault(serviceId, 0L);
                });
        
        // Executar e verificar relatório diário
        LocalDate reportDate = LocalDate.now();
        Map<String, Object> dailyReport = appointmentService.getDailyReport(reportDate);
        
        assertNotNull(dailyReport);
        assertEquals(15L, dailyReport.get("totalAppointments"));
        assertEquals(reportDate.toString(), dailyReport.get("date"));
        
        // Executar e verificar relatório de faturamento
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        Map<String, Object> revenueReport = appointmentService.getRevenueReport(startDate, endDate);
        
        assertNotNull(revenueReport);
        assertEquals(BigDecimal.valueOf(2500.0), revenueReport.get("totalRevenue"));
        assertEquals(startDate.toString(), revenueReport.get("startDate"));
        assertEquals(endDate.toString(), revenueReport.get("endDate"));
        
        // Verificar interações
        verify(appointmentRepository).countByDateTimeBetween(
                any(LocalDateTime.class), any(LocalDateTime.class));
        verify(appointmentRepository).sumPriceByDateTimeBetweenAndStatus(
                any(LocalDateTime.class), any(LocalDateTime.class), eq(AppointmentStatus.COMPLETED));
    }

    /**
     * Testa o comportamento quando ocorrem conflitos de horário
     */
    @Test
    public void testAppointmentSchedulingConflicts() {
        // Configurar dados de entrada
        AppointmentDto appointmentDto = new AppointmentDto();
        appointmentDto.setClientId(101L);
        appointmentDto.setProfessionalId(201L);
        appointmentDto.setServiceId(301L);
        appointmentDto.setDateTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
        
        // Configurar mocks para dados externos
        ClientDto clientDto = new ClientDto();
        clientDto.setId(101L);
        when(clientServiceClient.getClientById(101L)).thenReturn(Optional.of(clientDto));
        
        ProfessionalDto professionalDto = new ProfessionalDto();
        professionalDto.setId(201L);
        when(professionalServiceClient.getProfessionalById(201L)).thenReturn(Optional.of(professionalDto));
        
        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId(301L);
        serviceDto.setDuration(60);
        serviceDto.setPrice(BigDecimal.valueOf(150.0));
        when(professionalServiceClient.getServiceById(301L)).thenReturn(Optional.of(serviceDto));
        
        // Simular que o profissional NÃO está disponível neste horário
        when(professionalServiceClient.checkAvailability(eq(201L), any(LocalDateTime.class), eq(60)))
                .thenReturn(false);
        
        // Executar método sob teste (deve lançar exceção)
        Exception exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(appointmentDto);
        });
        
        // Verificar mensagem de erro
        assertTrue(exception.getMessage().contains("não está disponível"));
        
        // Verificar interações
        verify(clientServiceClient).getClientById(101L);
        verify(professionalServiceClient).getProfessionalById(201L);
        verify(professionalServiceClient).getServiceById(301L);
        verify(professionalServiceClient).checkAvailability(eq(201L), any(LocalDateTime.class), eq(60));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}
