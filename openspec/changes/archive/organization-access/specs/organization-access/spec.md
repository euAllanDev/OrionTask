# Organization Access Specification

## Purpose

Definir membership e autorizacao minima por organizacao para recursos futuros.

## Requirements

### Requirement: Papeis fixos de membership

Sistema DEVE aceitar somente papeis `OWNER`, `ADMIN` e `TECHNICIAN` em membership. Papel NAO DEVE ser definido por cliente fora de operacao administrativa futura autorizada. Nenhuma rota organizacional DEVE confiar em role enviado pelo cliente. Sistema NAO DEVE criar RBAC generico ou permissoes configuraveis nesta capacidade.

`OWNER`, `ADMIN` e `TECHNICIAN` DEVEM poder ler organizacao autorizada. Apenas `OWNER` e `ADMIN` poderao gerenciar memberships em capacidade futura. Apenas `OWNER` podera transferir ownership em capacidade futura. Toda organizacao existente DEVE possuir ao menos uma membership com papel `OWNER`; nenhuma operacao futura DEVE violar esta invariante.

Matriz define politica canonica para changes futuras. Nesta capacidade, somente leitura de organizacao autorizada e exercida por endpoint HTTP.

#### Scenario: migration preserva owner existente

- DADO membership existente com papel `OWNER`;
- QUANDO migration de papeis for aplicada;
- ENTAO membership DEVE permanecer valida como `OWNER`.

#### Scenario: migration aceita somente papeis aprovados

- DADO schema migrado;
- QUANDO persistir membership com `OWNER`, `ADMIN` ou `TECHNICIAN`;
- ENTAO persistencia DEVE aceitar o papel;
- E QUANDO persistir outro valor;
- ENTAO persistencia DEVE rejeitar a operacao.

### Requirement: Contexto explicito e autorizacao por membership

Todo endpoint organizacional DEVE receber `organizationId` explicito no path. Sistema NAO DEVE manter organizacao ativa em sessao, cookie ou `HttpSession`. Backend DEVE obter `accountId` exclusivamente da sessao server-side persistida e verificar membership existente por ambos `organizationId` e `accountId`. Sistema NAO DEVE carregar organizacao apenas por `organizationId` e usar essa leitura isolada como decisao de autorizacao.

#### Scenario: membro autorizado le organizacao

- DADO conta autenticada com membership existente na organizacao;
- QUANDO enviar `GET /api/v1/organizations/{organizationId}`;
- ENTAO sistema DEVE retornar `200 OK` com dados permitidos da organizacao.

#### Scenario: UUID conhecido sem membership

- DADO conta autenticada sem membership na organizacao alvo;
- QUANDO enviar `GET /api/v1/organizations/{organizationId}`;
- ENTAO sistema DEVE retornar `404 Not Found`;
- E NAO DEVE confirmar existencia da organizacao ou membership de terceiros.

#### Scenario: organizacao inexistente

- DADO conta autenticada;
- QUANDO enviar leitura para UUID valido sem organizacao existente;
- ENTAO sistema DEVE retornar `404 Not Found`;
- E status, corpo e formato DEVEM ser indistinguiveis do caso de organizacao existente sem membership.

#### Scenario: requisicao nao autenticada

- DADO cliente sem sessao autenticada;
- QUANDO solicitar organizacao por UUID valido;
- ENTAO sistema NAO DEVE retornar dados da organizacao;
- E DEVE aplicar resposta de autenticacao definida pela politica global.

### Requirement: Contrato minimo de leitura organizacional

`GET /api/v1/organizations/{organizationId}` DEVE retornar somente `id`, `name`, `createdAt` e `updatedAt` para membership existente. UUID malformado DEVE retornar `400 Bad Request`. Leitura NAO DEVE exigir CSRF e NAO DEVE atualizar organizacao, membership ou timestamp funcional; atualizacao tecnica de atividade da sessao permanece responsabilidade da autenticacao. Operacoes mutaveis futuras DEVEM exigir CSRF valido.

#### Scenario: papel tecnico le organizacao autorizada

- DADO conta autenticada com membership `TECHNICIAN` existente;
- QUANDO enviar leitura para organizacao vinculada;
- ENTAO sistema DEVE retornar `200 OK`.

### Requirement: Isolamento de organizacao

Consultas de autorizacao DEVEM incluir `organization_id` e `account_id`. Sistema NAO DEVE carregar organizacao apenas por `organization_id` e usar essa leitura isolada como decisao de autorizacao. Controller NAO DEVE acessar repositorio diretamente. Identificador de organizacao conhecido NAO DEVE conceder acesso. Erros, logs e respostas NAO DEVEM expor dados de organizacao nao autorizada.
