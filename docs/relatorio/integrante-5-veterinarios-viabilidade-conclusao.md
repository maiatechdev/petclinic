# Relatório: seções do Integrante 5 (Leo)

## 5.5 Veterinários, documentação e mensagens

O módulo dos veterinários é pequeno, contendo apenas um controller, uma view de feed Atom e uma classe que envolve a lista
de veterinários, mas concentrava problemas que ninguém percebia porque não havia testes nem documentação
sobre ele. O trabalho seguiu a mesma ordem dos outros módulos: primeiro testes de caracterização, depois a
refatoração em si.

**Cache que nunca tinha sido testado.** A lista de veterinários é guardada em cache pela anotação
`@Cacheable("vets")`, no método `findVets` de `ClinicServiceImpl`. O cache é ativado no arquivo
`tools-config.xml`, mas nenhum teste do projeto carregava esse arquivo, então o cache nunca tinha sido
testado de fato. O único comentário que explicava onde ele ficava estava em uma classe da implementação JDBC,
que foi removida na refatoração da persistência. Então foi criado um teste que usa o cache real com repositórios
simulados pelo Mockito e verifica que duas chamadas ao serviço geram uma única consulta ao repositório. Se
essa anotação for retirada, o teste falha. A primeira versão do teste comparava apenas se as duas chamadas
devolviam o mesmo objeto, mas a revisão feita por Davi, mostrou que isso não provava a consulta única, e o
teste foi reescrito para contar as chamadas.

**Código morto na view Atom.** Na `VetsAtomView`, a linha `response.setContentType("blabla")` não tinha
efeito, porque a classe-mãe (`AbstractFeedView`) definia o tipo da resposta depois que as entradas do feed
eram montadas. O teste de caracterização confirma que o feed sai como `application/atom+xml` antes e depois
da remoção. A classe também tinha código comentado, nomes repetidos e um Javadoc que falava em "visitas" 
numa view que trata de veterinários. Os textos viraram constantes e a montagem de cada entrada do feed foi extraída 
para um método próprio.

**Comportamento revelado pela caracterização.** Os testes mostraram que o `/vets`, sem extensão, ignora o
cabeçalho `Accept` da requisição e sempre devolvia HTML. O formato depende apenas da extensão da URL
(`.html`, `.xml` ou `.atom`). Esse comportamento foi mantido e documentado.

**Documentação.** A interface `ClinicService`, usada por todos os controllers, não tinha nenhum Javadoc. Então foi
feita a documentação dos oito métodos, incluindo o que os testes de persistência revelaram: as buscas por
identificador devolvem `null` quando o registro não existe, e a busca de tutores é feita pelo início do
sobrenome. O Javadoc foi escrito em inglês, para seguir o padrão dos nomes do projeto.

**Mensagens em português.** Foi criado o arquivo `messages_pt.properties`, com as oito mensagens do sistema
traduzidas. O Spring escolhe o idioma pelo navegador, sem mudança de configuração. Como o Java 8 lê esses
arquivos na codificação ISO-8859-1, os acentos foram escritos usando escapes Unicode (`\u00e1` para "á"), por conta disso, 
foi criado um teste que confere os acentos e falha se o arquivo for salvo com acento direto. A tradução cobre as mensagens de
validação e as boas-vindas, já o menu, os títulos e os botões continuam em inglês, direto nas páginas JSP.

**Quadro X - Veterinários e documentação antes e depois**

| Indicador | Antes | Depois |
|---|---:|---:|
| Testes do módulo de veterinários | 1 | 5 |
| Testes do cache de veterinários | 0 | 2 |
| Métodos do `ClinicService` com Javadoc | 0 de 8 | 8 de 8 |
| Métodos públicos com Javadoc no projeto | 17,7% | 45,5%  |
| Idiomas das mensagens | inglês e alemão | inglês, alemão e português |

Fonte: elaborado pelos autores (2026).

---

## 7 VIABILIDADE E ANÁLISE CRÍTICA

### 7.1 Valor da modernização

O PetClinic de 2014 ainda faz o que se espera dele, mas manter o sistema como estava tinha custo e risco.
Em um ambiente atual, com Java 8, ele nem chegava a funcionar: todas as páginas respondiam com erro até o
ajuste do compilador de JSP. A senha do banco MySQL estava escrita no repositório, em um script que criava
uma conta com todos os privilégios. A camada de dados tinha três implementações completas para o mesmo
comportamento (504 linhas, das quais só uma parte era usada, agora são 40), e não havia testes de controllers, o que
deixou passar uma rota que apontava para uma tela inexistente.

Nenhuma dessas mudanças e melhorias muda o que o usuário vê, e esse é justamente o ponto: o valor de negócio está em 
reduzir o custo e o risco de cada mudança futura. É o caso descrito por Sommerville (2019), de um sistema com valor
para o negócio e qualidade técnica baixa, para o qual a reestruturação compensa mais que o descarte ou a substituição.

### 7.2 Prazo: o que coube e o que ficou de fora

