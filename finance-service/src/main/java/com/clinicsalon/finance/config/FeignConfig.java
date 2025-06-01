package com.clinicsalon.finance.config;

import feign.Retryer;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuração para clientes Feign com circuit breaker
 */
@Configuration
public class FeignConfig {

    /**
     * Configura o circuit breaker para maior resiliência
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3))
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .slidingWindowSize(10)
                        .permittedNumberOfCallsInHalfOpenState(5)
                        .build())
                .build());
    }

    /**
     * Configuração de retry para clientes Feign
     * Tenta 3 vezes com intervalo de 1 segundo
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 1000, 3);
    }
}
