package com.clinicsalon.loyalty.repository;

import com.clinicsalon.loyalty.model.LoyaltyAccount;
import com.clinicsalon.loyalty.model.LoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    
    Page<LoyaltyTransaction> findByLoyaltyAccount(LoyaltyAccount account, Pageable pageable);
    
    List<LoyaltyTransaction> findByLoyaltyAccountAndTransactionDateBetween(
            LoyaltyAccount account, LocalDateTime startDate, LocalDateTime endDate);
    
    List<LoyaltyTransaction> findByLoyaltyAccountAndType(
            LoyaltyAccount account, LoyaltyTransaction.TransactionType type);
    
    List<LoyaltyTransaction> findByExpiryDateBefore(LocalDateTime date);
}
