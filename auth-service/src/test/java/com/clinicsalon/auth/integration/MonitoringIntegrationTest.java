package com.clinicsalon.auth.integration;

import com.clinicsalon.auth.service.UserService;
import com.clinicsalon.monitoring.aspect.MonitorPerformance;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para verificar o funcionamento do módulo de monitoramento
 */
@SpringBootTest
@ActiveProfiles("monitoring")
@Tag("integration")
public class MonitoringIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("Deve verificar se as anotações @MonitorPerformance estão presentes nos métodos críticos")
    public void shouldHaveMonitorPerformanceAnnotations() {
        // Verificar métodos anotados com @MonitorPerformance no UserService
        Method[] methods = UserService.class.getDeclaredMethods();
        long annotatedMethods = Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(MonitorPerformance.class))
                .count();
        
        // Deve haver pelo menos alguns métodos críticos anotados com @MonitorPerformance
        assertTrue(annotatedMethods > 0, 
                "O UserService deve ter pelo menos um método anotado com @MonitorPerformance");
        
        System.out.println("Número de métodos anotados com @MonitorPerformance: " + annotatedMethods);
    }

    @Test
    @DisplayName("Deve verificar se os contadores de métodos monitorados estão registrados")
    public void shouldRegisterMonitoringMetrics() {
        // Verificar a presença de métricas relacionadas ao monitoramento 
        // Estas são métricas geradas pelo aspecto @MonitorPerformance
        
        // Verificar contadores de execução de métodos
        assertTrue(
            Search.in(meterRegistry).name(name -> name.startsWith("method.invocation.count")).meters().size() > 0,
            "Deve haver contadores de invocação de métodos registrados"
        );
        
        // Verificar timers de execução de métodos
        assertTrue(
            Search.in(meterRegistry).name(name -> name.startsWith("method.execution.time")).meters().size() > 0,
            "Deve haver timers de execução de métodos registrados"
        );
    }
    
    @Test
    @DisplayName("Deve verificar se as métricas de cache estão sendo registradas")
    public void shouldRegisterCacheMetrics() {
        // Verificar a presença de métricas relacionadas ao cache
        assertTrue(
            Search.in(meterRegistry).name(name -> name.startsWith("cache")).meters().size() > 0,
            "Deve haver métricas de cache registradas quando o perfil de monitoramento está ativo"
        );
    }
}
