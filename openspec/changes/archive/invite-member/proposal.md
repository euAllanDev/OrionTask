# Proposal: Convite de membro

## Change ID

`invite-member`

## Status

Arquivada em 2026-08-08 apos aprovacao humana final.

## Problema

Organizacao possui memberships e papeis, mas ainda nao consegue adicionar outro membro com autorizacao controlada. Criacao direta de membership por identificador conhecido permitiria vinculo indevido, enumeracao de conta ou elevacao de privilegio.

## Objetivo

Permitir que membro autorizado crie convite direcionado e que conta autenticada elegivel aceite token unico para receber membership aprovada, sem acoplar esta capacidade a entrega de e-mail.

## Escopo pretendido

- criar convite pendente para organizacao e papel permitidos;
- exigir conta autenticada com autoridade para convidar;
- vincular convite a destinatario aprovado e impedir aceite por conta diferente;
- gerar token opaco de alta entropia, exposto somente uma vez para entrega externa;
- persistir somente representacao derivada do token;
- aceitar convite uma unica vez e criar membership atomicamente;
- expirar convite pendente conforme decisao aprovada;
- retornar erros sem confirmar conta, convite ou organizacao de terceiros;
- registrar evento operacional minimo sem e-mail, token ou payload;
- incluir testes de autorizacao, isolamento, token, expiracao e aceite atomico.

## Fora do escopo

- envio, reenvio ou rastreamento de e-mail;
- entrega por WhatsApp, SMS ou qualquer canal externo;
- remocao, revogacao ou alteracao de membership existente;
- cancelamento ou revogacao manual de convite, salvo decisao humana explicita;
- transferencia de ownership;
- clientes, tickets ou demais recursos organizacionais;
- audit trail persistida completa.

## Riscos

- token de convite vazar por resposta, log, trace ou historico de navegador;
- conta diferente aceitar convite destinado a outra pessoa;
- papel convidado exceder autoridade do criador;
- convite duplicado para mesmo destinatario e organizacao;
- convite expirado ou ja aceito criar membership;
- resposta revelar existencia de conta ou organizacao;
- coleta de e-mail sem finalidade, retencao e protecao documentadas.

## Criterio para avancar

Proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana antes da implementacao.
