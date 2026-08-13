# Design: Frontend organization shell

## Decision

`/app` permanece rota protegida por `AppGuard`. Depois de bootstrap autenticado, componente cliente busca `GET /api/v1/organizations` com `apiFetch` e cookies do browser. Resposta valida e somente array de itens abaixo define estado local da tela:

```ts
type Organization = {
  id: string;
  name: string;
  createdAt: string;
  updatedAt: string;
  role: "OWNER" | "ADMIN" | "TECHNICIAN";
};

type OrganizationListState =
  | { status: "loading" }
  | { status: "empty" }
  | { status: "ready"; organizations: Organization[] }
  | { status: "error" };
```

Estado e efemero por tela. Nenhuma organizacao ativa e salva em provider, storage, cookie ou URL auxiliar. `organizationId` no pathname e unica fonte de contexto para rota organizacional; lista serve somente para navegacao e nunca autoriza acesso.

## Routes

| Route | Behavior |
| --- | --- |
| `/app` | lista organizacoes acessiveis com loading, empty, error ou ready; logout disponivel |
| `/organizations/[organizationId]` | rota protegida; renderiza shell com organizacao atual obtida pela lista atual e conteudo estrutural sem capacidade de negocio |

Cada organizacao pronta e link para `/organizations/[organizationId]`. Shell recebe contexto por pathname, destaca item correspondente e preserva navegacao por URL em recarga e link direto. Quando item da URL nao estiver na lista atual, shell mostra `Recurso indisponível.`; nao revela se UUID existe nem tenta tratar lista como autorizacao backend.

## HTTP And Errors

Leitura nao exige CSRF. `401` em carregamento autenticado deve usar fluxo atual de sessao expirada: limpar estado em memoria e redirecionar para `/login`. `404` para recurso organizacional usa `responseError(response, true)` e mostra exatamente `Recurso indisponível.`. Outros erros mostram mensagem generica e botao para nova tentativa; nao expor status, corpo ou detalhes internos.

Resposta da lista deve ser array com somente `id`, `name`, `createdAt`, `updatedAt`, `role`; entrada invalida produz erro generico sem renderizar dados parciais. Papel pode ser apresentado como metadado da organizacao, sem controlar autorizacao de UI.

## Shell

Reaproveitar `AppShell` existente e ajusta-lo para receber organizacoes, organizacao atual e callback/logout sem criar dados. Header exibe marca, contexto atual e saida. Sidebar mostra somente organizacoes acessiveis e navega pelos links; layout deve funcionar desktop e mobile, manter foco e respeitar reduced motion. Conteudo organizacional permanece aviso estrutural, sem links para clientes, membros ou tickets ainda indisponiveis.

## Tests

Vitest e React Testing Library devem cobrir carregamento, lista pronta, lista vazia, erro e nova tentativa, link correto, destaque da organizacao da URL, `404` neutro, `401` para login e logout dentro do shell. `npm run check` e `npm run build` sao gates finais.
