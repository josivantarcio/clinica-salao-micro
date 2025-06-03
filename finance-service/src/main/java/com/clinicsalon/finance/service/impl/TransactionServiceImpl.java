package com.clinicsalon.finance.service.impl;

import com.clinicsalon.finance.dto.TransactionRequest;
import com.clinicsalon.finance.dto.TransactionResponse;
import com.clinicsalon.finance.exception.TransactionException;
import com.clinicsalon.finance.gateway.PaymentGateway;
import com.clinicsalon.finance.model.Transaction;
import com.clinicsalon.finance.model.TransactionStatus;
import com.clinicsalon.finance.model.TransactionType;
import com.clinicsalon.finance.repository.TransactionRepository;
import com.clinicsalon.finance.service.IntegrationService;
import com.clinicsalon.finance.service.TransactionService;
import com.clinicsalon.finance.util.DtoConverter;
import com.clinicsalon.monitoring.aspect.MonitorPerformance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final IntegrationService integrationService;
    private final PaymentGateway paymentGateway;
    private final DtoConverter dtoConverter;
    
    @Override
    @Transactional
    @MonitorPerformance(description = "Create financial transaction", thresholdMillis = 500, logParameters = true)
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("Creating new transaction for appointment: {}", request.getAppointmentId());
        
        // Verificar se o agendamento existe
        try {
            Map<String, Object> appointmentInfo = integrationService.getAppointmentInfo(request.getAppointmentId());
            if (appointmentInfo.containsKey("fallback") && (boolean) appointmentInfo.get("fallback")) {
                log.warn("Could not verify appointment existence, proceeding with caution");
            }
            
            // Se o valor não foi fornecido, tentar obter do serviço
            if (request.getAmount() == null && request.getType() == TransactionType.PAYMENT) {
                Map<String, Object> serviceDetails = integrationService.getServiceDetailsForAppointment(request.getAppointmentId());
                if (serviceDetails.containsKey("price")) {
                    Double servicePrice = Double.valueOf(serviceDetails.get("price").toString());
                    request.setAmount(servicePrice);
                    log.info("Retrieved service price from appointment-service: {}", servicePrice);
                }
            }
        } catch (Exception e) {
            log.warn("Error retrieving appointment data: {}", e.getMessage());
        }
        
        // Mapear a requisição para a entidade
        Transaction transaction = Transaction.builder()
                .appointmentId(request.getAppointmentId())
                .clientId(request.getClientId())
                .type(request.getType())
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING) // Inicia como pendente
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .build();
                
        // Salvar a transação
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with id: {}", savedTransaction.getId());
        
        return mapToResponse(savedTransaction);
    }
    
    @Override
    public TransactionResponse getTransactionById(UUID id) {
        log.info("Getting transaction with id: {}", id);
        return transactionRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.error("Transaction not found with id: {}", id);
                    return new TransactionException("Transaction not found with id: " + id);
                });
    }
    
    @Override
    public List<TransactionResponse> getAllTransactions() {
        log.info("Getting all transactions");
        return transactionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionResponse> getTransactionsByClientId(UUID clientId) {
        log.info("Getting transactions for client: {}", clientId);
        return transactionRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionResponse> getTransactionsByAppointmentId(UUID appointmentId) {
        log.info("Getting transactions for appointment: {}", appointmentId);
        return transactionRepository.findByAppointmentId(appointmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionResponse> getTransactionsByStatus(TransactionStatus status) {
        log.info("Getting transactions with status: {}", status);
        return transactionRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionResponse> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Getting transactions between {} and {}", startDate, endDate);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        return transactionRepository.findByDateRange(startDateTime, endDateTime)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    @MonitorPerformance(description = "Update transaction status", thresholdMillis = 300)
    public TransactionResponse updateTransactionStatus(UUID id, TransactionStatus status) {
        log.info("Updating transaction {} status to {}", id, status);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Transaction not found with id: {}", id);
                    return new TransactionException("Transaction not found with id: " + id);
                });
                
        transaction.setStatus(status);
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction status updated successfully");
        
        return mapToResponse(updatedTransaction);
    }
    
    @Override
    public void deleteTransaction(UUID id) {
        log.info("Deleting transaction with id: {}", id);
        if (!transactionRepository.existsById(id)) {
            log.error("Transaction not found with id: {}", id);
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        
        transactionRepository.deleteById(id);
        log.info("Transaction deleted successfully");
    }
    
    @Override
    @MonitorPerformance(description = "Generate payment link", thresholdMillis = 800, logParameters = true)
    public String generatePaymentLink(UUID transactionId) {
        log.info("Generating payment link for transaction: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.error("Transaction not found with id: {}", transactionId);
                    return new TransactionException("Transaction not found with id: " + transactionId);
                });
        
        try {
            // Integração com gateway de pagamento
            Map<String, Object> paymentLinkData = paymentGateway.generatePaymentLink(
                    transaction.getId(), 
                    transaction.getAmount(), 
                    transaction.getDescription());
            
            String paymentLink = (String) paymentLinkData.get("paymentLink");
            String gatewayTransactionId = (String) paymentLinkData.get("gatewayTransactionId");
            
            // Atualizar a transação com dados do gateway
            transaction.setInvoiceUrl(paymentLink);
            transaction.setPaymentGatewayId(gatewayTransactionId);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            log.info("Payment link generated via {}: {}", paymentGateway.getGatewayName(), paymentLink);
            return paymentLink;
        } catch (Exception e) {
            log.error("Error generating payment link: {}", e.getMessage());
            throw new TransactionException("Failed to generate payment link: " + e.getMessage(), e);
        }
    }
    
    @Override
    @MonitorPerformance(description = "Calculate revenue between dates", thresholdMillis = 1000)
    public Double calculateRevenueBetweenDates(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating revenue between {} and {}", startDate, endDate);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        Double totalRevenue = transactionRepository.sumTotalRevenue(startDateTime, endDateTime);
        return totalRevenue != null ? totalRevenue : 0.0;
    }
    
    @Override
    @Transactional
    @MonitorPerformance(description = "Process payment via gateway", thresholdMillis = 1000, logParameters = true, alertOnError = true)
    public TransactionResponse processPayment(UUID transactionId) {
        log.info("Processing payment for transaction: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.error("Transaction not found with id: {}", transactionId);
                    return new TransactionException("Transaction not found with id: " + transactionId);
                });
        
        // Verificar se a transação já foi processada
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            log.warn("Transaction {} already completed", transactionId);
            return mapToResponse(transaction);
        }
        
        if (transaction.getStatus() == TransactionStatus.REFUNDED) {
            log.error("Cannot process payment for refunded transaction: {}", transactionId);
            throw new TransactionException("Cannot process payment for refunded transaction");
        }
        
        try {
            // Preparação dos dados para o gateway
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("transactionId", transaction.getId());
            paymentData.put("amount", transaction.getAmount());
            paymentData.put("paymentMethod", transaction.getPaymentMethod());
            
            if (transaction.getPaymentGatewayId() != null) {
                paymentData.put("gatewayTransactionId", transaction.getPaymentGatewayId());
            }
            
            // Integração com o gateway de pagamento
            Map<String, Object> paymentResult = paymentGateway.processPayment(paymentData);
            
            // Atualizar os dados da transação
            transaction.setStatus(TransactionStatus.COMPLETED);
            
            if (paymentResult.containsKey("gatewayTransactionId")) {
                transaction.setPaymentGatewayId((String) paymentResult.get("gatewayTransactionId"));
            }
            
            transaction.setUpdatedAt(LocalDateTime.now());
            Transaction updatedTransaction = transactionRepository.save(transaction);
            
            log.info("Payment processed successfully via {}", paymentGateway.getGatewayName());
            return mapToResponse(updatedTransaction);
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            throw new TransactionException("Failed to process payment: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    @MonitorPerformance(description = "Process payment refund", thresholdMillis = 1000, logParameters = true, alertOnError = true)
    public TransactionResponse processRefund(UUID transactionId) {
        log.info("Processing refund for transaction: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.error("Transaction not found with id: {}", transactionId);
                    return new TransactionException("Transaction not found with id: " + transactionId);
                });
                
        // Validar se a transação pode ser reembolsada
        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            log.error("Cannot refund transaction that is not completed");
            throw new TransactionException("Can only refund completed transactions");
        }
        
        if (transaction.getPaymentGatewayId() == null) {
            log.error("Cannot refund transaction without payment gateway ID");
            throw new TransactionException("Cannot refund transaction without payment gateway reference");
        }
        
        try {
            // Integração com o gateway de pagamento para reembolso
            Map<String, Object> refundResult = paymentGateway.processRefund(
                    transaction.getPaymentGatewayId(), 
                    transaction.getAmount());
            
            // Atualizar a transação original
            transaction.setStatus(TransactionStatus.REFUNDED);
            transaction.setUpdatedAt(LocalDateTime.now());
            
            // Criar uma nova transação de reembolso vinculada à original
            Transaction refundTransaction = Transaction.builder()
                    .appointmentId(transaction.getAppointmentId())
                    .clientId(transaction.getClientId())
                    .type(TransactionType.REFUND)
                    .amount(transaction.getAmount())
                    .status(TransactionStatus.COMPLETED)
                    .paymentMethod(transaction.getPaymentMethod())
                    .description("Reembolso da transação " + transaction.getId())
                    .build();
            
            // Adicionando a referência do refund do gateway
            if (refundResult.containsKey("refundId")) {
                refundTransaction.setPaymentGatewayId((String) refundResult.get("refundId"));
            }
            
            Transaction updatedTransaction = transactionRepository.save(transaction);
            transactionRepository.save(refundTransaction);
            
            log.info("Refund processed successfully via {}", paymentGateway.getGatewayName());
            return mapToResponse(updatedTransaction);
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage());
            throw new TransactionException("Failed to process refund: " + e.getMessage(), e);
        }
    }
    
    private TransactionResponse mapToResponse(Transaction transaction) {
        // Construir o response básico com os dados da transação
        TransactionResponse.TransactionResponseBuilder builder = TransactionResponse.builder()
                .id(transaction.getId())
                .appointmentId(transaction.getAppointmentId())
                .clientId(transaction.getClientId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentGatewayId(transaction.getPaymentGatewayId())
                .invoiceUrl(transaction.getInvoiceUrl())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .description(transaction.getDescription())
                .clientName("N/A")
                .appointmentDate("N/A")
                .serviceName("N/A");
        
        // Tentar enriquecer com dados do client-service
        try {
            Map<String, Object> clientInfo = integrationService.getClientInfo(transaction.getClientId());
            if (clientInfo != null && clientInfo.containsKey("name")) {
                builder.clientName(clientInfo.get("name").toString());
            }
        } catch (Exception e) {
            log.warn("Could not retrieve client info for transaction {}: {}", transaction.getId(), e.getMessage());
        }
        
        // Tentar enriquecer com dados do appointment-service
        try {
            Map<String, Object> appointmentInfo = integrationService.getAppointmentInfo(transaction.getAppointmentId());
            if (appointmentInfo != null) {
                if (appointmentInfo.containsKey("date") && appointmentInfo.containsKey("time")) {
                    String date = appointmentInfo.get("date").toString();
                    String time = appointmentInfo.get("time").toString();
                    builder.appointmentDate(date + " " + time);
                }
            }
            
            Map<String, Object> serviceDetails = integrationService.getServiceDetailsForAppointment(transaction.getAppointmentId());
            if (serviceDetails != null && serviceDetails.containsKey("serviceName")) {
                builder.serviceName(serviceDetails.get("serviceName").toString());
            }
        } catch (Exception e) {
            log.warn("Could not retrieve appointment info for transaction {}: {}", transaction.getId(), e.getMessage());
        }
        
        return builder.build();
    }
}
