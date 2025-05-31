# Recomendações Detalhadas para Implantação na AWS

## Visão Geral da Arquitetura Proposta

Este documento detalha as recomendações para implantação do Saas-CS na AWS, baseadas na análise do sistema e nas melhores práticas para aplicações SaaS multi-tenant.

## Serviços AWS Recomendados

### 1. Computação

**Opção Recomendada: AWS Fargate**
- **Justificativa**: Oferece gerenciamento simplificado sem necessidade de provisionar ou gerenciar servidores, ideal para uma aplicação containerizada como o Saas-CS.
- **Configuração**:
  - Utilizar contêineres Docker baseados na imagem `eclipse-temurin:21-jre-alpine` para tamanho reduzido
  - Configurar limites de CPU e memória adequados (2 vCPU / 4GB RAM recomendado para início)
  - Implementar auto-scaling baseado em utilização de CPU (alarme em 70%) e número de requisições

**Alternativa: Amazon EC2 com Auto Scaling**
- Caso seja necessário maior controle sobre a infraestrutura
- Usar instâncias t3.medium (2 vCPU, 4GB RAM) inicialmente
- Configurar grupos de Auto Scaling com políticas de escalonamento dinâmico

### 2. Banco de Dados

**Amazon RDS para PostgreSQL**
- **Justificativa**: Serviço gerenciado que elimina a necessidade de administração de banco de dados, oferecendo backups automáticos, alta disponibilidade e escalabilidade.
- **Configuração**:
  - Instância db.t3.medium para ambientes de desenvolvimento
  - Instância db.m5.large para produção
  - Multi-AZ para alta disponibilidade em produção
  - Backup automático com retenção de 7 dias
  - Configurar grupo de parâmetros específico para otimizar performance PostgreSQL

### 3. Cache

**Amazon ElastiCache para Redis**
- **Justificativa**: Necessário para o cache distribuído da aplicação, especialmente para melhorar performance de verificação de disponibilidade de profissionais.
- **Configuração**:
  - Nós cache.t3.small para desenvolvimento
  - Nós cache.m5.large para produção
  - Replicação em produção para alta disponibilidade
  - Configurar TTL para cache conforme definido na aplicação

### 4. Balanceamento de Carga

**Application Load Balancer (ALB)**
- **Justificativa**: Distribui tráfego HTTP/HTTPS entre múltiplas instâncias, essencial para alta disponibilidade e escalabilidade.
- **Configuração**:
  - Health checks apontando para `/actuator/health`
  - Configurar terminação SSL/TLS com certificado via AWS Certificate Manager
  - Habilitar logs de acesso para análise
  - Configurar regras de roteamento baseadas em path para APIs específicas

### 5. Armazenamento

**Amazon S3**
- **Justificativa**: Para armazenamento de arquivos estáticos, relatórios exportados e backups.
- **Configuração**:
  - Bucket para arquivos exportados (relatórios, planilhas)
  - Bucket para logs de aplicação
  - Políticas de ciclo de vida para mover dados menos acessados para classes de armazenamento mais econômicas

### 6. Segurança e Identidade

**AWS IAM e Security Groups**
- **Justificativa**: Controle de acesso fino para recursos e proteção de rede.
- **Configuração**:
  - Roles IAM específicas para cada componente (ECS/Fargate, RDS, ElastiCache)
  - Security Groups restritivos para limitar acesso entre componentes
  - Implementar VPC com subnets públicas (apenas para ALB) e privadas (para aplicação e banco de dados)

**AWS Secrets Manager**
- **Justificativa**: Gerenciamento seguro de credenciais e chaves.
- **Configuração**:
  - Armazenar credenciais de banco de dados
  - Armazenar chaves JWT e outras credenciais da aplicação
  - Integrar com Spring Cloud AWS para busca dinâmica de segredos

### 7. Monitoramento e Observabilidade

**Amazon CloudWatch**
- **Justificativa**: Monitoramento centralizado de métricas, logs e alertas.
- **Configuração**:
  - Métricas personalizadas para KPIs de negócio (via Micrometer)
  - Dashboards para visualização de performance
  - Alarmes para utilização de recursos e erros
  - Centralização de logs da aplicação

**AWS X-Ray**
- **Justificativa**: Análise distribuída de traces para identificar gargalos.
- **Configuração**:
  - Integrar com OpenTelemetry já utilizado na aplicação
  - Configurar amostragem adequada para balancear custo e visibilidade

## Estratégia de Multi-tenancy na AWS

Para a arquitetura multi-tenant do Saas-CS, recomendamos:

1. **Modelo de Banco de Dados**:
   - Manter abordagem de discriminador (tenant_id) já implementada
   - Considerar particionamento de tabelas por tenant_id para maiores tenants
   - Configurar pools de conexão por tenant para isolamento de performance

2. **Isolamento de Cache**:
   - Utilizar prefixos de tenant para chaves Redis
   - Configurar limites de memória por tenant para evitar "noisy neighbors"

3. **Configuração por Tenant**:
   - Armazenar configurações específicas por tenant em DynamoDB
   - Implementar carregamento condicional de configurações baseado no tenant atual

## Estratégia de Implantação (CI/CD)

1. **Registro de Contêineres**:
   - Amazon ECR para armazenar imagens Docker
   - Políticas de ciclo de vida para limitar número de imagens armazenadas

2. **Pipeline de CI/CD**:
   - GitHub Actions para build e testes
   - AWS CodePipeline para orquestração de implantação
   - Estratégia Blue/Green para atualizações sem downtime

3. **Infraestrutura como Código**:
   - AWS CloudFormation ou AWS CDK para definição da infraestrutura
   - Repositório separado para código de infraestrutura
   - Ambientes separados para desenvolvimento, staging e produção

## Estimativa de Custos

| Serviço | Configuração | Custo Mensal Estimado (USD) |
|---------|--------------|------------------------------|
| AWS Fargate | 2 tarefas (2vCPU, 4GB) | $175 |
| RDS PostgreSQL | db.m5.large, Multi-AZ | $380 |
| ElastiCache Redis | cache.m5.large | $180 |
| Application Load Balancer | 1 ALB | $25 |
| S3 | 100GB armazenamento | $3 |
| CloudWatch | Métricas e Logs | $30 |
| Total aproximado | | $793 |

*Nota: Valores aproximados, sujeitos a variação conforme utilização e região AWS.*

## Próximos Passos

1. Criar templates CloudFormation/CDK para cada componente da infraestrutura
2. Atualizar Dockerfile para otimizar tamanho e segurança da imagem
3. Configurar pipeline de CI/CD no GitHub Actions para integração com AWS
4. Implementar rotinas de backup e recuperação
5. Desenvolver estratégia de monitoramento e alertas
