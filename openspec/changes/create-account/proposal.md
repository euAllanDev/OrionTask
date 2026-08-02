# Proposal: Criacao de conta interna

## Change ID

`create-account`

## Status

Pronta para revisão humana. Esta change não está aprovada para implementação.

## Problema

O OrionTask ainda nao possui uma identidade interna para que a primeira empresa possa iniciar o uso do produto. A criacao de conta deve estabelecer uma base segura e minima para os fluxos posteriores de autenticacao, organizacoes e membership, sem antecipar essas capacidades.

## Objetivo

Definir e implementar o cadastro inicial de uma conta interna, com dados minimos, protecao adequada de credenciais e criterios verificaveis de seguranca e privacidade.

## Escopo pretendido

- permitir cadastro aberto de conta interna com e-mail e senha local;
- coletar somente e-mail, normalizado e único sem distinção entre maiúsculas e minúsculas;
- proteger a senha com Argon2id, aceitando de 12 a 128 caracteres;
- impedir duplicidade de identificador de login sem revelar se a conta existe;
- limitar cadastro a cinco tentativas por endereço IP e três por e-mail normalizado em quinze minutos;
- retornar respostas que não exponham segredos nem permitam enumeração de contas;
- registrar os requisitos de privacidade e seguranca aplicaveis;
- incluir testes unitarios, de aplicacao, integracao PostgreSQL e autorizacao quando aplicavel.

## Fora do escopo

- autenticacao, sessao e logout;
- criação de organização;
- membership, papéis e convites;
- recuperação ou redefinição de credencial;
- confirmação e envio de e-mail;
- MFA;
- portal do cliente;
- notificações por e-mail;
- exclusao e exportacao de dados.

## Riscos

- permitir abuso de cadastro aberto ou enumeração de contas;
- coletar dados pessoais sem finalidade definida;
- vazar informacao sobre contas existentes;
- armazenar ou registrar credenciais de forma insegura;
- definir uma interface HTTP antes de resolver requisitos de autenticacao e protecao contra abuso.

## Criterio para avancar

Os artefatos desta change devem receber revisão e aprovação humana antes da implementação.
