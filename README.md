# ClinicaSalao - Arquitetura de Microsserviços

## Visão Geral
Este projeto é uma migração da aplicação ClinicaSalao de um monolito para uma arquitetura de microsserviços. A aplicação foi simplificada para ser monoempresa (removendo a funcionalidade multitenancy).

## Estrutura do Projeto
O projeto está organizado nos seguintes microsserviços:

1. **auth-service**: Autenticação e autorização de usuários
2. **api-gateway**: Ponto de entrada único para a API, gerenciando roteamento e segurança
3. **appointment-service**: Gerenciamento de agendamentos e disponibilidade
4. **client-service**: Gestão de clientes/pacientes
5. **professional-service**: Gestão de profissionais e especialidades
6. **loyalty-service**: Programa de fidelidade, pontos e recompensas
7. **report-service**: Geração de relatórios e exportação de dados

## Tecnologias Utilizadas
- Spring Boot 3.5.0
- Java 21
- Spring Cloud
- PostgreSQL
- Redis
- Docker & Docker Compose

## Pré-requisitos
- JDK 21 ou superior
- Maven 3.8+
- Docker Desktop
- Git

## Como Executar Localmente
Instruções detalhadas serão adicionadas à medida que cada microsserviço for implementado.

## Importante
**Sempre mantenha o GitHub atualizado após mudanças significativas em qualquer microsserviço.**

## Progresso da Migração
- [ ] Configuração inicial da estrutura
- [ ] API Gateway
- [ ] Serviço de Autenticação
- [ ] Serviço de Clientes
- [ ] Serviço de Profissionais
- [ ] Serviço de Agendamentos
- [ ] Serviço de Fidelidade
- [ ] Serviço de Relatórios
- [ ] Documentação completa
