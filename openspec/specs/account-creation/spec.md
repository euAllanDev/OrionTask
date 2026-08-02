# Account Creation Specification

## Purpose

Definir o cadastro aberto e seguro de contas internas do OrionTask.

## Requirements

### Requirement: Cadastro de conta interna

O sistema DEVE permitir cadastro aberto com e-mail e senha local, sem criar organização ou membership.

### Requirement: Identificador protegido

O e-mail DEVE ser normalizado, único sem distinção entre maiúsculas e minúsculas, e sua existência NÃO DEVE ser confirmada pela resposta de cadastro.

### Requirement: Senha protegida

O sistema DEVE aceitar senhas de 12 a 128 caracteres e armazená-las exclusivamente como hash Argon2id. Senhas e hashes NÃO DEVEM aparecer em logs, auditoria ou respostas HTTP.

### Requirement: Proteção contra abuso

O cadastro aberto DEVE limitar tentativas a cinco por endereço IP e três por e-mail normalizado em quinze minutos.

### Requirement: Minimização de dados

O cadastro DEVE coletar somente o e-mail. Ele NÃO DEVE implementar confirmação de e-mail, tokens de ativação, reenvio, expiração ou estado dependente de e-mail.

### Requirement: Segurança do endpoint

O endpoint DEVE validar somente campos permitidos, evitar mass assignment, tratar erros sem detalhes sensíveis e registrar auditoria mínima sem dados sensíveis.
