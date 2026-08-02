# Stack tecnológica

## Estado

Este documento define decisões iniciais. Alterações relevantes exigem uma change arquitetural.

## Backend

- Java 21 LTS;
- Spring Boot;
- Spring Security;
- Spring Validation;
- Spring Data JPA apenas nos adapters de persistência;
- PostgreSQL;
- Flyway;
- Testcontainers;
- JUnit 5;
- AssertJ;
- ArchUnit;
- Spotless;
- Checkstyle;
- OpenAPI;
- Maven;

## Arquitetura

- monólito modular;
- arquitetura hexagonal;
- domínio em Java puro;
- módulos organizados por capacidade de negócio;
- adapters de entrada e saída explícitos;
- entidades de persistência separadas do domínio;
- eventos internos para desacoplamento;
- transactional outbox quando notificações confiáveis forem introduzidas.

As convenções mínimas de pacotes e dependências estão em `docs/architecture/package-conventions.md`.

## Módulos iniciais previstos

- identity;
- organization;
- ticket;
- attachment;
- notification;
- audit;
- privacy;
- shared-kernel mínimo.

## Frontend

Decisão inicial proposta:

- Next.js;
- TypeScript estrito;
- Tailwind CSS;
- shadcn/ui;
- consumo da API Spring;
- acessibilidade e responsividade obrigatórias.

## Repositório

- monorepo com `backend/`, `frontend/`, `docs/` e `openspec/`;
- frontend e backend são aplicações separadas no mesmo repositório;
- mudanças coordenadas devem manter documentação e specs no mesmo histórico.

A direção visual será guiada por skills de UI/UX previamente aprovadas pelo responsável do projeto. Skills não podem sobrescrever requisitos de acessibilidade, segurança, privacidade ou consistência.

## Infraestrutura inicial

- Docker Compose para desenvolvimento;
- PostgreSQL;
- object storage compatível com S3 para anexos;
- serviço transacional de e-mail;
- GitHub Actions em pull requests, com build, testes, validação de migrations, formatação, análise estática e regras arquiteturais;
- deploy inicialmente simples, sem Kubernetes.

A versão exata do Spring Boot será selecionada na implementação da fundação a partir da linha estável suportada mais recente compatível com Java 21 e permanecerá fixada no `pom.xml` resultante.

## Autenticação

Direção inicial:

- autenticação própria no backend;
- cookies `HttpOnly`, `Secure` e `SameSite`;
- sessões revogáveis;
- proteção CSRF quando aplicável;
- MFA planejado após a fundação de identidade.

A decisão final será formalizada na change de autenticação.

## Multi-tenancy

Modelo inicial:

- banco compartilhado;
- schema compartilhado;
- coluna `organization_id` nos recursos organizacionais;
- autorização e filtragem obrigatórias por organização;
- constraints e índices compostos;
- testes automatizados de isolamento.

Possíveis mecanismos adicionais do PostgreSQL, como Row-Level Security, serão avaliados em uma change própria. Nenhum mecanismo isolado será tratado como única barreira.

## Observabilidade

- logs estruturados;
- somente `/actuator/health` é exposto como endpoint técnico na fundação, sem detalhes internos;
- correlation ID;
- métricas técnicas;
- health checks;
- trilha de auditoria separada de logs operacionais;
- proibição de dados sensíveis desnecessários nos logs.

## Segurança

Referências mínimas para decisões futuras:

- OWASP ASVS;
- OWASP Top 10;
- OWASP API Security Top 10;
- práticas seguras do Spring Security;
- LGPD e orientações aplicáveis da ANPD.

## Dados

- UUIDs ou identificadores não sequenciais em recursos expostos;
- timestamps em UTC;
- migrations imutáveis após aplicadas;
- soft delete somente quando houver requisito explícito;
- políticas de retenção documentadas por categoria de dado;
- backups criptografados e restauração testada.

## Restrições arquiteturais verificáveis

ArchUnit deve impedir que:

- domínio dependa de Spring;
- domínio dependa de JPA;
- domínio dependa de adapters;
- módulos acessem implementação interna de outros módulos;
- controllers acessem repositories diretamente;
- adapters de entrada contenham regras de negócio.
