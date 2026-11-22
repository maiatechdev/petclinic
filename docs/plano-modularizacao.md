# Plano de modularização do PetClinic

O PetClinic continua como uma aplicação Spring MVC com um único banco. Na
base desta proposta (`0f71603`), os controllers usam `ClinicService` e a sua
implementação coordena quatro repositórios. `Owner`, `Pet` e `Visit` também
participam do binding de formulários e da sessão HTTP. A proposta separa
casos de uso por contexto e restringe as dependências entre pacotes.

## Serviços por contexto

| Contexto | Serviço e responsabilidade | Dados de que precisa |
|---|---|---|
| `owner` | `OwnerService` busca por prefixo, mantém dados cadastrais e monta os detalhes do tutor. | `OwnerRepository` e consultas de leitura de animais e visitas. |
| `pet` | `PetService` cria e edita animais, resolve tipos e valida o nome no âmbito do tutor. | `PetRepository` e consulta de leitura de tutores. |
| `visit` | `VisitService` registra visitas para um animal e fornece o histórico por animal. | `VisitRepository` e consulta de leitura de animais. |
| `vet` | `VetService` lista veterinários e especialidades e mantém o cache existente. | `VetRepository`. |

Cada serviço controla a transação do caso de uso. As consultas devolvem os
dados de leitura necessários já carregados. Por exemplo, os detalhes do
tutor incluem os animais e suas visitas, com a montagem feita antes de a
view renderizar. Uma operação que atravesse contextos usa uma interface de
leitura específica, implementada por uma consulta que depende do repositório
do contexto de origem. Essas consultas independem dos serviços de escrita.

## Dependências permitidas

| Origem | Pode depender de |
|---|---|
| `web.owner`, `web.pet`, `web.visit`, `web.vet` | Formulários do próprio contexto e contratos dos serviços e resultados de leitura usados pela tela. |
| `service.owner`, `service.pet`, `service.visit`, `service.vet` | `model`, contratos de repositório e interfaces estreitas de leitura quando o caso de uso envolve outro contexto. |
| `repository` e suas implementações | Modelo persistido e infraestrutura de acesso a dados. |
| `model` | Classes do domínio e anotações de persistência ou validação em uso no projeto. |
| Configuração Spring | Serviços, repositórios, controllers e infraestrutura para ligar os componentes. |

Controllers acessam os repositórios pelo serviço. Repositórios e modelos
desconhecem HTTP, JSP e formulários. Dependências entre contextos passam por
contratos de leitura, evitando ciclos entre serviços como tutor → animal →
tutor. O Spring Data JPA fica atrás dos contratos de repositório.

## Formulários e modelos de leitura

- `OwnerForm` recebe nome, sobrenome, endereço, cidade e telefone. O ID da
  edição vem da rota, e o serviço carrega o tutor antes de aplicar os campos.
- `PetForm` recebe nome, data de nascimento e tipo. O serviço resolve o tipo e
  aplica as regras de cadastro e edição após caracterizá-las separadamente.
- `VisitForm` recebe data e descrição. O serviço associa a visita ao pet
  identificado pela rota. Um resultado como `OwnerDetails` expõe o histórico
  para a tela sem usar a mesma entidade como objeto de entrada.

Os controllers fazem o binding e reexibem erros com `BindingResult`. As regras
que dependem do banco, como verificar outro animal com o mesmo nome, ficam
no serviço. Os testes devem preservar nomes de campos, mensagens, valores
digitados após erro e redirecionamentos. Variáveis de sessão, quando ainda
necessárias durante a transição, guardam dados do formulário e IDs.

## Sequência de migração e aceite

1. Fixar o comportamento com os testes de caracterização, incluindo a busca
   sem sobrenome e o histórico do tutor. Manter `ClinicService` como fachada
   de compatibilidade enquanto houver consumidores.
2. Extrair primeiro a leitura de veterinários, que usa um repositório e tem
   cache próprio. Preservar a saída HTML, XML e Atom.
3. Migrar tutores, animais e visitas, um contexto por PR. Em cada etapa,
   introduzir o serviço, migrar os consumidores e trocar o binding por um
   formulário. Preservar a distinção entre cadastro e edição de pets até que
   os testes identifiquem quais regras podem mudar.
4. Inventariar os consumidores restantes de `ClinicService`, incluindo
   `PetTypeFormatter`, testes e configuração Spring. Remover a fachada quando
   todos usarem seus contratos de contexto.

Em cada PR, rodar `mvn clean test` no JDK 8 e conferir com
`mvn tomcat7:run` os fluxos alterados. A validação final inclui busca com zero,
um e vários tutores, formulários válidos e inválidos, histórico de visitas,
cadastro de pet e visita, formatos de veterinários e ausência de ciclos entre
pacotes. O [Antes/Depois do módulo](modulos/web-owner-visit.md) é a referência
para comparar a evolução dos controllers.
