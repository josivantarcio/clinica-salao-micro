package com.clinicsalon.finance.repository;

import com.clinicsalon.finance.model.Transaction;
import com.clinicsalon.finance.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByClientId(UUID clientId);
    
    List<Transaction> findByAppointmentId(UUID appointmentId);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.clientId = :clientId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByClientIdAndDateRange(UUID clientId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    Double sumTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);
}
