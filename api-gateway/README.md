# API Gateway

## Descrição
O API Gateway serve como ponto de entrada único para todos os microsserviços da plataforma ClinicaSalao. Ele gerencia o roteamento de requisições, autenticação centralizada e serve como uma camada de abstração entre os clientes externos e os serviços internos.

## Funcionalidades
- Roteamento de requisições para os microsserviços apropriados
- Autenticação e validação de tokens JWT
- Balanceamento de carga
- Limitação de taxa (rate limiting)
- Transformação de requisições/respostas
- Registro de logs centralizados
- Monitoramento e métricas

## Tecnologias
- Spring Cloud Gateway
- Spring Boot 3.5.0
- Java 21
- Redis (para cache e rate limiting)
- Maven

## Configuração de Rotas
- `/api/auth/**` → auth-service
- `/api/appointments/**` → appointment-service
- `/api/clients/**` → client-service
- `/api/professionals/**` → professional-service
- `/api/loyalty/**` → loyalty-service
- `/api/reports/**` → report-service

## Como Executar Localmente
Instruções de execução serão adicionadas à medida que o serviço for implementado.

## Configuração
As configurações do serviço serão fornecidas através de variáveis de ambiente:
- `JWT_SECRET` - Chave para validação de tokens JWT
- Configurações de endpoints para cada microsserviço

## Dependências
Este serviço depende da disponibilidade dos outros microsserviços para funcionar corretamente.
