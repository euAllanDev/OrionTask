# Open Questions: Consulta e listagem de tickets

## Q1 - Campos retornados

Decisao: listagem retorna somente `id`, `title`, `status`, `priority`, `customerId`, `assigneeAccountId`, `createdAt` e `updatedAt`. Leitura individual retorna tambem `description` e `creatorAccountId`. Nenhuma resposta inclui nome de cliente ou e-mail de conta.

## Q2 - Formato de paginacao

Decisao: paginacao por offset. `page` inicia em zero; `size` padrao e 50 e maximo e 100. Resposta retorna `items`, `page`, `size`, `totalElements` e `totalPages`. Valores invalidos de paginacao retornam `400 Bad Request`.

## Q3 - Ordenacao e desempate

Decisao: ordem fixa `createdAt DESC`, `id DESC`, sem parametro de ordenacao.

## Q4 - Filtros de status e prioridade

Decisao: `status` e `priority` aceitam somente um valor exato por filtro. Valor desconhecido ou repetido retorna `400 Bad Request`; filtro ausente retorna todos os valores. `status` aceita somente `OPEN`; `priority` aceita `LOW`, `MEDIUM`, `HIGH` e `URGENT`.

## Q5 - Filtros de cliente e assignee

Decisao: `customerId` e `assigneeAccountId` sao UUIDs opcionais e exatos, sempre escopados por `organizationId`. UUID malformado ou repetido retorna `400 Bad Request`. Nao ha valor especial para ticket sem assignee nesta change.

## Q6 - Filtro de recurso de outra organizacao

Decisao: filtro com `customerId` ou `assigneeAccountId` inexistente ou de outra organizacao retorna `200 OK` com `items` vazio. Filtro e criterio de busca, nao mecanismo de enumeracao. `404` continua reservado para organizacao, membership ou leitura individual de ticket.

## Q7 - Cliente inativo em ticket historico

Decisao: ticket associado a cliente desativado permanece visivel em leitura e listagem, inclusive por `customerId`. Desativacao impede abertura nova, sem apagar historico operacional.

## Q8 - Resposta para ticket individual

Decisao: `ticketId` malformado retorna `400 Bad Request`. Ticket inexistente ou de outra organizacao retorna `404 Not Found` com corpo identico. Membership ausente na organizacao retorna mesmo `404` generico.
