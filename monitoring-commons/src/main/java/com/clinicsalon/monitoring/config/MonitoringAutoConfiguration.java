package com.clinicsalon.monitoring.config;

import com.clinicsalon.monitoring.aspect.MonitorPerformanceAspect;
import com.clinicsalon.monitoring.aspect.PerformanceMonitoringAspect;
import com.clinicsalon.monitoring.cache.CacheConfig;
import com.clinicsalon.monitoring.cache.CacheMonitor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuração automática para o módulo de monitoramento
 * Esta classe facilita a importação de todas as funcionalidades de monitoramento
 * em qualquer microsserviço do projeto ClinicaSalao
 */
@Configuration
@EnableScheduling
@Import({
        MetricsConfig.class,
        ActuatorConfig.class,
        CacheConfig.class
})
@ComponentScan(basePackages = "com.clinicsalon.monitoring")
public class MonitoringAutoConfiguration {

    /**
     * Cria o aspecto de monitoramento de performance se ainda não existir
     */
    @Bean
    @ConditionalOnMissingBean
    public PerformanceMonitoringAspect performanceMonitoringAspect(MeterRegistry meterRegistry) {
        return new PerformanceMonitoringAspect(meterRegistry);
    }

    /**
     * Cria o aspecto para anotações @MonitorPerformance se ainda não existir
     */
    @Bean
    @ConditionalOnMissingBean
    public MonitorPerformanceAspect monitorPerformanceAspect(MeterRegistry meterRegistry) {
        return new MonitorPerformanceAspect(meterRegistry);
    }

    /**
     * Cria o monitor de cache se ainda não existir e se o cache estiver habilitado
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
    public CacheMonitor cacheMonitor(CacheManager cacheManager, MeterRegistry meterRegistry) {
        return new CacheMonitor(cacheManager, meterRegistry);
    }
}
