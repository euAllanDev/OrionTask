# AGENTS.md — Regras para agentes de código

## 1. Fonte de verdade

Antes de planejar ou implementar qualquer mudança, leia obrigatoriamente:

1. `docs/constitution/mission.md`
2. `docs/constitution/principles.md`
3. `docs/constitution/tech-stack.md`
4. `docs/constitution/roadmap.md`
5. as specs relevantes em `openspec/specs/`
6. todos os artefatos da change ativa em `openspec/changes/<change>/`

Conversas não substituem especificações versionadas.

## 2. Fluxo obrigatório

Toda mudança funcional ou arquitetural deve seguir:

1. explorar;
2. propor;
3. revisar;
4. especificar;
5. decompor em tarefas;
6. implementar;
7. testar;
8. verificar aderência;
9. arquivar;
10. atualizar a fonte de verdade.

Não implementar uma change ainda não aprovada.

## 3. Escopo

- Trabalhe apenas no escopo da change ativa.
- Não adicione funcionalidades “úteis” que não estejam especificadas.
- Registre descobertas fora do escopo em `parking-lot.md`.
- Prefira mudanças pequenas, revisáveis e com commits granulares.

## 4. Arquitetura do backend

O backend usa monólito modular com arquitetura hexagonal.

Dependências permitidas:

```text
adapter-in -> application -> domain
adapter-out -> application/domain contracts
infrastructure -> ports
domain -> Java puro
```

O domínio:

- não depende de Spring;
- não depende de JPA;
- não depende de HTTP;
- não depende de serialização;
- não depende de banco, filas ou serviços externos;
- não contém anotações de frameworks.

Entidades JPA não são entidades de domínio.

## 5. Segurança

Segurança é requisito funcional.

Toda implementação deve considerar:

- isolamento entre organizações;
- autenticação e autorização por recurso;
- princípio do menor privilégio;
- minimização de dados;
- proteção de anexos;
- prevenção de enumeração de recursos;
- logs sem segredos ou dados sensíveis desnecessários;
- auditoria de ações críticas;
- proteção contra abuso;
- tratamento seguro de erros;
- revogação de sessões;
- retenção e exclusão.

Nunca confiar apenas no frontend para autorização.

## 6. Multi-tenancy

- Todo recurso pertencente a uma organização possui `organizationId`.
- Toda consulta a recurso multiempresa deve incluir o contexto da organização.
- Um identificador conhecido não concede acesso.
- Recursos de outra organização não devem ser confirmados ao usuário.
- Testes de isolamento são obrigatórios em toda capacidade multi-tenant.

## 7. Privacidade e LGPD

- Coletar apenas dados necessários.
- Toda coleta deve ter finalidade documentada.
- Diferenciar papéis de controlador e operador.
- Permitir exportação, correção, retenção e exclusão quando aplicável.
- Documentar suboperadores e transferências relevantes.
- Não introduzir rastreamento ou anúncios de terceiros no painel autenticado.
- Alterações que afetem dados pessoais exigem avaliação de privacidade.

## 8. Qualidade

Toda change deve incluir, conforme aplicável:

- testes unitários do domínio;
- testes de aplicação;
- testes de integração com PostgreSQL;
- testes de autorização;
- testes de isolamento entre organizações;
- migrations versionadas;
- documentação de API;
- atualização de specs.

## 9. Restrições

Não fazer sem uma change específica:

- microserviços;
- Kafka;
- Kubernetes;
- event sourcing;
- CQRS completo;
- IA generativa no produto;
- anúncios comportamentais;
- integrações extensas;
- abstrações prematuras.

## 10. Comunicação

Ao encontrar ambiguidade que altere segurança, privacidade, modelo de dados ou comportamento de negócio:

- não adivinhe;
- registre a questão em `open-questions.md`;
- proponha opções e trade-offs;
- aguarde decisão antes de implementar aquela parte.
