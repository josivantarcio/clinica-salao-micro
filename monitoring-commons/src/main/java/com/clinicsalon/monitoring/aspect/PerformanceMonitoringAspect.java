package com.clinicsalon.monitoring.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Aspecto para monitoramento automático de desempenho dos métodos
 * Registra tempo de execução, taxa de erros e outros indicadores importantes
 */
@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {

    private final MeterRegistry meterRegistry;

    public PerformanceMonitoringAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Monitora métodos dos controladores REST
     */
    @Around("execution(* com.clinicsalon..*.controller..*.*(..))")
    public Object monitorControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorMethodExecution(joinPoint, "controller");
    }

    /**
     * Monitora métodos dos serviços de negócio
     */
    @Around("execution(* com.clinicsalon..*.service..*.*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorMethodExecution(joinPoint, "service");
    }

    /**
     * Monitora métodos que acessam repositórios/banco de dados
     */
    @Around("execution(* com.clinicsalon..*.repository..*.*(..))") 
    public Object monitorRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorMethodExecution(joinPoint, "repository");
    }

    /**
     * Monitora chamadas a clients Feign (comunicação entre serviços)
     */
    @Around("execution(* com.clinicsalon..*.client..*.*(..))") 
    public Object monitorFeignClientMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorMethodExecution(joinPoint, "feignClient");
    }
    
    /**
     * Monitora métodos anotados com @MonitorPerformance
     */
    @Around("@annotation(com.clinicsalon.monitoring.aspect.MonitorPerformance)")
    public Object monitorAnnotatedMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MonitorPerformance annotation = method.getAnnotation(MonitorPerformance.class);
        
        if (annotation != null) {
            return monitorAnnotatedMethodExecution(joinPoint, annotation);
        }
        
        // Fallback para tratamento padrão
        return monitorMethodExecution(joinPoint, "annotated");
    }

    /**
     * Implementação do monitoramento com medição de tempo e contagem de erros
     */
    private Object monitorMethodExecution(ProceedingJoinPoint joinPoint, String layerType) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String metricName = "method.execution";
        
        // Timer para medir o tempo de execução
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Executa o método original
            Object result = joinPoint.proceed();
            
            // Registra execução bem-sucedida
            sample.stop(Timer.builder(metricName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("layer", layerType)
                    .tag("status", "success")
                    .register(meterRegistry));
            
            return result;
            
        } catch (Throwable e) {
            // Registra execução com erro
            sample.stop(Timer.builder(metricName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("layer", layerType)
                    .tag("status", "error")
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry));
            
            // Incrementa contador de erros
            meterRegistry.counter("method.errors", 
                    "class", className, 
                    "method", methodName,
                    "layer", layerType,
                    "exception", e.getClass().getSimpleName())
                    .increment();
            
            log.error("Erro durante execução do método {}.{}: {}", 
                    className, methodName, e.getMessage());
            
            throw e;
        }
    }
    
    /**
     * Implementação específica para métodos anotados com @MonitorPerformance
     */
    private Object monitorAnnotatedMethodExecution(ProceedingJoinPoint joinPoint, MonitorPerformance annotation) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String description = annotation.description().isEmpty() ? methodName : annotation.description();
        String metricName = annotation.value().isEmpty() ? "method.monitored.execution" : annotation.value();
        long thresholdMillis = annotation.thresholdMillis();
        boolean logParams = annotation.logParameters();
        boolean alertOnError = annotation.alertOnError();
        
        if (logParams) {
            log.info("Executando método {} com parâmetros: {}", 
                   description, Arrays.toString(joinPoint.getArgs()));
        }
        
        // Timer para medir o tempo de execução
        long startTime = System.nanoTime();
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Executa o método original
            Object result = joinPoint.proceed();
            
            // Calcula tempo decorrido
            long executionTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            
            // Registra execução bem-sucedida
            sample.stop(Timer.builder(metricName)
                    .description(description)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "success")
                    .register(meterRegistry));
            
            // Verifica se ultrapassou o threshold
            if (executionTime > thresholdMillis) {
                log.warn("ALERTA DE PERFORMANCE: Método {} excedeu o limite de {} ms - Tempo de execução: {} ms", 
                        description, thresholdMillis, executionTime);
                
                // Registra métricas específicas para threshold excedido
                meterRegistry.counter("method.threshold.exceeded",
                        "class", className,
                        "method", methodName,
                        "threshold", String.valueOf(thresholdMillis))
                        .increment();
            }
            
            if (logParams) {
                log.info("Método {} concluído em {} ms com retorno: {}", 
                       description, executionTime, result);
            }
            
            return result;
            
        } catch (Throwable e) {
            // Calcula tempo decorrido mesmo em caso de erro
            long executionTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            
            // Registra execução com erro
            sample.stop(Timer.builder(metricName)
                    .description(description)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "error")
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry));
            
            // Incrementa contador de erros específico para o método monitorado
            meterRegistry.counter("method.monitored.errors",
                    "class", className, 
                    "method", methodName,
                    "exception", e.getClass().getSimpleName(),
                    "alertEnabled", String.valueOf(alertOnError))
                    .increment();
            
            // Log de erro com nível mais alto se alertOnError estiver habilitado
            if (alertOnError) {
                log.error("ALERTA: Erro crítico no método monitorado {} após {} ms: {}", 
                        description, executionTime, e.getMessage(), e);
            } else {
                log.error("Erro no método {} após {} ms: {}", 
                        description, executionTime, e.getMessage());
            }
            
            throw e;
        }
    }
}
