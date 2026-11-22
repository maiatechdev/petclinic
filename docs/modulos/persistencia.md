# Módulo de persistência: Antes e Depois

> **Versão com os PRs integrados.** Descreve a `main` depois do merge dos PRs #43 (remoção de JDBC e JPA), #44 (configuração por ambiente), #48 (driver MySQL) e #51 (testes de persistência). Os resultados de testes vêm das validações em JDK 8 registradas nos PRs. Os campos marcados com `[medir]` ainda dependem de medição com o mesmo comando e ambiente no Antes e no Depois.

Responsável: Integrante 1 (issues #23, #24 e #25).
Base de comparação: commit `df1596b` (tag `v0-legado`).

## 1. Problemas encontrados (Etapa 2)

| # | Problema | Onde | Tipo de smell (Fowler) |
|---|---|---|---|
| P1 | Cada interface de repositório tem **três implementações completas** (JDBC, JPA e Spring Data JPA). Só uma é usada por execução, mas as três precisam acompanhar qualquer mudança no modelo. | pacotes `repository.jdbc`, `repository.jpa`, `repository.springdatajpa` | Código duplicado, Generalidade especulativa |
| P2 | A implementação em uso é escolhida por **perfil Spring**, e o perfil é trocado editando e comentando blocos do `web.xml`. | `web.xml`, `business-config.xml` | Mudança divergente, Comentários como desodorante |
| P3 | Os testes de serviço rodam **uma vez para cada implementação**. | `ClinicServiceJdbcTests`, `ClinicServiceJpaTests`, `ClinicServiceSpringDataJpaTests` | Código duplicado (nos testes) |
| P4 | Usuário e senha do banco ficam fixos em `data-access.properties`, e trocar de banco exige editar o arquivo (comentar HSQLDB, descomentar MySQL). O `initDB.sql` do MySQL, executado pela aplicação, cria o banco e concede todos os privilégios a uma conta com senha fixa. O driver do MySQL estava comentado no `pom.xml`. | `data-access.properties`, `db/mysql/initDB.sql`, `pom.xml` | Dados globais, falta de separação por ambiente, segredo no código |

## 2. Antes e Depois

### 2.1 Implementações de repositório (P1)

Linhas efetivas por pacote, contadas com o mesmo método (sem linhas em branco, comentários, `import` e `package`; `package-info` excluído):

| Pacote | Classes | Antes | Depois |
|---|---|---|---|
| `repository.jdbc` | 6 | 264 | removido |
| `repository.jpa` | 4 | 81 | removido |
| `repository.springdatajpa` | 4 | 17 | 17 |
| **Total** | 14 | **362** | **17** |

Medição com o Lizard (NLOC), segundo o relatório da Etapa 2: JDBC 349, JPA 115 e Spring Data 40 linhas, total 504. Depois: `[medir]` (esperado: 40).

A mesma consulta, "buscar tutores pelo sobrenome", em duas das implementações.

**Antes (JDBC, `JdbcOwnerRepositoryImpl`):**

```java
@Override
public Collection<Owner> findByLastName(String lastName) throws DataAccessException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("lastName", lastName + "%");
    List<Owner> owners = this.namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name like :lastName",
            params,
            ParameterizedBeanPropertyRowMapper.newInstance(Owner.class)
    );
    loadOwnersPetsAndVisits(owners);
    return owners;
}
```

Além deste método, a classe tem o mapeamento manual de pets e visitas, e depende de `VisitRepository` (acoplamento entre repositórios).

**Depois (Spring Data JPA, `SpringDataOwnerRepository`, única implementação):**

```java
@Override
@Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
public Collection<Owner> findByLastName(@Param("lastName") String lastName);
```

**Explicação técnica.** A interface declara a consulta e o Spring Data gera a implementação, incluindo a carga dos pets (`left join fetch`). Desaparecem o SQL escrito à mão, o `RowMapper` e a classe `JdbcPet` (que estendia a entidade `Pet` só para guardar identificadores). Cada mudança no modelo passa a ser feita em um lugar, e não em três.

### 2.2 Seleção da implementação (P2)

**Antes (`web.xml`):**

```xml
<context-param>
    <param-name>spring.profiles.active</param-name>
    <param-value>jpa</param-value>
</context-param>

<!-- When using Spring JDBC, use the following: -->
<!--  <context-param> ... <param-value>jdbc</param-value> ... -->
<!--  When using Spring Data JPA, uncomment the following: -->
<!--  <context-param> ... <param-value>spring-data-jpa</param-value> ... -->
```

**Antes (`business-config.xml`):** quatro blocos `<beans profile="...">` (`jpa,spring-data-jpa`, `jdbc`, `jpa` e `spring-data-jpa`) com beans de transação, `JdbcTemplate` e varredura de pacotes.

**Depois (`business-config.xml`):** o `EntityManagerFactory`, o gerenciador de transações e uma linha de repositórios, sem perfil:

```xml
<jpa:repositories base-package="org.springframework.samples.petclinic.repository.springdatajpa"/>
```

| Arquivo | Linhas antes | Linhas depois |
|---|---|---|
| `business-config.xml` | 95 | 59 |
| `web.xml` | 128 | 109 |

**Explicação técnica.** Sem perfis de persistência, não há mais configuração que dependa de comentar e descomentar blocos de XML, e o comportamento em desenvolvimento e em produção é o mesmo.

### 2.3 Testes de serviço (P3)

| | Antes | Depois |
|---|---|---|
| Classes de teste de serviço | 3 (`Jdbc`, `Jpa`, `SpringDataJpa`) mais `AbstractClinicServiceTests` | 1 (`SpringDataJpa`) mais `AbstractClinicServiceTests` |
| Execuções dos 10 métodos de `AbstractClinicServiceTests` | 30 (10 x 3 implementações) | 10 |
| Total de testes executados pelo `mvn clean test` (JDK 8) | 33 no `df1596b` com o ajuste de build (13 métodos, 10 deles repetidos 3 vezes), validado no PR #45 | 26 depois do PR #43, validado no PR #43 |
| Tempo da suíte de serviço | `[medir]` | `[medir]` |

A queda de 33 para 26 corresponde exatamente às 20 execuções repetidas de JDBC e JPA que deixaram de existir. Nenhum cenário foi perdido: os 10 testes de serviço passam uma vez, com Spring Data JPA.

**Explicação técnica.** A suíte deixa de repetir os mesmos 10 cenários contra implementações que o sistema nunca usa juntas.

### 2.4 Configuração por ambiente e credenciais (P4)

**Antes (`data-access.properties`):** um arquivo com HSQLDB ativo e MySQL comentado, e trocar de banco exigia editá-lo.

```properties
jdbc.url=jdbc:hsqldb:mem:petclinic
jdbc.username=sa
jdbc.password=
#jdbc.url=jdbc:mysql://localhost:3306/petclinic
#jdbc.username=root
#jdbc.password=
```

**Antes (`db/mysql/initDB.sql`):** o script que a **aplicação** executa no startup criava o banco e concedia todos os privilégios a uma conta com senha fixa.

```sql
CREATE DATABASE IF NOT EXISTS petclinic;
GRANT ALL PRIVILEGES ON petclinic.* TO pc@localhost IDENTIFIED BY 'pc';

USE petclinic;
```

Além disso, o driver do MySQL estava comentado no `pom.xml` (versão 5.1.22, de 2012), então ativar o MySQL exigia editar o POM à mão.

**Depois:** um arquivo por banco, escolhido pela propriedade `petclinic.db` (padrão `hsqldb`) em `datasource-config.xml` e `business-config.xml`:

```xml
<context:property-placeholder location="classpath:spring/data-access-${petclinic.db:hsqldb}.properties" .../>
```

No MySQL (`data-access-mysql.properties`) as credenciais vêm do ambiente e a senha é obrigatória, sem valor padrão:

```properties
jdbc.url=${PETCLINIC_DB_URL:jdbc:mysql://localhost:3306/petclinic}
jdbc.username=${PETCLINIC_DB_USERNAME:petclinic}
jdbc.password=${PETCLINIC_DB_PASSWORD}
```

O `initDB.sql` do MySQL passou a conter **só o esquema** (`CREATE TABLE IF NOT EXISTS ...`). A criação do banco e de um usuário com privilégios mínimos (`SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, REFERENCES`) ficou em `createDB.sql`, que um administrador executa uma vez, preenchendo os placeholders `<SENHA>` e `<HOST>`. O driver Connector/J 5.1.49 passou a ser dependência `runtime` do `pom.xml` (PR #48).

Para trocar de banco: `-Dpetclinic.db=mysql` (ou `PETCLINIC_DB=mysql`), sem editar XML nem POM.

**Comprovação (validada no PR #44, JDK 8 e Maven 3.9.14).** No HSQLDB padrão, a suíte passou com 31 testes. Em um contêiner MySQL 5.7.44, executou-se o `createDB.sql` preenchendo apenas `<SENHA>` e `<HOST>`. Com o Tomcat no Windows e `-Dpetclinic.db=mysql`, `/owners/find` e `/vets.html` responderam HTTP 200, os 10 tutores de exemplo estavam no MySQL e uma visita cadastrada pela interface foi confirmada no banco.

**Explicação técnica.** A configuração que muda entre ambientes sai do código versionado, e nenhuma senha é gravada no repositório: a conta legada `pc` com senha `'pc'` deixou de existir, e a senha do MySQL só existe no ambiente de execução. A aplicação deixou de precisar de privilégios administrativos no startup. Também falha ao iniciar se a senha do MySQL não for informada, em vez de subir com uma credencial vazia.

**Lição do Docker.** Com o MySQL em contêiner, o servidor enxerga a aplicação como um cliente da rede do contêiner, e não como `localhost`. Por isso a conta criada para `'localhost'` foi recusada, e o host virou o placeholder `<HOST>`. O driver 5.1.x também não autentica no MySQL 8, então o roteiro valida e recomenda o MySQL 5.7.

## 3. Testes que protegem a mudança

| Teste | Quantidade | O que garante |
|---|---|---|
| `AbstractClinicServiceTests`, executado por `ClinicServiceSpringDataJpaTests` | 10 | O comportamento de busca, cadastro e edição se mantém com a única implementação |
| `PersistenceQueriesTests` (novo, PR #51) | 17 | Consultas dos repositórios: busca sem resultado, por prefixo e com prefixo vazio (lista os 10 tutores), tutor com vários animais retornado **uma vez** (`DISTINCT`), nenhum tutor repetido, id inexistente de tutor e de animal retorna `null`, tipos de animal **ordenados por nome** e **sem duplicados**, veterinários sem especialidade e visitas por animal |
| `DataAccessPropertiesTests` (novo, PR #44) | 4 | HSQLDB e MySQL definem as mesmas chaves, a senha do MySQL vem do ambiente, o MySQL usa seus próprios scripts e o `initDB.sql` do MySQL **não** contém `GRANT`, `IDENTIFIED BY`, `CREATE USER` nem `CREATE DATABASE` |
| `DataSourceDefaultsTests` (novo, PR #44) | 1 | Sem nenhuma configuração, o sistema usa HSQLDB em memória |

Resultados registrados nas revisões, todos em JDK 8 (Corretto 8u514) e Maven 3.9.14:

| Ponto | Testes | Onde |
|---|---|---|
| `df1596b` com o ajuste de build | 33 | PR #45 |
| Depois da remoção de JDBC e JPA | 26 | PR #43 |
| Com a configuração por ambiente | 31 | PR #44 |
| Com os testes de persistência (sobre o #43) | 43 | PR #51 |
| `main` atual (26 + 5 + 17) | 48 esperados, `[medir]` | a confirmar com `mvn clean test` |

Os testes de persistência evitam campos de data de propósito, para continuarem válidos quando o Joda-Time for trocado por `java.time` (issue #26).

## 4. Smells tratados (Fowler)

- **Código duplicado:** três implementações do mesmo contrato e três execuções da mesma suíte.
- **Generalidade especulativa:** suporte a JDBC e JPA "para o caso de trocar" que nunca foi usado em conjunto.
- **Mudança divergente:** uma alteração no modelo obrigava a alterar três pacotes.
- **Comentários como desodorante:** blocos comentados no `web.xml` e no `data-access.properties` indicando como trocar de implementação ou de banco.

## 5. O que fica para a evolução futura

- Migrar o mapeamento JPA/Hibernate 4 para versões atuais e `javax` para `jakarta` (plano de atualização tecnológica).
- Substituir `schema` e carga em SQL por migrações versionadas (Flyway ou Liquibase).
- Separar o `ClinicService` por contexto (owner, pet, visit, vet), apoiado na camada de repositórios simplificada (plano de modularização).

## 6. Pendências

- [ ] Medir NLOC com o Lizard antes e depois (`lizard src/main/java/.../repository`), com o mesmo comando e ambiente.
- [ ] Medir o tempo da suíte antes e depois, no mesmo ambiente.
- [ ] Rodar `mvn clean test` na `main` final e confirmar os 48 testes esperados.
- [ ] Reaplicar este documento se a migração para Spring Boot (issue #41) alterar os arquivos de configuração XML e de propriedades.
