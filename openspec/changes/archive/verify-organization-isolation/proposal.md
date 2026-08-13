# Proposal: Verificar isolamento organizacional

## Change ID

`verify-organization-isolation`

## Status

Arquivada apos verificacao local e revisao humana final.

## Problema

As capacidades de organizacao, memberships e clientes exigem isolamento rigoroso, mas os testes atuais cobrem cenarios de forma parcial e concentrada em uma classe de integracao extensa. Faltam evidencias explicitas de que um membro de outra organizacao nao le, lista, altera, desativa ou remove recursos conhecidos do tenant alvo.

## Objetivo

Adicionar evidencia automatizada de isolamento multiempresa para endpoints organizacionais existentes, sem mudar comportamento de producao, contratos HTTP, schema ou modelo de autorizacao.

## Escopo pretendido

- criar testes de integracao HTTP com PostgreSQL/Testcontainers para dois tenants independentes;
- verificar respostas `404` indistinguiveis entre recurso inexistente e recurso de outra organizacao quando exigido pelas specs;
- verificar que listagem de clientes retorna somente registros da organizacao autorizada;
- verificar que UUID de cliente conhecido em organizacao errada nao pode ser lido, alterado nem desativado;
- verificar que tentativa cruzada de revogacao nao remove memberships de nenhum tenant;
- confirmar por banco que respostas negadas nao alteram clientes ou memberships;
- manter cobertura de CSRF e papeis existente sem duplicar cenarios sem valor adicional.

## Fora do escopo

- alterar endpoints, regras de autorizacao, codigos HTTP, logs, auditoria, migrations ou producao;
- adicionar Row-Level Security, filtros globais JPA ou middleware de tenant;
- criar tickets, anexos, notificacoes, portal do cliente ou frontend funcional;
- refatorar capacidades existentes alem da extracao de fixtures de teste estritamente necessaria.

## Riscos

- teste usar apenas status HTTP e nao detectar alteracao indevida persistida;
- fixture compartilhada mascarar identidade ou organizacao errada;
- cobertura duplicada aumentar duracao sem testar fronteira nova;
- tratamento diferente entre recurso inexistente e recurso de outro tenant reintroduzir enumeracao.

## Criterio para avancar

Decisoes em `open-questions.md`, proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana antes da implementacao.
