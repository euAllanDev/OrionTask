# frontend-organization-shell Specification

## Purpose

Definir descoberta de organizacoes acessiveis e shell autenticado com contexto organizacional explicito na URL, sem antecipar capacidades de negocio.
## Requirements
### Requirement: Descoberta autenticada de organizacoes

Frontend MUST carregar organizacoes acessiveis somente por `GET /api/v1/organizations`, usando cookies gerenciados pelo browser e sem CSRF. Frontend MUST aceitar somente array de itens com `id`, `name`, `createdAt`, `updatedAt` e `role` entre `OWNER`, `ADMIN` e `TECHNICIAN`; resposta invalida MUST produzir erro generico sem dados parciais.

Pagina `/app` MUST apresentar estados acessiveis de loading, empty, error e ready. Lista vazia MUST informar ausencia de organizacoes acessiveis sem inventar organizacao, contadores ou dados mockados. Erro deve permitir nova tentativa sem expor detalhes internos.

#### Scenario: conta autenticada ve suas organizacoes

- **GIVEN** pessoa autenticada com organizacoes retornadas pelo backend;
- **WHEN** abrir `/app`;
- **THEN** frontend MUST listar cada organizacao retornada;
- **AND** cada item MUST navegar para sua rota organizacional.

### Requirement: Contexto organizacional explicito na URL

Cada organizacao listada MUST navegar para `/organizations/[organizationId]`. `organizationId` do pathname MUST ser unica fonte de contexto da rota; frontend MUST NOT persistir ou tratar organizacao ativa em provider, cookie ou storage como autoridade.

Shell MUST destacar organizacao cujo `id` corresponde ao pathname atual. Lista de organizacoes e contexto visual MUST NOT autorizar operacoes futuras; cada recurso backend continua responsavel por revalidar membership.

#### Scenario: navegacao preserva contexto na URL

- **GIVEN** lista pronta com organizacao acessivel;
- **WHEN** pessoa seleciona organizacao;
- **THEN** frontend MUST navegar para `/organizations/[organizationId]`;
- **AND** shell MUST destacar somente organizacao correspondente ao pathname.

### Requirement: Shell autenticado sem capacidades antecipadas

Rotas `/app` e `/organizations/[organizationId]` MUST usar shell responsivo com sidebar, header e logout funcional. Shell MUST manter navegacao acessivel em desktop e mobile e respeitar preferencias de reduced motion.

Shell MUST NOT implementar clientes, membros, convites, tickets, dashboard, contadores ou dados mockados. Papel retornado pode ser exibido como metadado, mas MUST NOT controlar autorizacao de frontend nesta capacidade.

#### Scenario: logout continua disponivel no shell

- **GIVEN** pessoa autenticada em rota organizacional;
- **WHEN** iniciar logout valido;
- **THEN** frontend MUST seguir contrato existente de logout;
- **AND** MUST navegar para `/login` somente apos conclusao permitida pelo contrato.

### Requirement: Erro organizacional nao enumeravel

Resposta `404` para recurso organizacional MUST ser apresentada exatamente como `Recurso indisponível.`. Frontend MUST NOT inferir ou informar se organizacao existe, se pessoa possui acesso ou se membership foi revogada.

`401` durante carregamento de conteudo autenticado MUST limpar estado autenticado em memoria e redirecionar para `/login`, conforme contrato de autenticacao existente.

#### Scenario: rota organizacional indisponivel

- **GIVEN** rota organizacional recebe resposta `404`;
- **WHEN** frontend apresenta falha;
- **THEN** frontend MUST exibir `Recurso indisponível.`;
- **AND** MUST NOT expor detalhes de existencia ou autorizacao.

### Requirement: Evidencia verificavel

Testes Vitest e React Testing Library MUST cobrir lista loading, empty, error, ready, nova tentativa, navegacao por link, contexto URL, destaque de organizacao, `404` neutro, `401` e logout no shell. `npm run check` e `npm run build` MUST passar antes de archive.

#### Scenario: gates frontend executam

- **GIVEN** implementacao concluida;
- **WHEN** executar gates frontend;
- **THEN** `npm run check` e `npm run build` MUST passar.
