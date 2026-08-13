# Open Questions: Frontend authentication

## Diagnostico de contratos backend

| Questao | Contrato verificado | Fonte |
| --- | --- | --- |
| Cadastro | `POST /api/v1/accounts`, corpo estrito `{ "email", "password" }`; `202` para entrada aceita, inclusive e-mail ja existente; `400` para entrada invalida; `429` para rate limit. Nao cria sessao. | `AccountRegistrationController`, `account-creation` |
| Login | `POST /api/v1/sessions`, corpo estrito `{ "email", "password" }`; `204` com cookies de sessao em sucesso; `401` com `{ "message": "E-mail ou senha inválidos." }`; `429` com `Retry-After: 900`. | `SessionAuthenticationController`, `session-authentication` |
| Logout | `DELETE /api/v1/session`; CSRF obrigatorio; `204` idempotente, mesmo sem sessao; backend revoga sessao apresentada e expira cookies. | `SessionAuthenticationController`, `session-authentication` |
| CSRF | `GET /api/v1/csrf`, publico, retorna `{ "token": "..." }`; token pertence ao contexto tecnico e nao autentica conta. | `CsrfController`, `session-authentication` |
| Sessao atual | `GET /api/v1/session`; `200` com `{ "accountId", "email" }` da sessao valida; `401` generico para sessao ausente, expirada, revogada ou conta ausente. | `session-authentication`, `current-session-identity` arquivada |
| Cadastro cria sessao | Nao. Endpoint retorna `202` sem `Set-Cookie`; service somente cria conta quando ausente. | `AccountRegistrationController`, `RegisterAccountService` |
| Login rotaciona CSRF/sessao tecnica | Sim. Backend limpa token CSRF, chama `request.changeSessionId()` e envia novo `JSESSIONID`; frontend deve limpar CSRF em memoria apos sucesso. | `SessionAuthenticationController:69-76` |
| Logout invalida CSRF | Sim. Backend limpa token CSRF, invalida `HttpSession` e expira `JSESSIONID`; frontend deve limpar CSRF em memoria apos `204`. | `SessionAuthenticationController:79-93` |
| `403` CSRF | Backend limpa token CSRF quando excecao e `CsrfException`, mas retorna `403` sem sinal especifico para cliente distinguir CSRF de autorizacao. | `SecurityConfiguration:27-35` |

## Q1 - Identidade da sessao atual

Como frontend reconstrui estado autenticado apos recarga sem ler cookie HttpOnly?

Opcoes:

- criar change backend separada para endpoint autenticado, por exemplo `GET /api/v1/session`, que retorna somente identidade minima aprovada;
- ampliar `frontend-auth` para incluir contrato e implementacao backend, mediante aprovacao explicita de escopo coordenado;
- nao oferecer protecao de rota nem estado reconstruivel nesta change.

Decisao: `current-session-identity` foi arquivada. Frontend reconstrui estado autenticado por `GET /api/v1/session`; resposta autenticada contem `{ "accountId": "uuid", "email": "user@example.com" }`, com e-mail normalizado. `401` generico representa estado nao autenticado.

## Q2 - Escopo da correcao de retry `403`

`apiFetch` atual limpa CSRF e repete qualquer mutacao com `403`. Como corrigir sem assumir sinal backend inexistente?

Opcoes:

- incluir na `frontend-auth` remocao do retry generico; cada fluxo trata erro real e solicita CSRF novamente somente em acao posterior do usuario;
- criar change tecnica separada para camada HTTP;
- criar sinal explicito de CSRF no backend, por change coordenada, e repetir somente com esse sinal.

Decisao: remover retry generico de `403` em `frontend-auth`. Sem sinal explicito de CSRF, frontend NAO DEVE repetir mutacao automaticamente; pode limpar token CSRF em memoria e apresentar erro sem inferir causa.

## Q3 - Destino apos login

Sem endpoint de identidade atual ou listagem de organizacoes, qual area autenticada minima aparece apos login?

Opcoes:

- shell neutro em `/app` com mensagem estrutural, sem dados ou organizacoes inventadas;
- permanecer em `/login` com confirmacao visual;
- adiar redirecionamento ate change de organizacoes.

Decisao: shell neutro em `/app`, condicionado ao endpoint de sessao atual. Conteudo somente informa que selecao/criacao de organizacao entra em change posterior; nao lista dados nem simula organizacoes.

## Q4 - Validacao de formularios

Adicionar React Hook Form ou Zod?

Opcoes:

- validacao local simples com estado React e tipos existentes;
- adicionar React Hook Form e Zod;
- adicionar somente uma das bibliotecas.

Decisao: validacao local simples. Formularios possuem dois campos, contrato pequeno e validacao primaria continua no backend. Nao adicionar dependencia nesta change.

## Q5 - Tratamento de erros de cadastro

Como apresentar `202`, `400` e `429` sem quebrar anti-enumeracao?

Opcoes:

- `202`: mensagem neutra orientando login; `400`: validar localmente e mensagem generica; `429`: mensagem temporaria generica, opcionalmente usando `Retry-After` sem expor identificador;
- diferenciar e-mail ja existente de conta criada;
- redirecionar automaticamente para login em qualquer resposta.

Decisao: `202` mostra mensagem neutra orientando login; `400` mostra erro generico de dados; `429` mostra indisponibilidade temporaria sem inferir identificador. Nunca inferir conta existente. Cadastro nao autentica; sucesso orienta login sem persistir senha.

## Decisoes fechadas

- `current-session-identity` foi arquivada e desbloqueia descoberta de sessao, protecao basica de rota e estado reconstruivel.
- `frontend-auth` remove retry automatico generico de `403`.
