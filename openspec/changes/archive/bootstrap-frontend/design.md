# Design: Bootstrap frontend

## Direcao tecnica

Next.js com App Router, TypeScript strict, Tailwind CSS, shadcn/ui e Motion. Usar `npm` e lockfile versionado. Componentes sao Server Components por padrao; Client Components somente para interacao, browser APIs, CSRF runtime, estado local e animacao. Nenhuma camada replica arquitetura hexagonal do backend.

Estrutura proposta:

```text
frontend/
├── app/
│   ├── (public)/
│   ├── (app)/organizations/[organizationId]/
│   ├── layout.tsx
│   └── globals.css
├── components/ui/
├── components/app-shell/
├── features/
│   ├── auth/
│   ├── organizations/
│   ├── memberships/
│   └── customers/
├── lib/api/
├── lib/csrf/
└── types/
```

`components/ui` contem apenas primitives shadcn instalados. Feature contem UI e logica daquela capacidade. `lib/api` centraliza fetch, erros e contratos HTTP. `lib/csrf` guarda token apenas em memoria de runtime. URL, nunca store global, determina organizacao atual.

## HTTP, sessao e CSRF

Browser chama somente caminhos relativos `/api/...` com `credentials: "include"`. Em desenvolvimento, rewrite do Next encaminha para `ORIONTASK_API_ORIGIN=http://localhost:8080`; em producao, ingress deve servir frontend e backend sob mesma origem e encaminhar `/api` ao Spring. Isto evita habilitar CORS nesta change e preserva envio normal de cookies pelo navegador.

Cliente HTTP nunca le, serializa ou armazena `__Host-oriontask-session`. Para mutacoes, cliente CSRF busca `/api/v1/csrf`, mantem token somente em memoria, envia `X-CSRF-TOKEN` e renova uma unica vez apos resposta CSRF rejeitada. Token CSRF nao determina estado autenticado. Nao usar localStorage, sessionStorage ou IndexedDB para credenciais ou CSRF.

Bootstrap nao pode determinar sessao valida com seguranca: backend nao possui endpoint de identidade atual nem listagem de organizacoes. Shell sera estrutural; `frontend-auth` definira UX de login e estrategia de bootstrap de identidade sem alterar modelo de sessao nesta change.

## Visual e UX

Tema claro: superficie neutra, contraste alto, azul discreto apenas para acao e foco, radius moderado, bordas sutis, tipografia system sans e densidade adequada a tabelas. Shell preparado para sidebar compacta em desktop e sheet em mobile. Header mostra titulo e contexto de organizacao vindo da URL, sem alegar autorizacao ou carregar dados ficticios.

Definir primitives para pagina, section header, empty state, error state, loading skeleton e tabela. Sem dark mode, dados fake ou graficos. Animacao segue hierarquia `CSS -> Motion -> GSAP -> Three.js`: CSS para transicoes simples; Motion para transicoes React, layout, modais, sheets e microinteracoes; GSAP somente para timelines, scroll ou sequencias cuja complexidade justifique; Three.js somente para experiencia 3D com beneficio visual claro, nunca CRUD, tabelas, formularios ou dashboard operacional.

Motion usa `motion/react` e pode usar `motion/react-client` quando reduzir JavaScript de Client Components. Animacoes priorizam `transform` e `opacity`, respeitam `prefers-reduced-motion`, nunca ocultam informacao essencial ou atrasam acao. React Bits e fonte de inspiracao/componentes pontuais adaptados, nao dependencia ou catalogo instalado. GSAP e Three.js nao entram na bootstrap; futura change deve justificar uso, lazy loading, fallback WebGL e impacto de CPU/GPU.

## Qualidade

`npm run check` executa `next lint` ou ESLint equivalente, Prettier check, `tsc --noEmit` e Vitest. React Testing Library cobre componentes com estados, inclusive reduced motion quando houver animacao. Playwright fica fora da bootstrap ate existir fluxo funcional autenticado. CI deve futuramente executar `npm ci` e `npm run check` em PR, mas workflow nao muda nesta change.

## Desenvolvimento local

Backend profile local define cookie tecnico sem `Secure`; sessao OrionTask tambem deve seguir perfil local existente para HTTP. Frontend em `localhost:3000` usa rewrite para backend em `localhost:8080`. Producao exige HTTPS e mesma origem publica. Se deploy exigir origens distintas, nova change deve definir CORS, dominio de cookie e CSRF antes de exposicao.
