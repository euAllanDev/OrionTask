# Tasks: Listagem das minhas organizacoes

## 1. Revisao e aprovacao

- [x] Decidir campos, papel, ordem, lista vazia, ausencia de paginacao e concorrencia
- [x] Revisar e aprovar proposal, design e delta spec em 2026-08-13

## 2. Dominio e aplicacao

- [x] Criar caso de uso, resultado e portas de listagem
- [x] Obter identidade somente da sessao server-side
- [x] Mapear dados minimos de organizacao e papel atual
- [x] Cobrir mapeamento, ordem e isolamento em testes unitarios

## 3. Persistencia e API

- [x] Implementar consulta por membership atual, projection minima e ordem fixa
- [x] Criar `GET /api/v1/organizations` sem CSRF
- [x] Rejeitar corpo ou query e preservar `401` global para sessao ausente
- [x] Cobrir HTTP/PostgreSQL para papeis, lista vazia, isolamento, ordem e entradas invalidas

## 4. Verificacao e archive

- [x] Executar `backend/mvnw.cmd spotless:apply` em 2026-08-13
- [x] Executar `backend/mvnw.cmd verify` com Docker disponivel em 2026-08-13: 80 testes, Flyway, ArchUnit, Spotless e Checkstyle passaram
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final em 2026-08-13
- [x] Consolidar delta spec, atualizar roadmap, README e retomada, arquivar change em 2026-08-13

## Evidencias

- `npx --yes @fission-ai/openspec validate list-my-organizations --strict` concluiu em 2026-08-13.
- `backend/mvnw.cmd verify` concluiu com 80 testes aprovados, PostgreSQL/Testcontainers, Flyway V8, ArchUnit, Spotless e Checkstyle em 2026-08-13.
