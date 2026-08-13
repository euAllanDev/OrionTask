# Tasks: Identidade da sessao atual

## 1. Revisao e aprovacao

- [x] Decidir resposta minima, e-mail normalizado, `401` generico e ausencia de CSRF
- [x] Revisar e aprovar proposal, design e delta spec em 2026-08-13

## 2. Dominio e aplicacao

- [x] Criar caso de uso, resultado e porta de entrada para identidade atual
- [x] Buscar conta somente por `accountId` proveniente do principal autenticado
- [x] Tratar conta ausente como autenticacao nao confirmada
- [x] Cobrir regras em testes unitarios

## 3. API e seguranca

- [x] Criar `GET /api/v1/session` autenticado e sem CSRF
- [x] Retornar somente `accountId` e e-mail normalizado
- [x] Preservar `401` generico para sessao ausente, expirada ou revogada
- [x] Cobrir HTTP, cookies de sessao e campos exatos da resposta com PostgreSQL/Testcontainers

## 4. Verificacao e archive

- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify` com Docker disponivel
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final em 2026-08-13
- [x] Consolidar delta spec, atualizar README e retomada, arquivar change em 2026-08-13

## Evidencias

- `npx --yes @fission-ai/openspec validate current-session-identity --strict` concluiu em 2026-08-13.
- `backend/mvnw.cmd spotless:apply` passou em 2026-08-13.
- `backend/mvnw.cmd verify` concluiu com 76 testes aprovados, PostgreSQL/Testcontainers, Flyway V8, ArchUnit, Spotless e Checkstyle em 2026-08-13.
- `GetCurrentSessionIdentityServiceTest` cobre identidade pelo `accountId` autenticado e conta ausente.
- `CurrentSessionHttpIntegrationTest` cobre resposta exata sem CSRF e `401` indistinguivel para sessao ausente, expirada e revogada.
