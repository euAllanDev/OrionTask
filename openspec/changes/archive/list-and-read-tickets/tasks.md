# Tasks: Consulta e listagem de tickets

## 1. Revisao e aprovacao

- [x] Decidir campos, paginacao, ordenacao, filtros e respostas anti-enumeracao
- [x] Revisar e aprovar proposal, design e delta spec em 2026-08-13

## 2. Dominio e aplicacao

- [x] Criar consultas, resultados e portas de entrada/saida necessarias
- [x] Validar pagina, filtros, UUIDs e parametros repetidos
- [x] Cobrir mapeamento de resumo, detalhe, ordem e paginacao em testes unitarios

## 3. Persistencia

- [x] Implementar consulta individual escopada por `organization_id`
- [x] Implementar listagem e contagem com filtros organizacionais identicos
- [x] Aplicar ordem fixa `createdAt DESC`, `id DESC`, limite e offset
- [x] Criar testes PostgreSQL/Testcontainers para isolamento, filtros e cliente inativo

## 4. API e seguranca

- [x] Criar rotas HTTP de detalhe e listagem com contratos estritos
- [x] Exigir sessao e nao CSRF para leituras
- [x] Retornar `404` indistinguivel para detalhe inacessivel ou inexistente
- [x] Retornar lista vazia para filtro UUID inexistente ou estrangeiro
- [x] Cobrir HTTP, todos os papeis, paginacao, filtros e anti-enumeracao

## 5. Verificacao e archive

- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify` com Docker disponivel
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final em 2026-08-13
- [x] Consolidar delta spec, atualizar roadmap e README, arquivar change em 2026-08-13

## Evidencias

- `npx --yes @fission-ai/openspec validate list-and-read-tickets --strict` concluiu em 2026-08-13.
- `backend/mvnw.cmd verify` concluiu com 72 testes aprovados, PostgreSQL/Testcontainers, Flyway V8, ArchUnit, Spotless e Checkstyle em 2026-08-13.
