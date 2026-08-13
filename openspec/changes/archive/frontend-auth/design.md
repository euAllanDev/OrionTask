# Design: Frontend authentication

## Decisao principal

Frontend usa sessao server-side existente como unica fonte de autenticacao. Browser envia cookie `HttpOnly` por mesma origem; frontend nunca le, serializa, persiste ou manipula cookie de sessao. Estado React em memoria e derivado exclusivamente de `GET /api/v1/session`.

```ts
type AuthState =
  | { status: "loading" }
  | { status: "authenticated"; account: { accountId: string; email: string } }
  | { status: "unauthenticated" };
```

## Rotas e protecao

| Rota | Comportamento |
| --- | --- |
| `/login` | formulario de login; redireciona para `/app` se sessao atual for autenticada |
| `/register` | formulario de cadastro; nao cria sessao; sucesso orienta login |
| `/app` | shell neutro protegido pelo estado de sessao; sem organizacao, dados ou navegacao funcional |

Provider cliente inicializa em `loading` e chama `GET /api/v1/session` com `credentials: include`. `200` define estado autenticado; `401` define nao autenticado; outra falha apresenta estado de erro recuperavel sem assumir autenticacao. Guard cliente nao renderiza conteudo protegido enquanto carrega e redireciona estado nao autenticado de `/app` para `/login`. Esta protecao melhora UX; backend permanece unica autoridade.

Shell `/app` apenas identifica conta autenticada por e-mail e informa que escolha/criacao de organizacao entra em change posterior. Nao lista organizacoes, nao cria organizacao e nao reutiliza shell organizacional existente.

## Formularios e contratos

Validacao local simples, sem React Hook Form ou Zod. Campos usam `autocomplete`, labels, foco visivel, mensagens acessiveis e bloqueiam submit duplicado enquanto requisicao estiver pendente. Senha permanece somente no estado de formulario pelo tempo necessario para envio, e e limpa apos sucesso ou quando apropriado; nunca entra em logs, storage ou URL.

Cadastro chama `POST /api/v1/accounts` com corpo estrito `{ email, password }`, sem CSRF, pois backend isenta rota. `202` apresenta mensagem neutra e link para `/login`; nao define estado autenticado nem redireciona automaticamente. `400` e `429` usam mensagens genericas sem confirmar e-mail existente.

Login primeiro obtém CSRF por `GET /api/v1/csrf`, depois chama `POST /api/v1/sessions` com `{ email, password }` e `X-CSRF-TOKEN`. `204` limpa CSRF em memoria porque backend rotaciona sessao tecnica e invalida token anterior; em seguida chama `GET /api/v1/session`. Somente resposta `200` desse endpoint define estado autenticado e permite navegar para `/app`. `401` mostra exatamente `E-mail ou senha inválidos.`; `429` mostra indisponibilidade temporaria sem inferir motivo.

Logout chama `DELETE /api/v1/session` com CSRF. Em `204`, limpa somente estado em memoria e CSRF em memoria, redirecionando para `/login`; nao remove cookie manualmente. `401` de descoberta de sessao ou resposta autenticada que indique sessao perdida muda estado para nao autenticado, limpa CSRF em memoria e redireciona rota protegida para `/login`.

## CSRF e HTTP

CSRF continua somente em memoria. Camada HTTP deve permitir mutacao com ou sem CSRF: login e logout exigem; cadastro nao. Login e logout sempre invalidam token em memoria apos sucesso. `403` nunca provoca retry automatico. Sem sinal backend explicito de rejeicao CSRF, cliente pode limpar token em memoria depois de `403`, mas deve retornar resposta ao fluxo para mensagem generica e aguardar nova acao explicita da pessoa.

`apiFetch` continua usando `credentials: include`, mas remove logica de repetir automaticamente mutacao. Nenhum token de sessao, CSRF ou credencial pode usar JWT, `localStorage`, `sessionStorage`, IndexedDB ou URL.

## Componentes e testes

Criar somente componentes necessarios sob `features/auth`: provider/guard, API tipada, `LoginForm`, `RegisterForm` e estados visuais. Motion pode sinalizar transicao curta de estado com `prefers-reduced-motion`; CSS atende demais feedbacks. Sem bibliotecas novas.

Vitest e React Testing Library cobrem renderizacao, submit valido, credenciais invalidas, loading, bloqueio de submit duplicado, cadastro, logout, bootstrap autenticado/nao autenticado, sessao expirada, CSRF, `403` sem retry, erros API e ausencia de persistencia de credenciais. `npm run check` permanece gate; `npm run build` deve executar na verificacao final.

## Fora do escopo

Organizacoes, clientes, membros, convites, tickets, dashboard, dados mockados, autorizacao por papel no frontend, confirmacao de e-mail, recuperacao de senha, MFA, Playwright, JWT e alteracoes backend permanecem fora do escopo.
