# Relatório: seções da Integrante 2 (Kézia Carneiro)

## 2 FUNDAMENTAÇÃO TEÓRICA

### 2.1 Evolução e manutenção de software

Depois de entregue, um sistema continua mudando. Aparecem novos requisitos, defeitos e o ambiente em que ele roda também muda, com novas versões de sistema operacional, de linguagem e de bibliotecas. A evolução faz parte do ciclo de vida do software e em sistemas usados por muitos anos a maior parte do custo total costuma estar na manutenção, e não no desenvolvimento inicial (SOMMERVILLE, 2019).

Essas mudanças podem ser de três tipos: a manutenção corretiva (corrige defeitos); a adaptativa (ajusta o sistema a um novo ambiente, como outra versão da plataforma ou outro banco de dados) e a evolutiva (acrescenta ou melhora funcionalidades) (SOMMERVILLE, 2019).

Chamamos de sistema legado aquele que ainda tem valor para quem o usa, mas foi construído com tecnologias que ficaram obsoletas e por isso se tornou difícil e arriscado de modificar. Para lidar com ele, Sommerville (2019) aponta quatro caminhos: descartar o sistema, continuar mantendo-o como está, reestruturá-lo por meio de reengenharia ou substituí-lo. A escolha depende do valor do sistema para o negócio e da sua qualidade técnica.

O Spring PetClinic de 2014 é um exemplo de sistema legado. Ele usava Java 1.6, Spring 4.0 e Tomcat 7, tecnologias sem suporte hoje, mas as suas funcionalidades continuam fazendo sentido. Por isso, o grupo optou pela reestruturação: manter o que o sistema faz e modernizar a sua base. Durante o trabalho, apareceram dois dos tipos de manutenção descritos acima. A migração para o Java 8 foi uma manutenção adaptativa, e a correção das datas de nascimento e de visita, que apareciam na tela com um dia a menos, foi uma manutenção corretiva.

### 2.2 Engenharia reversa e reengenharia

A engenharia reversa é o processo de analisar um sistema existente para recuperar informações sobre o seu projeto, como a arquitetura, os módulos e as regras de negócio, quando essa documentação não existe ou está desatualizada. Ela costuma ser o primeiro passo da reengenharia, que consiste em reestruturar ou reescrever partes do sistema para facilitar a sua manutenção, sem alterar o que ele faz para o usuário. Entre as atividades da reengenharia estão a melhoria da estrutura do código, a modularização e a migração de dados e de plataforma (SOMMERVILLE, 2019).

No projeto, a engenharia reversa foi feita na Etapa 2. Como o PetClinic não tinha documentação de requisitos, o grupo levantou as funcionalidades e as regras de negócio a partir dos controladores, das validações das entidades e das páginas JSP, mapeou a arquitetura em camadas e mediu o código com ferramentas. A reengenharia veio na etapa seguinte, com mudanças como a remoção de duas das três implementações da camada de dados e a separação da configuração do banco por ambiente, sempre mantendo as mesmas funcionalidades.

### 2.3 Refatoração e code smells

Fowler (2020) define refatoração como uma alteração feita na estrutura interna do software para torná-lo mais fácil de entender e de modificar, sem mudar o seu comportamento observável. Ela é feita em pequenos passos, cada um seguro, e depende de testes que confirmem que nada deixou de funcionar. Para saber onde refatorar, o autor usa a ideia de code smells, sinais no código que indicam um problema de estrutura, como código duplicado, métodos longos, generalidade especulativa (código feito para necessidades que nunca chegaram) e comentários usados para explicar trechos confusos.

Martin (2011) complementa essa visão com práticas de código limpo: nomes que revelam a intenção, funções pequenas e com um único propósito e comentários usados apenas quando o código não consegue se explicar sozinho.

No PetClinic, esses sinais apareceram com clareza. As três implementações completas da camada de dados, das quais só uma era usada, são um caso de código duplicado e de generalidade especulativa. Os controladores de tutores e de visitas repetiam a mesma sequência de validação, gravação e redirecionamento. Havia também código comentado sem uso, como as linhas removidas do VetsAtomView. As refatorações do grupo trataram esses pontos mantendo o comportamento do sistema, o que foi conferido pelos testes a cada mudança.

### 2.4 Código legado e testes de caracterização

Para Feathers (2013), código legado é, de forma prática, código sem testes, porque sem eles não há como saber se uma mudança quebrou algo. A recomendação do autor é cobrir o trecho que será alterado com testes antes de modificá-lo. Para isso ele propõe os testes de caracterização, que registram o comportamento atual do sistema, e não o comportamento desejado. Depois que esse comportamento está protegido, a refatoração pode ser feita com segurança.

