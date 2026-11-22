# Registro de contribuições da equipe

Este documento reúne a evidência de participação de cada integrante no repositório `maiatechdev/petclinic`, que é o critério de verificação individual descrito na proposta do projeto (commits, pull requests e revisões de código). Os dados vêm do Git e da API do GitHub, e os comandos para reproduzi-los estão na seção 5.

> **Retrato de 22/11/2026, 20:24.** Os números mudam até a entrega. Antes de entregar, **regenerar** com os comandos da seção 5 e atualizar o retrato (issue #40).

## 1. Critérios

A proposta exige contribuição ativa de todos. A equipe adotou como meta mínima, por integrante, **3 pull requests e 3 revisões**, com uma aprovação de outro integrante em cada PR e *merge* com histórico preservado (regras em `CONTRIBUTING.md`). A `main` é protegida e não aceita *push* direto.

## 2. Integrantes

**Quadro 1 - Integrantes e contas do GitHub**

| Nº | Integrante | Conta no GitHub |
|---|---|---|
| 1 | Gabriel Maia | `maiatechdev` |
| 2 | Kezia | `keziazcs` |
| 3 | Davi | `davv1-dev` |
| 4 | Gabriel Torre | `gabrieltorre-png` |
| 5 | Leo | `leo-case` |

Fonte: elaborado pelos autores (2026).

O integrante 1 aparece no Git com duas identidades de autor (`maiatechdev <maiatechdev@gmail.com>` e `Gabriel Maia <noreply do GitHub>`). As duas são da mesma pessoa. Nos commits de *merge*, o GitHub usa a segunda.

## 3. Resumo por integrante

**Quadro 2 - Contribuições até o retrato**

| Integrante | Commits na `main` | PRs abertos | PRs mergeados | Revisões feitas | PRs revisados | Issues fechadas |
|---|---:|---:|---:|---:|---:|---:|
| 1 Gabriel Maia | 13 | 11 | 7 | 8 | 7 | 3 |
| 2 Kezia | 0 | 0 | 0 | 0 | 0 | 0 |
| 3 Davi | 8 | 6 | 6 | 10 | 7 | 4 |
| 4 Gabriel Torre | 0 | 1 | 0 | 0 | 0 | 0 |
| 5 Leo | 0 | 0 | 0 | 0 | 0 | 0 |

Fonte: elaborado pelos autores com `git log` e a API do GitHub (22/11/2026, 20:24).

Como ler o quadro:

- **Commits na `main`:** commits que não são de *merge*, feitos depois do commit legado `df1596b` (08/08/2014), que é o ponto de partida do trabalho. Os commits anteriores, do projeto original, ficam de fora. Os commits de *merge* (13 pelo GitHub e 7 locais, todos do integrante 1) não contam como contribuição de código.
- **Revisões feitas:** eventos de revisão (aprovar, pedir mudanças ou comentar) em PRs de outras pessoas. Um mesmo PR pode ter mais de um evento, e por isso há uma coluna de PRs distintos revisados.
- **Trabalho em andamento:** o integrante 4 tem 13 commits na branch do PR #54, ainda não mergeado. Esses commits entram na coluna de commits da `main` quando o PR for mergeado.
- **Outras formas de participação, que o quadro não conta:** o integrante 2 comentou no PR #56 com uma validação própria em JDK 8 (53 testes, conferência no Tomcat) e apontou uma referência restante ao Joda-Time no `VetsAtomView`.

## 4. Pull requests

**Quadro 3 - Pull requests, autores e revisores**

| PR | Autor | Tema | Revisão por | Estado |
|---|---|---|---|---|
| #42 | 1 | `CONTRIBUTING` e template de PR | (merge direto, antes da proteção) | Mergeado |
| #43 | 1 | Remove JDBC e JPA, mantém Spring Data | 3 | Mergeado |
| #44 | 1 | Banco por ambiente e credenciais | 3 | Mergeado |
| #45 | 1 | Java 1.8 e ECJ no `pom.xml` | 3 | Mergeado |
| #46 | 1 | Antes/Depois da persistência | 3 | Em revisão |
| #47 | 3 | Testes de caracterização de tutores e visitas | 1 | Mergeado |
| #48 | 1 | Driver MySQL em execução | 3 | Mergeado |
| #49 | 3 | Refatora os controllers de tutores e visitas | 1 | Mergeado |
| #50 | 3 | Métricas e plano de modularização | 1 | Mergeado |
| #51 | 1 | Testes de consultas dos repositórios | 3 | Mergeado |
| #52 | 3 | Seção web do relatório | 1 | Mergeado |
| #53 | 3 | Métricas e roteiro da demonstração | 1 | Mergeado |
| #54 | 4 | Testes de `PetController` e `PetValidator` | 1 (mudanças pedidas) | Em revisão |
| #55 | 1 | Mockito como dependência de teste | 3 | Mergeado |
| #56 | 3 | Joda-Time para `java.time` | 1 (comentário do 2) | Mergeado |
| #57 | 1 | Rascunho do plano de atualização | (pendente) | Em revisão |
| #58 | 1 | Rascunho da introdução e persistência | (pendente) | Em revisão |
| #59 | 1 | Nomes no roteiro da apresentação | (pendente) | Em revisão |

Fonte: elaborado pelos autores com a API do GitHub (22/11/2026, 20:24).

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

- [ ] Regenerar os Quadros 2 e 3 no fim, depois dos últimos *merges*.
- [ ] Conferir que cada integrante atinge a meta de 3 PRs e 3 revisões, e registrar aqui quem ainda falta.
- [ ] Registrar a participação nos documentos do relatório (#38) e na apresentação (#39).
- [ ] Anexar a lista de PRs ao relatório como evidência de participação, se a professora pedir.
