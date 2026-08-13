# Workflow SDD do OrionTask

## Constituição

Os documentos em `docs/constitution/` definem a missão, os princípios, a stack e o roadmap.

Mudanças na constituição exigem revisão explícita.

## Ciclo de change

### 1. Explore

Entender o problema, o domínio afetado, riscos, segurança, privacidade e decisões pendentes.

### 2. Propose

Criar `proposal.md` com problema, objetivo, escopo, impacto e critérios mínimos.

### 3. Design

Criar `design.md` com arquitetura, dados, fluxos, trade-offs, segurança e questões abertas.

### 4. Specify

Criar delta specs com requisitos normativos e cenários verificáveis.

### 5. Tasks

Quebrar a mudança em tarefas pequenas e ordenadas, incluindo testes e documentação.

### 6. Apply

Implementar estritamente a change aprovada.

### 7. Verify

Comparar implementação, testes e comportamento com todos os artefatos.

### 8. Archive

Consolidar os deltas em `openspec/specs/`, mover a change e atualizar roadmap/changelog.

## Linguagem normativa

- **DEVE**: obrigatório;
- **NÃO DEVE**: proibido;
- **DEVERIA**: recomendado, aceita exceção justificada;
- **PODE**: opcional.

## Critério de conclusão

Uma change somente pode ser arquivada quando:

- tarefas obrigatórias estão concluídas;
- testes relevantes passam;
- critérios de aceite foram demonstrados;
- riscos pendentes estão registrados;
- documentação foi atualizada;
- não existem violações arquiteturais;
- implementação não excedeu o escopo.
