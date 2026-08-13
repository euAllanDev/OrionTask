# Proposal: Consulta e listagem de tickets

## Change ID

`list-and-read-tickets`

## Status

Aprovada para implementacao em 2026-08-13.

## Problema

Organizacoes ja podem abrir tickets, mas membros internos nao conseguem recuperar um ticket conhecido nem consultar fila de tickets. Isso impede acompanhamento operacional sem recorrer ao banco ou ferramentas externas.

## Objetivo

Permitir que membros internos autorizados leiam tickets da organizacao explicita, individualmente ou em lista paginada, com filtros aprovados e isolamento rigoroso.

## Escopo pretendido

- `GET /api/v1/organizations/{organizationId}/tickets`;
- `GET /api/v1/organizations/{organizationId}/tickets/{ticketId}`;
- leitura para `OWNER`, `ADMIN` e `TECHNICIAN` com identidade exclusiva da sessao server-side;
- paginacao e ordenacao deterministica do mais recente para o mais antigo;
- filtros por status, prioridade, `customerId` e `assigneeAccountId`;
- consultas e filtros sempre escopados por `organizationId`;
- `404 Not Found` indistinguivel para organizacao, membership ou ticket inacessivel;
- testes unitarios, HTTP e PostgreSQL/Testcontainers, apos aprovacao.

## Fora do escopo

- abertura, edicao, exclusao, mudanca de status ou fechamento de ticket;
- atribuicao ou reatribuicao;
- mensagens, anexos, historico, notificacoes, portal de cliente e auditoria nova;
- busca textual, dashboards, exportacao, contagens agregadas e filtros configuraveis;
- alteracao de cliente, prioridade, categoria ou dados de ticket;
- frontend.

## Riscos

- vazar ticket, cliente ou assignee de outra organizacao por UUID conhecido ou filtro;
- respostas distintas confirmarem existencia de organizacao, ticket ou membership;
- paginacao instavel causar duplicacao ou omissao entre paginas;
- expor descricao ou identificadores alem do necessario;
- aceitar parametro invalido de modo silencioso e induzir consulta operacional incorreta;
- introduzir filtros ou ordenacoes fora do contrato aprovado.

## Criterio para avancar

Decisoes em `open-questions.md` foram consolidadas. Proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana explicita antes de qualquer codigo.
