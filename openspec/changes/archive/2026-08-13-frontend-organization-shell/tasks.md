# Tasks: Frontend organization shell

## 1. Revisao e aprovacao

- [x] Explorar rotas, shell, cliente HTTP, autenticacao e contrato de listagem
- [x] Revisar e aprovar proposal, design e delta spec em 2026-08-13

## 2. Dados e rotas

- [x] Criar API tipada para `GET /api/v1/organizations` e validacao de resposta
- [x] Substituir `/app` neutro por lista protegida com loading, empty, error e ready
- [x] Navegar por links para `/organizations/[organizationId]`
- [x] Tratar `401` pelo fluxo de sessao e `404` como `Recurso indisponível.`

## 3. Shell

- [x] Ajustar sidebar e header para lista real, contexto URL e logout
- [x] Garantir responsividade, navegacao mobile e reduced motion
- [x] Manter conteudo organizacional estrutural sem capacidades futuras

## 4. Verificacao e archive

- [x] Adicionar testes de lista, navegacao, contexto, falhas e logout
- [x] Executar `npm run check` e `npm run build` em 2026-08-13
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final em 2026-08-13
- [x] Consolidar spec, atualizar roadmap, README e retomada, arquivar change em 2026-08-13

## Evidencias

- `npm run check` passou em 2026-08-13: lint, Prettier, TypeScript e 16 testes Vitest.
- `npm run build` passou em 2026-08-13.
- `npx --yes @fission-ai/openspec validate frontend-organization-shell --strict` passou em 2026-08-13.
