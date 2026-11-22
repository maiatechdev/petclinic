# Arquitetura e evolução do módulo web do PetClinic

## Arquitetura atual

O Spring PetClinic é uma aplicação web monolítica organizada em camadas. Na revisão `567c797`, o Maven empacota a aplicação como um WAR para o Tomcat, com Java 8 e Spring MVC. O arquivo `web.xml` registra o `DispatcherServlet`, responsável por receber as requisições e localizar o controller correspondente. O controller interpreta os parâmetros, prepara o modelo da página e seleciona a view. Nas telas de tutores e visitas, a resposta HTML é renderizada por JSP.

As operações de negócio passam pela interface `ClinicService`. Sua implementação, `ClinicServiceImpl`, centraliza as transações e delega as operações de dados a quatro contratos de repositório, um para tutores, um para animais, um para visitas e um para veterinários. O legado `v0-legado` permitia selecionar entre JDBC, JPA e Spring Data JPA. Após a integração do PR #43, o Spring Data JPA com Hibernate passou a ser a única implementação de repositório. O HSQLDB em memória permanece como banco padrão. O PR #44 permite selecionar MySQL por configuração de ambiente, com credenciais fornecidas na inicialização.

O acoplamento entre a camada web e a persistência aparece no uso de `Owner`, `Pet` e `Visit` em mais de uma função. As mesmas classes representam entidades persistidas, recebem dados submetidos nos formulários e, em alguns fluxos, ficam na sessão HTTP entre a abertura e o envio do formulário. Uma mudança nos campos dessas entidades pode repercutir na validação web, na renderização das páginas e no mapeamento do banco. Os controllers de tutores, animais, visitas e veterinários também dependem da fachada `ClinicService`, da qual cada um usa um subconjunto de operações.

## Refatoração dos controllers de tutores e visitas

A caracterização da issue #29 registrou os fluxos web em testes com MockMvc antes da refatoração. Os testes cobriram buscas com zero, um ou vários resultados, cadastro e edição com dados válidos e inválidos e abertura e envio do formulário de visitas. Também registraram que `VisitController.showVisits` encaminhava a listagem isolada para `visitList.jsp`, um arquivo que não existia.

Na issue #30, a duplicação entre `processCreationForm` e `processUpdateOwnerForm` foi tratada por extração de método. Antes, os dois handlers verificavam erros, salvavam o tutor e encerravam a sessão de formulário. Depois, `saveValidOwner` passou a executar essa sequência, enquanto cada handler preservou o seu destino. O cadastro usa o identificador gerado no salvamento, e a edição usa o identificador da URL. Os nomes das views passaram a ser constantes. Em `processFindForm`, a normalização do sobrenome e a consulta foram separadas da escolha entre lista e redirecionamento. O formulário de busca continua exibindo o erro quando não há resultados. [Extract Function, catálogo de Martin Fowler](https://refactoring.com/catalog/extractFunction.html)

A rota de listagem isolada de visitas foi removida porque não era usada pelos links da aplicação e repetia uma função disponível nos detalhes do tutor. O histórico de cada animal permanece em `GET /owners/{ownerId}`, e o formulário de cadastro de visita continua acessível na rota do animal. O teste que registrava o encaminhamento para o JSP ausente deu lugar a testes que verificam a ausência do mapeamento e a presença do histórico nos detalhes. Em uma execução no Tomcat, os detalhes e o formulário retornaram HTTP 200.

**Quadro. Indicadores dos controllers de tutores e visitas**

| Indicador | Legado `df1596b` | Após a refatoração `0f71603` |
|---|---:|---:|
| Complexidade ciclomática de `processFindForm` | 4 | 2 |
| Linhas de código de `processFindForm` (NLOC) | 17 | 8 |
| NLOC dos dois controllers | 146 | 145 |
| Complexidade ciclomática média dos dois controllers | 1,4 | 1,4 |
| Clones detectados nos dois controllers | 2 | 1 |
| Linhas duplicadas segundo o jscpd | 7,54% | 5,37% |

