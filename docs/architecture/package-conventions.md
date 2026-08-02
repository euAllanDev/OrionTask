# Convencoes de Pacotes

## Estrutura por modulo

Cada capacidade de negocio deve usar o prefixo `com.oriontask.<modulo>` e criar somente os pacotes que possuirem classes reais.

```text
com.oriontask.<modulo>
├── domain
│   ├── model
│   ├── event
│   ├── service
│   └── exception
├── application
│   ├── port
│   │   ├── in
│   │   └── out
│   ├── usecase
│   └── command
└── adapter
    ├── in
    │   └── web
    └── out
        ├── persistence
        ├── messaging
        └── external
```

Os nomes de subpacotes podem ser refinados na primeira change do modulo. Esta convencao fixa as fronteiras e as direcoes de dependencia, nao exige uma arvore vazia.

## Dependencias permitidas

- `domain` contem Java puro e nao depende de Spring, JPA, HTTP, serializacao, banco ou adaptadores;
- `application` coordena casos de uso e depende do dominio;
- `adapter.in` recebe chamadas externas e invoca portas de entrada;
- `adapter.out` implementa portas de saida definidas pela aplicacao ou dominio;
- entidades JPA pertencem a `adapter.out.persistence`;
- controllers nao acessam repositories diretamente;
- modulos nao acessam detalhes internos de outros modulos;
- `shared` deve ser minimo e nao pode se tornar um deposito generico.

As regras ArchUnit da fundacao devem validar essas fronteiras sem exigir a existencia antecipada de modulos de negocio.
