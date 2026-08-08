# Decisoes do acesso organizacional

Decisoes bloqueantes foram aprovadas. Esta change pode avancar para revisao humana dos artefatos; implementacao continua bloqueada ate aprovacao explicita.

## Contexto da organizacao - aprovada

Como uma requisicao informa organizacao alvo?

- Opcao A: `organizationId` explicito no path de cada endpoint organizacional. Backend sempre combina identidade da sessao, UUID da rota e membership existente. Nao cria estado de organizacao ativa e torna fronteira de tenant visivel no contrato.
- Opcao B: organizacao ativa persistida na sessao. Reduz repeticao no cliente, mas exige troca segura de contexto, semantica entre abas, revogacao e risco de acao na organizacao errada.

Decisao: opcao A. Cada endpoint organizacional recebe `organizationId` explicito no path. Organizacao ativa nao sera persistida na sessao.

## Papeis minimos e privilegios - aprovada

Quais papeis e capacidades entram nesta fundacao?

- Opcao A: `OWNER`, `ADMIN` e `TECHNICIAN`, com matriz fixa limitada a administracao organizacional e membership. Capacidades de clientes, tickets, categorias e configuracoes serao definidas nas changes que criarem esses recursos.
- Opcao B: somente `OWNER` e `TECHNICIAN`. Menor modelo inicial, mas requer migracao ou regra adicional quando administracao delegada for necessaria.

Decisao: opcao A. Papeis fixos sao `OWNER`, `ADMIN` e `TECHNICIAN`; nao existe RBAC generico ou configuravel.

| Capacidade | OWNER | ADMIN | TECHNICIAN |
| --- | --- | --- | --- |
| Ler organizacao autorizada | Sim | Sim | Sim |
| Gerenciar memberships futuros | Sim | Sim | Nao |
| Transferir ownership futuro | Sim | Nao | Nao |
| Alterar ou remover ultimo `OWNER` | Nunca | Nunca | Nunca |

## Contrato minimo demonstravel - aprovada

Qual recurso HTTP demonstra autorizacao antes de clientes ou tickets existirem?

- Opcao A: expor endpoint minimo de leitura da organizacao ou membership atual, protegido por `organizationId` explicito e membership existente.
- Opcao B: manter autorizacao somente como port interno ate primeiro recurso de negocio existir.

Decisao: opcao A. O contrato minimo sera `GET /api/v1/organizations/{organizationId}`, protegido por membership existente. Ele retorna somente dados da organizacao autorizada e nao confirma organizacao ou membership alheios.

## Invariante de ownership - aprovada

Como preparar protecao contra organizacao sem `OWNER` sem antecipar lifecycle completo?

- Opcao A: definir invariante normativa agora e implementar verificacao nas futuras operacoes de remocao, revogacao e alteracao de papel.
- Opcao B: implementar desde ja remocao, transferencia e bloqueio do ultimo `OWNER`.

Decisao: opcao A. Esta change define invariante; lifecycle nao sera antecipado. Toda organizacao existente DEVE possuir ao menos uma membership com papel `OWNER`. Nesta change, nenhuma operacao altera memberships, portanto invariante e preservada por construcao.

## Limites confirmados

- identidade da conta vem exclusivamente da sessao server-side persistida;
- `HttpSession` e `JSESSIONID` nao sao fontes de identidade ou contexto organizacional;
- UUID conhecido nunca concede acesso a organizacao;
- convites e lifecycle completo serao changes separadas;
- nenhuma implementacao inicia sem aprovacao humana dos artefatos.
