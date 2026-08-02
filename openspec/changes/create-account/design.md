# Design: Criação de conta interna

## Decisão principal

Implementar cadastro aberto de conta interna no módulo `identity`, sem criar organização, membership, sessão ou fluxo de confirmação de e-mail. A conta terá e-mail normalizado como identificador e senha local protegida por Argon2id.

## Fluxo

1. O adapter HTTP recebe e-mail e senha em `POST /api/v1/accounts`.
2. A aplicação valida a entrada, normaliza o e-mail e verifica o limite por IP e e-mail normalizado.
3. O caso de uso cria a conta somente se o identificador ainda não existir.
4. A senha é transformada em hash Argon2id antes de alcançar a persistência.
5. A aplicação registra auditoria mínima sem senha, hash ou e-mail bruto em logs.
6. O adapter retorna a mesma resposta de aceitação para cadastro novo e identificador já existente.

## Modelo e persistência

- `Account` pertence ao domínio `identity` e não depende de Spring ou JPA.
- A conta possui UUID, e-mail normalizado, hash de senha e timestamp UTC de criação.
- Não haverá estado de ativação ou confirmação de e-mail nesta change.
- A entidade JPA, o repositório e a migration pertencem ao adapter de persistência.
- A tabela de contas deve ter unicidade no e-mail normalizado para impedir duplicidade mesmo sob concorrência.
- A tentativa de cadastrar e-mail existente não deve atualizar senha, dados ou timestamps da conta existente.

## Segurança

- A senha aceita de 12 a 128 caracteres e nunca é armazenada ou registrada em texto puro.
- Argon2id será configurado por adapter de segurança; seus parâmetros serão cobertos por testes de integração e não pertencerão ao domínio.
- O limite é de cinco tentativas por IP e três por e-mail normalizado a cada quinze minutos. A resposta de limite não deve incluir dados sobre contas.
- A resposta para e-mail existente é indistinguível da resposta de criação bem-sucedida.
- O endpoint deve validar entrada, rejeitar campos não permitidos e evitar mass assignment.
- Logs e auditoria não devem conter senha, hash ou e-mail bruto. A auditoria registra apenas o tipo de evento e, após criação, o identificador da conta.

## Privacidade

- A única coleta desta change é o e-mail, necessário como identificador único da conta interna.
- Não serão coletados nome, telefone, CPF, endereço ou dados de organização.
- Verificação de e-mail, recuperação de senha, alteração de e-mail e suas políticas de retenção permanecem fora do escopo.

## Alternativas rejeitadas

### Criar organização e membership no cadastro

Rejeitado para manter os limites entre identidade, organização e membership definidos no roadmap.

### Exigir confirmação de e-mail

Rejeitado nesta etapa porque não há infraestrutura de e-mail. A conta não deve simular um estado de ativação que não pode ser concluído.

### Retornar conflito para e-mail existente

Rejeitado porque confirma a existência de uma conta e permite enumeração.
