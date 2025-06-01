package com.clinicsalon.finance.service;

import com.clinicsalon.finance.dto.TransactionRequest;
import com.clinicsalon.finance.dto.TransactionResponse;
import com.clinicsalon.finance.model.TransactionStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionRequest request);
    
    TransactionResponse getTransactionById(UUID id);
    
    List<TransactionResponse> getAllTransactions();
    
    List<TransactionResponse> getTransactionsByClientId(UUID clientId);
    
    List<TransactionResponse> getTransactionsByAppointmentId(UUID appointmentId);
    
    List<TransactionResponse> getTransactionsByStatus(TransactionStatus status);
    
    List<TransactionResponse> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate);
    
    TransactionResponse updateTransactionStatus(UUID id, TransactionStatus status);
    
    void deleteTransaction(UUID id);
    
    String generatePaymentLink(UUID transactionId);
    
    Double calculateRevenueBetweenDates(LocalDate startDate, LocalDate endDate);
    
    TransactionResponse processPayment(UUID transactionId);
    
    TransactionResponse processRefund(UUID transactionId);
}
