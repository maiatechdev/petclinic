# Registro de contribuições da equipe

Este documento reúne a evidência de participação de cada integrante no repositório `maiatechdev/petclinic`, que é o critério de verificação individual descrito na proposta do projeto (commits, pull requests e revisões de código). Os dados vêm do Git e da API do GitHub, e os comandos para reproduzi-los estão na seção 5.

> **Retrato de 23/11/2026, 23:56.** Os números mudam se houver novos *merges* ou revisões. Para atualizar, usar os comandos da seção 5 (issue #40).

## 1. Critérios

A proposta exige contribuição ativa de todos. A equipe adotou como meta mínima, por integrante, **3 pull requests e 3 revisões**, com uma aprovação de outro integrante em cada PR e *merge* com histórico preservado (regras em `CONTRIBUTING.md`). A `main` é protegida e não aceita *push* direto.

## 2. Integrantes

**Quadro 1 - Integrantes e contas do GitHub**

| Nº | Integrante | Conta no GitHub |
|---|---|---|
| 1 | Andrey Gabriel Maia Serra | `maiatechdev` |
| 2 | Kézia Carneiro Silva | `keziazcs` |
| 3 | Davi Amaral Santana | `davv1-dev` |
| 4 | Gabriel La Torre | `gabrieltorre-png` |
| 5 | Leonardo Casé Góes Fernandez Santos | `leo-case` |

Fonte: elaborado pelos autores (2026).

O integrante 1 aparece no Git com duas identidades de autor (`maiatechdev <maiatechdev@gmail.com>` e `Gabriel Maia <noreply do GitHub>`). As duas são da mesma pessoa. Nos commits de *merge*, o GitHub usa a segunda.

## 3. Resumo por integrante

**Quadro 2 - Contribuições até o retrato**

| Integrante | Commits na `main` | PRs abertos | PRs mergeados | Revisões feitas | PRs revisados | Issues fechadas |
|---|---:|---:|---:|---:|---:|---:|
| 1 Andrey Gabriel Maia Serra | 25 | 13 | 10 | 21 | 17 | 8 |
| 2 Kézia Carneiro Silva | 7 | 5 | 5 | 4 | 4 | 6 |
| 3 Davi Amaral Santana | 8 | 6 | 6 | 22 | 14 | 6 |
| 4 Gabriel La Torre | 14 | 3 | 2 | 0 | 0 | 3 |
| 5 Leonardo Casé Góes Fernandez Santos | 16 | 5 | 5 | 2 | 2 | 3 |

Fonte: elaborado pelos autores com `git log` e a API do GitHub (23/11/2026, 23:56).

Como ler o quadro:

- **Commits na `main`:** commits que não são de *merge*, feitos depois do commit legado `df1596b` (08/08/2014), que é o ponto de partida do trabalho. Os commits anteriores, do projeto original, ficam de fora. Os commits de *merge* (13 pelo GitHub e 7 locais, todos do integrante 1) não contam como contribuição de código.
- **Revisões feitas:** eventos de revisão (aprovar, pedir mudanças ou comentar) em PRs de outras pessoas. Um mesmo PR pode ter mais de um evento, e por isso há uma coluna de PRs distintos revisados.
- **Revisões ainda abertas:** os PRs #60, #69 e #71 seguem em revisão. O #71 aguarda correção do autor, e o #69 aguarda nova revisão do integrante 3.
- **Outras formas de participação, que o quadro não conta:** o integrante 2 comentou no PR #56 com uma validação própria em JDK 8 e apontou uma referência restante ao Joda-Time no `VetsAtomView`.

## 4. Pull requests

**Quadro 3 - Pull requests, autores e revisores**

| PR | Autor | Tema | Revisão por | Estado |
|---|---|---|---|---|
| #42 | 1 | `CONTRIBUTING` e template de PR | (merge direto, antes da proteção) | Mergeado |
| #43 | 1 | Remove JDBC e JPA, mantém Spring Data | 3 | Mergeado |
| #44 | 1 | Banco por ambiente e credenciais | 3 | Mergeado |
| #45 | 1 | Java 1.8 e ECJ no `pom.xml` | 3 | Mergeado |
| #46 | 1 | Antes/Depois da persistência | 3 | Mergeado |
| #47 | 3 | Testes de caracterização de tutores e visitas | 1 | Mergeado |
| #48 | 1 | Driver MySQL em execução | 3 | Mergeado |
| #49 | 3 | Refatora os controllers de tutores e visitas | 1 | Mergeado |
| #50 | 3 | Métricas e plano de modularização | 1 | Mergeado |
| #51 | 1 | Testes de consultas dos repositórios | 3 | Mergeado |
| #52 | 3 | Seção web do relatório | 1 | Mergeado |
| #53 | 3 | Métricas e roteiro da demonstração | 1 | Mergeado |
| #54 | 4 | Testes de `PetController` e `PetValidator` | 1, 3 | Mergeado |
| #55 | 1 | Mockito como dependência de teste | 3 | Mergeado |
| #56 | 3 | Joda-Time para `java.time` | 1 | Mergeado |
| #57 | 1 | Plano de atualização tecnológica | 2 | Mergeado |
| #58 | 1 | Introdução e persistência do relatório | 2, 5 | Mergeado |
| #59 | 1 | Nomes no roteiro da apresentação | 3 | Mergeado |
| #60 | 1 | Registro de contribuições | 2 | Em revisão |
| #61 | 5 | Testes de rotas e cache de veterinários | 3 | Mergeado |
| #62 | 5 | Refatora o módulo de veterinários | 3 | Mergeado |
| #63 | 2 | README para JDK 8 | 1 | Mergeado |
| #64 | 2 | README: seção de banco | 1 | Mergeado |
| #65 | 2 | Remove código comentado do `VetsAtomView` | 1 | Mergeado |
| #66 | 2 | Antes/Depois do `pom.xml` | 1 | Mergeado |
| #67 | 5 | Mensagens em português | 1, 3 | Mergeado |
| #68 | 5 | Javadoc do `ClinicService` | 1, 3 | Mergeado |
| #69 | 1 | Estrutura do relatório | 3 | Em revisão |
| #70 | 2 | Seções 2, 5.4 e 6 do relatório | 1 | Mergeado |
| #71 | 4 | Seções 4.2 e 5.3 do relatório | 1, 2, 5 | Em revisão |
| #72 | 5 | Seções 5.5, 7 e 8 do relatório | 1 | Mergeado |
| #73 | 4 | Testes de `PetTypeFormatter` e de data nula | 1 | Mergeado |

Fonte: elaborado pelos autores com a API do GitHub (23/11/2026, 23:56).

## 5. Como regenerar os números

Rodar dentro do repositório, depois de `git fetch origin`:

```bash
# Commits (sem merges) por autor, desde o commit legado
git log df1596b..origin/main --no-merges --format='%an <%ae>' | sort | uniq -c

# Commits de merge por autor (não contam como código)
git log df1596b..origin/main --merges --format='%an' | sort | uniq -c

# Pull requests: autor, estado e título
gh pr list --state all --limit 100 --json number,title,author,state \
  --jq 'sort_by(.number)|.[]|"#\(.number) \(.author.login) \(.state) \(.title)"'

# Revisões por autor e estado
gh api graphql -f query='{repository(owner:"maiatechdev",name:"petclinic"){pullRequests(first:100){nodes{number reviews(first:30){nodes{author{login} state}}}}}}' \
  --jq '.data.repository.pullRequests.nodes[].reviews.nodes[]|"\(.author.login) \(.state)"' | sort | uniq -c

# Issues fechadas por responsável
gh issue list --state closed --limit 100 --json number,assignees \
  --jq '.[]|"#\(.number) \(.assignees|map(.login)|join(","))"'
```

Para o número de **PRs revisados** (distintos), listar as revisões com o número do PR e contar cada par autor-PR uma única vez. Ao somar as identidades do integrante 1, juntar `maiatechdev` e `Gabriel Maia`.

## 6. Pendências até a entrega

- [x] Quadros 2 e 3 regenerados depois do PR #73. Regenerar de novo se houver novos *merges*.
- [ ] Conferir que cada integrante atinge a meta de 3 PRs e 3 revisões, e registrar aqui quem ainda falta.
- [ ] Registrar a participação nos documentos do relatório (#38) e na apresentação (#39).
- [ ] Anexar a lista de PRs ao relatório como evidência de participação, se a professora pedir.
