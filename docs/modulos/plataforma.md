# Plataforma: Antes e Depois do pom.xml

Responsável: Integrante 2 (issue #28).
Base de comparação: commit `df1596b` (tag `v0-legado`) e `main` em `1ae05dd`.

Este documento reúne as mudanças de plataforma feitas no `pom.xml` desde o legado. Cada uma veio de um PR pequeno, revisado e testado com JDK 8. A migração das datas tem documento próprio em [plataforma-datas.md](plataforma-datas.md).

## 1. Problemas encontrados (Etapa 2)

| # | Problema | Onde | Tipo de problema |
|---|---|---|---|
| P1 | O projeto compilava para Java 1.6. Os JDKs a partir da versão 12 não aceitam mais esse alvo de compilação. | `pom.xml`, linha 14 | Plataforma sem suporte |
| P2 | O `tomcat7-maven-plugin` 2.0 traz um compilador de JSP de 2012 que não lê classes do Java 8. Com JDK 8, o servidor subia, mas todas as páginas respondiam HTTP 500 com `JasperException: java.util.Map$Entry cannot be resolved`. | `pom.xml`, plugin na linha 439 | Dependência desatualizada |
| P3 | O driver do MySQL estava comentado em dois lugares, na versão 5.1.22. Para usar MySQL era preciso editar o `pom.xml`. | `pom.xml`, linha 67 e dependências comentadas | Código comentado, configuração manual |
| P4 | As datas dependiam de quatro bibliotecas: `joda-time`, `joda-time-hibernate`, `joda-time-jsptags` e o Jadira `usertype.core`. Além de obsoletas desde o `java.time` do Java 8, elas faziam a tela mostrar as datas com um dia a menos no fuso UTC-3. | `pom.xml`, linhas 55 a 58, 73 e 197 a 210 | Dependência obsoleta, defeito |
| P5 | Não havia biblioteca de dublês de teste. Testes unitários de controller precisariam subir o contexto do Spring e o banco. | `pom.xml`, seção de testes | Baixa testabilidade |

## 2. Antes e Depois

### 2.1 Versão do Java e compilador de JSP (P1 e P2), PR #45

Antes:

```xml
<java.version>1.6</java.version>
```

Depois:

```xml
<java.version>1.8</java.version>
```

No plugin do Tomcat, foi acrescentado o compilador ECJ 4.4.2, que substitui o compilador de JSP de 2012:

```xml
<dependencies>
    <dependency>
        <groupId>org.eclipse.jdt.core.compiler</groupId>
        <artifactId>ecj</artifactId>
        <version>4.4.2</version>
    </dependency>
</dependencies>
```

O Java 8 foi escolhido por ser a versão mais nova com que o Spring 4.0 e o Tomcat 7 ainda funcionam sem outras mudanças. A ida para Java 17 depende da migração para Spring Boot 3 (issue #41).

### 2.2 Driver do MySQL (P3), PR #48

Antes, comentado:

```xml
<!-- <dependency> <groupId>mysql</groupId> <artifactId>mysql-connector-java</artifactId> <version>${mysql.version}</version> </dependency> -->
```

Depois, ativo apenas em tempo de execução:

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>${mysql.version}</version>
    <scope>runtime</scope>
</dependency>
```

A versão passou de 5.1.22 para 5.1.49, a última da série 5.1. O banco usado é escolhido sem editar o `pom.xml`, como descrito no `readme.md`.

### 2.3 Remoção do Joda-Time e do Jadira (P4), PR #56

As quatro dependências e as quatro propriedades de versão foram removidas. `Pet` e `Visit` passaram a usar `java.time.LocalDate`, com um conversor JPA próprio. O detalhamento está em [plataforma-datas.md](plataforma-datas.md).

### 2.4 Mockito para testes (P5), PR #55

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>${mockito.version}</version>
    <scope>test</scope>
</dependency>
```

A versão 1.10.19 é a última da série 1.x, compatível com Java 8. Ela é usada pelos testes de `PetController` (issue #32).

## 3. Testes que comprovam o comportamento

Os PRs acima registram validação com `mvn clean test` em JDK 8.

Na minha máquina (Windows 11, Temurin 8u442, Maven 3.9.9):

- Antes do PR #45, com JDK 8 e o plugin original, as páginas JSP respondiam HTTP 500 (`JasperException`).
- Com o ECJ, o sistema subiu e as telas de tutores, pets, visitas e veterinários funcionaram, inclusive `/vets.xml`.
- Na revisão do PR #56, a Samantha passou a aparecer com nascimento em 04/09/2012, igual ao valor gravado no banco.
- Na `main` em `1ae05dd`, `mvn clean test` executou 67 testes, sem falhas.

## 4. Métricas

| Indicador | Legado `df1596b` | `main` `1ae05dd` |
|---|---:|---:|
| Alvo de compilação | Java 1.6 | Java 1.8 |
| Dependências declaradas no `pom.xml` | 36 | 34 |
| Bibliotecas de data além do JDK | 4 | 0 |
| Driver MySQL disponível sem editar o `pom.xml` | não | sim |
| Testes executados pelo Maven | 33 | 67 |

A contagem de 33 testes do legado vem de [metricas.md](../metricas.md).
