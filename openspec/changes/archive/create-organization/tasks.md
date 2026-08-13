# Tasks: Criacao de organizacao

## 1. Revisao e aprovacao

- [x] Resolver decisoes de dados minimos, membership inicial, multiplas organizacoes, evento operacional, atomicidade e autoridade do criador
- [x] Decidir retencao de `name` enquanto registro existir e finalidade permanecer
- [x] Revisar proposal, design, delta spec, limites de seguranca e privacidade
- [x] Aprovar humanamente a change para implementacao

## 2. Dominio e aplicacao

- [x] Criar modulo `organization` com modelos Java puros para organizacao, membership e papel fixo `OWNER`
- [x] Criar caso de uso de criacao e ports de persistencia e evento operacional
- [x] Obter `accountId` somente do contexto autenticado derivado da sessao persistida
- [x] Criar organizacao e membership inicial dentro de unica transacao
- [x] Impedir organizacao sem membership inicial e membership para organizacao inexistente
- [x] Criar testes unitarios de dominio e aplicacao para `OWNER`, multiplas organizacoes e rollback

## 3. Persistencia e seguranca

- [x] Criar migration versionada para organizacoes e memberships com UUIDs, timestamps UTC, FKs e `UNIQUE(account_id, organization_id)`
- [x] Criar entidades JPA, repositorios e adapter de persistencia fora do dominio
- [x] Garantir que nome nao tenha unicidade global e que conta possa ter multiplas memberships
- [x] Criar evento operacional `organization.created` somente apos commit, sem nome ou dados sensiveis
- [x] Cobrir falha do evento operacional sem rollback da criacao confirmada
- [x] Criar testes de integracao PostgreSQL/Testcontainers para migration, atomicidade, FKs e constraint composta

## 4. API e autorizacao

- [x] Criar `POST /api/v1/organizations` com corpo estrito contendo somente `name`
- [x] Validar `name`: texto obrigatorio, trim, 1 a 120 caracteres
- [x] Exigir sessao autenticada e CSRF valido
- [x] Rejeitar campos de autoridade e campos adicionais com `400 Bad Request`
- [x] Retornar `201 Created`, `Location`, `id`, `name` e `createdAt`
- [x] Criar testes HTTP para autenticacao ausente, CSRF ausente/invalido, contrato invalido e criacao valida
- [x] Criar testes que comprovem criador derivado da sessao e impossibilidade de escolher conta por corpo

## 5. Verificacao

- [x] Cobrir nomes duplicados em organizacoes distintas
- [x] Cobrir multiplas organizacoes criadas pela mesma conta
- [x] Cobrir isolamento de contas: criacao de uma nao cria membership para outra
- [x] Confirmar logs e eventos sem nome, e-mail, payload, token, cookie ou credencial
- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify`
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final antes do archive

## Estado da change

- implementacao concluida e verificada localmente;
- revisao humana final aprovada em 2026-08-08;
- change arquivada em 2026-08-08.

## Evidencias

- `backend/mvnw.cmd verify` concluido em 2026-08-08 com 32 testes aprovados, incluindo PostgreSQL/Testcontainers, Flyway V3, ArchUnit, Spotless e Checkstyle.
- Foram validados: criacao autenticada com CSRF, trim e validacao de nome, rejeicao de campos adicionais de autoridade, nomes repetidos, membership `OWNER`, isolamento entre contas, constraint composta, rollback completo de Organization e membership `OWNER` quando persistencia da membership viola FK, e falha de evento operacional sem rollback da criacao.
