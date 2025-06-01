package com.clinicsalon.finance.service;

import com.clinicsalon.finance.dto.TransactionRequest;
import com.clinicsalon.finance.dto.TransactionResponse;
import com.clinicsalon.finance.exception.TransactionException;
import com.clinicsalon.finance.model.Transaction;
import com.clinicsalon.finance.model.TransactionStatus;
import com.clinicsalon.finance.model.TransactionType;
import com.clinicsalon.finance.repository.TransactionRepository;
import com.clinicsalon.finance.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private IntegrationService integrationService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID transactionId;
    private UUID appointmentId;
    private UUID clientId;
    private Transaction transaction;
    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        transaction = Transaction.builder()
                .id(transactionId)
                .appointmentId(appointmentId)
                .clientId(clientId)
                .type(TransactionType.PAYMENT)
                .amount(100.0)
                .status(TransactionStatus.PENDING)
                .paymentMethod("CREDIT_CARD")
                .createdAt(LocalDateTime.now())
                .description("Pagamento de consulta")
                .build();

        transactionRequest = new TransactionRequest();
        transactionRequest.setAppointmentId(appointmentId);
        transactionRequest.setClientId(clientId);
        transactionRequest.setType(TransactionType.PAYMENT);
        transactionRequest.setAmount(100.0);
        transactionRequest.setPaymentMethod("CREDIT_CARD");
        transactionRequest.setDescription("Pagamento de consulta");
    }

    @Test
    void createTransaction_ShouldCreateAndReturnTransaction() {
        // Arrange
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        TransactionResponse response = transactionService.createTransaction(transactionRequest);

        // Assert
        assertNotNull(response);
        assertEquals(transactionId, response.getId());
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals(clientId, response.getClientId());
        assertEquals(TransactionType.PAYMENT, response.getType());
        assertEquals(100.0, response.getAmount());
        assertEquals(TransactionStatus.PENDING, response.getStatus());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void getTransactionById_WhenTransactionExists_ShouldReturnTransaction() {
        // Arrange
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act
        TransactionResponse response = transactionService.getTransactionById(transactionId);

        // Assert
        assertNotNull(response);
        assertEquals(transactionId, response.getId());
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    void getTransactionById_WhenTransactionDoesNotExist_ShouldThrowException() {
        // Arrange
        when(transactionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TransactionException.class, () -> transactionService.getTransactionById(UUID.randomUUID()));
        verify(transactionRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void getAllTransactions_ShouldReturnAllTransactions() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        // Act
        List<TransactionResponse> responses = transactionService.getAllTransactions();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void updateTransactionStatus_WhenTransactionExists_ShouldUpdateStatusAndReturnTransaction() {
        // Arrange
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        TransactionResponse response = transactionService.updateTransactionStatus(transactionId, TransactionStatus.COMPLETED);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void processPayment_WhenTransactionExists_ShouldUpdateToCompletedAndReturnTransaction() {
        // Arrange
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        TransactionResponse response = transactionService.processPayment(transactionId);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertNotNull(transaction.getPaymentGatewayId());
        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void processRefund_WhenTransactionIsCompleted_ShouldRefundAndCreateRefundTransaction() {
        // Arrange
        transaction.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        TransactionResponse response = transactionService.processRefund(transactionId);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionStatus.REFUNDED, transaction.getStatus());
        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, times(2)).save(any(Transaction.class)); // Original transaction and refund transaction
    }

    @Test
    void processRefund_WhenTransactionIsNotCompleted_ShouldThrowException() {
        // Arrange
        transaction.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThrows(TransactionException.class, () -> transactionService.processRefund(transactionId));
        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
