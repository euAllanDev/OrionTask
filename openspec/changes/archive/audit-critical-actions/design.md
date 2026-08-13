# Design: Auditoria de acoes criticas

## Eventos cobertos

Esta change registra somente acoes organizacionais efetivadas:

- `organization.created`;
- `membership_invitation.created`;
- `membership_invitation.accepted`;
- `organization.membership_revoked`;
- `organization.client_created`;
- `organization.client_updated`;
- `organization.client_deactivated`.

Eventos de identidade, leituras, tentativas negadas, validacao, CSRF, tickets e recursos futuros ficam fora. Logs operacionais existentes permanecem separados e nao sao fonte de auditoria.

## Modelo e atomicidade

Modulo `audit` possui evento imutavel com `id` UUID, `organizationId`, `actorAccountId`, `action`, `resourceType`, `resourceId` e `occurredAt` UTC. Nomes, e-mails, papeis, payloads, tokens, cookies, CSRF e metadados de rede nao sao persistidos.

Tabela `audit_events` armazena somente esses campos, indice composto por `organization_id`, `occurred_at` e `id`, e nao possui endpoint ou caso de uso de alteracao ou exclusao individual. `organization_id`, `actor_account_id` e `resource_id` nao usam foreign keys nem `ON DELETE CASCADE`: exclusao futura de organizacao ou conta nao pode impedir nem apagar retencao residual definida pela policy.

Cada evento e persistido na mesma transacao da mutacao efetiva que ele descreve. Rollback da mutacao tambem remove evento; falha ao persistir evento impede commit da mutacao. Isso difere de logs operacionais pos-commit, cuja falha nao causa rollback e nao substitui a auditoria persistida.

## Consulta autorizada

`GET /api/v1/organizations/{organizationId}/audit-events?page={page}&size={size}` exige sessao autenticada e nao exige CSRF. `OWNER` e `ADMIN` podem consultar somente propria organizacao; `TECHNICIAN` recebe `403 Forbidden`. Organizacao inexistente ou ausencia de membership retornam `404 Not Found` indistinguivel.

Resposta e paginada por offset, ordenada deterministicamente por `occurredAt` decrescente e `id` decrescente. `page` inicia em zero; `size` tem padrao 50 e maximo 100. Cada item expoe somente `id`, `action`, `resourceType`, `resourceId`, `actorAccountId` e `occurredAt`.

## Retencao e operacao

Expurgo executa diariamente e remove fisicamente eventos cujo `occurredAt` tenha completado 12 meses, conforme `audit-data-retention`. Execucao deve ocorrer antes de restauracao ser exposta ao trafego normal. Backups que contenham eventos de auditoria mantem no maximo 90 dias; runbook operacional documenta essa configuracao e reaplicacao de expurgo apos restauracao.

Antes de exposicao publica, controlador e orientacao juridica devem validar formalmente finalidade, categorias, prazo, base aplicavel, exclusao e tratamento de backups. Esse gate nao e substituido por testes ou revisao tecnica.

## Testes

Testes de integracao PostgreSQL devem demonstrar atomicidade de mutacao e evento, isolamento de leitura, matriz `OWNER`/`ADMIN`/`TECHNICIAN`, anti-enumeracao e expurgo. Testes devem confirmar que payloads e dados pessoais desnecessarios nao entram em `audit_events`.
