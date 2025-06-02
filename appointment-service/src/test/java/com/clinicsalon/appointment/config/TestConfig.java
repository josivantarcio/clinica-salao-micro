package com.clinicsalon.appointment.config;

import com.clinicsalon.appointment.client.FinanceServiceClient;
import com.clinicsalon.appointment.client.FinanceServiceClientFallbackFactory;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Configuração específica para testes de integração
 * Fornece mocks dos clientes Feign para evitar dependência de serviços externos
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public FinanceServiceClient financeServiceClient() {
        return Mockito.mock(FinanceServiceClient.class);
    }

    @Bean
    public FinanceServiceClientFallbackFactory financeServiceClientFallbackFactory() {
        return new FinanceServiceClientFallbackFactory();
    }
}
