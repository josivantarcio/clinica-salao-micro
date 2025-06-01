# Projeto ClinicaSalao Microservices

## Status do Projeto: Em desenvolvimento (Versão 2.0.0)
*Última atualização: 01/06/2025 - 14:20*

## Visão Geral
O ClinicaSalao é um sistema completo de gestão para clínicas de estética e salões de beleza, originalmente desenvolvido como uma aplicação monolítica e agora sendo migrado para uma arquitetura de microsserviços. Esta migração visa melhorar a escalabilidade, manutenibilidade e resiliência do sistema.

## Stack Tecnológica
- **Backend**: Java 21, Spring Boot 3.5.0, Spring Cloud
- **Persistência**: PostgreSQL, Redis (cache)
- **Comunicação**: Spring Cloud OpenFeign, Resilience4j
- **Segurança**: Spring Security, JWT
- **Documentação**: SpringDoc OpenAPI
- **Containerização**: Docker, Docker Compose
- **CI/CD**: GitHub Actions
- **Frontend**: React, Material UI, TypeScript

## Arquitetura do Sistema

### Microsserviços Implementados e Status

| Serviço | Status | Descrição |
|---------|--------|-----------|
| **discovery-service** | ✅ Completo | Registro e descoberta de serviços (Eureka) |
| **api-gateway** | ✅ Completo | Gateway de API para roteamento de requisições |
| **auth-service** | ✅ Completo | Autenticação e autorização |
| **client-service** | ✅ Completo | Gerenciamento de clientes |
| **professional-service** | ✅ Completo | Gerenciamento de profissionais |
| **appointment-service** | ✅ Completo | Agendamentos e disponibilidade |
| **loyalty-service** | ✅ Completo | Sistema de fidelidade e pontos |
| **report-service** | ✅ Completo | Geração de relatórios |
| **finance-service** | ✅ Concluído | Gerenciamento financeiro e pagamentos |

### Integrações entre Serviços

- **client-service-client**: Permite outros serviços acessarem dados de clientes
- **professional-service-client**: Permite outros serviços acessarem dados de profissionais
- **appointment-service-client**: Permite outros serviços acessarem dados de agendamentos
- **loyalty-service-client**: Permite outros serviços acessarem dados de fidelidade
- **report-service-client**: Permite outros serviços solicitarem relatórios

## Funcionalidades Implementadas

### 1. Cadastro e Gerenciamento
- Perfis completos de clientes e profissionais
- Controle de permissões por tipo de usuário
- APIs RESTful para CRUD de entidades

### 2. Agendamento
- Engine de disponibilidade em tempo real
- Controle de conflitos e bloqueios de horários
- Notificações para clientes e profissionais

### 3. Fidelidade
- Sistema de pontos e recompensas
- Regras configuráveis para acúmulo e resgate
- Dashboard para acompanhamento

### 4. Relatórios
- Relatórios de histórico de clientes
- Relatórios de popularidade de serviços
- Relatórios de receita
- Exportação em PDF

### 5. Segurança e Infraestrutura
- Autenticação e autorização com JWT
- Service discovery com Eureka
- API Gateway com Spring Cloud Gateway
- Circuit breakers com Resilience4j

## Próximos Passos

### Implementações em Andamento

1. **Finance Service (Alta - 100% Concluído)**
   - ✅ Estrutura básica do microsserviço de finanças implementada
   - ✅ CRUD completo de transações financeiras
   - ✅ Processamento de pagamentos e reembolsos
   - ✅ Integração com serviços de cliente e agendamento via Feign clients
   - ✅ Cálculo de receita por período
   - ✅ Integração com gateway de pagamento Asaas (simulação implementada)
   - ✅ Circuit breakers para comunicação resiliente
   - ✅ Testes unitários para componentes principais
   - ✅ Documentação OpenAPI e Swagger UI

2. **Frontend (Alta - Próximo passo)**
   - Implementação da interface completa em React
   - Integrações com todos os microsserviços
   - Dashboard responsivo para desktop e mobile
   - Tema personalizável

### Melhorias e Expansões (Médio/Longo Prazo)

1. **Serviço de IA e Chatbot**
   - Implementação de chatbot para WhatsApp
   - Sistema de processamento de linguagem natural
   - Agendamentos automatizados

2. **Gestão de Estoque**
   - Controle de insumos e produtos
   - Alertas de níveis mínimos
   - Integração com fornecedores

3. **Dashboards & BI**
   - KPIs operacionais e indicadores de performance
   - Análises de desempenho por profissional
   - Métricas de negócio com visualizações gráficas

## Histórico de Versões

### Versão 2.0.0 (Atual - Em Desenvolvimento)
- Migração completa para arquitetura de microsserviços
- Implementação dos serviços core: clientes, profissionais, agendamentos, fidelidade e relatórios
- Integração entre serviços via Feign Clients
- Melhorias de resiliência com Circuit Breakers

### Versão 1.1.0 (31/05/2025)
- Correções em API Gateway e Auth Service
- Melhorias em testes unitários e de integração
- Documentação de código aprimorada

## Testes e Qualidade
- Testes unitários implementados para todos os serviços
- Testes de integração em desenvolvimento
- Cobertura de código > 80% para os serviços principais

## Implantação
- Docker Compose configurado para ambiente de desenvolvimento
- Preparação para implantação em AWS
- Monitoramento com Prometheus e Grafana em configuração

---

Este documento será atualizado regularmente à medida que o projeto avança.
