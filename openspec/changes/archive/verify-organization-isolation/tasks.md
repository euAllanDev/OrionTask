# Tasks: Verificar isolamento organizacional

## 1. Revisao e aprovacao

- [x] Decidir classe dedicada, cobertura de convite e criterio de nao enumeracao
- [x] Revisar e aprovar proposal, design, delta spec e tarefas

## 2. Testes de integracao

- [x] Criar fixture de duas organizacoes, membros, sessoes e CSRF independentes
- [x] Cobrir leitura organizacional e listagem cruzada de clientes
- [x] Cobrir leitura, edicao e desativacao cruzadas de cliente por UUID conhecido
- [x] Cobrir revogacao cruzada de membership por UUID conhecido
- [x] Confirmar estado PostgreSQL inalterado apos cada mutacao negada
- [x] Comparar respostas anti-enumeracao quando exigido pela spec

## 3. Verificacao e archive

- [x] Executar `backend/mvnw.cmd verify`
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final antes do archive
- [x] Consolidar delta spec, atualizar roadmap e README, arquivar change

## Evidencias

- `backend/mvnw.cmd verify` concluiu em 2026-08-10 com 57 testes aprovados, PostgreSQL/Testcontainers, Flyway, ArchUnit, Spotless e Checkstyle.
- Revisao humana final aprovada; change arquivada em 2026-08-10.
