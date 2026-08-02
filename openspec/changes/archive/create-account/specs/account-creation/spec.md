# Delta Specification: Account Creation

## ADDED Requirements

### Requirement: Cadastro aberto de conta interna

O sistema DEVE permitir o cadastro aberto de uma conta interna com e-mail e senha local, sem criar organização ou membership nesta change.

#### Scenario: cadastro válido

- DADO um e-mail ainda não cadastrado e uma senha válida;
- QUANDO uma pessoa solicitar o cadastro;
- ENTÃO o sistema DEVE criar uma conta interna;
- E DEVE persistir somente o e-mail normalizado, o hash da senha, o identificador e o timestamp necessários;
- E NÃO DEVE criar organização ou membership.

### Requirement: Identificador de e-mail protegido

O e-mail DEVE ser normalizado e único sem distinção entre maiúsculas e minúsculas. O sistema NÃO DEVE confirmar a existência de uma conta pelo resultado do cadastro.

#### Scenario: e-mail já cadastrado

- DADO um e-mail normalizado já associado a uma conta;
- QUANDO uma pessoa solicitar novo cadastro com esse e-mail;
- ENTÃO o sistema NÃO DEVE criar ou alterar uma conta;
- E DEVE retornar resposta indistinguível da resposta de cadastro novo.

### Requirement: Senha protegida

O sistema DEVE aceitar senhas de 12 a 128 caracteres e armazená-las exclusivamente como hash Argon2id. Senhas e hashes NÃO DEVEM aparecer em logs, auditoria ou respostas HTTP.

#### Scenario: senha válida

- DADO uma senha entre 12 e 128 caracteres;
- QUANDO o cadastro for concluído;
- ENTÃO a persistência DEVE conter somente um hash Argon2id;
- E a senha original NÃO DEVE ser recuperável da persistência.

#### Scenario: senha inválida

- DADO uma senha com menos de 12 ou mais de 128 caracteres;
- QUANDO uma pessoa solicitar o cadastro;
- ENTÃO o sistema DEVE rejeitar a entrada;
- E NÃO DEVE criar uma conta.

### Requirement: Proteção contra abuso

O cadastro aberto DEVE limitar tentativas a cinco por endereço IP e três por e-mail normalizado em uma janela de quinze minutos.

#### Scenario: limite por endereço IP

- DADO cinco tentativas de cadastro pelo mesmo endereço IP nos últimos quinze minutos;
- QUANDO ocorrer uma sexta tentativa pelo mesmo endereço IP;
- ENTÃO o sistema DEVE rejeitar a tentativa sem expor dados de conta.

#### Scenario: limite por e-mail

- DADO três tentativas de cadastro para o mesmo e-mail normalizado nos últimos quinze minutos;
- QUANDO ocorrer uma quarta tentativa para esse e-mail;
- ENTÃO o sistema DEVE rejeitar a tentativa sem expor dados de conta.

### Requirement: Minimização de dados e sem ativação por e-mail

Esta change DEVE coletar somente o e-mail. Ela NÃO DEVE implementar confirmação de e-mail, tokens de ativação, reenvio, expiração ou estado de ativação dependente de e-mail.

#### Scenario: conta criada sem infraestrutura de e-mail

- DADO um cadastro válido;
- QUANDO a conta for criada;
- ENTÃO o sistema NÃO DEVE enviar e-mail;
- E NÃO DEVE criar estado pendente de confirmação;
- E NÃO DEVE coletar nome, telefone, CPF ou endereço.

### Requirement: Segurança do endpoint

O endpoint de cadastro DEVE validar somente os campos permitidos, evitar mass assignment, tratar erros sem detalhes sensíveis e registrar auditoria mínima de eventos de segurança.

#### Scenario: corpo com campo não permitido

- DADO uma solicitação de cadastro com atributo fora do contrato público;
- QUANDO o endpoint processar a solicitação;
- ENTÃO o sistema DEVE rejeitar a entrada;
- E NÃO DEVE persistir o atributo não permitido.
