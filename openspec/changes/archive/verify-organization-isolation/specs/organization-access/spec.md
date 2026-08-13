# Organization Access Specification Delta

## ADDED Requirements

### Requirement: Evidencia automatizada de isolamento organizacional

Suite de integracao com PostgreSQL DEVE demonstrar que conta autenticada sem membership na organizacao alvo nao consegue ler organizacao, listar clientes, ler cliente, alterar cliente, desativar cliente ou revogar membership daquela organizacao, mesmo conhecendo UUIDs validos. Onde especificacao define `404 Not Found` indistinguivel, teste DEVE comparar status e corpo com recurso inexistente equivalente.

Testes de mutacao negada DEVEM usar CSRF valido da conta nao autorizada e confirmar que clientes e memberships de ambas organizacoes permanecem inalterados apos a resposta. Suite DEVE cobrir duas organizacoes distintas e ao menos uma conta autenticada por tenant.
