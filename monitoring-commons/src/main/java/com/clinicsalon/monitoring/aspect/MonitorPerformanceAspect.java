package com.clinicsalon.monitoring.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspecto para processar a anotação @MonitorPerformance
 * Oferece monitoramento mais detalhado e personalizado para métodos específicos
 */
@Aspect
@Component
@Order(1) // Prioridade mais alta que o aspecto genérico
@Slf4j
public class MonitorPerformanceAspect {

    private final MeterRegistry meterRegistry;

    public MonitorPerformanceAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Intercepta métodos anotados com @MonitorPerformance
     */
    @Around("@annotation(com.clinicsalon.monitoring.aspect.MonitorPerformance)")
    public Object monitorAnnotatedMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Obter a anotação
        MonitorPerformance annotation = method.getAnnotation(MonitorPerformance.class);
        String metricName = annotation.value().isEmpty() 
                ? "custom.method.execution" 
                : annotation.value();
        
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        long threshold = annotation.thresholdMillis();
        
        // Registrar parâmetros se configurado
        if (annotation.logParameters()) {
            log.info("Executando método {}.{} com parâmetros: {}", 
                    className, methodName, Arrays.toString(joinPoint.getArgs()));
        }
        
        // Iniciar timer
        Timer.Sample sample = Timer.start(meterRegistry);
        long startTime = System.currentTimeMillis();
        
        try {
            // Executar o método
            Object result = joinPoint.proceed();
            
            // Calcular duração
            long duration = System.currentTimeMillis() - startTime;
            
            // Parar o timer e registrar métrica
            sample.stop(Timer.builder(metricName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "success")
                    .description(annotation.description())
                    .register(meterRegistry));
            
            // Verificar se excedeu o limiar
            if (duration > threshold) {
                log.warn("Método {}.{} excedeu o limiar de tempo: {}ms (limiar: {}ms)", 
                        className, methodName, duration, threshold);
                
                // Incrementar contador de alertas de performance
                meterRegistry.counter("performance.alerts", 
                        "class", className, 
                        "method", methodName)
                        .increment();
            }
            
            // Registrar parâmetros de saída se configurado
            if (annotation.logParameters() && result != null) {
                log.info("Método {}.{} retornou: {}", 
                        className, methodName, result);
            }
            
            return result;
            
        } catch (Throwable e) {
            // Registrar erro nas métricas
            sample.stop(Timer.builder(metricName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "error")
                    .tag("exception", e.getClass().getSimpleName())
                    .description(annotation.description())
                    .register(meterRegistry));
            
            log.error("Erro durante execução do método monitorado {}.{}: {}", 
                    className, methodName, e.getMessage(), e);
            
            throw e;
        }
    }
}
