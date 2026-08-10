# OrionTask

OrionTask é um SaaS multiempresa de suporte técnico e gestão de tickets, voltado inicialmente a pequenas empresas que prestam atendimento técnico aos próprios clientes.

Este repositório será desenvolvido com:

- Spec-Driven Development (SDD);
- OpenSpec como fluxo de mudanças;
- revisão humana obrigatória;
- monólito modular;
- arquitetura hexagonal;
- segurança e privacidade desde o design;
- backend Java com Spring Boot;
- frontend web separado.

## Estado atual

A fundação técnica, a criação de conta interna, a autenticação por sessão, a criação de organização, o acesso organizacional, os convites de usuários, a revogação de membros, a gestão de clientes, a fundação frontend e os testes de isolamento organizacional foram concluídos e arquivados localmente. A abertura de pull requests e a validação remota do CI permanecem adiadas por decisão do responsável.

## Documentos fundamentais

- `docs/constitution/mission.md`
- `docs/constitution/tech-stack.md`
- `docs/constitution/roadmap.md`
- `docs/constitution/principles.md`
- `AGENTS.md`
- `docs/product/vision.md`
- `docs/architecture/package-conventions.md`
- `docs/workflow/contributing.md`
- `docs/workflow/quality.md`
- `docs/decisions/ADR-0001-foundation-tooling-and-repository.md`

## OpenSpec

- `openspec/specs/`: comportamento atualmente acordado;
- `openspec/changes/`: mudanças propostas ou em desenvolvimento;
- `openspec/changes/archive/`: mudanças concluídas e arquivadas.

## Change ativa

Não há change ativa. A próxima capacidade prevista é auditoria de ações críticas, que exige uma nova change OpenSpec aprovada antes da implementação.

## Execução local

Pré-requisitos: Java 21 e Docker Desktop em execução.

1. Copie `.env.example` para `.env`, gere `ORIONTASK_SESSION_HMAC_KEY` com ao menos 32 bytes e ajuste somente os valores locais, se necessário.
2. Execute `docker compose --env-file ../.env up -d` em `backend/` para iniciar o PostgreSQL.
3. Execute `mvnw.cmd verify` no Windows ou `./mvnw verify` em ambientes POSIX, também em `backend/`.
4. Execute `mvnw.cmd spring-boot:run` no Windows ou `./mvnw spring-boot:run` em ambientes POSIX.
5. Consulte `http://localhost:8080/actuator/health`.

Os endpoints atuais são `/actuator/health`, `POST /api/v1/accounts`, `GET /api/v1/csrf`, `POST /api/v1/sessions`, `DELETE /api/v1/session`, `POST /api/v1/organizations`, `GET /api/v1/organizations/{organizationId}`, `POST /api/v1/organizations/{organizationId}/invitations`, `POST /api/v1/membership-invitations/{token}/accept`, `DELETE /api/v1/organizations/{organizationId}/members/{accountId}`, `POST /api/v1/organizations/{organizationId}/clients`, `GET /api/v1/organizations/{organizationId}/clients`, `GET /api/v1/organizations/{organizationId}/clients/{clientId}`, `PATCH /api/v1/organizations/{organizationId}/clients/{clientId}` e `DELETE /api/v1/organizations/{organizationId}/clients/{clientId}`.
