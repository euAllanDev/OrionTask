# Open Questions: Verificar isolamento organizacional

## Q1 - Local dos testes

Decisao aprovada: criar `OrganizationIsolationIntegrationTest` dedicado. Isolamento e fronteira de seguranca ficam localizaveis, e evita crescimento da classe atual sem alterar producao.

## Q2 - Cobertura de convite

Decisao aprovada: manter convites fora desta change. Convites nao expoem leitura ou listagem e ja possuem testes de autorizacao; adicionar teste cruzado pode entrar em change de auditoria de todas as capacidades se evidencia adicional for necessaria.

## Q3 - Evidencia de nao enumeracao

Decisao aprovada: comparar status e corpo onde a spec exige resposta indistinguivel; demais cenarios verificam status e estado persistido.
