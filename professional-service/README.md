# Serviço de Profissionais (professional-service)

## Descrição
O serviço de profissionais gerencia todas as informações relacionadas aos profissionais (médicos, esteticistas, cabeleireiros, etc.) da plataforma ClinicaSalao. Este serviço mantém um registro completo dos dados profissionais, especialidades, disponibilidade e histórico de atendimentos.

## Funcionalidades
- Cadastro, consulta, atualização e remoção de profissionais
- Gerenciamento de especialidades e habilidades
- Gerenciamento de horários de trabalho e folgas
- Cálculo de produtividade e desempenho
- Associação de profissionais a tratamentos/serviços
- Controle de agenda e disponibilidade

## Tecnologias
- Spring Boot 3.5.0
- Java 21
- PostgreSQL
- Spring Data JPA
- Maven
- Feign Client (para comunicação com outros serviços)

## Estrutura do Banco de Dados
- Tabela de profissionais
- Tabela de especialidades
- Tabela de horários de trabalho
- Tabela de avaliações de desempenho

## Endpoints Principais
- `GET /professionals` - Listar profissionais
- `POST /professionals` - Cadastrar profissional
- `GET /professionals/{id}` - Obter profissional por ID
- `PUT /professionals/{id}` - Atualizar profissional
- `DELETE /professionals/{id}` - Remover profissional
- `GET /professionals/search` - Buscar profissionais por critérios
- `GET /professionals/{id}/schedule` - Obter agenda do profissional
- `GET /professionals/{id}/specialties` - Obter especialidades do profissional
- `POST /professionals/{id}/availability` - Definir disponibilidade

## Como Executar Localmente
Instruções de execução serão adicionadas à medida que o serviço for implementado.

## Configuração
As configurações do serviço serão fornecidas através de variáveis de ambiente:
- Configuração de banco de dados
- URLs de serviços dependentes

## Dependências
Este serviço depende dos seguintes serviços:
- auth-service: para validação de tokens
