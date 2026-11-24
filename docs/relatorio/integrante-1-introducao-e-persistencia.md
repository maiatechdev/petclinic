# Evolução e modernização do Spring PetClinic: introdução, sistema legado e persistência

> **Texto do Integrante 1** para a issue #38. Cobre capa, introdução, ficha técnica, requisitos e a seção de persistência e configuração. Os itens entre colchetes são dados que só o grupo tem (nomes e professora).

---

## CAPA

UNIVERSIDADE CATÓLICA DO SALVADOR (UCSAL)
ENGENHARIA DE SOFTWARE

[NOME COMPLETO DO INTEGRANTE 1]
[NOME COMPLETO DO INTEGRANTE 2]
[NOME COMPLETO DO INTEGRANTE 3]
[NOME COMPLETO DO INTEGRANTE 4]
[NOME COMPLETO DO INTEGRANTE 5]

**EVOLUÇÃO E MODERNIZAÇÃO DE UM SISTEMA LEGADO: REFATORAÇÃO DO SPRING PETCLINIC**

Trabalho da disciplina de Evolução de Software, apresentado à professora [NOME DA PROFESSORA].

Salvador
2026

---

## 1 INTRODUÇÃO

Sistemas de software continuam em uso muito depois de terem sido escritos, e é durante esse uso que a maior parte do custo aparece. Sommerville (2019) trata a evolução como a continuação do desenvolvimento após a entrega, e a reengenharia como o conjunto de atividades que torna um sistema existente mais fácil de manter sem alterar o que ele faz. Feathers (2013) chama de código legado aquele que não tem testes, porque sem eles ninguém consegue alterá-lo com segurança. Fowler (2020) define a refatoração como a mudança da estrutura interna do código sem mudar o seu comportamento observável.

Este trabalho aplica esses conceitos a um sistema real. Escolhemos o Spring PetClinic, aplicação de referência do ecossistema Spring que gerencia uma clínica veterinária (tutores, animais, visitas e veterinários), em uma versão de agosto de 2014 (commit `df1596b`, marcado como `v0-legado` no repositório do grupo). Essa revisão usa Spring 4.0, Hibernate 4.3, páginas JSP, Ehcache 2 e Bootstrap 2, e não possui documentação de requisitos nem testes de controllers. O repositório do trabalho é um fork público em `github.com/maiatechdev/petclinic`.

### 1.1 Objetivos

O **objetivo geral** é diagnosticar o débito técnico do sistema legado, refatorá-lo de forma incremental, mantendo-o funcional, e planejar a sua evolução tecnológica.

Os **objetivos específicos** são:

a) reconstruir a arquitetura e os requisitos do sistema a partir do código (engenharia reversa);
b) medir acoplamento, duplicação, complexidade ciclomática, documentação e testes, para ter uma base de comparação;
c) escrever testes de caracterização antes de cada refatoração, como propõe Feathers (2013);
d) refatorar os módulos de persistência, controllers, domínio e plataforma, registrando o Antes e o Depois com explicação técnica;
e) propor planos de atualização tecnológica e de modularização para a evolução futura.

### 1.2 Metodologia

O trabalho seguiu cinco etapas. Primeiro, a engenharia reversa do commit `df1596b`, com leitura do código, execução do sistema e levantamento de requisitos. Depois, o diagnóstico de débito técnico com ferramentas: o Lizard 1.26.1 para a complexidade ciclomática (McCABE, 1976) e o NLOC (linhas de código sem comentários e linhas em branco), o jscpd 4.3.0 para duplicação e um script próprio para o acoplamento. Em seguida, a refatoração, sempre precedida de testes que fixam o comportamento atual. Por fim, a medição do resultado e a redação deste relatório.

Toda a execução usou JDK 8 (Amazon Corretto 8u514) e Maven 3.9.14, em Windows 11. A colaboração seguiu um fluxo único no GitHub: uma *issue* por tarefa, uma *branch* por entrega, *pull request* de poucas linhas, ao menos uma aprovação de outro integrante e *merge* com histórico preservado, de modo que cada contribuição fica registrada por autor. A `main` é protegida e não aceita *push* direto.

### 1.3 Estrutura do trabalho

A seção 2 descreve o sistema legado: ficha técnica, funcionalidades e requisitos. A seção 3 trata da persistência e da configuração, módulo que o Integrante 1 refatorou. As demais seções, escritas pelos outros integrantes, tratam da fundamentação teórica, da arquitetura e dos controllers, do domínio, da plataforma, da documentação e da conclusão.

---

## 2 O SISTEMA LEGADO

### 2.1 Ficha técnica

**Quadro 1 - Ficha técnica do sistema legado**

