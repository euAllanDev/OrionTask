# Organization Access Specification

## Purpose

Definir membership e autorizacao minima por organizacao para recursos futuros.

## Requirements

### Requirement: Papeis fixos de membership

Sistema DEVE aceitar somente `OWNER`, `ADMIN` e `TECHNICIAN`. Papel NAO DEVE ser definido ou confiado a partir do cliente. Sistema NAO DEVE criar RBAC generico ou permissoes configuraveis nesta capacidade.

Os tres papeis DEVEM poder ler organizacao autorizada. `OWNER` e `ADMIN` poderao gerenciar memberships em capacidade futura. Apenas `OWNER` podera transferir ownership em capacidade futura. Toda organizacao existente DEVE possuir ao menos uma membership `OWNER`; nenhuma operacao futura DEVE violar esta invariante.

Migration DEVE preservar memberships `OWNER` existentes, aceitar os tres papeis aprovados e rejeitar outros valores.

### Requirement: Contexto explicito e autorizacao por membership

Todo endpoint organizacional DEVE receber `organizationId` explicito no path. Sistema NAO DEVE manter organizacao ativa em sessao, cookie ou `HttpSession`. Backend DEVE obter `accountId` exclusivamente da sessao server-side persistida e verificar membership existente por ambos `organizationId` e `accountId`.

Sistema NAO DEVE carregar organizacao apenas por `organizationId` e usar leitura isolada como decisao de autorizacao. UUID conhecido NAO DEVE conceder acesso.

### Requirement: Leitura organizacional autorizada

`GET /api/v1/organizations/{organizationId}` DEVE retornar `200 OK` e somente `id`, `name`, `createdAt` e `updatedAt` para membership existente. UUID malformado DEVE retornar `400 Bad Request`.

UUID valido sem organizacao e organizacao existente sem membership DEVEM retornar `404 Not Found` com mesmo status, corpo, formato e sem metadata que revele existencia ou vinculo. Cliente sem sessao autenticada NAO DEVE receber dados e DEVE receber resposta de autenticacao global.

GET NAO DEVE exigir CSRF, nem atualizar organizacao, membership ou timestamp funcional. Atualizacao tecnica de atividade da sessao permanece responsabilidade da autenticacao. Operacoes mutaveis futuras DEVEM exigir CSRF valido.

### Requirement: Isolamento organizacional

Consultas de autorizacao DEVEM incluir `organization_id` e `account_id`. Controller NAO DEVE acessar repositorio diretamente. Erros, logs e respostas NAO DEVEM expor dados de organizacao nao autorizada.
