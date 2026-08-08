# Tasks: Acesso organizacional

## 1. Revisao e aprovacao

- [x] Decidir contexto explicito, papeis, contrato minimo e invariante de ownership
- [x] Revisar proposal, design, delta spec e riscos de isolamento
- [x] Aprovar humanamente a change para implementacao

## 2. Dominio e aplicacao

- [x] Evoluir enum de membership para `OWNER`, `ADMIN` e `TECHNICIAN`
- [x] Criar caso de uso e port para localizar organizacao autorizada por conta e organizacao
- [x] Manter dominio independente de Spring, JPA e HTTP
- [x] Criar testes unitarios para papeis e acesso autorizado ou negado

## 3. Persistencia e migracao

- [x] Criar migration versionada que aceite tres papeis e preserve memberships `OWNER` existentes
- [x] Implementar consulta composta por `organization_id` e `account_id`
- [x] Criar testes PostgreSQL/Testcontainers para migration e isolamento entre contas
- [x] Confirmar que migration aceita `OWNER`, `ADMIN` e `TECHNICIAN`
- [x] Confirmar que papel fora do enum e rejeitado
- [x] Confirmar que memberships `OWNER` existentes permanecem intactas

## 4. API e seguranca

- [x] Criar `GET /api/v1/organizations/{organizationId}`
- [x] Obter `accountId` somente do contexto autenticado da sessao persistida
- [x] Retornar `200` somente para membership existente e dados permitidos
- [x] Retornar `404` identico para organizacao inexistente ou nao autorizada
- [x] Rejeitar UUID malformado com `400`
- [x] Cobrir acesso de `OWNER`, `ADMIN` e `TECHNICIAN`
- [x] Cobrir UUID conhecido de organizacao alheia e ausencia de autenticacao
- [x] Confirmar mesmo status e formato para organizacao inexistente e organizacao sem membership

## 5. Verificacao

- [x] Confirmar ausencia de organizacao ativa em sessao, cookie ou `HttpSession`
- [x] Confirmar que logs e erros nao expõem dados de organizacao alheia
- [x] Executar `backend/mvnw.cmd spotless:apply`
- [x] Executar `backend/mvnw.cmd verify`
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final antes do archive

## Evidencias

- `backend/mvnw.cmd verify` concluido em 2026-08-08 com 35 testes aprovados, incluindo PostgreSQL/Testcontainers, Flyway V4, ArchUnit, Spotless e Checkstyle.
- Foram validados: migration V3 para V4 com preservacao de `OWNER`, aceite de `OWNER`, `ADMIN` e `TECHNICIAN`, rejeicao de papel invalido, consulta composta por organizacao e conta, acesso dos tres papeis, `404` indistinguivel para organizacao inexistente e sem membership, UUID invalido e ausencia de autenticacao.

## Estado da change

- revisao humana final aprovada em 2026-08-08;
- change arquivada em 2026-08-08.
