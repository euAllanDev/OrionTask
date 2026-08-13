# Decisoes da criacao de organizacao

Todas decisoes bloqueantes desta change foram resolvidas. Esta change pode avancar para revisao humana dos artefatos; implementacao continua bloqueada ate aprovacao explicita.

## Questao de privacidade - resolvida

### Retencao de `name`

`name` deve ser mantido enquanto organizacao existir e for necessario para identifica-la e opera-la no OrionTask. Eliminacao, anonimização e retencao apos encerramento nao sao definidas por esta capacidade e devem ser especificadas em change propria.

## Questoes bloqueantes

### Dados minimos da organizacao - resolvida

Somente `name` sera coletado. Nome e obrigatorio, nao e identificador tecnico e nao tem unicidade global. UUID sera identificador tecnico. CNPJ, CPF, endereco, telefone, slug e outros dados nao entram no escopo. Finalidade: identificacao e exibicao da organizacao dentro do OrionTask. Nome pode ser dado pessoal de profissional autonomo e deve seguir minimizacao e finalidade.

### Relacao inicial da conta criadora - resolvida

Criacao gera membership minima com papel fixo `OWNER` entre conta autenticada e organizacao. Nao sera usado `owner_account_id` na organizacao. Gerenciamento, outros membros, outros papeis, transferencia de ownership e remocao continuam fora do escopo.

### Quantidade de organizacoes por conta - resolvida

Conta autenticada pode criar multiplas organizacoes. Nao havera unicidade por `account_id`; a persistencia deve impedir somente duplicidade de `(account_id, organization_id)`. Selecao ou troca de organizacao ativa nao entra no escopo.

### Auditoria da criacao - resolvida

Criacao deve registrar evento operacional estruturado minimo `organization.created`, contendo somente `accountId` e `organizationId`. Nome, e-mail, payload, cookies, tokens e credenciais sao proibidos. Evento nao substitui trilha persistida; modulo `audit`, consultas e retencao propria permanecem fora do escopo.

### Atomicidade e identidade do criador - resolvida

Organizacao e membership `OWNER` devem ser persistidas na mesma transacao; qualquer falha causa rollback completo. `accountId` vem exclusivamente do contexto autenticado derivado da sessao server-side persistida. O contrato HTTP nao aceita `ownerAccountId`, `accountId` ou `createdByAccountId` como fonte de autoridade.

## Limites confirmados

- identidade autenticada vem exclusivamente da sessao server-side existente;
- qualquer recurso organizacional futuro deve usar `organization_id` e filtro/autorizacao por organizacao;
- esta change nao implementa convites, membros adicionais, papeis adicionais, clientes ou tickets;
- nenhuma implementacao inicia sem aprovacao humana de proposal, design, delta spec e tasks.
