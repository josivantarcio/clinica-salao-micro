package com.clinicsalon.monitoring.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.ExposeExcludePropertyEndpointFilter;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para os endpoints do Spring Boot Actuator
 * Disponibiliza endpoints para monitoramento e observabilidade
 */
@Configuration
public class ActuatorConfig {

    /**
     * Repositório para armazenar traces HTTP recentes
     * Permite visualizar as últimas requisições processadas pelo serviço
     */
    @Bean
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }
    
    /**
     * Configuração para expor todos os endpoints do Actuator
     * Em ambientes de produção, considerar restringir apenas aos necessários
     */
    @Bean
    public ExposeExcludePropertyEndpointFilter exposeAllEndpoints(WebEndpointDiscoverer discoverer) {
        return new ExposeExcludePropertyEndpointFilter(
                discoverer, 
                org.springframework.boot.actuate.autoconfigure.endpoint.EndpointFilter.Type.INCLUDE, 
                "*");
    }
}
