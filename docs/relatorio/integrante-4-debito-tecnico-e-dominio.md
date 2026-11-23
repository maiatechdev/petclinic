## 4.2 Débito técnico

Durante a análise do Spring PetClinic, encontramos alguns problemas que dificultavam a manutenção do sistema. Mesmo funcionando, a versão antiga tinha código repetido, dependências entre componentes, métodos com várias condições e partes que precisavam de mais testes e documentação. Esses problemas são exemplos de débito técnico, pois tornam futuras alterações mais difíceis e demoradas.

Para entender a situação, analisamos cinco aspectos: acoplamento, duplicação, complexidade, documentação e testes automatizados. Utilizamos os levantamentos da Etapa 2 e as medições registradas em `docs/metricas.md`.

### Problemas encontrados

**Acoplamento:** observamos as dependências entre as partes do sistema. Quando um componente depende muito de outros, uma mudança pode exigir alterações em vários arquivos.

**Duplicação:** identificamos diferentes implementações para operações semelhantes no banco de dados, usando JDBC, JPA e Spring Data JPA. O grupo removeu as implementações JDBC e JPA, mantendo o Spring Data JPA.

**Complexidade:** avaliamos a quantidade de caminhos possíveis dentro dos métodos. Um caso importante foi `PetValidator.validate`, que apresentou complexidade ciclomática 7. Isso indica a necessidade de testar diferentes situações antes de modificar o método.

**Documentação:** encontramos a necessidade de melhorar as explicações sobre classes e métodos, facilitando o entendimento do código por outros desenvolvedores.

**Testes:** alguns comportamentos não tinham testes específicos. Por isso, o grupo criou testes de caracterização antes de realizar determinadas mudanças.

### Como medimos

Usamos NLOC para medir linhas de código sem comentários e linhas vazias, complexidade ciclomática para avaliar os caminhos dos métodos, análise de acoplamento para observar dependências e ferramentas de detecção de duplicação. Também acompanhamos a documentação e a quantidade de testes automatizados.

O Lizard foi utilizado para linhas de código e complexidade, enquanto o jscpd auxiliou na identificação de duplicações.

### Comparação antes e depois

Na versão legada, identificada pelo commit `df1596b`, o levantamento inicial registrou 33 execuções de testes. Segundo a atualização da equipe em 23/11/2026, a `main` passou a apresentar 67 testes.

Na persistência, a retirada das implementações JDBC e JPA reduziu a repetição de código. Também houve ampliação dos testes e melhorias na documentação.

Entretanto, alguns problemas continuam pendentes. Um exemplo é a complexidade de `PetValidator.validate`, cuja refatoração foi planejada para outra etapa.

Os valores detalhados de NLOC, acoplamento, duplicação e documentação precisam ser conferidos em `docs/metricas.md` antes da consolidação final.

### Análise

Percebemos que modernizar o sistema não significa apenas trocar tecnologias. Também envolve organizar melhor o código, diminuir repetições, documentar funcionalidades e criar testes para evitar problemas durante a manutenção.

O grupo conseguiu reduzir parte do débito técnico, mas registrou outras melhorias para trabalhos futuros.

## 5.3 Domínio e validação de animais

Nesta parte do trabalho, analisamos os componentes responsáveis pelo cadastro e pela edição dos animais no Spring PetClinic, principalmente `PetController`, `PetValidator` e `PetTypeFormatter`.

### Diagnóstico

Durante a análise, percebemos uma diferença importante entre as regras do cadastro e da edição de animais. No cadastro, o sistema verifica se o tutor já possui outro animal com o mesmo nome e exige a escolha de um tipo. Na edição, essas duas verificações não são aplicadas da mesma forma.

Essa diferença chamou nossa atenção porque uma pessoa poderia imaginar que as regras seriam iguais nas duas operações. Por isso, consideramos importante registrar o comportamento existente antes de alterar o código.

Também identificamos que `PetValidator.validate` possui complexidade ciclomática 7, uma das maiores do projeto, o que exige cuidado durante futuras refatorações.

### Testes realizados

Criamos testes para verificar o comportamento de `PetTypeFormatter` em três situações: quando recebe um tipo existente, quando recebe um tipo desconhecido e quando precisa transformar um tipo de animal em texto.

No `PetValidator`, acrescentamos um teste para verificar o tratamento de uma data de nascimento nula.

Esses testes ajudam a confirmar que o comportamento atual continua funcionando após futuras mudanças.

Também iniciamos testes com MockMvc para simular requisições de cadastro e edição de animais no `PetController`. Durante essa etapa, alguns cenários apresentaram falhas e ainda precisavam de ajustes na última execução acompanhada.

### Antes e depois

**Antes:** não tínhamos os testes específicos acrescentados nesta atividade para determinados comportamentos de conversão e validação dos animais. Além disso, o método de validação concentrava várias condições.

**Depois da etapa de testes:** ampliamos a verificação automática de `PetTypeFormatter` e `PetValidator`, criando uma base mais segura para mudanças futuras.

A refatoração estrutural prevista na issue #33 ainda não havia sido concluída no levantamento do grupo. Por isso, não afirmamos que a complexidade do método foi reduzida.

### Resultados e trabalho futuro

O principal resultado dessa etapa foi entender melhor o comportamento do módulo de animais e criar testes para proteger suas funcionalidades.

A próxima etapa será concluir os testes do controller, realizar a refatoração planejada e comparar os resultados antes e depois das alterações.

Assim, conseguimos avançar na manutenção do sistema sem modificar suas regras de negócio de maneira inesperada.