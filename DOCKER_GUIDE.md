# Guia de Execução do ClinicaSalao com Docker

Este guia explica como executar o projeto ClinicaSalao completo utilizando Docker, integrando o frontend React com os microsserviços backend.

## Pré-requisitos

- Docker e Docker Compose instalados
- Git (para clonar o repositório, se necessário)

## Estrutura dos Serviços

O ambiente Docker do ClinicaSalao é composto por:

1. **Banco de Dados**: PostgreSQL 16
2. **Frontend**: Aplicação React servida via Nginx
3. **Serviços de Infraestrutura**:
   - Discovery Service (Eureka)
   - API Gateway
4. **Microsserviços**:
   - Auth Service (Autenticação)
   - Client Service (Gestão de Clientes)
   - Professional Service (Gestão de Profissionais)
   - Appointment Service (Agendamentos)
   - Finance Service (Gestão Financeira)
   - Loyalty Service (Programa de Fidelidade)
   - Report Service (Relatórios)

## Execução do Ambiente Completo

### Passo 1: Compilar os Serviços

Antes de iniciar o Docker, é recomendável compilar os módulos Java para garantir que não há erros:

```bash
# Na raiz do projeto
./mvnw clean package -DskipTests
```

**Nota**: No Windows, use `mvnw.cmd` em vez de `./mvnw`.

### Passo 2: Iniciar os Contêineres

Para iniciar todos os serviços em contêineres Docker:

```bash
# Na raiz do projeto
docker-compose up -d
```

Este comando inicia todos os serviços definidos no arquivo `docker-compose.yml` em modo detached (background).

Para ver apenas o frontend e os serviços essenciais:

```bash
docker-compose up -d postgres discovery-service api-gateway auth-service frontend
```

### Passo 3: Verificar o Status dos Serviços

Para verificar se todos os serviços estão em execução:

```bash
docker-compose ps
```

Para ver os logs de um serviço específico:

```bash
docker-compose logs -f frontend  # Substitua 'frontend' pelo nome do serviço
```

### Passo 4: Acessar a Aplicação

- **Frontend**: http://localhost:3000
- **Eureka Discovery**: http://localhost:8761
- **API Gateway Swagger**: http://localhost:8080/swagger-ui.html
- **Serviços individuais**: Acessíveis via API Gateway

## Ordem de Inicialização

O Docker Compose foi configurado com dependências que garantem a ordem correta de inicialização dos serviços:

1. Primeiro o PostgreSQL e o Discovery Service são iniciados
2. Em seguida, o API Gateway, aguardando o Discovery Service estar saudável
3. Os microsserviços são iniciados depois que suas dependências estão prontas
4. Por fim, o frontend é iniciado quando o API Gateway está disponível

## Resolução de Problemas

### Serviços não aparecendo no Eureka

Verifique se os serviços estão configurados com o perfil correto:

```bash
docker-compose logs discovery-service
```

Confira se os serviços estão tentando se registrar no endereço correto do Eureka:

```
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka
```

### Erro de conexão com o banco de dados

Verifique se o PostgreSQL está em execução e saudável:

```bash
docker-compose logs postgres
```

Confirme se as URLs de conexão nos serviços estão corretas.

### Frontend não consegue acessar a API

Verifique a configuração do Nginx em `frontend/nginx.conf` e confirme que o proxy reverso está apontando para o endpoint correto:

```
location /api/ {
    proxy_pass http://api-gateway:8080/;
    ...
}
```

## Parando o Ambiente

Para parar todos os serviços:

```bash
docker-compose down
```

Para parar e remover volumes (dados do banco):

```bash
docker-compose down -v
```

## Construindo apenas o Frontend

Se você precisar reconstruir apenas o frontend após alterações:

```bash
docker-compose build frontend
docker-compose up -d frontend
```

## Depuração

Para depurar um serviço específico, você pode entrar no contêiner:

```bash
docker exec -it clinicsalon-frontend /bin/sh
```

## Observações Importantes

1. O frontend utiliza o Nginx como servidor web e proxy reverso para o API Gateway
2. A aplicação React está configurada para utilizar o endpoint `/api/` para todas as requisições, que é redirecionado para o API Gateway
3. O Docker Compose configura networks e healthchecks para garantir a comunicação e inicialização correta entre os serviços
4. Dados persistentes são armazenados em volumes Docker para preservar informações entre reinicializações
