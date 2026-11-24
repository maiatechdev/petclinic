## 4.2 Débito técnico

Durante a análise do Spring PetClinic, encontramos alguns problemas que dificultavam a manutenção do sistema. Mesmo funcionando, a versão antiga tinha código repetido, dependências entre componentes, métodos com várias condições e partes que precisavam de mais testes e documentação.

Para entender a situação, analisamos cinco aspectos: acoplamento, duplicação, complexidade, documentação e testes automatizados. Utilizamos os levantamentos da Etapa 2 e as medições registradas em `docs/metricas.md`.

### Problemas encontrados

**Acoplamento:** a classe `Pet` apresentou acoplamento aferente de 13. Isso indica que outros componentes dependem dessa classe, exigindo cuidado durante alterações para evitar impactos em diferentes partes do sistema.

**Duplicação:** identificamos três implementações de persistência, utilizando JDBC, JPA e Spring Data JPA, que somavam 504 NLOC. O grupo removeu as implementações JDBC e JPA, mantendo Spring Data JPA, reduzindo esse número para 40 NLOC. No diagnóstico da Etapa 2, também foram identificados 11 clones de código.

**Complexidade:** o método `PetValidator.validate` apresentou complexidade ciclomática 7 (CCN 7), indicando diferentes caminhos de execução que precisam ser considerados durante os testes e as refatorações.

**Documentação:** dos 106 métodos analisados na Etapa 2, apenas 14 possuíam Javadoc, correspondendo a aproximadamente 13,2%. Esse resultado mostrou a necessidade de melhorar a documentação do sistema.

**Testes automatizados:** a versão legada apresentava 33 execuções de testes. Durante a evolução do projeto, o grupo ampliou os testes para verificar o comportamento existente e reduzir o risco de regressões.

### Comparação antes e depois

| Indicador | Versão legada | Situação posterior |
|---|---|---|
| Acoplamento aferente da classe Pet | 13 | Nova medição não confirmada |
| NLOC das implementações de persistência | 504 | 40 |
| Clones de código | 11 | Nova medição não confirmada |
| Métodos com Javadoc | 14 de 106 (13,2%) | Documentação ampliada |
| Complexidade de PetValidator.validate | CCN 7 | Refatoração pendente |
| Testes automatizados | 33 execuções | 67 execuções registradas em levantamento posterior do grupo |

Fonte: elaborado pelos autores com base no diagnóstico da Etapa 2, em `docs/metricas.md` e nos registros dos pull requests (2026).

A redução de 504 para 40 NLOC representa aproximadamente 92,1% de redução nas implementações de persistência. Essa mudança diminuiu a quantidade de código que precisa ser mantida.

Os resultados mostram que parte do débito técnico foi tratada durante a evolução do sistema. Entretanto, ainda existem melhorias que precisam ser realizadas, principalmente em relação à complexidade, ao acoplamento e à documentação.

## 5.3 Domínio e validação de animais

### Diagnóstico

A análise do domínio de animais concentrou-se nas classes `PetController`, `PetValidator` e `PetTypeFormatter`, responsáveis pelo processamento, pela validação e pela conversão das informações relacionadas aos animais.

Durante a análise, identificamos uma diferença entre as regras de cadastro e edição. A verificação de nomes duplicados entre animais do mesmo tutor e a obrigatoriedade do tipo são aplicadas no cadastro, mas não na edição.

Também identificamos que o método `PetValidator.validate` apresenta complexidade ciclomática 7 (CCN 7). Por isso, a criação de testes de caracterização é importante para registrar o comportamento existente antes de realizar alterações estruturais.

### Testes realizados

O PR #54 registra 14 testes relacionados ao módulo de animais.

Criamos 7 testes do `PetController` com Mockito. Os testes com MockMvc ficaram como trabalho futuro.

Como complemento à issue #32, foram desenvolvidos quatro novos testes na branch `feature/i4-pet-formatter-tests`.

No `PetTypeFormatter`, foram acrescentados três testes:

- Conversão de um tipo de animal existente.
- Tratamento de um tipo desconhecido por meio de `ParseException`.
- Conversão de um tipo de animal para sua representação textual.

No `PetValidator`, foi acrescentado um teste para verificar se a ausência da data de nascimento gera o erro de validação `required`.

Para verificar o funcionamento dessas duas classes de testes, executamos o comando:

`mvn -Dtest=PetTypeFormatterTests,PetValidatorTests test`

**Quadro — Resultado da execução dos testes**

| Indicador | Resultado |
|---|---|
| Testes executados | 11 |
| Falhas | 0 |
| Erros | 0 |
| Testes ignorados | 0 |
| Tempo de execução | 5,782 segundos |
| Resultado | BUILD SUCCESS |

Fonte: elaborado pelos autores a partir da execução dos testes no Codespaces (2026).

Os 11 testes correspondem ao total executado nas duas classes, incluindo os quatro novos testes. As alterações foram publicadas na branch de desenvolvimento para revisão.

### Antes e depois

**Antes:** os cenários específicos de conversão dos tipos de animais e de validação da data de nascimento nula não possuíam os testes adicionais desenvolvidos nesta etapa.

**Depois:** foram acrescentados quatro testes de caracterização, permitindo verificar esses comportamentos sem modificar o código de produção.

### Resultados e trabalho futuro

A principal contribuição desta etapa foi ampliar os testes de caracterização do módulo de animais, permitindo compreender melhor o comportamento existente e reduzir os riscos de futuras alterações.

A refatoração estrutural prevista na issue #33 permanece como trabalho futuro. Portanto, não é possível afirmar que houve redução da complexidade ciclomática do método `PetValidator.validate` ou uniformização das regras de cadastro e edição.

Dessa forma, o trabalho realizado contribuiu para preparar o módulo de animais para futuras refatorações, mantendo o comportamento existente do sistema.