# Tasks: Bootstrap frontend

## 1. Revisao e aprovacao

- [x] Aprovar package manager, proxy, CSRF, sessao e shell
- [x] Aprovar proposal, design e delta spec

## 2. Fundacao tecnica

- [x] Inicializar Next.js com TypeScript strict, Tailwind e npm lockfile
- [x] Configurar shadcn/ui com primitives minimos
- [x] Adicionar Motion e definir reduced motion para animacoes do shell
- [x] Configurar lint, Prettier, Vitest, RTL e `npm run check`
- [x] Criar estrutura de rotas, componentes, features, lib e types

## 3. Seguranca e integracao

- [x] Configurar rewrite local por variavel de ambiente sem alterar backend
- [x] Implementar cliente HTTP relativo com credenciais do navegador
- [x] Implementar CSRF em memoria e retry seguro limitado
- [x] Criar tratamento seguro de erros HTTP e `404` organizacional

## 4. Shell e UX

- [x] Implementar tema claro e tokens visuais minimos
- [x] Implementar shell responsivo por organizacao sem dados falsos
- [x] Implementar primitives de loading, empty e error
- [x] Cobrir reduced motion e estados animados relevantes em testes de componentes
- [x] Cobrir componentes e camada HTTP com testes relevantes

## 5. Verificacao

- [x] Documentar execucao local, env e requisitos de mesma origem/HTTPS
- [x] Executar `npm run check`
- [x] Revisar aderencia a spec e seguranca
- [x] Realizar revisao humana final antes do archive

## Evidencias parciais

- Next.js, TypeScript strict, Tailwind, Motion, Vitest, RTL, Prettier e shadcn/ui foram inicializados.
- Rewrite local, cliente HTTP e CSRF em memoria foram adicionados sem alterar backend.
- `npm run check` passou: ESLint, Prettier, TypeScript e Vitest (3 testes).
- `npm run build` passou com rota dinâmica estrutural `/organizations/[organizationId]`.
- Revisao humana final concluida; change arquivada.
