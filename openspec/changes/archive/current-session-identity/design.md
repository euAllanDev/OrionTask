# Design: Identidade da sessao atual

## Decisao principal

Adicionar `GET /api/v1/session` como leitura autenticada de identidade minima. Endpoint usa somente principal estabelecido pelo filtro de sessao server-side persistida. Nao recebe corpo, query ou headers de identidade.

## API

| Operacao | Rota | Autenticacao | CSRF |
| --- | --- | --- | --- |
| Consultar sessao atual | `GET /api/v1/session` | sessao server-side persistida | nao exige |

Quando sessao for valida, endpoint retorna `200 OK`:

```json
{
  "accountId": "uuid",
  "email": "user@example.com"
}
```

`accountId` e UUID da conta associada a sessao persistida. `email` e valor normalizado persistido, com trim e lowercase. Resposta nao inclui cookie, token, derivacao, timestamps, organizacao, membership ou papel.

Sessao ausente, expirada ou revogada segue resposta global de autenticacao `401 Unauthorized`, generica e indistinguivel. Endpoint nao diferencia causa, nem valida `accountId` enviado pelo cliente.

## Arquitetura

- modulo `identity` define caso de uso e porta de entrada para identidade atual;
- aplicacao recebe somente `accountId` do principal autenticado;
- porta de saida busca conta pelo UUID da sessao, sem buscar por e-mail fornecido pelo cliente;
- adapter web injeta `@AuthenticationPrincipal UUID` e chama porta de entrada;
- Spring Security e filtro existente continuam responsaveis por validar token opaco, expiracao e revogacao;
- controller nao acessa repository; dominio permanece Java puro.

Conta nao localizada para principal autenticado deve ser tratada como `401` generico, sem retornar identificador parcial. Nao ha alteracao de sessao, atividade, cookies ou contexto CSRF alem do comportamento tecnico atual da autenticacao.

## Privacidade e operacao

E-mail normalizado e retornado somente para identificar conta autenticada na interface. Endpoint nao registra e-mail, corpo, cookie, token ou derivacao em logs, auditoria, metricas, traces ou mensagens de erro. Nao cria evento de auditoria por leitura.

## Fora do escopo

- login, logout, cadastro, renovacao, revogacao ou listagem de sessoes;
- perfil e alteracao de e-mail/senha;
- organizacao ativa, memberships, papeis e permissões;
- frontend, JWT, `HttpSession` como identidade, CSRF novo, migration e auditoria nova.
