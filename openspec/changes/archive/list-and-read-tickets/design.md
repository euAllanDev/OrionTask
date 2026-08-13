# Design: Consulta e listagem de tickets

## Decisao principal

Tickets sao recursos internos lidos somente pela fronteira explicita de `organizationId`. `OWNER`, `ADMIN` e `TECHNICIAN` autenticados podem consultar detalhes ou lista de tickets da propria organizacao. Leitura nao exige CSRF e nao altera ticket, membership ou estado funcional.

## API e contrato

| Operacao | Rota | Autoridade |
| --- | --- | --- |
| Listar | `GET /api/v1/organizations/{organizationId}/tickets` | `OWNER`, `ADMIN`, `TECHNICIAN` |
| Consultar | `GET /api/v1/organizations/{organizationId}/tickets/{ticketId}` | `OWNER`, `ADMIN`, `TECHNICIAN` |

Listagem aceita somente `page`, `size`, `status`, `priority`, `customerId` e `assigneeAccountId`, uma vez cada. `page` inicia em zero; `size` padrao e 50 e maximo e 100. Valores ausentes usam padrao; valores malformados, repetidos ou fora dos limites retornam `400 Bad Request`.

`status` aceita somente `OPEN`; `priority` aceita `LOW`, `MEDIUM`, `HIGH` e `URGENT`. `customerId` e `assigneeAccountId` aceitam UUID exato. Ordenacao e fixa: `createdAt DESC`, `id DESC`.

Resposta de lista retorna somente `items`, `page`, `size`, `totalElements` e `totalPages`. Cada item retorna `id`, `title`, `status`, `priority`, `customerId`, `assigneeAccountId`, `createdAt` e `updatedAt`. Detalhe retorna esses campos e tambem `description` e `creatorAccountId`.

## Isolamento e filtros

Backend obtem `accountId` somente da sessao server-side e verifica membership por `organizationId` e `accountId` antes de consultar. Queries de tickets sempre incluem `organization_id`. Ticket conhecido de outra organizacao, ticket inexistente, organizacao inexistente ou membership ausente retornam `404 Not Found` com mesmo corpo e sem metadata de existencia.

Na listagem, `customerId` e `assigneeAccountId` sao criterios de busca escopados. UUID inexistente ou pertencente a outra organizacao retorna lista vazia com `200 OK`, indistinguivel de filtro local sem tickets. Ticket de cliente que se tornou `INACTIVE` continua consultavel e filtravel, pois desativacao nao apaga historico.

## Persistencia e arquitetura

Nenhuma migration e necessaria. Adapter de persistencia usa `organization_tickets`, aplicando filtros opcionais e `count` com exatamente os mesmos predicados organizacionais. Busca individual usa `organization_id` e `id` no mesmo predicado. Pagina usa `LIMIT` e `OFFSET`; ordem e contagem devem ser consistentes com filtros aprovados.

- dominio `ticket` permanece Java puro;
- aplicacao define consultas e portas de entrada/saida;
- adapter HTTP valida query/path e invoca portas, sem repositories;
- adapter de persistencia aplica autorizacao e escopo organizacional;
- nao ha novo RBAC, evento de auditoria, shared kernel, cache ou frontend.

## Privacidade e fora do escopo

Descricao e creator somente aparecem no detalhe autorizado. Resumo nao expõe descricao. Nenhuma resposta inclui nome de cliente, e-mail de conta, dados de sessao ou metadados de rede. Logs, metricas, traces e erros nao devem registrar descricao ou payload completo.

Ficam fora do escopo mensagens, historico de mudancas, mudancas de status, fechamento, atribuicao posterior, busca textual, ordenacao configuravel, agregacoes, exportacao e interface web.