Fonte do quadro — elaboração própria, com Lizard 1.26.1 e jscpd 4.3.0. A duplicação foi medida no modo `weak`, com mínimo de cinco linhas e 30 tokens, excluindo imports. Com 50 tokens e o mesmo filtro, o jscpd não detectou clones nas duas versões.

A média de complexidade dos dois controllers permaneceu em 1,4 na precisão apresentada pelo Lizard. A redução em `processFindForm` ocorreu com a distribuição da busca e da seleção da view entre métodos menores. O clone eliminado era o trecho de salvamento de tutores. O clone restante abrange os construtores e a configuração de binding.

Após a #30, os 46 testes da aplicação passaram com JDK 8. Com o Spring Data JPA como única implementação de persistência, a validação do PR #43 registrou 26 testes, incluindo os 13 de `OwnerVisitControllerTests`. A diferença de 20 execuções corresponde às duas suítes de serviço removidas junto com JDBC e JPA, com dez testes em cada uma. No Tomcat com Spring Data JPA, também foi confirmado que um pet e uma visita cadastrados apareciam em uma nova consulta aos detalhes do tutor. O PR #51 acrescentou 17 testes de consulta, totalizando 43 testes na revisão `30260c4`.

## Plano de modularização

A proposta divide a fachada em serviços por contexto dentro da mesma aplicação. `OwnerService` cobre busca, cadastro, edição e composição dos detalhes do tutor. `PetService` mantém os dados dos animais e resolve seus tipos. `VisitService` registra as visitas e fornece o histórico por animal. `VetService` consulta veterinários e especialidades e mantém o cache de leitura existente.

Os controllers dependem dos contratos dos serviços, dos formulários e dos resultados de leitura usados nas telas. Os serviços delimitam a transação de cada caso de uso e dependem do modelo e dos contratos de repositório. Quando um contexto precisa de dados de outro, usa uma interface de leitura específica cuja implementação consulta o repositório de origem. Assim, a tela de detalhes pode receber tutor, animais e visitas em um único resultado composto, com os dados carregados antes da renderização. Os repositórios e as entidades ficam independentes de HTTP e JSP.

O binding migra para `OwnerForm`, `PetForm` e `VisitForm`, que recebem apenas os campos editáveis. Os identificadores das operações vêm da rota. O controller trata erros de entrada e reexibe o formulário, enquanto o serviço carrega a entidade e aplica as regras que dependem do banco, como a verificação de nome de animal no âmbito do tutor. Um modelo de leitura como `OwnerDetails` fornece à JSP os dados de que ela precisa para exibir o histórico.

A migração começa pela leitura de veterinários, que usa um único repositório e tem cache próprio. Em seguida, tutores, animais e visitas migram um contexto por vez. A fachada `ClinicService` mantém compatibilidade durante a transição e é removida depois que seus consumidores, incluindo `PetTypeFormatter`, passarem aos contratos de contexto. Cada etapa deve preservar os fluxos já caracterizados. A aceitação inclui verificar buscas e formulários, histórico, cadastro de pets e visitas, formatos HTML, XML e Atom de veterinários e ausência de ciclos entre pacotes.

## Fontes para a consolidação

- Spring PetClinic, código compartilhado nas revisões `df1596b`, `0f71603` e `567c797`. [Repositório na revisão da arquitetura descrita](https://github.com/maiatechdev/petclinic/tree/567c7973a72443e088614ebf7ce130169a470680)
- PetClinic, caracterização dos controllers e refatoração do módulo web. [PR #47](https://github.com/maiatechdev/petclinic/pull/47) e [PR #49](https://github.com/maiatechdev/petclinic/pull/49)
- PetClinic, validação da suíte com Spring Data JPA e detalhes da medição e do plano de modularização. [Revisão do PR #43](https://github.com/maiatechdev/petclinic/pull/43#pullrequestreview-6010176043), [PR #50](https://github.com/maiatechdev/petclinic/pull/50) e [PR #51](https://github.com/maiatechdev/petclinic/pull/51)
