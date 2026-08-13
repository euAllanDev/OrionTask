# Retomada da Próxima Sessão

## Estado atual

- A criação de conta e a autenticação com encerramento de sessão estão concluídas e arquivadas.
- A criação de organização foi arquivada; especificação canônica em `openspec/specs/organization-creation/spec.md`.
- `organization-access` foi arquivada; especificação canônica em `openspec/specs/organization-access/spec.md`.
- `invite-member` foi arquivada; especificacao canonica em `openspec/specs/membership-invitation/spec.md`.
- `revoke-member-access` e `manage-clients` foram arquivadas localmente.
- `bootstrap-frontend` foi arquivada; especificação canônica em `openspec/specs/frontend-foundation/spec.md`.
- `verify-organization-isolation` foi arquivada; evidências consolidadas em `openspec/specs/organization-access/spec.md`.
- `define-audit-retention` foi arquivada; política canônica em `openspec/specs/audit-data-retention/spec.md`.
- `audit-critical-actions` foi arquivada; especificação canônica em `openspec/specs/critical-action-audit/spec.md`.
- A especificação canônica de autenticação está em `openspec/specs/session-authentication/spec.md`.
- `open-ticket` foi arquivada; especificação canônica em `openspec/specs/ticket-opening/spec.md`.
- `list-and-read-tickets` foi arquivada; especificação canônica em `openspec/specs/ticket-reading/spec.md`.
- `current-session-identity` foi arquivada; especificação canônica em `openspec/specs/session-authentication/spec.md`.
- `frontend-auth` foi arquivada; especificação canônica em `openspec/specs/frontend-authentication/spec.md`.
- `list-my-organizations` foi arquivada; especificação canônica em `openspec/specs/organization-access/spec.md`.
- `frontend-organization-shell` foi arquivada; especificação canônica em `openspec/specs/frontend-organization-shell/spec.md`.
- `backend/mvnw.cmd verify` concluiu com 80 testes, PostgreSQL/Testcontainers, Flyway V8, ArchUnit, Spotless e Checkstyle.

## Próximo ponto de partida

Não há change ativa. Explorar e propor próxima capacidade aprovada no roadmap antes de implementar código. Nao implementar codigo antes de aprovar os artefatos OpenSpec completos.

## Decisões implementadas

- `organizationId` explícito na rota, sem organização ativa em sessão;
- papéis fixos `OWNER`, `ADMIN` e `TECHNICIAN`;
- autorização por membership existente e consulta composta de conta e organização;
- UUID válido sem acesso e UUID inexistente retornam `404` indistinguível;
- toda organização existente mantém ao menos uma membership `OWNER`;
- frontend usa cookie de sessão exclusivo do backend, CSRF em memória e rotas organizacionais explícitas.
- isolamento entre organizacoes e nao enumeracao possuem cobertura HTTP com PostgreSQL/Testcontainers.
- auditoria organizacional persiste eventos atomicos, isolados e retidos por 12 meses.
- auditoria cobre criacao de organizacao, convites, revogacao de membership e mutacoes de cliente; `OWNER` e `ADMIN` consultam eventos paginados, enquanto `TECHNICIAN` recebe `403`.
- eventos de auditoria contem somente IDs tecnicos, acao e timestamp; nao possuem foreign keys ou `ON DELETE CASCADE`, preservando periodo residual apos futura exclusao de organizacao.
- expurgo fisico de auditoria ocorre diariamente apos 12 meses; backups possuem limite de 90 dias e exigem reaplicacao de expurgo antes de restauracao ser exposta ao trafego normal.
- ticket e aberto somente por `OWNER`, `ADMIN` ou `TECHNICIAN`, com cliente `ACTIVE` da organizacao, prioridade padrao `MEDIUM`, status inicial `OPEN` e assignee opcional com membership na mesma organizacao.
- ticket pode ser lido por todo membership da propria organizacao; listagem usa pagina por offset, ordem `createdAt DESC`, `id DESC`, filtros estritos e retorno vazio para UUID filtrado inexistente ou estrangeiro.
- `GET /api/v1/session` retorna `accountId` e e-mail normalizado da sessao server-side valida; sessao ausente, expirada, revogada ou com conta ausente retorna `401` generico.
- frontend usa estado autenticado somente em memoria, bootstrap por `GET /api/v1/session`, CSRF em memoria e nao repete automaticamente `403`.
- `GET /api/v1/organizations` lista somente memberships atuais da conta autenticada, com `id`, `name`, timestamps e papel; ordem `updatedAt DESC`, `id DESC`, sem organizacao ativa ou parametros de listagem.
- `/app` lista organizacoes acessiveis com estados loading, empty, error e ready; shell usa `organizationId` na URL como contexto visual, sem organizacao ativa persistida.

## Limites atuais

Não antecipar novas capacidades sem change aprovada.

## Gate de exposicao publica

Antes de exposicao publica do OrionTask, controlador e orientacao juridica devem validar formalmente finalidade, categorias, prazo, base aplicavel, exclusao e tratamento de backups da auditoria.
