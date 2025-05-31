# Escopo do Projeto ClinicaSalao

## Visão Geral
O projeto ClinicaSalao é um Sistema de Gestão para Clínicas de Estética e Salões de Beleza originalmente implementado como uma aplicação monolítica e agora sendo migrado para uma arquitetura de microsserviços.

## Tecnologias Utilizadas (Projeto Original)
- **Linguagem:** Java 17
- **Framework:** Spring Boot 3.2
- **Banco de Dados:** PostgreSQL, Redis (cache)
- **Migração de Dados:** Flyway
- **Segurança:** Spring Security, JWT
- **Documentação:** SpringDoc OpenAPI
- **Integrações:** Spring Cloud OpenFeign, WhatsApp Business API
- **Inteligência Artificial:** Processamento de Linguagem Natural, Chatbot
- **Outros:** Lombok, jUnit, H2 (testes)

## Tecnologias Utilizadas (Projeto de Microsserviços)
- Spring Boot 3.5.0
- Java 21
- Spring Cloud
- PostgreSQL
- Redis
- Docker
- Maven

## Módulos do Sistema
1. **Cadastro de Clientes e Profissionais**
   - Perfis completos com histórico de serviços e preferências
   - Controle de permissões por tipo de usuário

2. **Agendamento e Calendário**
   - Engine de disponibilidade em tempo real
   - Integração com Google Calendar e Outlook
   - Notificações por SMS/WhatsApp e e-mail
   - Templates personalizados para confirmação e lembretes

3. **Catálogo de Serviços e Preços**
   - Pacotes, durações variáveis por profissional
   - Promoções e regras de fidelidade

4. **Fluxo de Atendimento**
   - Check-in digital, registro de procedimentos
   - Registro de consumo de insumos

5. **Gestão de Estoque e Compras**
   - Controle de níveis mínimos e alertas
   - Pedidos programados a fornecedores
   - Exportação de relatórios de estoque (PDF, Excel, CSV)

6. **Faturamento e Financeiro**
   - Integração com gateway de pagamento Asaas
   - Geração de links de pagamento
   - Notificações automatizadas de confirmação de pagamento
   - Relatórios financeiros exportáveis

7. **Dashboards & BI**
   - KPIs operacionais e indicadores de performance
   - Análises de desempenho por profissional
   - Métricas de negócio com visualizações gráficas
   - Análise de receita e lucratividade
   - Detecção de anomalias em métricas de negócio

8. **Chatbot & Inteligência Artificial**
   - Atendimento automatizado via WhatsApp
   - Processamento de linguagem natural (NLP)
   - Agendamentos, cancelamentos e reagendamentos automatizados
   - Configurações especiais para assinantes premium

## Arquitetura do Sistema
### Microsserviços Implementados
- **auth-service**: Serviço de autenticação e autorização
- **api-gateway**: Gateway de API para roteamento de requisições
- **discovery-service**: Serviço de descoberta (Eureka)
- **appointment-service**: Serviço de agendamentos
- **client-service**: Serviço de clientes
- **professional-service**: Serviço de profissionais
- **loyalty-service**: Serviço de fidelidade
- **report-service**: Serviço de relatórios

### Observações sobre o Frontend
Não foram encontradas informações específicas sobre o frontend no repositório original. O projeto original parece ser majoritariamente Java (97.4%) com pequena quantidade de HTML (0.8%), sugerindo que:

1. O frontend pode estar em outro repositório não mencionado
2. Pode estar usando templates HTML simples com Thymeleaf (mencionado em 'templates' na estrutura)
3. Pode estar em fase de desenvolvimento ou planejamento

## Próximos Passos
- Concluir a implementação dos testes automatizados para todos os microsserviços
- Implementar testes de integração entre os serviços
- Definir e documentar a estratégia para o frontend (SPA, MPA, tecnologias, etc.)
