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

A fundação técnica e a criação de conta interna foram concluídas e arquivadas localmente. A abertura de pull requests e a validação remota do CI permanecem adiadas por decisão do responsável.

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

`authenticate-and-logout` está em implementação. A change foi aprovada e ainda requer cenários de teste pendentes e revisão humana antes do archive.

## Execução local

Pré-requisitos: Java 21 e Docker Desktop em execução.

1. Copie `.env.example` para `.env`, gere `ORIONTASK_SESSION_HMAC_KEY` com ao menos 32 bytes e ajuste somente os valores locais, se necessário.
2. Execute `docker compose --env-file ../.env up -d` em `backend/` para iniciar o PostgreSQL.
3. Execute `mvnw.cmd verify` no Windows ou `./mvnw verify` em ambientes POSIX, também em `backend/`.
4. Execute `mvnw.cmd spring-boot:run` no Windows ou `./mvnw spring-boot:run` em ambientes POSIX.
5. Consulte `http://localhost:8080/actuator/health`.

Os endpoints atuais são `/actuator/health`, `POST /api/v1/accounts`, `GET /api/v1/csrf`, `POST /api/v1/sessions` e `DELETE /api/v1/session`.
