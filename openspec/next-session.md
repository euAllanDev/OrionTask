# Retomada da Próxima Sessão

## Estado atual

- A criação de conta e a autenticação com encerramento de sessão estão concluídas e arquivadas.
- A criação de organização foi arquivada; especificação canônica em `openspec/specs/organization-creation/spec.md`.
- `organization-access` foi arquivada; especificação canônica em `openspec/specs/organization-access/spec.md`.
- `invite-member` foi arquivada; especificacao canonica em `openspec/specs/membership-invitation/spec.md`.
- A especificação canônica de autenticação está em `openspec/specs/session-authentication/spec.md`.

## Próximo ponto de partida

Iniciar exploracao e proposal para revogacao de acesso. Nao implementar codigo antes de aprovar os artefatos OpenSpec.

## Decisões implementadas

- `organizationId` explícito na rota, sem organização ativa em sessão;
- papéis fixos `OWNER`, `ADMIN` e `TECHNICIAN`;
- autorização por membership existente e consulta composta de conta e organização;
- UUID válido sem acesso e UUID inexistente retornam `404` indistinguível;
- toda organização existente mantém ao menos uma membership `OWNER`.

## Limites atuais

Não antecipar lifecycle de membership, clientes ou tickets sem change aprovada.
