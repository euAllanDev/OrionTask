# Proposal: Autenticação e encerramento de sessão

## Change ID

`authenticate-and-logout`

## Status

Aprovada para implementação em 2026-08-04.

## Problema

Contas do OrionTask já podem ser cadastradas, mas não conseguem provar sua identidade nem encerrar acessos autenticados. O OrionTask precisa de autenticação própria e sessões revogáveis antes das capacidades organizacionais.

## Objetivo

Permitir login por e-mail e senha local, criar uma sessão server-side própria, segura e revogável, identificar internamente a conta autenticada exclusivamente por essa sessão e encerrar somente o acesso atual sem expor dados sobre contas existentes.

## Escopo pretendido

- autenticar conta por e-mail e senha local;
- usar exclusivamente sessão server-side própria, persistida no PostgreSQL, associada a uma conta e identificada por token opaco;
- persistir somente uma representação criptográfica derivada e única do token, nunca o token bruto;
- criar novo token opaco, nova sessão persistida e novo contexto autenticado a cada login bem-sucedido;
- transportar o token somente no cookie `__Host-oriontask-session`, seguro e inacessível ao JavaScript;
- aplicar expiração por inatividade e duração absoluta sem renovar a duração absoluta por atividade;
- disponibilizar internamente o `accountId` autenticado para adapters e casos de uso protegidos, exclusivamente a partir da sessão persistida;
- revogar somente a sessão apresentada no logout idempotente;
- aplicar proteção CSRF a login, logout e operações autenticadas mutáveis, com emissão inicial em `GET /api/v1/csrf`;
- separar o estado técnico eventualmente usado pelo CSRF da sessão de autenticação do OrionTask;
- limitar tentativas por identificador protegido derivado do e-mail normalizado e por origem confiável;
- retornar resposta uniforme para e-mail inexistente e senha incorreta;
- registrar auditoria mínima sem dados sensíveis;
- incluir testes de autenticação, CSRF, expiração, revogação, rate limiting e isolamento entre sessões.

## Fora do escopo

- criação de organização, membership, papéis e autorização organizacional;
- confirmação de e-mail;
- recuperação ou alteração de senha;
- alteração de e-mail;
- MFA;
- revogação de todas as sessões da conta;
- listagem e gerenciamento de dispositivos ou sessões;
- tokens JWT autocontidos;
- login social, sem senha ou portal do cliente;
- convites e notificações por e-mail;
- endpoint público de perfil ou sessão, salvo decisão explícita em change posterior.

## Riscos

- sequestro ou fixação de sessão;
- enumeração de contas por resposta ou tempo de login;
- CSRF em autenticação por cookie;
- sessões que continuem válidas após logout;
- inconsistência entre a sessão de autenticação e o estado técnico do CSRF;
- amplificação de escrita causada pela atualização de atividade da sessão;
- confiança indevida em headers de proxy enviados pelo cliente;
- exposição acidental do token em logs, traces ou mensagens de erro;
- reutilização de contexto técnico anterior ao login.

## Critério para avançar

Os artefatos desta change devem receber revisão e aprovação humana antes da implementação.