O grupo seguiu essa prática em todos os módulos. Antes de refatorar os controladores de tutores e de visitas, foram criados testes que registravam como eles respondiam, e o mesmo foi feito com as consultas da camada de dados. Os testes de caracterização das datas revelaram um defeito real do legado: as datas eram lidas como meia-noite no fuso UTC e, no fuso do Brasil, apareciam na tela com um dia a menos. Ao longo do trabalho, o número de testes executados passou de 33 no legado para 67.

### 2.5 Débito técnico e complexidade ciclomática

Débito técnico é o custo que se acumula quando o sistema é construído ou mantido com atalhos: cada solução provisória deixa o código mais difícil de mudar no futuro, como uma dívida que cobra juros. Ele aparece como acoplamento excessivo, duplicação, falta de testes e de documentação e dependências desatualizadas, e foi esse o diagnóstico feito na Etapa 2.

Uma das métricas usadas para medir a dificuldade de um código é a complexidade ciclomática, proposta por McCabe (1976). Ela conta o número de caminhos independentes dentro de um método, o que corresponde, na prática, ao número de decisões, como os comandos if e os laços. Quanto maior o valor, mais difícil é entender e testar o método. No PetClinic, a complexidade medida com a ferramenta Lizard foi baixa, com média de 1,3 por método e valor máximo de 7, no PetValidator.validate. Isso mostrou que o principal débito do sistema não estava em métodos complicados, mas no acoplamento e na duplicação entre as camadas.

## 5.4 Plataforma

A primeira barreira encontrada foi a própria execução do sistema. O pom.xml compilava para o Java 1.6, versão que os JDKs atuais não aceitam mais como alvo. Mesmo com o JDK 8, o servidor subia, mas todas as páginas respondiam com erro HTTP 500, porque o compilador de JSP embutido no plugin do Tomcat 7, de 2012, não conseguia ler as classes do Java 8. A correção foi mudar o alvo de compilação para o Java 1.8 e acrescentar ao plugin o compilador ECJ 4.4.2. Com isso, o legado voltou a rodar e passou a servir de base para as demais etapas.

Em seguida, o pom.xml foi ajustado em pequenos passos. O driver do MySQL, que estava comentado na versão 5.1.22, foi ativado na versão 5.1.49 apenas em tempo de execução, permitindo escolher o banco sem editar o arquivo. O Mockito 1.10.19 foi adicionado para os testes, e as quatro bibliotecas de datas (joda-time, joda-time-hibernate, joda-time-jsptags e Jadira) foram removidas. As datas dos animais e das visitas passaram a usar o LocalDate, do pacote java.time, nativo do Java 8. Essa troca corrigiu o defeito encontrado nos testes de caracterização: a data de nascimento 2012-09-04, gravada no banco, aparecia na tela como 2012-09-03, e depois da mudança passou a aparecer corretamente.

No total, o número de dependências declaradas no pom.xml caiu de 36 para 34, nenhuma biblioteca externa de datas permaneceu e o projeto passou a rodar em JDK 8 com 67 testes, todos aprovados. O detalhamento de cada mudança, com os trechos antes e depois, está no documento de plataforma do repositório.

## 6 Planos de evolução: atualização tecnológica

O plano de atualização tecnológica parte de um inventário das versões usadas no pom.xml e indica, para cada componente, a situação atual e o alvo sugerido. A maior parte da pilha está sem suporte: Spring Framework 4.0, Hibernate 4.3, Tomcat 7, Ehcache 2, Bootstrap 2 e Dandelion DataTables. O destino é uma pilha com Java 17 e Spring Boot, com Thymeleaf no lugar das páginas JSP.

A atualização foi dividida em etapas, de E0 a E8, seguindo alguns princípios: manter os testes como rede de proteção, fazer uma etapa por vez em PRs pequenos, medir antes e depois e separar mudanças mecânicas de mudanças de comportamento. A E0 reúne o que já foi entregue neste trabalho (Java 8, camada de dados única, configuração por ambiente, driver MySQL e novos testes), e a E1, a troca do Joda-Time pelo java.time, também foi concluída. As etapas seguintes são: atualizar as bibliotecas dentro da mesma geração (E2), trocar o cache (E3) e a interface (E4), migrar para Spring Boot e Java 17 (E5), adotar migrações de banco versionadas (E6), incluir segurança e integração contínua (E7) e, por fim, modularizar o sistema (E8).

A migração para Spring Boot, a etapa de maior risco, não foi executada nesta entrega. Ela exige trocar os pacotes javax por jakarta, substituir as páginas JSP e a configuração em XML de uma só vez, o que não cabia no prazo do projeto com segurança. O plano registra os principais riscos dessa migração e como reduzi-los, sempre com a suíte de testes como critério para decidir se cada etapa pode avançar.