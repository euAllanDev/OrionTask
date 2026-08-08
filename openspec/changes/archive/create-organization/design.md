# Design: Criacao de organizacao

## Decisao principal

Uma conta autenticada pode criar organizacoes. Cada criacao persiste uma organizacao minima e uma membership inicial de papel fixo `OWNER` na mesma transacao. A autoridade do criador vem somente do contexto derivado da sessao server-side do OrionTask; `HttpSession`, `JSESSIONID` e campos enviados pelo cliente nao sao fontes de identidade.

## Dados e privacidade

`Organization` contem `id`, `name`, `createdAt` e `updatedAt`. `id` e UUID nao sequencial; timestamps usam UTC. `name` e unico dado descritivo, necessario para identificar e exibir organizacao no produto. Ele pode ser dado pessoal de profissional autonomo e nao deve aparecer em logs ou evento operacional.

`Membership` contem `id`, `organizationId`, `accountId`, `role`, `createdAt` e `updatedAt`. Nesta change, `role` so aceita `OWNER`. A base deve impor chave estrangeira para organizacao e conta e unicidade composta de `account_id` e `organization_id`. Nao existe `owner_account_id` em `Organization`.

`name` deve ser mantido enquanto organizacao existir e for necessario para identifica-la e opera-la no OrionTask. Esta change nao define eliminacao, anonimização ou retencao apos encerramento; essas politicas pertencem a change especifica. Esta capacidade nao cria soft delete, exclusao ou dado fiscal.

## Validacao e contrato HTTP

`POST /api/v1/organizations` recebe exclusivamente objeto JSON `{ "name": "..." }`. `name` deve ser texto nao vazio apos remover espacos nas extremidades e ter de 1 a 120 caracteres. Campos ausentes, nulos, nao textuais, vazios ou adicionais devem ser rejeitados com `400 Bad Request`, sem persistencia.

Com sessao autenticada e CSRF valido, sucesso retorna `201 Created`, `Location: /api/v1/organizations/{organizationId}` e corpo com `id`, `name` e `createdAt`. O UUID e identificador tecnico exposto; nao existe slug ou identificador publico adicional.

Requisicao sem autenticacao nao cria recursos e recebe resposta de autenticacao definida pela configuracao de seguranca. Requisicao mutavel sem CSRF valido e rejeitada pelo mecanismo CSRF existente. Erros nao revelam dados de outras organizacoes ou detalhes internos.

## Fluxo

1. Spring Security valida CSRF e filtro de sessao recupera `accountId` somente da sessao persistida ativa.
2. Adapter HTTP valida corpo estrito e chama caso de uso com nome validado e `accountId` do contexto autenticado.
3. Aplicacao cria `Organization` e membership `OWNER` para mesma conta em uma transacao.
4. Adapter de persistencia grava ambos, respeitando FKs e unicidade composta.
5. Apos commit, aplicacao registra evento operacional minimo `organization.created` com UUIDs de conta e organizacao.
6. Adapter HTTP retorna resposta de criacao.

Evento operacional ocorre somente apos commit para nao produzir evidencia de criacao revertida. Falha durante persistencia faz rollback de organizacao e membership. Falha no log operacional nao deve reverter criacao ja confirmada; logs nao constituem trilha de auditoria persistida.

## Arquitetura

- `organization.domain` usa Java puro para `Organization`, `Membership` e papel `OWNER`;
- `organization.application` define caso de uso e ports para persistencia e evento operacional;
- `organization.adapter.in.web` extrai somente identidade autenticada, valida HTTP e invoca caso de uso;
- `organization.adapter.out.persistence` contem entidades JPA, repositorios e implementacao transacional;
- adapter HTTP nao acessa repositorios; dominio nao depende de Spring, JPA, HTTP ou identidade;
- integracao com `identity` ocorre somente por contexto autenticado, sem acessar detalhes internos do modulo.

## Seguranca e isolamento

Criacao e autenticada; nenhum identificador de conta enviado pelo cliente pode escolher criador. Nomes duplicados sao permitidos, portanto resposta nao confirma existencia por unicidade. Esta change nao consulta organizacoes de terceiros. Modelo e constraints estabelecem membership como fronteira para autorizacao futura; recursos organizacionais futuros deverao usar `organization_id` e filtros/autorizacao por tenant.

Nenhum limitador novo sera introduzido sem requisito mensuravel. Endpoint exige sessao valida, CSRF e controles existentes de autenticacao; necessidade de limites por conta para criacao de organizacao deve ser reavaliada em change futura se houver evidencia de abuso.

## Evento operacional

Evento estruturado minimo contem `event=organization.created`, `accountId` e `organizationId`. Nao inclui nome, e-mail, payload, cookies, tokens, credenciais ou detalhes de erro. Nao ha auditoria persistida, consulta de eventos ou politica de retencao nesta change.

## Fora do escopo

- listar, consultar, editar, desativar ou excluir organizacoes;
- membros adicionais, convites, papeis adicionais, remocao ou transferencia de ownership;
- selecao ou troca de organizacao ativa;
- clientes, tickets, anexos, notificacoes, faturamento e portal do cliente;
- slug, identificador fiscal, endereco, telefone e outros dados organizacionais;
- audit trail persistida e Row-Level Security.
