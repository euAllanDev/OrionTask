# Design: Acesso organizacional

## Decisao principal

Toda rota organizacional recebe `organizationId` no path. Backend combina UUID da rota, `accountId` da sessao server-side persistida e membership existente para provar acesso. UUID conhecido nunca basta. Nao existe organizacao ativa em sessao, cookie ou `HttpSession`.

## Membership e papeis

Membership existente evolui de papel unico `OWNER` para enum fixo `OWNER`, `ADMIN` e `TECHNICIAN`. Migration altera constraint de papel sem modificar memberships existentes. Nesta capacidade, membership existente concede acesso conforme papel enquanto registro existir; convite, expiracao, revogacao e remocao ficam fora do escopo.

Matriz aprovada:

| Capacidade | OWNER | ADMIN | TECHNICIAN |
| --- | --- | --- | --- |
| Ler organizacao autorizada | Sim | Sim | Sim |
| Gerenciar memberships futuros | Sim | Sim | Nao |
| Transferir ownership futuro | Sim | Nao | Nao |
| Alterar ou remover ultimo `OWNER` | Nunca | Nunca | Nunca |

Nenhuma operacao desta change modifica memberships. Invariante normativa prepara lifecycle futuro: toda organizacao existente deve manter ao menos uma membership com papel `OWNER`.

Matriz define politica canonica para changes futuras. Nesta change, somente capacidade de ler organizacao autorizada e exercida por endpoint HTTP.

## Autorizacao e HTTP

`GET /api/v1/organizations/{organizationId}` exige sessao autenticada e aceita somente UUID valido no path. Adapter HTTP obtem `accountId` do contexto Spring Security, derivado exclusivamente da sessao persistida. Aplicacao consulta membership por `organizationId` e `accountId`; somente membership existente permite leitura.

Sucesso retorna `200 OK` e somente `id`, `name`, `createdAt` e `updatedAt`. Organizacao inexistente e UUID valido sem membership retornam `404 Not Found` com mesmo status, corpo, formato e sem metadata que revele existencia ou vinculo. UUID malformado retorna `400 Bad Request`. CSRF nao e exigido para GET; leitura nao atualiza organizacao, membership ou timestamp funcional. Atualizacao tecnica de atividade da sessao permanece responsabilidade da autenticacao. Operacoes mutaveis futuras devem usar CSRF existente.

## Arquitetura

- dominio `organization` mantem modelos e enum de papeis em Java puro;
- aplicacao define port para localizar organizacao autorizada pela dupla `organizationId` e `accountId`;
- adapter HTTP nao acessa repositorio e nao aceita `accountId` do cliente;
- adapter de persistencia executa consulta com ambos identificadores, nunca busca organizacao isoladamente para autorizar;
- Spring Security continua responsavel apenas por autenticar sessao e disponibilizar `accountId` da requisicao.

Consulta composta e restricao de acesso pertencem ao backend. Cliente nao decide papel, membership ou organizacao autorizada; nenhuma rota organizacional confia em role enviado pelo cliente.

## Dados, logs e privacidade

Migration versionada altera constraint de role para os tres valores aprovados. Nenhuma nova categoria de dado pessoal e coletada. Logs, erros e respostas nao devem conter dados de organizacao ou membership nao autorizadas. Leitura autorizada nao gera evento operacional; audit trail persistida continua fora do escopo.

## Fora do escopo

- criar, listar, editar, remover, revogar ou transferir memberships;
- convidar ou aceitar convite;
- alterar organizacao ou selecionar contexto ativo;
- clientes, tickets e demais recursos organizacionais;
- RBAC generico, permissao configuravel e Row-Level Security.
