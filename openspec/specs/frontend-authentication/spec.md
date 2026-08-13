# Frontend Authentication Specification

## Purpose

Definir autenticacao funcional do frontend OrionTask com sessao server-side, CSRF em memoria e protecao basica de rotas.

## Requirements

### Requirement: Estado autenticado reconstruivel e rotas basicas

Frontend MUST reconstruir estado de autenticacao somente por `GET /api/v1/session` com cookies gerenciados pelo browser. Estado em memoria DEVE ser `loading`, `authenticated` com `accountId` e e-mail, ou `unauthenticated`. Frontend NAO DEVE ler, persistir, serializar ou manipular token de sessao; NAO DEVE usar JWT, `localStorage`, `sessionStorage` ou IndexedDB para credenciais.

`/app` DEVE aguardar bootstrap antes de renderizar conteudo protegido e redirecionar estado nao autenticado para `/login`. `200` de sessao atual DEVE definir estado autenticado; `401` DEVE definir estado nao autenticado, limpar CSRF em memoria e retirar pessoa de rota protegida. Falha diferente de `401` NAO DEVE assumir autenticacao.

#### Scenario: sessao expirada retorna ao login

- DADO pessoa em `/app` com sessao expirada;
- QUANDO bootstrap ou consulta autenticada receber `401`;
- ENTAO frontend DEVE limpar estado autenticado e CSRF em memoria;
- E DEVE redirecionar para `/login` sem expor dados protegidos.

### Requirement: Login por sessao server-side

Formulario `/login` MUST aceitar somente e-mail e senha, impedir submit duplicado e exigir CSRF antes de `POST /api/v1/sessions`. Frontend DEVE enviar credenciais somente ao backend, nunca receber token de sessao em JSON, armazenar senha alem do envio necessario ou inferir autenticacao sem consultar `GET /api/v1/session` depois de `204`.

Depois de login `204`, frontend DEVE limpar CSRF em memoria, consultar sessao atual e somente navegar para `/app` apos `200`. `401` DEVE mostrar exatamente `E-mail ou senha inválidos.` sem distinguir conta inexistente de senha incorreta. `429` e falhas inesperadas DEVEM usar mensagem generica. Campos, loading, erro e foco DEVEM ser acessiveis.

#### Scenario: login valido reconstrui identidade

- DADO formulario valido e CSRF em memoria;
- QUANDO backend retornar `204` no login e `200` na sessao atual;
- ENTAO frontend DEVE definir estado autenticado com resposta da sessao;
- E DEVE navegar para `/app` sem manipular cookie de sessao.

### Requirement: Cadastro sem autenticacao implicita

Formulario `/register` MUST enviar somente `{ email, password }` a `POST /api/v1/accounts` e NAO DEVE exigir CSRF. `202` DEVE apresentar mensagem neutra orientando login, sem definir estado autenticado, criar organizacao, manter senha, confirmar conta existente ou redirecionar automaticamente.

`400` DEVE produzir mensagem generica de dados invalidos e `429` mensagem generica de indisponibilidade temporaria. Frontend NAO DEVE expor ou inferir existencia de e-mail.

#### Scenario: cadastro aceito nao cria sessao frontend

- DADO formulario valido;
- QUANDO backend retornar `202`;
- ENTAO frontend DEVE orientar pessoa a entrar;
- E DEVE manter estado nao autenticado e nao persistir senha.

### Requirement: Logout e CSRF sem retry generico

Logout MUST obter CSRF em memoria e chamar `DELETE /api/v1/session`. Em `204`, frontend DEVE limpar somente estado em memoria e CSRF em memoria, depois navegar para `/login`; frontend NAO DEVE apagar cookie manualmente.

Camada HTTP NAO DEVE repetir automaticamente mutacao que recebe `403`. Sem sinal backend explicito de rejeicao CSRF, `403` PODE limpar CSRF em memoria, mas DEVE retornar resposta ao fluxo sem novo envio automatico. Cadastro NAO DEVE buscar ou enviar CSRF; login e logout DEVEM renovar ou invalidar CSRF conforme contrato de sessao.

#### Scenario: `403` nao repete logout

- DADO logout iniciado com CSRF em memoria;
- QUANDO backend retornar `403`;
- ENTAO frontend DEVE realizar somente uma requisicao de logout;
- E DEVE apresentar erro generico sem inferir causa.

### Requirement: Area autenticada neutra e qualidade verificavel

`/app` autenticado MUST mostrar somente shell neutro sem organizacao, clientes, membros, tickets, dashboard, graficos ou dados mockados. Conteudo PODE identificar conta autenticada pelo e-mail retornado na sessao e DEVE informar que escolha ou criacao de organizacao entra em change posterior. Frontend NAO DEVE implementar autorizacao por papel nesta capacidade.

Testes Vitest e React Testing Library MUST cobrir login, cadastro, logout, bootstrap de sessao, sessao expirada, CSRF, `403` sem retry, loading, submit duplicado, erros API e ausencia de persistencia de credenciais. `npm run check` e `npm run build` DEVEM passar antes de archive.

#### Scenario: shell neutro nao inventa organizacao

- DADO sessao atual autenticada;
- QUANDO pessoa navegar para `/app`;
- ENTAO frontend DEVE mostrar shell estrutural neutro;
- E NAO DEVE listar, selecionar ou criar organizacao.
