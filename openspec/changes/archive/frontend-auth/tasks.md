# Tasks: Frontend authentication

## 1. Revisao e aprovacao

- [x] Verificar cadastro, login, logout, CSRF e sessao atual contra backend arquivado
- [x] Decidir shell neutro, validacao simples e correcao de retry `403`
- [x] Revisar e aprovar proposal, design e delta spec em 2026-08-13

## 2. Estado e camada HTTP

- [x] Remover retry automatico generico de `403` em `apiFetch`
- [x] Permitir mutacoes com ou sem CSRF conforme contrato backend
- [x] Criar API tipada e estado em memoria para sessao atual
- [x] Implementar bootstrap e guard de rota sem fonte persistente de autenticacao
- [x] Cobrir bootstrap, `401`, CSRF e ausencia de retry em testes

## 3. Fluxos e rotas

- [x] Criar `/login`, `/register` e `/app` neutro
- [x] Implementar formulario de login, consulta de sessao pos-login e redirecionamento
- [x] Implementar formulario de cadastro sem sessao implicita
- [x] Implementar logout com CSRF, limpeza em memoria e redirecionamento
- [x] Cobrir loading, erros, submit duplicado, teclado e foco

## 4. Verificacao e archive

- [x] Executar `npm run check` em `frontend/`
- [x] Executar `npm run build` em `frontend/`
- [x] Revisar aderencia entre implementacao, design e delta spec
- [x] Realizar revisao humana final em 2026-08-13
- [x] Consolidar delta spec, atualizar README e retomada, arquivar change em 2026-08-13

## Evidencias

- `npx --yes @fission-ai/openspec validate frontend-auth --strict` concluiu em 2026-08-13.
- `frontend/npm run check` concluiu com lint, Prettier, TypeScript e 12 testes Vitest aprovados em 2026-08-13.
- `frontend/npm run build` concluiu em 2026-08-13.
