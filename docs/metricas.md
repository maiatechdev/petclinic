# Métricas do PetClinic antes e depois

A comparação usa o legado `v0-legado` (`df1596b`) e a `main` após os PRs #43, #44, #48 e #51 (`567c797`). A atualização para `c11b6ad` acrescentou somente o rascunho do relatório, com POM, código e testes iguais à revisão medida. A revisão `3d78d7c` do PR #46 já registra 48 testes na versão integrada. O quadro confirma esse total em três rodadas e acrescenta o NLOC e os tempos que seguem pendentes no documento de persistência. [PR #46](https://github.com/maiatechdev/petclinic/pull/46)

## Quadro geral

| Indicador | Legado `df1596b` | Integrado `567c797` |
|---|---:|---:|
| Arquivos Java de produção | 44 | 32 |
| Linhas de código Java de produção (NLOC) | 1.388 | 923 |
| Linhas de código no pacote `repository`, com interfaces (NLOC) | 541 | 77 |
| NLOC das implementações JDBC, JPA e Spring Data somadas | 504 | 40 |
| NLOC do pacote `web` | 338 | 337 |
| Métodos detectados pelo Lizard | 127 | 94 |
| Complexidade ciclomática média de produção | 1,3 | 1,3 |
| Complexidade ciclomática máxima de produção | 7 | 7 |
| Complexidade de `OwnerController.processFindForm` | 4 | 2 |
| Duplicação Java, jscpd `mild` | 10 clones, 144 linhas (4,97%) | 7 clones, 110 linhas (5,41%) |
| Duplicação Java, jscpd `weak`, sem imports | 11 clones, 110 linhas (4,87%) | 8 clones, 78 linhas (5,01%) |
| Métodos públicos com Javadoc, produção | 22/124 (17,7%) | 28/95 (29,5%) |
| Métodos públicos com Javadoc, `OwnerController` e `VisitController` | 1/12 (8,3%) | 11/11 (100%) |
| Testes executados pelo Maven | 33 | 48 |
| Falhas, erros e ignorados | 0 / 0 / 0 | 0 / 0 / 0 |
| Testes MockMvc de tutores e visitas | 0 | 13 |
| Execuções dos dez cenários de `AbstractClinicServiceTests` | 30 | 10 |
| Mediana de `mvn clean test`, tempo de parede | 8,397 s | 8,183 s |
| Mediana da soma dos tempos das suítes de serviço, Surefire | 2,698 s | 1,657 s |

### Leitura dos resultados

- **Persistência.** Os subpacotes JDBC, JPA e Spring Data tinham respectivamente **349, 115 e 40 NLOC**. Na integrada, permanece o Spring Data com **40 NLOC**. As interfaces dos repositórios correspondem às outras 37 linhas que completam o total de 77 do pacote.
- **Controllers.** O máximo do projeto continua sendo 7 em `PetValidator.validate`. A queda de 4 para 2 é específica de `processFindForm`. Nos dois controllers de tutores e visitas, o jscpd no modo `weak` com 30 tokens passou de 2 para 1 clone e de 7,54% para 5,37%. [Medição da #31](modulos/web-owner-visit.md)
- **Duplicação global.** Caíram os contadores absolutos de clones e linhas duplicadas. A porcentagem cresceu porque o denominador do jscpd encolheu mais. No modo `mild`, ele passou de 2.898 para 2.034 linhas analisadas.
- **Contagem dos testes.** Os 13 métodos distintos do legado geravam 33 execuções, pois dez cenários de serviço eram executados nas três implementações. O ponto imediatamente anterior ao #43 tinha 46 execuções. A retirada das duas suítes levou esse total a 26, e os PRs #44 (+5) e #51 (+17) o elevaram a **48**. Assim, a redução de 20 se refere a **46 → 26**. Entre o legado e o marco de 26 também entraram os 13 testes web.

## Método e reprodução

As duas revisões foram exportadas separadamente, sem alterações nos POMs. O Lizard 1.26.1 foi executado em `src/main/java` e nos pacotes indicados na tabela. O jscpd 4.3.0 analisou os mesmos arquivos, com cinco linhas e 50 tokens como mínimos. Os totais vêm de `statistics.total` do JSON. Exemplo em cada cópia:

```powershell
lizard src/main/java
lizard src/main/java/org/springframework/samples/petclinic/repository
npx.cmd --yes jscpd@4.3.0 --format java --mode mild --min-lines 5 --min-tokens 50 --reporters json --output jscpd-mild --no-gitignore src/main/java
npx.cmd --yes jscpd@4.3.0 --format java --mode weak --min-lines 5 --min-tokens 50 --ignore-pattern '^\s*import[^\r\n]*' --reporters json --output jscpd-weak --no-gitignore src/main/java
```

O Javadoc foi contado pela árvore sintática do Java 8 (`JavacTask.parse` e `DocTrees`). O denominador inclui métodos declarados públicos e métodos implicitamente públicos de interfaces. Construtores, comentários de classe e documentação herdada ficam fora.

Os testes rodaram no mesmo Windows 11 com **Corretto 8u514**, **Maven 3.9.14**, HSQLDB e cache Maven já preenchido. Foram três execuções sequenciais de `mvn -o clean test` por revisão, alternando a ordem entre as rodadas. A mediana do tempo de parede inclui inicialização, limpeza, compilação e testes. Os tempos de serviço vêm da soma do atributo `time` dos XMLs Surefire de `ClinicServiceJdbcTests`, `ClinicServiceJpaTests` e `ClinicServiceSpringDataJpaTests` que existem em cada revisão.

| Rodada | Legado completo | Integrado completo | Serviço legado | Serviço integrado |
|---|---:|---:|---:|---:|
| 1 | 9,906 s | 8,382 s | 3,246 s | 1,657 s |
| 2 | 8,397 s | 8,183 s | 2,698 s | 1,672 s |
| 3 | 8,200 s | 8,158 s | 2,607 s | 1,640 s |

Os tempos descrevem essa máquina e essa execução. A diferença pequena na suíte completa não sustenta uma conclusão sobre desempenho da aplicação.
