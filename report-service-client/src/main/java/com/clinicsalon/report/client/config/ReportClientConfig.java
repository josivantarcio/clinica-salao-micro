package com.clinicsalon.report.client.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para habilitar clientes Feign para o serviço de relatórios
 */
@Configuration
@EnableFeignClients(basePackages = "com.clinicsalon.report.client")
public class ReportClientConfig {
    // A anotação @EnableFeignClients permite o escaneamento e registro dos clientes Feign
}
