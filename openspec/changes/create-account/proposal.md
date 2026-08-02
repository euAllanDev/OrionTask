# Proposal: Criacao de conta interna

## Change ID

`create-account`

## Status

Em proposta. Esta change nao esta aprovada para implementacao.

## Problema

O OrionTask ainda nao possui uma identidade interna para que a primeira empresa possa iniciar o uso do produto. A criacao de conta deve estabelecer uma base segura e minima para os fluxos posteriores de autenticacao, organizacoes e membership, sem antecipar essas capacidades.

## Objetivo

Definir e implementar o cadastro inicial de uma conta interna, com dados minimos, protecao adequada de credenciais e criterios verificaveis de seguranca e privacidade.

## Escopo pretendido

- criar uma conta interna a partir de dados definidos e justificados;
- proteger a credencial no armazenamento;
- impedir duplicidade de identificador de login;
- retornar erros que nao exponham segredos;
- registrar os requisitos de privacidade e seguranca aplicaveis;
- incluir testes unitarios, de aplicacao, integracao PostgreSQL e autorizacao quando aplicavel.

## Fora do escopo

- autenticacao, sessao e logout;
- criacao de organizacao;
- membership, papeis e convites;
- recuperacao ou redefinicao de credencial;
- MFA;
- portal do cliente;
- notificacoes por e-mail;
- exclusao e exportacao de dados.

## Riscos

- permitir cadastro sem definir como a conta se vinculara posteriormente a uma organizacao;
- coletar dados pessoais sem finalidade ou retencao definida;
- vazar informacao sobre contas existentes;
- armazenar ou registrar credenciais de forma insegura;
- definir uma interface HTTP antes de resolver requisitos de autenticacao e protecao contra abuso.

## Criterio para avancar

As questoes em `open-questions.md` devem receber decisao humana antes de elaborar design, delta spec, tarefas ou codigo.
