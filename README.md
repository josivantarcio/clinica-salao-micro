# ClinicaSalao - Arquitetura de Microsserviços

## Visão Geral
Este projeto é uma implementação completa do sistema ClinicaSalao usando arquitetura de microsserviços. O sistema gerencia clínicas de estética e salões de beleza, oferecendo funcionalidades avançadas de agendamento, gestão de clientes, profissionais, e integração com IA para atendimento automatizado.

## Estrutura de Microsserviços

1. **auth-service**: Autenticação e autorização de usuários
   - Gestão de usuários, perfis e permissões
   - Autenticação JWT
   - Single Sign-On

2. **api-gateway**: Ponto de entrada único para todas as requisições
   - Roteamento para microsserviços
   - Rate limiting e proteção contra ataques
   - Logging centralizado

3. **discovery-service**: Registro e descoberta de serviços
   - Implementado com Spring Cloud Netflix Eureka

4. **appointment-service**: Gerenciamento de agendamentos
   - Motor de disponibilidade em tempo real
   - Integração com Google Calendar/Outlook
   - Notificações automáticas
   - Gerenciamento de bloqueios de horários

5. **client-service**: Gestão de clientes
   - Cadastro completo de clientes
   - Histórico de atendimentos
   - Preferências e observações médicas
   - Gestão de contatos e comunicações

6. **professional-service**: Gestão de profissionais
   - Cadastro de profissionais e especialidades
   - Gerenciamento de agenda e disponibilidade
   - Métricas de performance

7. **loyalty-service**: Programa de fidelidade
   - Acúmulo e resgate de pontos
   - Regras de promoções e descontos
   - Campanhas de marketing

8. **report-service**: Relatórios e Business Intelligence
   - KPIs de negócio
   - Exportação de relatórios em múltiplos formatos
   - Análise de métricas de negócio

9. **ai-service**: Serviço de Inteligência Artificial
   - Chatbot para WhatsApp
   - Processamento de linguagem natural
   - Agendamentos automatizados
   - Recomendações personalizadas

## Tecnologias Utilizadas

### Backend
- Java 21
- Spring Boot 3.5.0
- Spring Cloud (Netflix Eureka, Gateway, OpenFeign)
- PostgreSQL
- Redis (cache e mensageria)
- Docker & Docker Compose
- JWT para autenticação
- OpenAPI (Swagger) para documentação

### Frontend
- React 18
- TypeScript
- Material UI
- React Query
- Axios
- Context API / Redux

### DevOps e Infraestrutura
- Docker & Docker Compose
- CI/CD com GitHub Actions
- AWS (deployment final)
- Prometheus & Grafana (monitoramento)

## Pré-requisitos para Desenvolvimento
- JDK 21
- Maven 3.8+
- Node.js 18+
- Docker Desktop
- Git

## Como Executar Localmente

### Usando Docker Compose (Recomendado)
```bash
# Clone o repositório
git clone https://github.com/josivantarcio/clinica-salao-micro.git
cd clinica-salao-micro

# Execute com Docker Compose
docker-compose up -d
```

### Execução Manual (Desenvolvimento)
Cada microsserviço possui instruções específicas em seu próprio README.md

## Acesso à Aplicação
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Eureka Dashboard: http://localhost:8761

## Arquitetura da Solução

```
┌───────────────┐     ┌───────────────┐
│   Frontend    │────▶│  API Gateway  │
└───────────────┘     └───────┬───────┘
                             │
                  ┌──────────┴──────────┐
                  ▼                      ▼
         ┌─────────────────┐   ┌─────────────────┐
         │ Discovery Service│   │  Auth Service   │
         └─────────────────┘   └─────────────────┘
                  │
     ┌────────────┼────────────┬────────────┬────────────┐
     ▼            ▼            ▼            ▼            ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│  Client  │ │Professional│ │Appointment│ │ Loyalty  │ │   AI     │
│ Service  │ │ Service   │ │ Service  │ │ Service  │ │ Service  │
└──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘
     │            │            │            │            │
     └────────────┴────────────┼────────────┴────────────┘
                               ▼
                       ┌───────────────┐
                       │Report Service │
                       └───────────────┘
```

## Progresso da Implementação
- [x] Configuração inicial da estrutura
- [x] Discovery Service
- [ ] API Gateway
- [ ] Serviço de Autenticação
- [ ] Serviço de Clientes
- [ ] Serviço de Profissionais
- [ ] Serviço de Agendamentos
- [ ] Serviço de Fidelidade
- [ ] Serviço de IA
- [ ] Serviço de Relatórios
- [ ] Frontend
- [ ] Integração Completa
- [ ] Testes End-to-End
- [ ] Documentação

## Contribuição
Para contribuir com este projeto, siga as práticas de GitFlow e padrões de código documentados.
