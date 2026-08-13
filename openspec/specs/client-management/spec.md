# Client Management Specification

## Purpose

Definir cadastro, consulta, edicao e desativacao de clientes internos por organizacao.

## Requirements

### Requirement: Cliente organizacional minimo

Sistema DEVE persistir cliente com UUID nao sequencial, `organization_id`, nome, status `ACTIVE` ou `INACTIVE` e timestamps UTC. Cliente DEVE coletar somente nome nesta capacidade. Nome DEVE ser obrigatorio, nao vazio apos trim e ter entre 1 e 120 caracteres. Sistema NAO DEVE exigir nem coletar e-mail, telefone, documento, endereco, conta ou credencial de cliente.

Nome NAO DEVE possuir unicidade global ou por organizacao. Cliente DEVE pertencer a uma unica organizacao e consultas DEVEM incluir `organization_id`.

### Requirement: Gestao autorizada de clientes

`POST /api/v1/organizations/{organizationId}/clients`, `PATCH /api/v1/organizations/{organizationId}/clients/{clientId}` e `DELETE /api/v1/organizations/{organizationId}/clients/{clientId}` DEVEM exigir sessao server-side persistida e CSRF valido. `OWNER` e `ADMIN` DEVEM poder executar essas mutacoes; `TECHNICIAN` DEVE receber `403 Forbidden` sem modificar cliente.

Criacao e edicao DEVEM aceitar exclusivamente `{ "name": "..." }`. Campos adicionais, corpo ausente, UUID malformado ou nome invalido DEVEM retornar `400 Bad Request` sem persistir dados. Criacao DEVE retornar `201 Created`; edicao DEVE retornar `200 OK`; desativacao efetiva DEVE retornar `204 No Content`.

### Requirement: Consulta e isolamento de clientes

`GET /api/v1/organizations/{organizationId}/clients/{clientId}` e `GET /api/v1/organizations/{organizationId}/clients` DEVEM exigir sessao autenticada, mas NAO CSRF. Todos memberships DEVEM poder ler clientes da propria organizacao. Listagem sem filtro DEVE retornar somente clientes `ACTIVE`; `status=active`, `status=inactive` e `status=all` DEVEM ser aceitos. Filtro invalido DEVE retornar `400 Bad Request`.

Organizacao inexistente, membership ausente e cliente inexistente ou pertencente a outra organizacao DEVEM retornar `404 Not Found` com mesmo status, corpo e sem metadata que revele existencia. Controller NAO DEVE acessar repository diretamente.

### Requirement: Desativacao preserva cliente

Desativacao DEVE alterar somente cliente alvo da organizacao para `INACTIVE`, definir `deactivatedAt` e preservar demais dados e identificador. Sistema NAO DEVE excluir fisicamente nem reativar cliente nesta capacidade. Cliente inativo DEVE continuar consultavel por membro autorizado e aparecer somente em listagem com filtro `inactive` ou `all`.

### Requirement: Evidencia operacional minima

Depois de commit bem-sucedido, criar, editar e desativar cliente DEVEM registrar, respectivamente, `organization.client_created`, `organization.client_updated` e `organization.client_deactivated`. Eventos DEVEM conter somente `organizationId`, `clientId` e UUID da conta autora. Eventos, logs, metricas, traces e erros NAO DEVEM conter nome ou payload completo. Falha de log apos commit NAO DEVE causar rollback.
