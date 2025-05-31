# Changelog

## Versão 2.0.0 (31/05/2025) - Em Desenvolvimento

### Alterações Estruturais
- **Arquitetura de Microsserviços**
  - Migração da aplicação de monolítica multi-tenant para microsserviços mono-empresa
  - Implementação de configuração centralizada com Config Server
  - Implantação de sistema de descoberta de serviços com Eureka
  - Implementação de circuit breakers com Resilience4j

### Novos Recursos
- **Frontend**
  - Nova interface moderna desenvolvida com React e Material UI
  - Implementação de dashboard responsivo para desktop e mobile
  - Tema personalizável para adaptar-se à identidade visual do cliente
  - Componentes reutilizáveis para formulários, tabelas e visualizações

- **Serviço de IA**
  - Implementação de chatbot para WhatsApp para agendamentos automatizados
  - Sistema de processamento de linguagem natural (NLP) para entender solicitações
  - Integração com API do WhatsApp Business para comunicação bidirecional
  - Sistema de aprendizado contínuo para melhorar respostas automatizadas

- **Serviço de Agendamentos**
  - Nova engine de disponibilidade em tempo real
  - Integração com Google Calendar e Outlook
  - Sistema avançado de notificações para clientes e profissionais
  - Controle de conflitos e bloqueios de horários

- **Serviço de Fidelidade**
  - Sistema completo de pontos e recompensas
  - Regras configuráveis para acúmulo e resgate
  - Campanhas promocionais automatizadas
  - Dashboard para acompanhamento de pontos

### Melhorias Técnicas
- Atualização para Java 21 e Spring Boot 3.5.0
- Implementação de documentação automática com OpenAPI 3.0
- Sistema completo de logs distribuídos
- Configuração de métricas e traces para monitoramento
- Implementação de testes unitários e de integração em todos os microsserviços

### DevOps
- Configuração de Docker Compose para execução local
- Preparação para implantação em AWS
- Pipelines de CI/CD com GitHub Actions
- Configuração de monitoramento com Prometheus e Grafana

## Versão 1.1.0 (31/05/2025)

### Correções
- **API Gateway**
  - Corrigido problema com testes unitários do JWT que apresentavam WeakKeyException
  - Implementação adequada do RouteValidator para evitar erros de compilação com variáveis finais em lambdas
  - Corrigido NullPointerException nos testes do AuthenticationFilter com configuração correta dos mocks
  - Substituídos todos os `when()` por `lenient().when()` para evitar UnnecessaryStubbingException
  - Adicionada anotação @SuppressWarnings para resolver avisos de type safety no mock do Predicate
  - Removidos imports não utilizados em diversos arquivos
  - Adicionadas dependências explícitas do Mockito para resolver problemas com MockitoExtension

- **Auth Service**
  - Atualizado DaoAuthenticationProvider para não usar métodos deprecated
  - Corrigidos avisos no application.yml com o escape adequado de caracteres especiais
  - Removidos imports não utilizados nos testes

### Melhorias
- Adicionada documentação no código para explicar as decisões de implementação
- Melhorada a organização e legibilidade dos testes

### Testes
- Corrigidos e estabilizados todos os testes no api-gateway e auth-service
- Implementada configuração correta para o assert de status HTTP nos testes reativos
