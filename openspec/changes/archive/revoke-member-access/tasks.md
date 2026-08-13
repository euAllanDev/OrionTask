# Tasks: Revogacao de acesso de membro

## 1. Revisao e aprovacao

- [x] Decidir matriz de autoridade, sessoes, persistencia e resposta para alvo ausente
- [x] Revisar e aprovar proposal, design e delta spec

## 2. Dominio e aplicacao

- [x] Criar porta de entrada, comando e caso de uso para revogar membership
- [x] Aplicar matriz fixa de papeis e erros `404`/`403` aprovados
- [x] Criar porta de persistencia e auditoria operacional minima
- [x] Cobrir regras em testes unitarios de aplicacao

## 3. Persistencia e concorrencia

- [x] Implementar busca e exclusao escopadas por organizacao e conta na mesma transacao
- [x] Serializar revogacoes concorrentes e preservar isolamento
- [x] Criar testes PostgreSQL/Testcontainers para exclusao, isolamento e concorrencia

## 4. API e seguranca

- [x] Criar endpoint `DELETE` autenticado e protegido por CSRF
- [x] Validar UUIDs e rejeitar corpo
- [x] Garantir `404` indistinguivel para recursos ou memberships nao acessiveis
- [x] Registrar evento pos-commit sem dados sensiveis
- [x] Cobrir autenticacao, CSRF, autorizacao e isolamento em testes HTTP

## 5. Verificacao e archive

- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify` com Docker disponivel
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final antes do archive
- [x] Consolidar delta spec, atualizar roadmap e README, arquivar change

## Estado da change

- revisao humana final aprovada em 2026-08-09;
- change arquivada em 2026-08-09.

## Evidencias

- `backend/mvnw.cmd verify` concluiu em 2026-08-09 com 51 testes aprovados, PostgreSQL/Testcontainers, Flyway, ArchUnit, Spotless e Checkstyle.
