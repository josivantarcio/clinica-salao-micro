# Serviço de Agendamentos (appointment-service)

## Descrição
O serviço de agendamentos gerencia todo o ciclo de vida de agendamentos de clientes com profissionais na plataforma ClinicaSalao. Ele lida com a disponibilidade dos profissionais, slots de tempo, confirmações e cancelamentos.

## Funcionalidades
- Criação, consulta, atualização e cancelamento de agendamentos
- Gerenciamento de disponibilidade de profissionais
- Verificação de conflitos de horários
- Notificações de agendamentos (integração com serviço externo)
- Gerenciamento de status de agendamentos (confirmado, cancelado, realizado)
- Histórico de agendamentos por cliente e profissional

## Tecnologias
- Spring Boot 3.5.0
- Java 21
- PostgreSQL
- Spring Data JPA
- Maven
- Feign Client (para comunicação com outros serviços)

## Estrutura do Banco de Dados
- Tabela de agendamentos
- Tabela de disponibilidade
- Tabela de configurações de agendamento

## Endpoints Principais
- `GET /appointments` - Listar agendamentos
- `POST /appointments` - Criar agendamento
- `GET /appointments/{id}` - Obter agendamento por ID
- `PUT /appointments/{id}` - Atualizar agendamento
- `DELETE /appointments/{id}` - Cancelar agendamento
- `GET /appointments/availability` - Verificar disponibilidade de horários
- `GET /appointments/client/{clientId}` - Obter agendamentos por cliente
- `GET /appointments/professional/{professionalId}` - Obter agendamentos por profissional

## Como Executar Localmente
Instruções de execução serão adicionadas à medida que o serviço for implementado.

## Configuração
As configurações do serviço serão fornecidas através de variáveis de ambiente:
- Configuração de banco de dados
- URLs de serviços dependentes

## Dependências
Este serviço depende dos seguintes serviços:
- auth-service: para validação de tokens
- client-service: para informações de clientes
- professional-service: para informações de profissionais
