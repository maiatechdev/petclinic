# Datas de pets e visitas

## Antes da migração

Em `Pet.java` e `Visit.java`, os campos de data usavam `org.joda.time.DateTime`
e `@Type` do Hibernate para selecionar o tipo Jadira. As colunas `birth_date`
e `visit_date` já eram do tipo SQL `DATE`. No Tomcat com JDK 8, a data
`2012-09-04`, armazenada para Samantha, aparecia na tela como `2012-09-03`.
O tipo Java carregava hora e fuso para um dado que representa um dia do
calendário.

```java
@Column(name = "birth_date")
@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
@DateTimeFormat(pattern = "yyyy/MM/dd")
private DateTime birthDate;
```

## Depois da migração

```java
@Column(name = "birth_date")
@Convert(converter = LocalDateConverter.class)
private LocalDate birthDate;
```

`Pet` e `Visit` usam `java.time.LocalDate`. O conversor JPA 2.1 mapeia
`LocalDate` para `java.sql.Date`, preservando o valor das colunas
existentes. O Spring MVC usa um formatter para aceitar e exibir datas com
barras nos formulários, rejeitando datas impossíveis. As JSPs exibem
`yyyy-MM-dd` nos detalhes do tutor e `yyyy/MM/dd` no formulário de visita.
A visita nova continua com a data local de hoje.

## Validação

Os testes de caracterização passaram com Joda-Time antes da refatoração.
Eles verificam o cadastro e a edição do pet, o cadastro de uma visita com
data explícita e a rejeição de datas inválidas. Após a migração, um quinto
teste confere a leitura das datas existentes diretamente das entidades.
No JDK 8, os 53 testes passaram e o WAR foi gerado sem Joda-Time e Jadira.
No Tomcat, os fluxos também foram conferidos com HSQLDB e com uma
instância isolada de MySQL 8.0.43 usando autenticação nativa. O MySQL
gravou `2020-03-01` para o pet editado e `2024-02-28` para a visita.