O trabalho de refatoração foi feito em cerca de três dias, entre 21 e 23 de novembro. Nesse período coube:
a persistência com uma única implementação e o banco configurável por ambiente, a refatoração dos
controllers de tutores e visitas, a troca das datas para `java.time` (que corrigiu datas exibidas com um
dia a menos), o módulo de veterinários, a documentação e as mensagens em português. O total de testes
passou de 33 para 75.

Ficou de fora, por decisão do grupo:

- a migração para Spring Boot 3 e Java 17, adiada por um critério de go/no-go definido antes, e mantida
  como plano de evolução;
- a API em JSON para veterinários e tutores, que exige incluir o Jackson no projeto e um teste novo;
- a refatoração do validador de animais, o método mais complexo do sistema;
- a extração dos textos fixos das páginas JSP para os arquivos de mensagens.

### 7.3 Riscos e como foram tratados

**Quadro Y - Riscos do projeto e tratamento**

| Risco | Tratamento |
|---|---|
| Quebrar um comportamento ao refatorar | Testes de caracterização antes de cada mudança (FEATHERS, 2013) e `mvn clean test` antes de cada integração |
| Conflito entre integrantes no mesmo arquivo | Um responsável por arquivo compartilhado, PRs pequenos e revisão obrigatória por outro integrante |
| Estourar o prazo com a migração | Critério de go/no-go com horário definido; a migração foi adiada em vez de deixar a versão principal sem funcionar |
| Ambiente que não reproduz o build | JDK 8 e ajuste do compilador de JSP documentados no readme |

Fonte: elaborado pelos autores (2026).

Durante o desenvolvimento, também tivemos que lidar com conflitos de código causados por alterações realizadas ao mesmo tempo por integrantes diferentes. Um desses casos aconteceu na refatoração da view Atom, que acabou modificando trechos que também haviam sido alterados por outro integrante. O conflito foi resolvido de forma que as duas alterações fossem mantidas, e a suíte de testes foi executada antes de realizar a integração, garantindo que as mudanças não prejudicassem o funcionamento do sistema.

### 7.4 Análise crítica

**O que funcionou.** A revisão do código ajudou a identificar problemas que poderiam chegar à versão 
principal do projeto, como um teste de cache que não validava corretamente o comportamento esperado, 
mensagens em letras maiúsculas que causavam falhas nos próprios testes e uma tabela com a quantidade 
de testes fora de ordem em uma versão inicial do relatório.

Além disso, os testes de caracterização permitiram identificar alguns problemas que já existiam no 
sistema original, como o direcionamento para uma tela que não existia e o registro de datas com um dia 
a menos do que o esperado.

**O que não funcionou tão bem.** Uma das dificuldades encontradas durante o desenvolvimento foi o início 
tardio do grupo, além do tempo necessário para configurar o ambiente de desenvolvimento, principalmente 
para quem entrou depois, que precisou instalar e configurar o JDK 8 e o Maven. Outro problema foi a falta 
de centralização dos dados e resultados, que ficaram distribuídos em vários documentos e precisaram ser 
recalculados a cada integração do trabalho.

Por conta dessas dificuldades e do tempo disponível, foi necessário reduzir o escopo do projeto, fazendo 
com que algumas atividades que estavam previstas inicialmente fossem deixadas como melhorias para trabalhos futuros.

**Um efeito colateral que os testes não pegaram.** Depois da remoção das implementações JDBC e JPA, o
aspecto de monitoramento (`CallMonitoringAspect`) deixou de medir qualquer chamada, porque ele só observa
classes com a anotação `@Repository`, que existia apenas nas classes removidas. Os testes continuaram
passando, e em uma execução com três consultas o contador ficou em zero.
O caso mostra o limite da estratégia: os testes protegem o comportamento que cobrem, e o monitoramento não
era coberto por nenhum.

---

## 8 CONCLUSÃO

O trabalho conseguiu aplicar ao Spring PetClinic os conceitos de evolução, reengenharia e refatoração de software, 
alcançando os objetivos propostos inicialmente, embora algumas etapas tenham ficado apenas no planejamento.

A engenharia reversa permitiu identificar os oito requisitos funcionais do sistema e compreender sua arquitetura em camadas. 
Também foram realizadas medições para analisar características como tamanho do código, complexidade, duplicação, documentação 
e cobertura de testes. Antes de iniciar as alterações, foram criados testes de caracterização para todos os módulos, fazendo 
com que a quantidade total de testes passasse de 33 para 75.

Durante a refatoração, a camada de acesso a dados foi reduzida de 504 para 40 linhas, os controllers foram simplificados e a 
porcentagem de métodos públicos documentados aumentou de 17,7% para 45,5%. Além disso, foram elaborados os planos de modularização 
e atualização tecnológica. Porém, a migração para Spring Boot foi apenas planejada e não chegou a ser implementada, devido a prazos.

Como trabalho futuro, ficam a atualização para Spring Boot 3 e Java 17, a criação de uma API que trabalhe com JSON, a refatoração do validador de animais, a tradução completa das telas, a implementação de migrações de banco de dados versionadas, a adição de autenticação e a correção do monitoramento de chamadas.


---