| Item | Descrição |
|---|---|
| Nome | Spring PetClinic |
| Objetivo | Aplicação web de clínica veterinária: cadastro de tutores e animais, registro de visitas e consulta de veterinários |
| Revisão estudada | `df1596b`, de 08/08/2014 (tag `v0-legado`) |
| Linguagem e build | Java 1.6 declarado no `pom.xml`, Maven, empacotamento WAR |
| Frameworks | Spring Framework 4.0.6, Spring Data JPA 1.6.2, Hibernate 4.3.6, Hibernate Validator 4.3.1 |
| Camada de visão | JSP 2.2 e JSTL 1.2, duas tags próprias, Bootstrap 2.3.0, jQuery 2.0.3 e Dandelion DataTables 0.10.0 |
| Servidor | Tomcat 7, por meio do `tomcat7-maven-plugin` 2.0 |
| Banco | HSQLDB 2.3.2 em memória (padrão) e MySQL |
| Cache e monitoramento | Ehcache 2.6.9 e um aspecto com AspectJ 1.8.1 exposto por JMX |
| Datas | Joda-Time 2.4 com Jadira 3.2.0 |
| Tamanho | 44 arquivos Java de produção e 1.388 linhas de código (NLOC) |
| Testes | 13 métodos de teste, que geram 33 execuções |

Fonte: elaborado pelos autores a partir do `pom.xml` e do código da revisão `df1596b`, com Lizard 1.26.1 (2026).

