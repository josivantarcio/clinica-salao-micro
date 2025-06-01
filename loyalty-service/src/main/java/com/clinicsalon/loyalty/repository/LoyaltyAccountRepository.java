package com.clinicsalon.loyalty.repository;

import com.clinicsalon.loyalty.model.LoyaltyAccount;
import com.clinicsalon.loyalty.model.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
    
    Optional<LoyaltyAccount> findByClientId(Long clientId);
    
    List<LoyaltyAccount> findByTier(LoyaltyTier tier);
    
    List<LoyaltyAccount> findByPointsBalanceGreaterThan(Integer pointsThreshold);
}
