# Módulo de persistência: Antes e Depois

> **Rascunho.** Os números marcados com `[medir]` dependem do build em JDK 8 e das ferramentas de métricas (Lizard e jscpd) e serão preenchidos antes do relatório. O conteúdo descreve o estado da `main` **depois** do merge dos PRs #43 e #44.

Responsável: Integrante 1 (issues #23, #24 e #25).
Base de comparação: commit `df1596b` (tag `v0-legado`).

## 1. Problemas encontrados (Etapa 2)

| # | Problema | Onde | Tipo de smell (Fowler) |
|---|---|---|---|
| P1 | Cada interface de repositório tem **três implementações completas** (JDBC, JPA e Spring Data JPA). Só uma é usada por execução, mas as três precisam acompanhar qualquer mudança no modelo. | pacotes `repository.jdbc`, `repository.jpa`, `repository.springdatajpa` | Código duplicado, Generalidade especulativa |
| P2 | A implementação em uso é escolhida por **perfil Spring**, e o perfil é trocado editando e comentando blocos do `web.xml`. | `web.xml`, `business-config.xml` | Mudança divergente, Comentários como desodorante |
| P3 | Os testes de serviço rodam **uma vez para cada implementação**. | `ClinicServiceJdbcTests`, `ClinicServiceJpaTests`, `ClinicServiceSpringDataJpaTests` | Código duplicado (nos testes) |
| P4 | Usuário e senha do banco ficam fixos em `data-access.properties`, e trocar de banco exige editar o arquivo (comentar HSQLDB, descomentar MySQL). | `data-access.properties` | Dados globais, falta de separação por ambiente |

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
| Tempo da suíte de serviço | `[medir]` | `[medir]` |

**Explicação técnica.** A suíte deixa de repetir os mesmos 10 cenários contra implementações que o sistema nunca usa juntas.

### 2.4 Configuração por ambiente (P4)

**Antes (`data-access.properties`):** um arquivo com HSQLDB ativo e MySQL comentado.

```properties
jdbc.url=jdbc:hsqldb:mem:petclinic
jdbc.username=sa
jdbc.password=
#jdbc.url=jdbc:mysql://localhost:3306/petclinic
#jdbc.username=root
#jdbc.password=
```

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

Para trocar de banco: `-Dpetclinic.db=mysql` (ou `PETCLINIC_DB=mysql`), sem editar XML.

**Explicação técnica.** A configuração que muda entre ambientes sai do código versionado. Nenhuma senha é gravada no repositório, e a aplicação falha ao iniciar se a senha do MySQL não for informada, em vez de subir com uma credencial vazia.

## 3. Testes que protegem a mudança

| Teste | O que garante |
|---|---|
| `ClinicServiceSpringDataJpaTests` (10 cenários herdados de `AbstractClinicServiceTests`) | O comportamento de busca, cadastro e edição se mantém com a única implementação |
| `DataAccessPropertiesTests` (novo) | HSQLDB e MySQL definem as mesmas chaves; a senha do MySQL vem do ambiente; o MySQL usa seus próprios scripts |
| `DataSourceDefaultsTests` (novo) | Sem nenhuma configuração, o sistema usa HSQLDB em memória |
| Testes adicionais da issue #25 | `[a escrever]`: consultas do Spring Data (busca sem resultado, com vários, ordenação de tipos) |

Resultado da suíte completa: `[medir]` (JDK 8, `mvn clean test`).

## 4. Smells tratados (Fowler)

- **Código duplicado:** três implementações do mesmo contrato e três execuções da mesma suíte.
- **Generalidade especulativa:** suporte a JDBC e JPA "para o caso de trocar" que nunca foi usado em conjunto.
- **Mudança divergente:** uma alteração no modelo obrigava a alterar três pacotes.
- **Comentários como desodorante:** blocos comentados no `web.xml` e no `data-access.properties` indicando como trocar de implementação ou de banco.

## 5. O que fica para a evolução futura

- Migrar o mapeamento JPA/Hibernate 4 para versões atuais e `javax` para `jakarta` (plano de atualização tecnológica).
- Substituir `schema` e carga em SQL por migrações versionadas (Flyway ou Liquibase).
- Separar o `ClinicService` por contexto (owner, pet, visit, vet), apoiado na camada de repositórios simplificada (plano de modularização).

## 6. Pendências do rascunho

- [ ] Medir NLOC com o Lizard antes e depois (`lizard src/main/java/.../repository`).
- [ ] Medir tempo da suíte de serviço antes e depois (JDK 8).
- [ ] Rodar `mvn clean test` na `main` após os merges e registrar o resultado.
- [ ] Incluir os testes novos da issue #25 na tabela da seção 3.