Para executar o sistema legado em um ambiente atual foi necessário corrigir o `pom.xml`: o compilador de JSP do plugin do Tomcat (2012) não lê classes do Java 8, o que fazia todas as páginas responderem HTTP 500 (`JasperException` em `java.util.Map$Entry`). O ajuste foi acrescentar o compilador ECJ ao plugin (PR #45).

### 2.2 Funcionalidades e requisitos

Como o projeto não possui documentação de requisitos, os requisitos foram levantados a partir dos controllers, das anotações de validação nas entidades, das consultas ao banco e das páginas JSP.

**Quadro 2 - Requisitos funcionais do sistema legado**

| Código | Requisito |
|---|---|
| RF01 | Buscar tutores pelo sobrenome, listando todos quando a busca é feita com o campo vazio |
| RF02 | Cadastrar tutor com nome, sobrenome, endereço, cidade e telefone |
| RF03 | Editar os dados de um tutor |
| RF04 | Exibir os dados de um tutor com seus animais e o histórico de visitas de cada um |
| RF05 | Cadastrar e editar animais de um tutor, com nome, tipo e data de nascimento |
| RF06 | Registrar visitas de um animal, com data e descrição |
| RF07 | Listar os veterinários e suas especialidades, também nos formatos XML e Atom |
| RF08 | Exibir uma página de erro quando ocorre uma exceção no sistema |

Fonte: elaborado pelos autores a partir do código (2026).

Entre as regras de negócio, destaca-se uma inconsistência: o nome do animal não pode se repetir entre os animais do mesmo tutor e o tipo é obrigatório, mas as duas verificações só acontecem no cadastro, e não na edição. Entre os requisitos não funcionais, o sistema legado não tem autenticação, perde todos os dados ao reiniciar (HSQLDB em memória), mantém usuário e senha do banco em arquivo e só oferece XML e Atom para os veterinários, sem API em JSON.

---

## 3 PERSISTÊNCIA E CONFIGURAÇÃO

### 3.1 Diagnóstico

Quatro problemas se destacaram na camada de persistência do legado.

O **primeiro** é a duplicação estrutural. As quatro interfaces de repositório (tutor, animal, visita e veterinário) têm três implementações completas cada: JDBC com SQL escrito à mão, JPA com consultas JPQL e Spring Data JPA. Somam 504 linhas de código (349, 115 e 40) para entregar o mesmo comportamento. Apenas uma é usada por vez, mas as três precisam acompanhar qualquer mudança no modelo, o que corresponde aos *smells* de código duplicado e de mudança divergente (FOWLER, 2020).

O **segundo** é a forma de escolher a implementação. A escolha é feita por perfil do Spring, e o perfil é trocado editando e comentando blocos no `web.xml`, o que é fonte de erro e deixa a configuração difícil de ler.

O **terceiro** é o custo nos testes. Dez cenários de serviço são executados nas três implementações, o que dá 30 execuções para 10 comportamentos.

O **quarto** é a configuração do banco. Usuário e senha ficam fixos em `data-access.properties`, trocar de banco exige editar o arquivo, e o script `initDB.sql` do MySQL, executado pela própria aplicação no início, cria o banco e concede todos os privilégios a uma conta com senha fixa:

```sql
CREATE DATABASE IF NOT EXISTS petclinic;
GRANT ALL PRIVILEGES ON petclinic.* TO pc@localhost IDENTIFIED BY 'pc';
```

### 3.2 Intervenção

Seguindo Feathers (2013), a rede de proteção veio antes das mudanças: os testes de serviço já existiam e foram complementados por testes de controllers (PRs #47 e #49) e por 17 testes de consultas dos repositórios (PR #51). As quatro mudanças foram feitas em *pull requests* separados.

1. **Remoção das implementações JDBC e JPA (PR #43).** Foram apagados os pacotes `repository.jdbc` e `repository.jpa` e os testes correspondentes. Os beans de JPA passaram a ser carregados sem perfil, e o `web.xml` deixou de selecionar a implementação.
2. **Configuração por ambiente (PR #44).** O arquivo único de propriedades foi dividido em `data-access-hsqldb.properties` (padrão) e `data-access-mysql.properties`, escolhidos pela propriedade `petclinic.db`. No MySQL, URL, usuário e senha vêm de variáveis de ambiente, e a senha é obrigatória, sem valor padrão.
3. **Separação entre esquema e administração (PR #44).** O `initDB.sql` do MySQL passou a conter só o esquema. A criação do banco e de um usuário com privilégios mínimos (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE`, `INDEX` e `REFERENCES`) ficou em `createDB.sql`, executado uma vez por um administrador.
4. **Driver do MySQL (PR #48).** O Connector/J 5.1.49 passou a ser dependência de execução, no lugar de um bloco comentado do `pom.xml`.

**Antes** (`web.xml`): o perfil de persistência era escolhido em parâmetro de contexto, com alternativas comentadas.

```xml
<context-param>
    <param-name>spring.profiles.active</param-name>
    <param-value>jpa</param-value>
</context-param>
<!-- When using Spring JDBC, use the following: -->
<!--  <context-param> ... <param-value>jdbc</param-value> ... -->
```

**Depois** (`business-config.xml`): uma única linha declara os repositórios, sem perfil.

```xml
<jpa:repositories base-package="org.springframework.samples.petclinic.repository.springdatajpa"/>
```

A mesma consulta, buscar tutores pelo sobrenome, mostra a diferença. No JDBC ela exigia SQL, mapeamento manual e a carga separada dos animais; no Spring Data ela é uma declaração:

```java
@Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
public Collection<Owner> findByLastName(@Param("lastName") String lastName);
```

Para trocar de banco, basta iniciar a aplicação com `-Dpetclinic.db=mysql` (ou a variável de ambiente `PETCLINIC_DB=mysql`), sem editar XML nem o `pom.xml`.

### 3.3 Resultados

O Quadro 3 reúne as medições. Os valores de código vêm do Lizard 1.26.1 e os de tempo, de três rodadas de `mvn clean test` por revisão, alternando a ordem entre elas, com a mediana apresentada.

**Quadro 3 - Persistência antes e depois da refatoração**

| Indicador | Legado `df1596b` | Integrado |
|---|---:|---:|
| NLOC das implementações de repositório (JDBC, JPA e Spring Data) | 504 | 40 |
| NLOC do pacote `repository`, com as interfaces | 541 | 77 |
| Linhas de `business-config.xml` | 95 | 59 |
| Linhas de `web.xml` | 128 | 109 |
| Execuções dos 10 testes de serviço | 30 | 10 |
| Soma dos tempos das suítes de serviço (mediana) | 2,698 s | 1,657 s |
| Tempo de `mvn clean test` (mediana) | 8,397 s | 8,183 s |

Fonte: elaborado pelos autores com Lizard 1.26.1, JDK 8 (Corretto 8u514) e Maven 3.9.14 (2026).

A queda de 504 para 40 linhas nas implementações eliminou duas das três bases de código duplicadas. A suíte de serviço ficou cerca de 39% mais rápida, o que é coerente com a retirada de 20 execuções repetidas. O tempo total de `mvn clean test` variou pouco, e essa diferença pequena não permite concluir nada sobre o desempenho da aplicação.

**Quadro 4 - Evolução do total de testes**

| Ponto | Testes | Origem |
|---|---:|---|
| Legado `df1596b` com o ajuste de build | 33 | PR #45 |
| Imediatamente antes da remoção de JDBC e JPA | 46 | PRs #47 e #49 |
| Depois da remoção de JDBC e JPA | 26 | PR #43 |
| Com a configuração por ambiente | 31 | PR #44 |
| Com os testes de persistência | 43 | PR #51 |
| Com `java.time` e os testes de datas (`fe3f003`) | 53 | PRs #55 e #56 |
| Com os testes de animais | 67 | PR #54 |
| Com os testes de veterinários e cache | 73 | PR #61 |
| Com as mensagens em português (`main` final) | 75 | PR #67 |

Fonte: elaborado pelos autores a partir das validações registradas nos pull requests (2026).

Os 20 testes a menos entre 46 e 26 correspondem às execuções de JDBC e JPA que deixaram de existir, e nenhum cenário foi perdido. Os testes de persistência (PR #51) verificam, sobre os dados de exemplo, que um tutor com vários animais é retornado uma única vez, que os tipos de animal vêm ordenados por nome e sem duplicados e que a busca por um identificador inexistente devolve `null`, comportamento que o serviço legado nunca tratou.

A comprovação da configuração por ambiente foi feita em duas frentes. No HSQLDB padrão, a suíte passou sem configuração alguma. Em um contêiner com MySQL 5.7.44, o `createDB.sql` foi executado com a senha e o host de origem preenchidos, e o Tomcat foi iniciado com `-Dpetclinic.db=mysql`: as telas de busca e de veterinários responderam HTTP 200, os dez tutores de exemplo estavam no banco e uma visita cadastrada pela interface foi confirmada nele. O Connector/J 5.1.49 também conectou ao MySQL 8.0.43 com uma conta que usa `mysql_native_password`.

### 3.4 Discussão

Três lições merecem registro. A primeira é que **segredo no código é dívida técnica** além de risco de segurança: a conta `pc` com senha `'pc'` estava no repositório e na carga inicial da aplicação, e a correção exigiu separar quem administra o banco de quem o usa. A segunda é o *host* da conta no Docker: com o MySQL em contêiner, o servidor enxerga a aplicação como um cliente da rede do contêiner, e não como `localhost`, e uma conta criada para `localhost` é recusada. O `createDB.sql` passou a ter o *host* como parâmetro. A terceira é metodológica: a remoção de código só foi segura porque os testes de controllers e de consultas existiam antes e foram rodados depois, em um ambiente que outro integrante do grupo reproduziu de forma independente.

Há limitações. O esquema continua em scripts SQL executados a cada inicialização, e não em migrações versionadas, e o conjunto continua sem autenticação. Ambos estão no plano de atualização tecnológica (`docs/plano-atualizacao.md`), junto com a migração para Spring Boot, que não foi executada nesta entrega.

---

## REFERÊNCIAS

FEATHERS, Michael. **Trabalho eficaz com código legado**. Porto Alegre: Bookman, 2013.

FOWLER, Martin. **Refatoração**: aperfeiçoando o design de código existente. 2. ed. São Paulo: Novatec, 2020.

KUCHERENKO, Andrey. **jscpd**: copy/paste detector for programming source code. Versão 4.3.0. GitHub. Disponível em: https://github.com/kucherenko/jscpd. Acesso em: 21 nov. 2026.

McCABE, Thomas J. A complexity measure. **IEEE Transactions on Software Engineering**, v. SE-2, n. 4, p. 308-320, 1976.

SOMMERVILLE, Ian. **Engenharia de software**. 10. ed. São Paulo: Pearson Education do Brasil, 2019.

SPRING PROJECTS. **Spring PetClinic**. GitHub, 2014. Disponível em: https://github.com/spring-projects/spring-petclinic. Acesso em: 21 nov. 2026.

YIN, Terry. **Lizard**: an extensible cyclomatic complexity analyzer. GitHub. Disponível em: https://github.com/terryyin/lizard. Acesso em: 21 nov. 2026.

---

## NOTAS PARA QUEM FINALIZAR (apagar antes de entregar)

- [ ] Preencher nomes dos integrantes, da professora e a cidade na capa.
- [ ] Cada integrante deve conseguir explicar o que assina; revisar o texto antes de entregar.
- [ ] Conferir cada número no PR citado e em `docs/metricas.md`. O total de testes mais recente deve ser repetido com `mvn clean test` na `main` final.
- [ ] Conferir os trechos de Sommerville (2019), Feathers (2013) e Fowler (2020) no livro antes de entregar: a seção resume as ideias dos autores, mas as citações precisam de página se o texto passar a citá-los diretamente.
- [ ] Inserir a figura da arquitetura da Etapa 2 (diagrama em camadas) na seção 2, com legenda e fonte.
- [ ] Aplicar a formatação ABNT no editor: papel A4, margens 3 cm (esquerda e superior) e 2 cm (direita e inferior), fonte 12, espaçamento 1,5, numeração de páginas e legenda dos quadros acima do quadro e da fonte abaixo.
- [ ] Unificar a numeração das seções com as dos demais integrantes quando o relatório for consolidado.
