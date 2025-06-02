package com.clinicsalon.finance.integration;

import com.clinicsalon.finance.controller.PaymentController;
import com.clinicsalon.finance.dto.*;
import com.clinicsalon.finance.enums.PaymentStatus;
import com.clinicsalon.finance.enums.TransactionType;
import com.clinicsalon.finance.gateway.PaymentGateway;
import com.clinicsalon.finance.model.Transaction;
import com.clinicsalon.finance.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de integração para o serviço financeiro
 * Testa fluxos completos de transações financeiras usando uma API REST
 * e verifica a correta persistência e integração com o gateway de pagamento
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
public class FinanceServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private PaymentGateway paymentGateway;

    private String baseUrl;

    @BeforeEach
    public void setup() {
        baseUrl = "http://localhost:" + port;
        
        // Limpar transações de testes anteriores
        transactionRepository.deleteAll();
        
        // Configurar comportamento do gateway de pagamento mockado
        when(paymentGateway.createPaymentLink(any(PaymentLinkRequestDto.class)))
                .thenAnswer(invocation -> {
                    PaymentLinkRequestDto request = invocation.getArgument(0);
                    
                    PaymentLinkResponseDto response = new PaymentLinkResponseDto();
                    response.setId(UUID.randomUUID().toString());
                    response.setPaymentLink("https://payment.gateway.com/pay/" + UUID.randomUUID().toString());
                    response.setExpirationDate(LocalDateTime.now().plusDays(7));
                    response.setAmount(request.getAmount());
                    response.setStatus(PaymentStatus.PENDING.name());
                    
                    return response;
                });
    }

    @AfterEach
    public void cleanup() {
        // Limpar todas as transações criadas durante os testes
        transactionRepository.deleteAll();
    }

    /**
     * Testa o fluxo completo de criação de link de pagamento
     */
    @Test
    public void testCreatePaymentLinkFlow() {
        // Criar request para link de pagamento
        PaymentLinkRequestDto request = new PaymentLinkRequestDto();
        request.setAppointmentId(1001L);
        request.setClientId(101L);
        request.setProfessionalId(201L);
        request.setAmount(BigDecimal.valueOf(150.00));
        request.setDescription("Serviço de corte de cabelo");

        // Fazer requisição REST para criar link de pagamento
        ResponseEntity<PaymentLinkResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/api/finance/payment-link",
                request,
                PaymentLinkResponseDto.class);

        // Verificar resposta HTTP
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertNotNull(response.getBody().getPaymentLink());
        assertEquals(PaymentStatus.PENDING.name(), response.getBody().getStatus());

        // Verificar que a transação foi salva no banco de dados
        List<Transaction> transactions = transactionRepository.findAll();
        assertFalse(transactions.isEmpty());
        
        Transaction transaction = transactions.get(0);
        assertEquals(request.getAppointmentId(), transaction.getAppointmentId());
        assertEquals(request.getClientId(), transaction.getClientId());
        assertEquals(request.getProfessionalId(), transaction.getProfessionalId());
        assertEquals(0, request.getAmount().compareTo(transaction.getAmount()));
        assertEquals(PaymentStatus.PENDING, transaction.getStatus());
        assertEquals(TransactionType.PAYMENT, transaction.getType());
        
        // Verificar que o gateway de pagamento foi chamado
        verify(paymentGateway, times(1)).createPaymentLink(any(PaymentLinkRequestDto.class));
    }

    /**
     * Testa o fluxo completo de atualização de status de pagamento
     */
    @Test
    public void testPaymentStatusUpdateFlow() {
        // Preparar uma transação existente
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setExternalId("ext-" + UUID.randomUUID().toString());
        transaction.setAppointmentId(1002L);
        transaction.setClientId(102L);
        transaction.setProfessionalId(202L);
        transaction.setAmount(BigDecimal.valueOf(200.00));
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setType(TransactionType.PAYMENT);
        transaction.setDescription("Serviço de manicure");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        // Criar request para atualização de status
        PaymentStatusUpdateDto updateRequest = new PaymentStatusUpdateDto();
        updateRequest.setTransactionId(transaction.getId());
        updateRequest.setStatus(PaymentStatus.PAID.name());
        updateRequest.setPaymentDate(LocalDateTime.now());

        // Configurar mock do gateway para verificação de status
        when(paymentGateway.checkPaymentStatus(transaction.getExternalId()))
                .thenReturn(PaymentStatus.PAID);

        // Fazer requisição REST para atualizar status
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<TransactionDto> response = restTemplate.exchange(
                baseUrl + "/api/finance/payment/status",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, headers),
                TransactionDto.class);

        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PaymentStatus.PAID.name(), response.getBody().getStatus());

        // Verificar que a transação foi atualizada no banco de dados
        Optional<Transaction> updatedTransaction = transactionRepository.findById(transaction.getId());
        assertTrue(updatedTransaction.isPresent());
        assertEquals(PaymentStatus.PAID, updatedTransaction.get().getStatus());
    }

    /**
     * Testa o fluxo completo de processamento de reembolso
     */
    @Test
    public void testRefundProcessingFlow() {
        // Preparar uma transação existente que será reembolsada
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setExternalId("ext-" + UUID.randomUUID().toString());
        transaction.setAppointmentId(1003L);
        transaction.setClientId(103L);
        transaction.setProfessionalId(203L);
        transaction.setAmount(BigDecimal.valueOf(300.00));
        transaction.setStatus(PaymentStatus.PAID);
        transaction.setType(TransactionType.PAYMENT);
        transaction.setDescription("Serviço de tratamento facial");
        transaction.setCreatedAt(LocalDateTime.now().minusDays(1));
        transaction = transactionRepository.save(transaction);

        // Configurar mock do gateway para processar reembolso
        when(paymentGateway.processRefund(any(RefundRequestDto.class)))
                .thenAnswer(invocation -> {
                    RefundRequestDto request = invocation.getArgument(0);
                    
                    TransactionDto response = new TransactionDto();
                    response.setId(UUID.randomUUID().toString());
                    response.setAppointmentId(request.getAppointmentId());
                    response.setAmount(request.getAmount());
                    response.setStatus(PaymentStatus.REFUNDED.name());
                    response.setType(TransactionType.REFUND.name());
                    response.setDescription("Reembolso processado");
                    response.setCreatedAt(LocalDateTime.now());
                    
                    return response;
                });

        // Criar request para reembolso
        RefundRequestDto refundRequest = new RefundRequestDto();
        refundRequest.setTransactionId(transaction.getId());
        refundRequest.setAppointmentId(transaction.getAppointmentId());
        refundRequest.setAmount(transaction.getAmount());
        refundRequest.setReason("Cliente insatisfeito com o serviço");

        // Fazer requisição REST para processar reembolso
        ResponseEntity<TransactionDto> response = restTemplate.postForEntity(
                baseUrl + "/api/finance/refund",
                refundRequest,
                TransactionDto.class);

        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TransactionType.REFUND.name(), response.getBody().getType());
        assertEquals(PaymentStatus.REFUNDED.name(), response.getBody().getStatus());

        // Verificar que a transação original foi atualizada no banco de dados
        Optional<Transaction> updatedOriginalTransaction = transactionRepository.findById(transaction.getId());
        assertTrue(updatedOriginalTransaction.isPresent());
        assertEquals(PaymentStatus.REFUNDED, updatedOriginalTransaction.get().getStatus());

        // Verificar que uma nova transação de reembolso foi criada
        List<Transaction> allTransactions = transactionRepository.findAll();
        assertEquals(2, allTransactions.size());
        
        Optional<Transaction> refundTransaction = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.REFUND)
                .findFirst();
        
        assertTrue(refundTransaction.isPresent());
        assertEquals(transaction.getAppointmentId(), refundTransaction.get().getAppointmentId());
        assertEquals(transaction.getClientId(), refundTransaction.get().getClientId());
        assertEquals(transaction.getProfessionalId(), refundTransaction.get().getProfessionalId());
        assertEquals(0, transaction.getAmount().compareTo(refundTransaction.get().getAmount()));
    }

    /**
     * Testa a recuperação de transações por agendamento
     */
    @Test
    public void testGetTransactionsByAppointment() {
        // Preparar múltiplas transações para o mesmo agendamento
        Long appointmentId = 1004L;
        
        // Transação de pagamento
        Transaction paymentTransaction = new Transaction();
        paymentTransaction.setId(UUID.randomUUID().toString());
        paymentTransaction.setExternalId("ext-" + UUID.randomUUID().toString());
        paymentTransaction.setAppointmentId(appointmentId);
        paymentTransaction.setClientId(104L);
        paymentTransaction.setProfessionalId(204L);
        paymentTransaction.setAmount(BigDecimal.valueOf(250.00));
        paymentTransaction.setStatus(PaymentStatus.PAID);
        paymentTransaction.setType(TransactionType.PAYMENT);
        paymentTransaction.setDescription("Serviço de coloração");
        paymentTransaction.setCreatedAt(LocalDateTime.now().minusDays(2));
        transactionRepository.save(paymentTransaction);
        
        // Transação de reembolso parcial
        Transaction refundTransaction = new Transaction();
        refundTransaction.setId(UUID.randomUUID().toString());
        refundTransaction.setExternalId("ext-" + UUID.randomUUID().toString());
        refundTransaction.setAppointmentId(appointmentId);
        refundTransaction.setClientId(104L);
        refundTransaction.setProfessionalId(204L);
        refundTransaction.setAmount(BigDecimal.valueOf(100.00));
        refundTransaction.setStatus(PaymentStatus.REFUNDED);
        refundTransaction.setType(TransactionType.REFUND);
        refundTransaction.setDescription("Reembolso parcial - serviço incompleto");
        refundTransaction.setCreatedAt(LocalDateTime.now().minusDays(1));
        transactionRepository.save(refundTransaction);

        // Fazer requisição REST para buscar transações por agendamento
        ResponseEntity<TransactionListDto> response = restTemplate.getForEntity(
                baseUrl + "/api/finance/transactions/appointment/" + appointmentId,
                TransactionListDto.class);

        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTransactions());
        assertEquals(2, response.getBody().getTransactions().size());
        
        // Verificar conteúdo das transações retornadas
        boolean hasPayment = false;
        boolean hasRefund = false;
        
        for (TransactionDto dto : response.getBody().getTransactions()) {
            if (TransactionType.PAYMENT.name().equals(dto.getType())) {
                hasPayment = true;
                assertEquals(PaymentStatus.PAID.name(), dto.getStatus());
                assertEquals(0, BigDecimal.valueOf(250.00).compareTo(new BigDecimal(dto.getAmount().toString())));
            } else if (TransactionType.REFUND.name().equals(dto.getType())) {
                hasRefund = true;
                assertEquals(PaymentStatus.REFUNDED.name(), dto.getStatus());
                assertEquals(0, BigDecimal.valueOf(100.00).compareTo(new BigDecimal(dto.getAmount().toString())));
            }
        }
        
        assertTrue(hasPayment, "Deve conter uma transação de pagamento");
        assertTrue(hasRefund, "Deve conter uma transação de reembolso");
    }

    /**
     * Testa o cenário de falha no gateway de pagamento e recuperação de erro
     */
    @Test
    public void testPaymentGatewayFailureAndRecovery() {
        // Configurar mock do gateway para falhar na primeira chamada e suceder na segunda
        when(paymentGateway.createPaymentLink(any(PaymentLinkRequestDto.class)))
                .thenThrow(new RuntimeException("Gateway de pagamento indisponível (simulação)"))
                .thenAnswer(invocation -> {
                    PaymentLinkRequestDto request = invocation.getArgument(0);
                    
                    PaymentLinkResponseDto response = new PaymentLinkResponseDto();
                    response.setId(UUID.randomUUID().toString());
                    response.setPaymentLink("https://payment.gateway.com/pay/" + UUID.randomUUID().toString());
                    response.setExpirationDate(LocalDateTime.now().plusDays(7));
                    response.setAmount(request.getAmount());
                    response.setStatus(PaymentStatus.PENDING.name());
                    
                    return response;
                });

        // Criar request para link de pagamento
        PaymentLinkRequestDto request = new PaymentLinkRequestDto();
        request.setAppointmentId(1005L);
        request.setClientId(105L);
        request.setProfessionalId(205L);
        request.setAmount(BigDecimal.valueOf(175.00));
        request.setDescription("Serviço de depilação");

        // Primeira tentativa - deve falhar
        ResponseEntity<PaymentLinkResponseDto> firstResponse = restTemplate.postForEntity(
                baseUrl + "/api/finance/payment-link",
                request,
                PaymentLinkResponseDto.class);

        // Verificar resposta HTTP da primeira tentativa
        // Dependendo da implementação, pode retornar erro 500 ou um status fallback
        // Em ambos os casos, não deve ter um link de pagamento válido
        if (firstResponse.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            assertNull(firstResponse.getBody()); // Sem corpo em caso de erro 500
        } else {
            // Se implementar fallback, deve retornar status especial
            assertNotNull(firstResponse.getBody());
            assertTrue(firstResponse.getBody().getStatus().contains("RETRY") 
                    || firstResponse.getBody().getStatus().contains("ERROR"));
        }

        // Segunda tentativa - deve ter sucesso
        ResponseEntity<PaymentLinkResponseDto> secondResponse = restTemplate.postForEntity(
                baseUrl + "/api/finance/payment-link",
                request,
                PaymentLinkResponseDto.class);

        // Verificar resposta HTTP da segunda tentativa
        assertEquals(HttpStatus.CREATED, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());
        assertNotNull(secondResponse.getBody().getPaymentLink());
        assertEquals(PaymentStatus.PENDING.name(), secondResponse.getBody().getStatus());

        // Verificar que o gateway foi chamado duas vezes
        verify(paymentGateway, times(2)).createPaymentLink(any(PaymentLinkRequestDto.class));
    }
}
