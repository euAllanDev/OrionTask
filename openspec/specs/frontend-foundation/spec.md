# Frontend Foundation Specification

## Purpose

Definir a fundacao tecnica, arquitetural e visual minima do frontend OrionTask, sem implementar fluxos de negocio autenticados.

## Requirements

### Requirement: Fundacao frontend segura

Frontend DEVE usar Next.js, TypeScript strict, Tailwind CSS e shadcn/ui. DEVE manter sessao exclusivamente no cookie controlado pelo backend e NAO DEVE ler, armazenar, serializar ou enviar manualmente token de sessao. Frontend NAO DEVE usar JWT, autenticacao paralela, localStorage, sessionStorage ou IndexedDB para credenciais.

### Requirement: CSRF separado

Camada HTTP DEVE obter CSRF por `/api/v1/csrf`, manter token somente em memoria e envia-lo em mutacoes por `X-CSRF-TOKEN`. CSRF NAO DEVE ser tratado como identidade autenticada. Cliente DEVE renovar token apos login, logout ou rejeicao CSRF conforme estrategia aprovada.

### Requirement: Contexto organizacional explicito

Rotas organizacionais DEVEM conter `organizationId` explicito. Frontend NAO DEVE manter organizacao ativa oculta como fonte de autoridade. Resposta `404` organizacional DEVE ser apresentada sem inferir existencia ou ausencia de acesso.

### Requirement: Qualidade minima frontend

Frontend DEVE fornecer comando unico para lint, formatacao, typecheck e testes unitarios/componentes acordados. UI DEVE fornecer estados acessiveis de loading, empty e error, sem expor detalhes internos de erro.

### Requirement: Animacao acessivel e proporcional

Frontend DEVE preferir CSS e usar Motion para animacoes orientadas a estado React. Animacoes DEVEM respeitar `prefers-reduced-motion`, priorizar `transform` e `opacity` e NAO DEVEM impedir navegacao, atrasar acao, esconder informacao essencial ou depender somente de movimento para transmitir estado. GSAP e Three.js NAO DEVEM ser adicionados sem requisito visual concreto e revisao de acessibilidade e performance. Three.js NAO DEVE ser usado em CRUDs, tabelas, formularios ou dashboards operacionais.
