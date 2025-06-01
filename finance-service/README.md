# Serviço Financeiro (Finance Service)

Este microsserviço é responsável por gerenciar transações financeiras, processamento de pagamentos e geração de links de pagamento para o sistema ClinicaSalao.

## Funcionalidades

- Gestão completa de transações financeiras (CRUD)
- Geração de links de pagamento
- Processamento de pagamentos e reembolsos
- Cálculo de receitas por período
- Integração com gateway de pagamento (Asaas)
- Comunicação resiliente com client-service e appointment-service

## Tecnologias

- Java 21
- Spring Boot 3.5.0
- Spring Data JPA
- Spring Cloud (Eureka, OpenFeign)
- Resilience4j para Circuit Breaker
- PostgreSQL
- Flyway para migrações
- SpringDoc OpenAPI para documentação da API

## Endpoints API

### Transações

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET    | /api/v1/transactions | Lista todas as transações |
| GET    | /api/v1/transactions/{id} | Busca uma transação específica |
| POST   | /api/v1/transactions | Cria uma nova transação |
| PUT    | /api/v1/transactions/{id} | Atualiza uma transação |
| DELETE | /api/v1/transactions/{id} | Remove uma transação |
| POST   | /api/v1/transactions/{id}/payment-link | Gera link de pagamento |
| POST   | /api/v1/transactions/{id}/process-payment | Processa um pagamento |
| POST   | /api/v1/transactions/{id}/process-refund | Processa um reembolso |
| GET    | /api/v1/transactions/revenue | Calcula receita por período |

### Saúde do Serviço

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET    | /api/v1/health | Verificação de saúde do serviço |

## Documentação da API

A documentação interativa da API está disponível através do Swagger UI:

```
http://localhost:8083/swagger-ui.html
```

## Configuração do Banco de Dados

O serviço utiliza PostgreSQL como banco de dados. As migrações são gerenciadas pelo Flyway. A estrutura inicial da tabela de transações é criada automaticamente quando o serviço é iniciado.

## Integração com Gateway de Pagamento

O serviço utiliza uma implementação simulada do gateway Asaas para testes. Em produção, esta implementação seria substituída por uma integração real com a API do Asaas.

## Comunicação Entre Serviços

O finance-service se comunica com:

- client-service: para obter informações sobre os clientes
- appointment-service: para obter informações sobre agendamentos e serviços

Estas comunicações são feitas via Feign clients com circuit breakers para garantir resiliência.
