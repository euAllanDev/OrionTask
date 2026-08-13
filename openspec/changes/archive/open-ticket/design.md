# Design: Abertura de ticket

## Decisao principal

Ticket e recurso interno de uma organizacao. Contem UUID nao sequencial, `organizationId`, `customerId`, `creatorAccountId`, `assigneeAccountId` opcional, `title`, `description` opcional, prioridade fixa, status `OPEN`, `createdAt` e `updatedAt` em UTC. Ticket nao possui categoria nesta change.

## API e autorizacao

| Operacao | Rota | Autoridade |
| --- | --- | --- |
| Abrir | `POST /api/v1/organizations/{organizationId}/tickets` | `OWNER`, `ADMIN`, `TECHNICIAN` |

Rota exige sessao server-side persistida e CSRF valido. `creatorAccountId` vem somente da sessao. Corpo aceita exclusivamente `title`, `description`, `customerId`, `priority` e `assigneeAccountId`.

`title` e obrigatorio, passa por trim e deve ter 1 a 120 caracteres. `description` e opcional e, quando presente, deve ter no maximo 4000 caracteres. `customerId` e obrigatorio. Prioridade ausente assume `MEDIUM`; valores permitidos sao `LOW`, `MEDIUM`, `HIGH` e `URGENT`. `status`, `creatorAccountId`, `organizationId`, categoria e campos adicionais retornam `400 Bad Request` sem persistencia.

Em sucesso, API retorna `201 Created`, `Location` para ticket criado e somente `id`, `customerId`, `creatorAccountId`, `assigneeAccountId`, `title`, `description`, `priority`, `status`, `createdAt` e `updatedAt`.

## Isolamento e validacao relacional

Persistencia e autorizacao devem operar na fronteira de `organizationId`. Criador deve ter membership atual na organizacao e qualquer um dos tres papeis fixos pode abrir ticket. Cliente deve ser localizado pela combinacao de `organizationId`, `customerId` e `ACTIVE`.

Quando presente, assignee deve ser localizado pela combinacao de `organizationId` e `assigneeAccountId`; seu papel nao restringe elegibilidade. Organizacao inexistente, criador sem membership, cliente inexistente, cliente de outra organizacao, cliente inativo e assignee sem membership retornam mesmo `404 Not Found` generico. Nenhuma consulta por UUID isolado pode autorizar ou confirmar recurso de outra organizacao.

## Modelo e persistencia

Migration cria `organization_tickets` com:

- `id UUID` chave primaria;
- `organization_id UUID` chave estrangeira para organizacao;
- `customer_id UUID` chave estrangeira para `organization_clients`;
- `creator_account_id UUID` chave estrangeira para conta;
- `assignee_account_id UUID` opcional, chave estrangeira para conta;
- `title VARCHAR(120)`;
- `description VARCHAR(4000)` opcional;
- `priority VARCHAR(16)` restrita a `LOW`, `MEDIUM`, `HIGH`, `URGENT`;
- `status VARCHAR(16)` restrito a `OPEN`;
- `created_at` e `updated_at` em UTC;
- indices para consultas futuras por `organization_id`, `customer_id` e `assignee_account_id`.

Chaves estrangeiras preservam integridade de IDs, mas nao provam que cliente ou assignee pertencem a organizacao. Aplicacao e consultas escopadas devem validar estes vinculos antes de inserir. Esta change nao cria leitura, listagem, alteracao, atribuicao posterior, transicao de status ou exclusao.

## Arquitetura

- dominio `ticket` contem modelo e enums Java puro;
- aplicacao define caso de uso, comando e portas para abertura atomica;
- adapter HTTP valida contrato e usa identidade fornecida pela seguranca, sem acessar repositories;
- adapter de persistencia autoriza criador e valida cliente/assignee na mesma transacao da criacao;
- auditoria persistida nao e ampliada: `critical-action-audit` nao define acao de ticket e esta change nao altera capacidade de auditoria;
- nao ha RBAC configuravel, categoria livre, shared kernel novo ou integracao externa.

## Privacidade e operacao

Titulo e descricao registram demanda de suporte. UI e futuras changes devem orientar usuarios a nao inserir dados pessoais desnecessarios; esta change nao coleta contatos adicionais. Titulo, descricao e payload completo nao devem entrar em logs, metricas, traces ou respostas de erro. Sem acao nova aprovada, ticket nao gera evento na trilha de auditoria persistida atual.

## Fora do escopo

- consulta, listagem, filtros, busca e paginacao;
- categoria, SLA, notificacao, mensagens, anexos e historico;
- alteracao de ticket, prioridade, status ou cliente;
- atribuicao ou reatribuicao posterior;
- portal de cliente e integracoes;
- auditoria persistida de acao de ticket, retencao, exportacao e exclusao de ticket.
