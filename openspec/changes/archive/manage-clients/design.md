# Design: Gestao de clientes

## Decisao principal

Cliente e recurso interno pertencente a uma organizacao. Registro contem somente UUID, `organizationId`, `name`, status `ACTIVE` ou `INACTIVE`, `createdAt`, `updatedAt` e `deactivatedAt`. Nao existe conta, credencial, contato, documento, endereco ou acesso externo de cliente.

## API e autorizacao

Todas rotas recebem `organizationId` explicito e usam `accountId` somente da sessao server-side persistida.

| Operacao | Rota | Autoridade |
| --- | --- | --- |
| Criar | `POST /api/v1/organizations/{organizationId}/clients` | `OWNER`, `ADMIN` |
| Consultar | `GET /api/v1/organizations/{organizationId}/clients/{clientId}` | todos membros |
| Listar | `GET /api/v1/organizations/{organizationId}/clients?status=active|inactive|all` | todos membros |
| Editar | `PATCH /api/v1/organizations/{organizationId}/clients/{clientId}` | `OWNER`, `ADMIN` |
| Desativar | `DELETE /api/v1/organizations/{organizationId}/clients/{clientId}` | `OWNER`, `ADMIN` |

Criacao e edicao aceitam exclusivamente `{ "name": "..." }`. Nome e obrigatorio, removido espacos nas extremidades e limitado a 1-120 caracteres. Campos adicionais, UUIDs malformados, corpo ausente ou `status` invalido retornam `400 Bad Request` sem persistencia. Mutacoes exigem CSRF. Sucesso em criacao retorna `201 Created`; edicao retorna `200 OK`; desativacao retorna `204 No Content`.

Consulta de recurso exige sempre `organizationId` e `clientId`. Organizacao inexistente, membership ausente ou cliente ausente naquela organizacao retornam `404 Not Found` indistinguivel. Acesso sem sessao segue resposta global de autenticacao. Listagem autorizada retorna somente registros da organizacao; sem filtro, `status=active` e somente clientes ativos retornam.

## Modelo e persistencia

Migration cria `organization_clients` com:

- `id UUID` chave primaria;
- `organization_id UUID` chave estrangeira para organizacao;
- `name VARCHAR(120)`;
- `status VARCHAR(16)` restrito a `ACTIVE` e `INACTIVE`;
- `created_at`, `updated_at` e `deactivated_at` em UTC;
- indice para `organization_id`, `status` e `created_at`.

Nome nao possui unicidade. Desativacao muda status para `INACTIVE`, define `deactivatedAt` e atualiza `updatedAt`; registro nao e excluido. Reativacao fica fora do escopo. Tickets futuros poderao manter referencia a cliente inativo para preservar historico, mas esta change nao cria tickets.

## Arquitetura

- dominio `client` contem entidade e lifecycle Java puro;
- aplicacao define casos de uso e portas de entrada/saida;
- adapter HTTP valida contrato e invoca portas, sem repositories;
- adapter JPA filtra consultas por `organization_id` e confirma membership pela dupla `organizationId` e `accountId` antes de retornar recurso;
- Spring Security fornece somente identidade autenticada;
- nao ha modulo compartilhado novo nem RBAC configuravel.

## Privacidade e operacao

`name` tem finalidade de identificar cliente para atendimento e associacao futura de tickets. Nome nao deve entrar em logs, eventos, metricas, traces ou respostas de erro. Depois de commit, mutacoes registram somente:

- `organization.client_created` com `organizationId`, `clientId`, `createdByAccountId`;
- `organization.client_updated` com `organizationId`, `clientId`, `updatedByAccountId`;
- `organization.client_deactivated` com `organizationId`, `clientId`, `deactivatedByAccountId`.

Falha de log apos commit nao causa rollback. Audit trail persistida, retencao, exportacao, exclusao e anonimização ficam fora do escopo.

## Fora do escopo

- reativacao, exclusao fisica e alteracao de status arbitraria;
- contatos, documentos, enderecos e campos customizados;
- autenticacao ou portal de cliente;
- tickets e regras para associar ticket a cliente inativo;
- paginação, busca textual, ordenacao configuravel e importacao;
- notificacao e auditoria persistida completa.
