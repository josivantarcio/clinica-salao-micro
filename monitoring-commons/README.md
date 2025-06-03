# Módulo de Monitoramento e Memória Cache - ClinicaSalao

Este módulo fornece funcionalidades centralizadas de monitoramento, métricas e gerenciamento de cache para todos os microsserviços do projeto ClinicaSalao.

## Funcionalidades

### Monitoramento e Métricas
- Métricas JVM (memória, threads, GC)
- Métricas de performance de chamadas HTTP
- Métricas de performance de métodos (controllers, services, repositories)
- Métricas de comunicação entre serviços (Feign clients)
- Endpoints Actuator para health checks e informações do sistema

### Cache
- Configuração centralizada de cache usando Caffeine
- Caches pré-configurados para entidades frequentemente acessadas
- Monitoramento de estatísticas de cache (hit rate, miss rate, eviction)
- Configurações otimizadas para diferentes tipos de dados

## Como Usar

### 1. Adicionar a Dependência

Adicione a dependência no `pom.xml` do seu microsserviço:

```xml
<dependency>
    <groupId>com.clinicsalon</groupId>
    <artifactId>monitoring-commons</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Ativar Monitoramento em Métodos Específicos

Use a anotação `@MonitorPerformance` em métodos críticos:

```java
import com.clinicsalon.monitoring.aspect.MonitorPerformance;

@Service
public class PaymentService {
    
    @MonitorPerformance(description = "Processamento de pagamento", thresholdMillis = 300)
    public PaymentResponse processPayment(PaymentRequest request) {
        // Lógica de processamento
    }
}
```

### 3. Utilizar Cache

```java
import org.springframework.cache.annotation.Cacheable;
import static com.clinicsalon.monitoring.cache.CacheConfig.*;

@Service
public class ClientService {
    
    @Cacheable(CLIENTS_CACHE)
    public ClientDto getClientById(Long id) {
        // Busca no banco de dados
    }
}
```

### 4. Configurações Adicionais (application.yml)

```yaml
# Configurações de monitoramento e cache personalizadas
management:
  metrics:
    tags:
      application: ${spring.application.name}
  tracing:
    sampling:
      probability: 1.0
      
# Configurações de cache
spring:
  cache:
    type: caffeine
    cache-names: professionals,clients,appointments,services
```

## Endpoints de Monitoramento

- `/actuator/health` - Status de saúde do serviço
- `/actuator/metrics` - Métricas disponíveis
- `/actuator/prometheus` - Métricas no formato Prometheus
- `/actuator/caches` - Informações sobre caches

## Observações

- O monitoramento é automaticamente aplicado a métodos nos pacotes controller, service, repository e client
- As métricas são coletadas em tempo real e podem ser visualizadas via Actuator
- O módulo é compatível com ferramentas de observabilidade como Prometheus e Grafana
