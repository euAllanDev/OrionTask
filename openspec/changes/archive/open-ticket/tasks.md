# Tasks: Abertura de ticket

## 1. Revisao e aprovacao

- [x] Decidir dados minimos, cliente, prioridade, status, atribuicao inicial e matriz de autoridade
- [x] Fechar questoes abertas de limites textuais e categoria
- [x] Revisar e aprovar proposal, design e delta spec em 2026-08-13

## 2. Dominio e aplicacao

- [x] Criar modelo Java puro de ticket e enums fixos de prioridade e status
- [x] Criar caso de uso, comando, resultado e portas para abertura
- [x] Validar titulo, descricao, prioridade padrao e campos obrigatorios
- [x] Aplicar autoridade para `OWNER`, `ADMIN` e `TECHNICIAN`
- [x] Validar cliente `ACTIVE` e assignee pela fronteira organizacional
- [x] Cobrir regras em testes unitarios

## 3. Persistencia e migration

- [x] Criar migration versionada de tickets, constraints e indices
- [x] Implementar abertura atomica escopada por organizacao
- [x] Garantir que cliente e assignee nao sejam aceitos por UUID isolado
- [x] Criar testes PostgreSQL/Testcontainers para migration, atomicidade e isolamento

## 4. API e seguranca

- [x] Criar rota HTTP com contrato estrito e `201 Created`
- [x] Exigir CSRF e obter criador somente da sessao server-side
- [x] Retornar `404` indistinguivel para organizacao, cliente ou assignee inacessivel
- [x] Cobrir HTTP, CSRF, autorizacao e ausencia de persistencia em mutacoes negadas

## 5. Verificacao e archive

- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify` com Docker disponivel em 2026-08-13
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final em 2026-08-13
- [x] Consolidar delta spec, atualizar roadmap e README, arquivar change em 2026-08-13

## Evidencias

- `npx --yes @fission-ai/openspec validate open-ticket --strict` concluiu em 2026-08-13.
- `backend/mvnw.cmd -Dtest=OpenTicketServiceTest test` concluiu com 3 testes aprovados em 2026-08-13.
- `backend/mvnw.cmd -Dtest=ArchitectureTest test` concluiu com 3 testes aprovados em 2026-08-13.
- `backend/mvnw.cmd verify -DskipTests` concluiu com compilacao, Spotless e Checkstyle aprovados em 2026-08-13.
- `backend/mvnw.cmd verify` concluiu com 68 testes aprovados, PostgreSQL/Testcontainers, Flyway V8, ArchUnit, Spotless e Checkstyle em 2026-08-13.
