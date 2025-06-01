package com.clinicsalon.professional.client.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para habilitar os clientes Feign do módulo professional-service-client.
 * Esta configuração deve ser importada para usar os clientes Feign.
 */
@Configuration
@EnableFeignClients(basePackages = "com.clinicsalon.professional.client")
public class ProfessionalClientConfig {
    // A anotação @EnableFeignClients é suficiente para habilitar 
    // o escaneamento e registro dos clientes Feign no pacote especificado
}
