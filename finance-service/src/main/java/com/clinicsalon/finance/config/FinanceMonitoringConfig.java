package com.clinicsalon.finance.config;

import com.clinicsalon.monitoring.aspect.MonitorPerformance;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuração de monitoramento específica para o Finance Service
 * Importa as configurações comuns do módulo de monitoramento
 */
@Configuration
@EnableCaching
@MonitorPerformance(description = "Finance Service core operations")
public class FinanceMonitoringConfig {
    
    // A configuração principal é importada automaticamente via auto-configuração
    // Esta classe serve para personalizações específicas do finance-service
    
}
