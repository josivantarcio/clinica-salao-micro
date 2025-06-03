# Documentação do Módulo de Monitoramento - ClinicaSalao

## Introdução

Este documento descreve o módulo de monitoramento integrado nos microsserviços da ClinicaSalao. O módulo permite monitorar o desempenho, detectar falhas e gerar métricas para todos os serviços da arquitetura.

## Componentes Principais

O sistema de monitoramento é composto por:

- **Monitoring Commons**: Módulo compartilhado que contém aspectos, configurações e anotações para monitoramento
- **Métricas Micrometer**: Integração com Micrometer para coleta de métricas
- **Prometheus**: Para armazenamento de métricas
- **Grafana**: Para visualização de dashboards
- **AlertManager**: Para gerenciamento e notificação de alertas

## Configuração do Monitoramento

### Perfil de Monitoramento

O monitoramento é ativado através do perfil Spring `monitoring`. Para ativar:

```
java -jar app.jar --spring.profiles.active=monitoring
```

Ou via variável de ambiente:

```
SPRING_PROFILES_ACTIVE=monitoring
```

### Estrutura dos Arquivos de Configuração

Cada serviço possui:

1. **application-monitoring.yml**: Configurações específicas de monitoramento
2. **Dependência para monitoring-commons**: No arquivo pom.xml
3. **Anotações @MonitorPerformance**: Aplicadas nos métodos críticos

### Exemplo de Configuração

```yaml
# application-monitoring.yml
management:
  endpoints:
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}

# Cache Monitoring
spring:
  cache:
    type: caffeine

# Logging
logging:
  level:
    root: INFO
    com.clinicsalon: DEBUG
    '[org.springframework.cache]': DEBUG
    '[io.micrometer]': DEBUG

# Resilience4j
resilience4j:
  circuitbreaker:
    metrics:
      enabled: true
```

## Anotações Disponíveis

### @MonitorPerformance

A principal anotação para monitorar métodos:

```java
@MonitorPerformance(
    description = "Descrição da operação", 
    thresholdMillis = 500,
    alertOnError = true
)
public void metodoMonitorado() { ... }
```

#### Parâmetros:
- **description**: Descrição do método para identificação nas métricas
- **thresholdMillis**: Limite de tempo para execução do método em milissegundos
- **alertOnError**: Se verdadeiro, gera alertas em caso de exceção

## Dashboards e Alertas

### Dashboard Grafana

O dashboard principal está disponível em `monitoring/dashboards/clinica-salao-dashboard.json` e pode ser importado no Grafana. Ele contém:

- Tempo de execução de métodos por serviço
- Estatísticas de cache (hits, misses)
- Estado dos circuit breakers
- Utilização de recursos (CPU, memória)

### Regras de Alertas

As regras de alerta estão em `monitoring/alerting-rules.yml` e incluem:

- Alertas para métodos excedendo limites de tempo
- Alertas para alta taxa de erros (status 5xx)
- Alertas para circuit breakers abertos
- Alertas para alta taxa de cache miss
- Alertas para alta utilização de CPU e memória

## Serviços Monitorados

O monitoramento está configurado nos seguintes serviços:
- auth-service
- professional-service
- loyalty-service
- report-service
- discovery-service
- api-gateway
- finance-service (em desenvolvimento)

## Boas Práticas

1. **Use @MonitorPerformance em métodos críticos**: Especialmente em operações de banco de dados, chamadas externas e lógicas complexas.

2. **Configure thresholds adequados**: Cada método deve ter um threshold baseado em seu comportamento esperado.

3. **Ative o perfil de monitoramento em ambientes de teste e produção**: Isso garante visibilidade contínua.

4. **Revise os dashboards regularmente**: Para identificar tendências e problemas potenciais.

## Troubleshooting

### Métricas não aparecem no Prometheus

1. Verifique se o endpoint `/actuator/prometheus` está acessível
2. Confirme se o perfil `monitoring` está ativo
3. Verifique se o Prometheus está configurado para raspar (scrape) o endpoint

### Alertas não estão sendo gerados

1. Verifique se o AlertManager está configurado corretamente
2. Confirme se as regras de alerta estão carregadas
3. Teste manualmente as expressões de alerta no Prometheus

## Próximos Passos

- Implementar tracing distribuído com Zipkin
- Expandir monitoramento para novos serviços
- Criar dashboards específicos por domínio de negócio
- Integrar notificações de alertas via Slack/Email
