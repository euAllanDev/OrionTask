# Proposal: Gestao de clientes

## Change ID

`manage-clients`

## Status

Arquivada em 2026-08-09 apos revisao humana final.

## Problema

Organizacao nao possui cadastro proprio de clientes. Sem recurso organizacional para identifica-los, tickets futuros nao podem ser associados a quem solicitou atendimento e equipe depende de dados dispersos.

## Objetivo

Permitir que membros autorizados criem, consultem, editem e desativem clientes da propria organizacao, com dados minimos, isolamento multiempresa e preservacao para historico futuro.

## Escopo pretendido

- criar cliente em organizacao explicita;
- consultar e listar clientes autorizados;
- editar campos aprovados;
- desativar cliente sem exclusao fisica;
- aplicar autorizacao, CSRF, isolamento e respostas anti-enumeracao;
- persistir `organization_id`, UUID nao sequencial e timestamps UTC;
- registrar evento operacional minimo para mutacoes;
- incluir testes unitarios, HTTP e PostgreSQL/Testcontainers.

## Fora do escopo

- portal, conta ou autenticacao de cliente;
- tickets, mensagens, anexos, contratos, faturamento ou CRM;
- importacao em massa;
- exclusao fisica, anonimização, exportacao ou retencao definitiva;
- busca avancada, tags, campos customizados e enderecos;
- notificacao por e-mail ou WhatsApp.

## Riscos

- coletar contato pessoal sem finalidade ou retencao definida;
- acesso a cliente de outra organizacao por UUID conhecido;
- usuario sem autoridade alterar ou desativar cliente;
- desativacao apagar dados necessarios para historico de tickets futuro;
- resposta, log ou evento expor dados pessoais desnecessarios;
- regra de unicidade impedir clientes validos com dados semelhantes.

## Criterio para avancar

Decisoes em `open-questions.md`, proposal, design, delta spec e tarefas devem receber revisao e aprovacao humana antes da implementacao.
