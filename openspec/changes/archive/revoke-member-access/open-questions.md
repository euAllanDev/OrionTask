# Open Questions: Revogacao de acesso de membro

## Q1 - Matriz de autoridade

Quem pode revogar qual papel?

Opcoes:

- `OWNER` revoga `ADMIN` e `TECHNICIAN`; `ADMIN` revoga somente `TECHNICIAN`; ninguem revoga `OWNER` nesta change.
- `OWNER` revoga qualquer papel, inclusive outro `OWNER`, preservando ao menos um owner.

Recomendacao: primeira opcao. Evita regras de concorrencia e transferencia implicita de ownership.

## Q2 - Efeito em sessoes existentes

Revogacao de membership deve encerrar sessoes ativas da conta revogada?

Opcoes:

- nao encerrar sessoes; toda operacao organizacional consulta membership atual e passa a negar acesso imediatamente;
- encerrar todas sessoes da conta revogada;
- encerrar somente sessoes que acessaram organizacao.

Recomendacao: primeira opcao. Sessao autentica conta, nao concede tenant; revogacao global excede escopo e pode afetar outras organizacoes da mesma conta.

## Q3 - Semantica de persistencia

Membership revogada deve ser excluida ou manter estado de revogacao?

Opcoes:

- exclusao fisica, com auditoria persistida definida em change futura;
- soft delete ou status `REVOKED` agora.

Recomendacao: exclusao fisica. Soft delete introduz retencao e modelo de dados sem requisito aprovado; auditoria exige change propria.

## Q4 - Resposta para alvo inexistente

Ator autorizado tentando revogar conta sem membership deve receber qual resposta?

Opcoes:

- `404 Not Found` generico, igual a identificador inexistente;
- `204 No Content` idempotente.

Recomendacao: `404 Not Found` generico. Mantem semantica de recurso alvo ausente e reduz risco de afirmar efeito quando nenhum membership existia.
