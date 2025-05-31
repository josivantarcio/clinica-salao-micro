# Serviço de Relatórios (report-service)

## Descrição
O serviço de relatórios é responsável pela geração, exportação e agendamento de relatórios para a plataforma ClinicaSalao. Este serviço permite a extração de dados e métricas de negócio para análise e tomada de decisão.

## Funcionalidades
- Geração de relatórios financeiros
- Relatórios de desempenho de profissionais
- Estatísticas de agendamentos e atendimentos
- Exportação para vários formatos (PDF, Excel, CSV)
- Agendamento de relatórios periódicos
- Relatórios de atividade de clientes
- Métricas e KPIs de negócio

## Tecnologias
- Spring Boot 3.5.0
- Java 21
- PostgreSQL
- Spring Data JPA
- Maven
- Apache POI (para Excel)
- iText (para PDF)
- Feign Client (para comunicação com outros serviços)

## Estrutura do Banco de Dados
- Tabela de relatórios gerados
- Tabela de configurações de relatórios
- Tabela de agendamentos de relatórios

## Endpoints Principais
- `GET /reports` - Listar relatórios disponíveis
- `POST /reports/generate` - Gerar novo relatório sob demanda
- `GET /reports/download/{id}` - Baixar relatório gerado
- `POST /reports/schedule` - Agendar geração periódica de relatório
- `GET /reports/scheduled` - Listar relatórios agendados
- `DELETE /reports/scheduled/{id}` - Cancelar agendamento de relatório
- `GET /reports/templates` - Listar templates de relatórios
- `POST /reports/templates` - Criar novo template de relatório

## Como Executar Localmente
Instruções de execução serão adicionadas à medida que o serviço for implementado.

## Configuração
As configurações do serviço serão fornecidas através de variáveis de ambiente:
- Configuração de banco de dados
- URLs de serviços dependentes
- Configurações de armazenamento de arquivos

## Dependências
Este serviço depende dos seguintes serviços:
- auth-service: para validação de tokens
- client-service: para informações de clientes
- professional-service: para informações de profissionais
- appointment-service: para informações de agendamentos
- loyalty-service: para informações de fidelidade
