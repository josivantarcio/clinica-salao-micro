package com.clinicsalon.loyalty.integration;

import com.clinicsalon.loyalty.controller.LoyaltyAccountController;
import com.clinicsalon.loyalty.dto.LoyaltyAccountDto;
import com.clinicsalon.loyalty.dto.PointsTransactionDto;
import com.clinicsalon.loyalty.dto.PointsTransactionResponseDto;
import com.clinicsalon.loyalty.enums.PointsTransactionStatus;
import com.clinicsalon.loyalty.enums.PointsTransactionType;
import com.clinicsalon.loyalty.model.LoyaltyAccount;
import com.clinicsalon.loyalty.repository.LoyaltyAccountRepository;
import com.clinicsalon.loyalty.service.ClientLookupService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Testes de integração para o serviço de fidelidade
 * Testa fluxos completos de acumulação e resgate de pontos usando a API REST
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
public class LoyaltyServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @MockBean
    private ClientLookupService clientLookupService;

    private String baseUrl;

    @BeforeEach
    public void setup() {
        baseUrl = "http://localhost:" + port;
        
        // Limpar contas de testes anteriores
        loyaltyAccountRepository.deleteAll();
        
        // Configurar mock do serviço de lookup de cliente
        when(clientLookupService.clientExists(anyLong())).thenReturn(true);
        when(clientLookupService.getClientName(anyLong())).thenReturn("Cliente Teste");
    }

    @AfterEach
    public void cleanup() {
        // Limpar todas as contas criadas durante os testes
        loyaltyAccountRepository.deleteAll();
    }

    /**
     * Testa a criação de uma nova conta de fidelidade para um cliente
     */
    @Test
    public void testCreateLoyaltyAccount() {
        Long clientId = 101L;
        
        // Verificar que a conta não existe inicialmente
        Optional<LoyaltyAccount> existingAccount = loyaltyAccountRepository.findByClientId(clientId);
        assertFalse(existingAccount.isPresent());
        
        // Fazer requisição REST para criar conta
        ResponseEntity<LoyaltyAccountDto> response = restTemplate.postForEntity(
                baseUrl + "/api/loyalty/accounts/" + clientId,
                null,
                LoyaltyAccountDto.class);
        
        // Verificar resposta HTTP
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(clientId, response.getBody().getClientId());
        assertEquals(0, response.getBody().getCurrentPoints());
        assertEquals(0, response.getBody().getTotalAccumulatedPoints());
        
        // Verificar que a conta foi realmente criada no banco de dados
        Optional<LoyaltyAccount> createdAccount = loyaltyAccountRepository.findByClientId(clientId);
        assertTrue(createdAccount.isPresent());
        assertEquals(clientId, createdAccount.get().getClientId());
        assertEquals(0, createdAccount.get().getCurrentPoints());
    }

    /**
     * Testa o fluxo completo de acumulação de pontos para uma conta existente
     */
    @Test
    public void testEarnPointsFlow() {
        // Preparar uma conta de fidelidade existente
        Long clientId = 102L;
        LoyaltyAccount account = new LoyaltyAccount();
        account.setClientId(clientId);
        account.setCurrentPoints(500);
        account.setTotalAccumulatedPoints(1000);
        account.setCreatedAt(LocalDateTime.now().minusMonths(2));
        account.setUpdatedAt(LocalDateTime.now().minusDays(5));
        loyaltyAccountRepository.save(account);
        
        // Criar transação de pontos
        PointsTransactionDto transactionDto = new PointsTransactionDto();
        transactionDto.setClientId(clientId);
        transactionDto.setAppointmentId(1001L);
        transactionDto.setPoints(200);
        transactionDto.setType(PointsTransactionType.EARN);
        transactionDto.setDescription("Pontos por serviço premium");
        
        // Fazer requisição REST para acumular pontos
        ResponseEntity<PointsTransactionResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/api/loyalty/transactions",
                transactionDto,
                PointsTransactionResponseDto.class);
        
        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PointsTransactionStatus.COMPLETED, response.getBody().getStatus());
        assertEquals(200, response.getBody().getPoints());
        assertEquals(700, response.getBody().getNewBalance());
        
        // Verificar que a conta foi atualizada no banco de dados
        Optional<LoyaltyAccount> updatedAccount = loyaltyAccountRepository.findByClientId(clientId);
        assertTrue(updatedAccount.isPresent());
        assertEquals(700, updatedAccount.get().getCurrentPoints());
        assertEquals(1200, updatedAccount.get().getTotalAccumulatedPoints());
    }

    /**
     * Testa o fluxo completo de resgate de pontos
     */
    @Test
    public void testRedeemPointsFlow() {
        // Preparar uma conta de fidelidade existente com saldo
        Long clientId = 103L;
        LoyaltyAccount account = new LoyaltyAccount();
        account.setClientId(clientId);
        account.setCurrentPoints(1000);
        account.setTotalAccumulatedPoints(2000);
        account.setCreatedAt(LocalDateTime.now().minusMonths(3));
        account.setUpdatedAt(LocalDateTime.now().minusDays(10));
        loyaltyAccountRepository.save(account);
        
        // Criar transação de resgate de pontos
        PointsTransactionDto transactionDto = new PointsTransactionDto();
        transactionDto.setClientId(clientId);
        transactionDto.setAppointmentId(1002L);
        transactionDto.setPoints(300);
        transactionDto.setType(PointsTransactionType.REDEEM);
        transactionDto.setDescription("Desconto em serviço de estética");
        
        // Fazer requisição REST para resgatar pontos
        ResponseEntity<PointsTransactionResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/api/loyalty/transactions",
                transactionDto,
                PointsTransactionResponseDto.class);
        
        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PointsTransactionStatus.COMPLETED, response.getBody().getStatus());
        assertEquals(300, response.getBody().getPoints());
        assertEquals(700, response.getBody().getNewBalance());
        
        // Verificar que a conta foi atualizada no banco de dados
        Optional<LoyaltyAccount> updatedAccount = loyaltyAccountRepository.findByClientId(clientId);
        assertTrue(updatedAccount.isPresent());
        assertEquals(700, updatedAccount.get().getCurrentPoints());
        assertEquals(2000, updatedAccount.get().getTotalAccumulatedPoints()); // Total acumulado não muda
    }

    /**
     * Testa o cenário de tentativa de resgate com pontos insuficientes
     */
    @Test
    public void testInsufficientPointsForRedemption() {
        // Preparar uma conta de fidelidade existente com saldo baixo
        Long clientId = 104L;
        LoyaltyAccount account = new LoyaltyAccount();
        account.setClientId(clientId);
        account.setCurrentPoints(200);
        account.setTotalAccumulatedPoints(500);
        account.setCreatedAt(LocalDateTime.now().minusMonths(1));
        account.setUpdatedAt(LocalDateTime.now().minusDays(2));
        loyaltyAccountRepository.save(account);
        
        // Criar transação de resgate com valor maior que o saldo
        PointsTransactionDto transactionDto = new PointsTransactionDto();
        transactionDto.setClientId(clientId);
        transactionDto.setAppointmentId(1003L);
        transactionDto.setPoints(500); // Maior que o saldo disponível
        transactionDto.setType(PointsTransactionType.REDEEM);
        transactionDto.setDescription("Tentativa de resgate com saldo insuficiente");
        
        // Fazer requisição REST para resgatar pontos
        ResponseEntity<PointsTransactionResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/api/loyalty/transactions",
                transactionDto,
                PointsTransactionResponseDto.class);
        
        // Verificar resposta HTTP
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PointsTransactionStatus.INSUFFICIENT_POINTS, response.getBody().getStatus());
        
        // Verificar que a conta não foi alterada
        Optional<LoyaltyAccount> unchangedAccount = loyaltyAccountRepository.findByClientId(clientId);
        assertTrue(unchangedAccount.isPresent());
        assertEquals(200, unchangedAccount.get().getCurrentPoints());
    }

    /**
     * Testa a criação automática de conta quando não existe
     */
    @Test
    public void testAutoCreateAccountOnTransaction() {
        Long clientId = 105L;
        
        // Verificar que a conta não existe inicialmente
        Optional<LoyaltyAccount> existingAccount = loyaltyAccountRepository.findByClientId(clientId);
        assertFalse(existingAccount.isPresent());
        
        // Criar transação de pontos
        PointsTransactionDto transactionDto = new PointsTransactionDto();
        transactionDto.setClientId(clientId);
        transactionDto.setAppointmentId(1004L);
        transactionDto.setPoints(100);
        transactionDto.setType(PointsTransactionType.EARN);
        transactionDto.setDescription("Primeira visita - bônus de boas-vindas");
        
        // Fazer requisição REST para acumular pontos (deve criar conta automaticamente)
        ResponseEntity<PointsTransactionResponseDto> response = restTemplate.postForEntity(
                baseUrl + "/api/loyalty/transactions",
                transactionDto,
                PointsTransactionResponseDto.class);
        
        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PointsTransactionStatus.COMPLETED, response.getBody().getStatus());
        assertEquals(100, response.getBody().getPoints());
        assertEquals(100, response.getBody().getNewBalance());
        
        // Verificar que a conta foi criada e atualizada no banco de dados
        Optional<LoyaltyAccount> newAccount = loyaltyAccountRepository.findByClientId(clientId);
        assertTrue(newAccount.isPresent());
        assertEquals(100, newAccount.get().getCurrentPoints());
        assertEquals(100, newAccount.get().getTotalAccumulatedPoints());
    }

    /**
     * Testa a obtenção de extrato de transações de uma conta
     */
    @Test
    public void testGetAccountStatement() {
        Long clientId = 106L;
        
        // Criar conta e algumas transações
        LoyaltyAccount account = new LoyaltyAccount();
        account.setClientId(clientId);
        account.setCurrentPoints(800);
        account.setTotalAccumulatedPoints(1200);
        account.setCreatedAt(LocalDateTime.now().minusMonths(4));
        account.setUpdatedAt(LocalDateTime.now().minusDays(1));
        loyaltyAccountRepository.save(account);
        
        // Fazer transações para gerar histórico
        // 1. Acumular pontos
        PointsTransactionDto earnTransaction = new PointsTransactionDto();
        earnTransaction.setClientId(clientId);
        earnTransaction.setAppointmentId(1005L);
        earnTransaction.setPoints(200);
        earnTransaction.setType(PointsTransactionType.EARN);
        earnTransaction.setDescription("Pontos regulares");
        
        restTemplate.postForEntity(
                baseUrl + "/api/loyalty/transactions",
                earnTransaction,
                PointsTransactionResponseDto.class);
        
        // 2. Resgatar alguns pontos
        PointsTransactionDto redeemTransaction = new PointsTransactionDto();
        redeemTransaction.setClientId(clientId);
        redeemTransaction.setAppointmentId(1006L);
        redeemTransaction.setPoints(100);
        redeemTransaction.setType(PointsTransactionType.REDEEM);
        redeemTransaction.setDescription("Desconto pequeno");
        
        restTemplate.postForEntity(
                baseUrl + "/api/loyalty/transactions",
                redeemTransaction,
                PointsTransactionResponseDto.class);
        
        // Fazer requisição REST para obter extrato
        ResponseEntity<LoyaltyAccountDto> response = restTemplate.getForEntity(
                baseUrl + "/api/loyalty/accounts/" + clientId,
                LoyaltyAccountDto.class);
        
        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(clientId, response.getBody().getClientId());
        assertEquals(900, response.getBody().getCurrentPoints()); // 800 + 200 - 100
        assertEquals(1400, response.getBody().getTotalAccumulatedPoints()); // 1200 + 200
        
        // Verificar histórico de transações
        assertNotNull(response.getBody().getTransactions());
        assertTrue(response.getBody().getTransactions().size() >= 2);
        
        // Verificar conta atualizada no banco de dados
        Optional<LoyaltyAccount> updatedAccount = loyaltyAccountRepository.findByClientId(clientId);
        assertTrue(updatedAccount.isPresent());
        assertEquals(900, updatedAccount.get().getCurrentPoints());
        assertEquals(1400, updatedAccount.get().getTotalAccumulatedPoints());
    }

    /**
     * Testa a detecção de cliente inexistente (com mock configurado para retornar falso)
     */
    @Test
    public void testNonexistentClientHandling() {
        Long nonExistentClientId = 999L;
        
        // Configurar mock para retornar cliente inexistente
        when(clientLookupService.clientExists(nonExistentClientId)).thenReturn(false);
        
        // Tentar criar conta para cliente inexistente
        ResponseEntity<LoyaltyAccountDto> response = restTemplate.postForEntity(
                baseUrl + "/api/loyalty/accounts/" + nonExistentClientId,
                null,
                LoyaltyAccountDto.class);
        
        // Verificar resposta HTTP
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // Verificar que nenhuma conta foi criada
        Optional<LoyaltyAccount> account = loyaltyAccountRepository.findByClientId(nonExistentClientId);
        assertFalse(account.isPresent());
    }
}
