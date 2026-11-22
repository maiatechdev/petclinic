# Guia de contribuição

Regras curtas para que os cinco integrantes trabalhem no mesmo padrão. A base do trabalho é o commit `df1596b` (tag `v0-legado`), que não deve ser alterado.

## 1. Antes de qualquer alteração

```
git checkout main
git pull origin main
git checkout -b feature/iN-assunto
```

Antes de abrir o PR, atualize a branch:

```
git pull --rebase origin main
```

Nunca faça commit direto na `main`. Nunca use `push --force` em branch compartilhada.

## 2. Branches

Formato: `feature/iN-assunto`, em que `N` é o número do integrante (1 a 5).

Exemplos: `feature/i1-remove-jdbc-jpa`, `feature/i4-pet-validator`.

Branches têm vida curta: abra o PR no mesmo dia em que a tarefa ficar pronta.

## 3. Commits

Formato (Conventional Commits):

```
tipo(escopo): descrição no imperativo, até 72 caracteres

Corpo opcional explicando o porquê da mudança.

Closes #N
```

Tipos:

| Tipo | Uso |
|---|---|
| `feat` | funcionalidade nova |
| `fix` | correção de defeito |
| `refactor` | mudança de estrutura sem mudar comportamento |
| `test` | testes novos ou ajustados |
| `docs` | documentação, Javadoc, relatório |
| `build` | `pom.xml`, plugins, dependências |
| `chore` | tarefas de manutenção do repositório |

Escopos: `persistencia`, `web`, `dominio`, `plataforma`, `vet`, `api`, `docs`, `config`.

Exemplos:

- `refactor(web): extrai constantes de view do OwnerController`
- `test(dominio): caracteriza regras do PetValidator`
- `build(plataforma): adiciona ecj ao tomcat7-maven-plugin`

Regras:

- Um commit, uma ideia. Cada commit deve compilar e passar nos testes.
- Mensagem escrita pelo próprio integrante, sem rodapés automáticos de ferramentas.
- Referencie a issue no rodapé (`Closes #N` ou `Refs #N`).

## 4. Pull requests

- Título no mesmo padrão dos commits.
- Tamanho de até cerca de 150 linhas de diff, exceto remoções em massa (por exemplo, apagar implementações de repositório).
- Preencher o template de PR.
- **1 aprovação obrigatória de outro integrante.** Ninguém aprova o próprio PR.
- Rodízio de revisão: 1 revisa 2, 2 revisa 3, 3 revisa 4, 4 revisa 5, 5 revisa 1.
- Usar **merge commit** (sem squash), para o histórico preservar o trabalho de cada integrante.
- Responder todos os comentários de review antes do merge.

## 5. Definição de pronto

Um PR só está pronto quando:

- [ ] `mvn clean test` passa.
- [ ] O sistema sobe (`mvn tomcat7:run`, JDK 8) e a tela alterada foi conferida manualmente.
- [ ] Existe teste de caracterização do comportamento anterior (Feathers) antes da refatoração.
- [ ] Há testes novos para o que foi criado ou alterado.
- [ ] Métodos públicos novos ou alterados têm Javadoc.
- [ ] O trecho Antes/Depois foi registrado em `docs/modulos/<modulo>.md`.

## 6. Código

- Nomes de classes, métodos e variáveis em inglês, como no legado.
- Textos de tela em `messages*.properties`, nunca fixos no JSP ou no controller.
- Nomes de views e números repetidos viram constantes.
- Métodos pequenos, com um propósito. Comentários explicam o porquê, não o quê.
- Não deixar código comentado nem `TODO` sem número de issue.
- Antes de `git add`, conferir `git status`. Não versionar arquivos locais, pastas de IDE, `target/` ou ferramentas pessoais. Evitar `git add .`.

## 7. Arquivos compartilhados e seus donos

Para evitar conflito de merge, estes arquivos têm um responsável. Quem precisar de mudança neles pede por comentário na issue correspondente.

| Arquivo | Responsável |
|---|---|
| `pom.xml` | Integrante 2 (keziazcs) |
| `web.xml`, `business-config.xml`, `datasource-config.xml`, `data-access*.properties` | Integrante 1 (maiatechdev) |
| `messages*.properties` | Integrante 5 (leo-case) |
| `Pet.java` | Integrante 2 primeiro (java.time); depois Integrante 4 |
| `.github/**`, `CONTRIBUTING.md` | Integrante 1 |

## 8. Documentação

Cada integrante registra seu trabalho em `docs/modulos/<modulo>.md` com:

1. Problema encontrado (arquivo, linha, tipo de code smell).
2. Código **Antes**.
3. Código **Depois**.
4. Explicação técnica da melhoria.
5. Testes que comprovam que o comportamento foi preservado.
6. Métricas antes e depois (linhas, complexidade, duplicação), quando aplicável.
