package com.clinicsalon.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class LoyaltyClientFallbackFactory implements FallbackFactory<LoyaltyClient> {

    @Override
    public LoyaltyClient create(Throwable cause) {
        return new LoyaltyClientFallback(cause);
    }

    @Slf4j
    static class LoyaltyClientFallback implements LoyaltyClient {
        private final Throwable cause;

        LoyaltyClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public List<LoyaltyPointsDto> getClientLoyaltyPoints(Long clientId) {
            log.error("Fallback para getClientLoyaltyPoints. ClientId: {}, erro: {}", clientId, cause.getMessage());
            return Collections.emptyList();
        }

        @Override
        public List<LoyaltyPointsDto> getClientLoyaltyPointsByDateRange(Long clientId, LocalDate startDate, LocalDate endDate) {
            log.error("Fallback para getClientLoyaltyPointsByDateRange. ClientId: {}, startDate: {}, endDate: {}, erro: {}", 
                clientId, startDate, endDate, cause.getMessage());
            return Collections.emptyList();
        }

        @Override
        public Integer getTotalClientPoints(Long clientId) {
            log.error("Fallback para getTotalClientPoints. ClientId: {}, erro: {}", clientId, cause.getMessage());
            return 0;
        }
    }
}
