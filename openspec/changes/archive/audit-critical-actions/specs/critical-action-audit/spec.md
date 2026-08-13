# Critical Action Audit Specification Delta

## ADDED Requirements

### Requirement: Eventos organizacionais minimos e atomicos

Sistema DEVE persistir evento de auditoria para cada acao efetivada `organization.created`, `membership_invitation.created`, `membership_invitation.accepted`, `organization.membership_revoked`, `organization.client_created`, `organization.client_updated` e `organization.client_deactivated`. Evento DEVE conter somente UUID proprio, `organizationId`, `actorAccountId`, `action`, `resourceType`, `resourceId` e `occurredAt` UTC.

Evento DEVE ser persistido na mesma transacao da mutacao efetiva. Rollback de mutacao NAO DEVE deixar evento; falha ao persistir evento DEVE impedir commit da mutacao. Sistema NAO DEVE registrar evento para tentativa negada, erro de validacao, CSRF rejeitado, leitura, login, logout ou cadastro de conta nesta capacidade.

Evento NAO DEVE conter nome, e-mail, papel, payload HTTP, senha, hash, cookie, token, CSRF, IP ou metadados de rede. Logs operacionais permanecem separados e falha de log NAO DEVE substituir nem alterar semantica da auditoria persistida.

### Requirement: Imutabilidade proporcional

Aplicacao NAO DEVE expor operacao de update ou delete individual para evento de auditoria. Evento NAO DEVE usar soft delete, reativacao, anonimização ou mecanismo generico de excecao. Exclusao por retencao segue `audit-data-retention`.

### Requirement: Consulta organizacional autorizada

`GET /api/v1/organizations/{organizationId}/audit-events?page={page}&size={size}` DEVE exigir sessao autenticada e NAO DEVE exigir CSRF. `OWNER` e `ADMIN` DEVEM consultar somente eventos da propria organizacao; `TECHNICIAN` DEVE receber `403 Forbidden`. Organizacao inexistente ou membership ausente DEVEM retornar `404 Not Found` indistinguivel, sem confirmar existencia de organizacao ou eventos.

Resposta DEVE ser paginada por offset, ordenada por `occurredAt` decrescente e `id` decrescente. `page` inicia em zero; `size` padrao e 50 e maximo e 100. Item DEVE retornar somente `id`, `action`, `resourceType`, `resourceId`, `actorAccountId` e `occurredAt`.

### Requirement: Expurgo e validacao de exposicao

Sistema DEVE executar expurgo diario de eventos cujo `occurredAt` tenha completado 12 meses, com exclusao fisica. Backups e restauracao DEVEM obedecer `audit-data-retention`. Antes de exposicao publica do OrionTask, controlador e orientacao juridica DEVEM validar formalmente finalidade, categorias, prazo, base aplicavel, exclusao e tratamento de backups.

### Requirement: Evidencia verificavel

Testes de integracao PostgreSQL DEVEM cobrir persistencia atomica, isolamento entre organizacoes, consulta por matriz de papel, respostas anti-enumeracao e expurgo. Testes DEVEM confirmar que campos nao permitidos nao sao persistidos em evento de auditoria.
