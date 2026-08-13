# Design: Verificar isolamento organizacional

## Estrategia

Testes de integracao HTTP criam duas organizacoes, contas e sessoes independentes no PostgreSQL Testcontainers. Cada requisicao usa somente cookie de sessao e contexto CSRF da conta autora. IDs da organizacao A e de seus clientes ou memberships sao deliberadamente enviados por membro da organizacao B para demonstrar que identificador conhecido nao concede acesso.

## Matriz de evidencia

| Recurso | Acao do membro B sobre recurso A | Resultado esperado | Evidencia de estado |
| --- | --- | --- | --- |
| Organizacao | `GET /organizations/{organizationA}` | `404` igual a UUID inexistente | nenhum dado retornado |
| Clientes | `GET /organizations/{organizationA}/clients` | `404` | nenhum cliente de A no corpo |
| Cliente | `GET`, `PATCH`, `DELETE` com `clientA` | `404` | nome e status de A inalterados |
| Cliente | `GET`, `PATCH`, `DELETE` com `clientA` sob organizacao B | `404` | cliente A inalterado e nenhum registro B criado |
| Membership | `DELETE /organizations/{organizationA}/members/{memberA}` | `404` | memberships A e B inalteradas |

Cada tentativa de mutacao usa CSRF valido da conta B. Assim, `404` prova escopo organizacional e nao apenas rejeicao CSRF. Para respostas que a spec define como indistinguiveis, o teste compara status e corpo com equivalente inexistente.

## Estrutura de teste

Criar classe de integracao dedicada a isolamento organizacional no mesmo pacote de testes de integracao. Ela pode reutilizar helpers HTTP minimos existentes ou extrair helpers sem alterar semantica de testes atuais. Banco e servidor reais permanecem obrigatorios; mocks nao demonstram filtro por `organization_id` nem constraints.

Assercoes de banco usam consultas por `organization_id` para confirmar invariantes apos recusas. Nenhuma fixture inclui dados pessoais alem de e-mails sintéticos ja necessarios para contas internas; testes nao verificam logs com e-mails ou tokens.

Cada cenario DEVE criar explicitamente suas organizacoes, memberships, clientes e sessoes. Testes NAO DEVEM depender da ordem de execucao nem reutilizar estado de outro cenario.

## Seguranca e privacidade

O escopo testa fronteira de tenant e nao introduz coleta, persistencia ou novo processamento de dados pessoais. Tokens e cookies permanecem somente em memoria do processo de teste e nao sao impressos. Nomes de clientes usados nas assercoes permanecem apenas no banco efemero do Testcontainers.
