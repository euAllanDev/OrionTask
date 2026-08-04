# Decisões de autenticação e logout

As decisões bloqueantes desta change foram resolvidas. Nenhuma implementação deve iniciar antes da aprovação humana dos artefatos.

## Decisões resolvidas

- Sessões server-side expiram após trinta minutos de inatividade ou oito horas de duração absoluta, aplicadas no servidor. Os valores serão configuráveis com esses padrões iniciais.
- O identificador de sessão será renovado após autenticação e eventos sensíveis.
- O cookie de sessão será `Secure`, `HttpOnly`, `SameSite=Lax` e `Path=/`.
- A proteção CSRF será ativa para requisições que alteram estado. Métodos seguros não podem alterar estado, e `SameSite` será somente defesa adicional.
- E-mail inexistente, senha incorreta, conta bloqueada ou desativada retornam o mesmo status e a mensagem `E-mail ou senha inválidos.`. O fluxo deve evitar diferenças relevantes de tempo.
- Falhas serão limitadas a cinco por e-mail normalizado em quinze minutos, com atraso progressivo e bloqueio temporário de quinze minutos.
- Tentativas serão limitadas a vinte por IP em quinze minutos, retornando `429` quando excedido.
- O logout inicial revoga somente a sessão atual.
- A arquitetura deve suportar revogação de todas as sessões para alteração de senha, comprometimento detectado e ação administrativa futura, sem expor essa operação nesta change.

## Decisões adiadas

- Interface para sair de todos os dispositivos.
- Revogação global disparada por alteração de senha, comprometimento ou ação administrativa.
- MFA, recuperação de senha, alteração de e-mail e notificações de segurança.
