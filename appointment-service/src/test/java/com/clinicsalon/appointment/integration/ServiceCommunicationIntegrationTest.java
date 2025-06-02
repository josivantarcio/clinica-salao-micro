package com.clinicsalon.appointment.integration;

import com.clinicsalon.appointment.client.FinanceServiceClient;
import com.clinicsalon.appointment.client.FinanceServiceClientFallbackFactory;
import com.clinicsalon.appointment.client.LoyaltyServiceClient;
import com.clinicsalon.appointment.dto.LoyaltyPointsDto;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import com.clinicsalon.appointment.model.PaymentStatus;
import com.clinicsalon.appointment.repository.AppointmentRepository;
import com.clinicsalon.appointment.service.AppointmentPaymentService;
import com.clinicsalon.appointment.service.LoyaltyIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Testes de integração avançados para verificar a comunicação entre serviços
 * com foco na integração do appointment-service com finance-service e loyalty-service
 */
@ExtendWith(MockitoExtension.class)
@Tag("integration")
public class ServiceCommunicationIntegrationTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private FinanceServiceClient financeServiceClient;

    @Mock
    private LoyaltyServiceClient loyaltyServiceClient;

    @Mock
    private FinanceServiceClientFallbackFactory fallbackFactory;

    private AppointmentPaymentService appointmentPaymentService;
    private LoyaltyIntegrationService loyaltyIntegrationService;

    @BeforeEach
    public void setup() {
        // Inicializar serviços com suas dependências
        appointmentPaymentService = new AppointmentPaymentService(
                appointmentRepository, financeServiceClient, loyaltyServiceClient);
        
        loyaltyIntegrationService = new LoyaltyIntegrationService(
                appointmentRepository, loyaltyServiceClient);
    }

    /**
     * Testa o fluxo completo de pagamento:
     * 1. Criação de link de pagamento
     * 2. Verificação de status
     * 3. Processamento de reembolso
     * 
     * Simula a comunicação com finance-service via client
     */
    @Test
    public void testCompletePaymentFlow() {
        // Configurar appointment
        Appointment appointment = createSampleAppointment();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        
        // 1. Configurar resposta para criação de link de pagamento
        Map<String, String> paymentLinkResponse = new HashMap<>();
        paymentLinkResponse.put("paymentLink", "https://payment.gateway.com/pay/123");
        when(financeServiceClient.createPaymentLink(
                eq(1L), anyDouble(), any(), any(), any()))
                .thenReturn(paymentLinkResponse);
        
        // 2. Configurar resposta para verificação de status
        Map<String, String> statusResponse = new HashMap<>();
        statusResponse.put("status", "PAID");
        when(financeServiceClient.getPaymentStatusByAppointmentId(1L))
                .thenReturn(statusResponse);
        
        // 3. Configurar resposta para reembolso
        Map<String, String> refundResponse = new HashMap<>();
        refundResponse.put("status", "REFUNDED");
        refundResponse.put("refundId", "ref-123");
        when(financeServiceClient.processRefund(eq(1L), any()))
                .thenReturn(refundResponse);
                
        // Testar fluxo completo
        
        // 1. Criar link de pagamento
        Map<String, String> paymentLinkResult = appointmentPaymentService.createPaymentLink(1L);
        assertNotNull(paymentLinkResult);
        assertEquals("https://payment.gateway.com/pay/123", paymentLinkResult.get("paymentLink"));
        
        // 2. Verificar status do pagamento
        Map<String, String> statusResult = appointmentPaymentService.getPaymentStatus(1L);
        assertNotNull(statusResult);
        assertEquals("PAID", statusResult.get("status"));
        
        // 3. Processar reembolso
        Map<String, String> refundResult = appointmentPaymentService.processRefund(1L);
        assertNotNull(refundResult);
        assertEquals("REFUNDED", refundResult.get("status"));
        assertEquals("ref-123", refundResult.get("refundId"));
        
        // Verificar que o fluxo completo funciona corretamente
        assertTrue(true, "O fluxo completo de pagamento foi executado com sucesso");
    }
    
    /**
     * Testa o comportamento quando finance-service está indisponível
     * Verifica se os circuit breakers e fallbacks estão funcionando
     */
    @Test
    public void testFallbackBehavior() {
        // Configurar appointment
        Appointment appointment = createSampleAppointment();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        
        // Configurar falhas nos serviços para acionar fallbacks
        when(financeServiceClient.createPaymentLink(anyLong(), anyDouble(), any(), any(), any()))
                .thenThrow(new RuntimeException("Service unavailable"));
        
        when(financeServiceClient.getPaymentStatusByAppointmentId(anyLong()))
                .thenThrow(new RuntimeException("Service unavailable"));
                
        when(financeServiceClient.processRefund(anyLong(), any()))
                .thenThrow(new RuntimeException("Service unavailable"));
        
        // 1. Testar fallback para criação de link de pagamento
        Map<String, String> paymentLinkResult = appointmentPaymentService.createPaymentLink(1L);
        assertNotNull(paymentLinkResult);
        assertTrue(paymentLinkResult.containsKey("message"));
        assertTrue(paymentLinkResult.get("message").contains("fallback"));
        
        // 2. Testar fallback para verificação de status
        Map<String, String> statusResult = appointmentPaymentService.getPaymentStatus(1L);
        assertNotNull(statusResult);
        assertTrue(statusResult.containsKey("message"));
        assertTrue(statusResult.get("message").contains("fallback"));
        
        // 3. Testar fallback para reembolso
        Map<String, String> refundResult = appointmentPaymentService.processRefund(1L);
        assertNotNull(refundResult);
        assertTrue(refundResult.containsKey("message"));
        assertTrue(refundResult.get("message").contains("fallback"));
        
        // Verificar que todos os fallbacks foram acionados corretamente
        assertTrue(true, "Todos os fallbacks foram acionados corretamente");
    }
    
    /**
     * Testa a integração com o serviço de fidelidade
     * Verifica o cálculo e atribuição de pontos
     */
    @Test
    public void testLoyaltyIntegration() {
        // Configurar appointment
        Appointment appointment = createSampleAppointment();
        appointment.setPaymentStatus(PaymentStatus.PAID);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        
        // Configurar resposta do serviço de fidelidade
        LoyaltyPointsDto pointsDto = new LoyaltyPointsDto();
        pointsDto.setClientId(101L);
        pointsDto.setAppointmentId(1L);
        pointsDto.setPoints(15); // 10% do valor do serviço
        when(loyaltyServiceClient.addPoints(any(LoyaltyPointsDto.class)))
                .thenReturn(pointsDto);
        
        // Testar adição de pontos
        LoyaltyPointsDto result = loyaltyIntegrationService.addLoyaltyPoints(1L);
        
        // Verificar resultado
        assertNotNull(result);
        assertEquals(101L, result.getClientId());
        assertEquals(1L, result.getAppointmentId());
        assertEquals(15, result.getPoints());
    }
    
    /**
     * Testa o comportamento quando loyalty-service está indisponível
     */
    @Test
    public void testLoyaltyServiceFallback() {
        // Configurar appointment
        Appointment appointment = createSampleAppointment();
        appointment.setPaymentStatus(PaymentStatus.PAID);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        
        // Configurar falha no serviço de fidelidade
        when(loyaltyServiceClient.addPoints(any(LoyaltyPointsDto.class)))
                .thenThrow(new RuntimeException("Service unavailable"));
        
        // Testar adição de pontos com serviço indisponível
        LoyaltyPointsDto result = loyaltyIntegrationService.addLoyaltyPoints(1L);
        
        // Verificar resultado do fallback
        assertNotNull(result);
        assertEquals(0, result.getPoints());
        assertEquals("fallback", result.getStatus());
    }
    
    /**
     * Método auxiliar para criar um appointment de exemplo
     */
    private Appointment createSampleAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setClientId(101L);
        appointment.setProfessionalId(201L);
        appointment.setDateTime(LocalDateTime.now().plusDays(1));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setPaymentStatus(PaymentStatus.PENDING);
        appointment.setPrice(BigDecimal.valueOf(150.00));
        appointment.setServiceId(301L);
        appointment.setDuration(60);
        return appointment;
    }
}
