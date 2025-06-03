# Documentação de Monitoramento - ClinicaSalao Microservices

## Visão Geral

Este documento descreve a implementação do sistema de monitoramento nos microsserviços do projeto ClinicaSalao, utilizando Spring Boot 3.5.0, Micrometer, Prometheus e Resilience4j.

## Configurações Padronizadas

Todos os microsserviços utilizam o perfil `monitoring` para ativar as configurações de monitoramento. Este perfil é ativado automaticamente através da configuração `spring.profiles.active` em cada serviço.

### Arquivos de Configuração

- **application-monitoring.yml**: Presente em cada microsserviço, contém configurações específicas para monitoramento
- **monitoring-defaults.properties**: No módulo `monitoring-commons`, define configurações padrão compartilhadas

## Principais Métricas Coletadas

- **Tempo de Resposta de API**: Percentis de latência para chamadas HTTP (50%, 90%, 95%, 99%)
- **Taxa de Erros**: Contagem de erros por serviço, endpoint e tipo de exceção
- **Performance de Métodos**: Tempo de execução de métodos críticos anotados com `@MonitorPerformance`
- **Circuit Breaker**: Estado dos circuit breakers e taxas de falha
- **Cache**: Estatísticas de uso e eficiência do cache
- **JVM**: Uso de memória, CPU e threads

## Anotação @MonitorPerformance

A anotação `@MonitorPerformance` permite monitoramento detalhado de métodos específicos:

```java
@MonitorPerformance(
    description = "Descrição da operação",
    thresholdMillis = 500,
    logParameters = false,
    alertOnError = true
)
public void metodoMonitorado() {
    // implementação
}
```

### Parâmetros

| Parâmetro | Descrição | Valor Padrão |
|-----------|-----------|--------------|
| description | Descrição da operação monitorada | "" |
| thresholdMillis | Limiar em ms que, se excedido, gera alerta | 500 |
| logParameters | Se deve registrar parâmetros de entrada/saída | false |
| alertOnError | Se deve gerar alertas específicos para erros | false |

## Endpoints de Monitoramento

Cada serviço expõe os seguintes endpoints (protegidos por autenticação em ambiente de produção):

- `/actuator/health`: Status de saúde do serviço
- `/actuator/metrics`: Todas as métricas disponíveis
- `/actuator/prometheus`: Métricas no formato Prometheus

## Integração com Sistemas de Monitoramento

### Prometheus + Grafana

1. O Prometheus coleta métricas periodicamente de cada serviço via endpoint `/actuator/prometheus`
2. O Grafana conecta-se ao Prometheus para visualização e alertas
3. Dashboards pré-configurados estão disponíveis na pasta `/monitoring/dashboards`

### Rastreamento Distribuído

Implementado usando Spring Cloud Sleuth com Zipkin para visualização.

## Melhores Práticas

1. Usar a anotação `@MonitorPerformance` em:
   - Métodos de negócio críticos
   - Operações de longa duração
   - Pontos de integração com sistemas externos

2. Configurar corretamente os thresholds com base em:
   - Requisitos de SLA
   - Capacidade da infraestrutura
   - Tempo médio de resposta em testes de carga

3. Monitorar proativamente:
   - Tempo de resposta de APIs acima do 95º percentil
   - Taxa de erros superior a 1%
   - Falhas em circuit breakers
   - Uso de memória acima de 80%

## Alterações Recentes (Junho/2025)

- Migração das configurações para Spring Boot 3.5.0
- Correção da sintaxe para chaves com caracteres especiais usando colchetes simples (`[http.server.requests]`)
- Substituição de `management.metrics.export.prometheus.enabled` por `management.prometheus.metrics.export.enabled`
- Substituição de configuração `sla` por `percentiles` com valores numéricos
- Adição do atributo `alertOnError` à anotação `@MonitorPerformance`
- Criação de arquivo vazio `META-INF/spring.factories` para compatibilidade com verificações
