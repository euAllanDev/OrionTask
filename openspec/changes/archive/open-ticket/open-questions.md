# Open Questions: Abertura de ticket

## Q1 - Campos minimos

Decisao: `title` obrigatorio, normalizado por trim, entre 1 e 120 caracteres; `description` opcional, com no maximo 4000 caracteres quando presente. Nao ha campos de contato do solicitante.

## Q2 - Criador

Decisao: membro interno autenticado abre em nome do cliente. `creatorAccountId` vem exclusivamente da sessao server-side persistida. Portal de cliente e integracoes permanecem fora do escopo.

## Q3 - Cliente associado

Decisao: `customerId` e obrigatorio e deve identificar cliente da mesma organizacao.

## Q4 - Cliente inativo

Decisao: cliente `INACTIVE` nao recebe ticket novo. Resposta e `404 Not Found` generica, sem criar ticket, para nao confirmar status ou vinculo fora da fronteira organizacional.

## Q5 - Prioridade

Decisao: prioridade aceita somente `LOW`, `MEDIUM`, `HIGH` e `URGENT`; quando ausente, valor persistido e `MEDIUM`. `URGENT` nao implica SLA, escalonamento ou notificacao nesta change.

## Q6 - Categoria

Decisao: ticket nao possui campo de categoria nesta change. Texto livre e taxonomia fixa ficam fora do escopo.

## Q7 - Status inicial

Decisao: status inicial e `OPEN`, definido exclusivamente pelo servidor. Nao ha transicoes nesta change.

## Q8 - Atribuicao inicial

Decisao: `assigneeAccountId` e opcional. Quando informado, deve ser UUID de conta com membership atual na mesma organizacao; qualquer papel de membership e elegivel. Atribuicao posterior e regras de carga permanecem fora do escopo.

## Q9 - Autoridade por papel

Decisao: `OWNER`, `ADMIN` e `TECHNICIAN` podem abrir ticket.

## Q10 - Isolamento e anti-enumeracao

Decisao: organizacao inexistente, membership ausente, cliente ausente, cliente de outra organizacao, cliente inativo e assignee sem membership na organizacao retornam `404 Not Found` indistinguivel. UUID conhecido nunca atravessa fronteira de `organizationId`.
