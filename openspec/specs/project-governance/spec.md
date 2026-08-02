# Project Governance Specification

## Purpose

Definir o processo obrigatório de evolução do OrionTask.

## Requirements

### Requirement: Mudanças orientadas por especificação

Toda mudança funcional, arquitetural, de segurança, privacidade ou modelo de dados DEVE possuir uma change OpenSpec aprovada antes da implementação.

#### Scenario: agente recebe pedido direto de implementação

- DADO que não existe uma change aprovada;
- QUANDO o agente recebe uma solicitação para implementar uma funcionalidade;
- ENTÃO ele DEVE interromper a implementação;
- E DEVE propor a criação ou conclusão dos artefatos necessários.

### Requirement: Revisão humana

O processo DEVE exigir revisão humana dos artefatos antes do apply e da implementação antes do archive.

### Requirement: Escopo controlado

A implementação NÃO DEVE introduzir comportamento funcional fora das specs da change ativa.

### Requirement: Atualização da fonte de verdade

Ao arquivar uma change, seus deltas aprovados DEVEM ser incorporados em `openspec/specs/`.
