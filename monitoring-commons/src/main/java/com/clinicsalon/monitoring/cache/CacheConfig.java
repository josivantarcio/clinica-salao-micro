package com.clinicsalon.monitoring.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Configuração centralizada de cache para microsserviços da ClinicaSalao
 * Utiliza Caffeine como implementação de cache em memória para melhorar o desempenho
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Caches predefinidos para os serviços mais utilizados
     */
    public static final String PROFESSIONALS_CACHE = "professionals";
    public static final String SERVICES_CACHE = "services";
    public static final String CLIENTS_CACHE = "clients";
    public static final String APPOINTMENTS_CACHE = "appointments";
    public static final String PAYMENT_STATUS_CACHE = "paymentStatus";
    public static final String LOYALTY_POINTS_CACHE = "loyaltyPoints";

    /**
     * Configuração do gerenciador de cache com Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configura os caches predefinidos
        cacheManager.setCacheNames(Arrays.asList(
                PROFESSIONALS_CACHE, 
                SERVICES_CACHE,
                CLIENTS_CACHE,
                APPOINTMENTS_CACHE,
                PAYMENT_STATUS_CACHE,
                LOYALTY_POINTS_CACHE
        ));
        
        // Configuração padrão para todos os caches
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats());
        
        return cacheManager;
    }
    
    /**
     * Cache especializado para profissionais com maior tempo de expiração
     */
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<Object, Object> professionalsCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * Cache especializado para status de pagamentos com menor tempo de expiração
     */
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<Object, Object> paymentStatusCache() {
        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(2000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
