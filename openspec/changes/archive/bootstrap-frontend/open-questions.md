# Open Questions: Bootstrap frontend

## Q1 - Package manager

Opcoes: `npm`, `pnpm` ou outro. Recomendacao: `npm`, ja instalado e suficiente para unica app frontend.

## Q2 - Integracao local e producao

Opcoes: chamadas cross-origin com CORS ou `/api` por rewrite/proxy local e mesma origem publica. Recomendacao: rewrite local e mesma origem publica. Evita CORS e protege modelo de cookies.

## Q3 - Renovacao de CSRF

Opcoes: buscar antes de toda mutacao, cache em memoria com retry unico apos `403`, ou token persistido. Recomendacao: cache em memoria com retry unico; persistencia e proibida.

## Q4 - Estado de sessao

Backend ainda nao expoe identidade autenticada ou lista de organizacoes. Opcao A: bootstrap so estrutural, sem declarar pessoa autenticada. Opcao B: incluir endpoint backend nesta change. Recomendacao: A; B excede escopo e muda backend.

## Q5 - Shell inicial

Opcoes: somente primitives e paginas de referencia, ou shell estrutural com sidebar e rotas organizacionais sem dados. Recomendacao: shell estrutural. Torna direcao visual verificavel sem fluxo falso.

## Q6 - Validacao de formularios

Opcoes: validacao simples nativa agora ou Zod. Recomendacao: validacao simples; nao ha formulario funcional nesta change.

## Q7 - Testes e gates

Opcoes: Vitest + React Testing Library ou Jest + RTL; E2E agora ou depois. Recomendacao: Vitest + RTL e sem Playwright nesta change. `npm run check` agrega lint, formatacao, tipos e testes.

## Q8 - shadcn/ui

Opcoes: instalar primitives necessarios ou gerar catalogo amplo. Recomendacao: somente Button, Input, Dialog/Sheet, Skeleton, Table e utilitarios realmente usados.

## Q9 - Motion e bibliotecas especializadas

Motion entra como dependencia da bootstrap para animacoes React necessarias ao shell. GSAP, Three.js e React Bits nao sao dependencias da bootstrap. Seu uso futuro exige caso concreto, revisao de acessibilidade, reduced motion e performance. Recomendacao: aprovar esta separacao.
