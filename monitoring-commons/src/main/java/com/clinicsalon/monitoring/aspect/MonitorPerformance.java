package com.clinicsalon.monitoring.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para marcar métodos que devem ter seu desempenho monitorado
 * Pode ser utilizada em métodos específicos para monitoramento detalhado além
 * do monitoramento automático por camada
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorPerformance {
    
    /**
     * Nome personalizado para a métrica (opcional)
     */
    String value() default "";
    
    /**
     * Descrição da operação sendo monitorada
     */
    String description() default "";
    
    /**
     * Se verdadeiro, registra parâmetros de entrada e saída para depuração
     * Use com cuidado em métodos que manipulam dados sensíveis
     */
    boolean logParameters() default false;
    
    /**
     * Define limiar de tempo em milissegundos que, se excedido, gera um alerta
     */
    long thresholdMillis() default 500;
    
    /**
     * Se verdadeiro, gera alertas para erros durante a execução do método monitorado
     * Útil para integração com sistemas de monitoramento de erros
     */
    boolean alertOnError() default false;
}
