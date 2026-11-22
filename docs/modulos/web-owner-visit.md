# Módulo web de tutores e visitas

## Caracterização do legado ([#29](https://github.com/maiatechdev/petclinic/issues/29))

Antes, a suíte web verificava o XML de veterinários em `VisitsViewTests`. Os fluxos
de `OwnerController` e `VisitController` não tinham testes de controller.

Em `VisitController.showVisits` (linhas 80–84), o nome de view literal
`new ModelAndView("visitList")` representa uma referência que o compilador
Java não consegue verificar. O resolvedor tenta encaminhar
`GET /owners/*/pets/{petId}/visits` a `/WEB-INF/jsp/visitList.jsp`.

Depois da caracterização, `OwnerVisitControllerTests` verifica busca com zero, um
e vários resultados, cadastro e edição de tutores com formulários válidos e
inválidos, abertura e envio do formulário de visita e o destino da listagem.
Para a listagem, o teste confirma o modelo, o encaminhamento e a ausência do
JSP. MockMvc exercita o despacho do Spring MVC sem executar o JSP.
`VetsViewTests` continua verificando o XML de veterinários.

Com JDK 8, `mvn clean test` passou com 44 testes, incluindo os 11 novos. No
Tomcat, usando o ajuste de build do PR #45 em uma cópia local, o formulário de
visita respondeu HTTP 200 e a listagem respondeu HTTP 404.

## Refatoração ([#30](https://github.com/maiatechdev/petclinic/issues/30))

**Antes.** `processCreationForm` e `processUpdateOwnerForm` repetiam a
validação, `clinicService.saveOwner(owner)` e `status.setComplete()`.
`showVisits` retornava `new ModelAndView("visitList")` para um JSP ausente.

**Depois.** Os formulários compartilham `saveValidOwner(owner, result, status)`.
O cadastro redireciona usando o ID atribuído no salvamento e a edição usa o
ID da URL. A busca separa a consulta da escolha entre redirecionamento e
lista. Os nomes das views ficam em constantes nos dois controllers.

A rota isolada foi removida porque o aplicativo não aponta para ela e o
histórico de cada animal já aparece em `GET /owners/{ownerId}`. O formulário
`GET /owners/{ownerId}/pets/{petId}/visits/new` continua acessível.
Os testes verificam a ausência do mapeamento e o histórico no modelo dos
detalhes do tutor. Com JDK 8, os 46 testes passaram. No Tomcat, os detalhes
exibiram o histórico, o formulário abriu e a URL removida respondeu 404.
## Antes e depois medidos ([#31](https://github.com/maiatechdev/petclinic/issues/31))

O **Antes** usa `v0-legado` (`df1596b`). O **Depois** usa o merge da #30
(`0f71603`). A #29 acrescentou os testes de caracterização entre essas duas
versões sem alterar os controllers.

### Código e diagnóstico

- **Duplicated Code.** Em `OwnerController.java:69–77` e `:119–127`, criar e
  editar executavam a mesma validação, gravação e finalização da sessão. A
  decisão do redirecionamento ficou nos handlers e a sequência comum foi
  extraída. [Extract Function](https://refactoring.com/catalog/extractFunction.html)
- **Responsabilidades misturadas.** Em `OwnerController.java:86–109`, a busca
  normalizava o sobrenome, consultava o serviço, adicionava erros e montava três
  saídas. No depois, `processFindForm` trata o resultado vazio e delega a busca
  e a apresentação dos resultados a métodos privados.
- **Código morto na navegação e literal de view.** Em `VisitController.java:80–85`,
  `showVisits` encaminhava para um JSP ausente. Sem links internos para essa
  rota, a #30 removeu o mapeamento. Os nomes das views usados pelos handlers
  restantes passaram para constantes. [Remove Dead Code](https://refactoring.com/catalog/removeDeadCode.html) e [Replace Magic Literal](https://refactoring.com/catalog/replaceMagicLiteral.html)

**Antes.** O trecho abaixo aparecia no cadastro e na edição, seguido do
redirecionamento específico de cada operação.

```java
if (result.hasErrors()) {
    return "owners/createOrUpdateOwnerForm";
} else {
    this.clinicService.saveOwner(owner);
    status.setComplete();
    // Cada handler montava seu próprio redirecionamento.
}
```

**Depois.** Os dois handlers chamam o mesmo método. O cadastro usa o ID gerado
no salvamento e a edição usa o ID da URL.

```java
private boolean saveValidOwner(Owner owner, BindingResult result, SessionStatus status) {
    if (result.hasErrors()) {
        return false;
    }
    this.clinicService.saveOwner(owner);
    status.setComplete();
    return true;
}
```

Para visitas, o Antes retornava `new ModelAndView("visitList")`. No Depois,
`showOwner` resolve `VIEW_OWNER_DETAILS` e o JSP existente percorre
`items="${pet.visits}"`. Os testes preservam a busca, o cadastro e a edição
de tutores e o formulário de visitas. Os cenários novos cobrem a busca sem
sobrenome e o histórico nos detalhes.

### Métricas

| Lizard 1.26.1 | NLOC antes → depois | Métodos antes → depois | CCN média antes → depois | CCN máxima antes → depois |
|---|---:|---:|---:|---:|
| `OwnerController` | 92 → 97 | 9 → 12 | 1,6 → 1,5 | 4 → 2 |
| `VisitController` | 54 → 48 | 5 → 4 | 1,2 → 1,2 | 2 → 2 |
| Total dos dois arquivos | 146 → 145 | 14 → 16 | 1,4 → 1,4 | 4 → 2 |

`processFindForm` passou de 17 para 8 NLOC e de CCN 4 para 2. Os dois handlers
de salvamento passaram de 9 para 3 NLOC cada, ambos com CCN 2. A complexidade
foi distribuída entre métodos menores, e a média global ficou estável.

| jscpd 4.3.0 nos dois arquivos | Clones antes → depois | Linhas duplicadas antes → depois | Percentual antes → depois |
|---|---:|---:|---:|
| `mild`, 50 tokens | 1 → 1 | 18 → 9 | 7,86% → 3,83% |
| `weak`, 50 tokens, sem imports | 0 → 0 | 0 → 0 | 0% → 0% |
| `weak`, 30 tokens, sem imports | 2 → 1 | 15 → 11 | 7,54% → 5,37% |

A execução `mild` conta uma sobreposição que inclui imports. Com `weak` a
30 tokens, o clone entre os formulários de tutores desaparece e sobra o
trecho estrutural em comum entre construtores e `@InitBinder`. O resultado
depende do limite de detecção e vale para os dois arquivos analisados.

### Reprodução

Em duas pastas separadas, extraia com `git archive` somente
`OwnerController.java` e `VisitController.java` de cada revisão, no pacote
`src/main/java/org/springframework/samples/petclinic/web`. No diretório dos
dois arquivos de cada revisão, rode:

```powershell
lizard .
npx --yes jscpd@4.3.0 --format java --mode mild --min-lines 5 --min-tokens 50 --reporters json --output ../mild-50 --no-gitignore .
npx --yes jscpd@4.3.0 --format java --mode weak --min-lines 5 --min-tokens 50 --ignore-pattern '^\s*import[^\r\n]*' --reporters json --output ../weak-50 --no-gitignore .
npx --yes jscpd@4.3.0 --format java --mode weak --min-lines 5 --min-tokens 30 --ignore-pattern '^\s*import[^\r\n]*' --reporters json --output ../weak-30 --no-gitignore .
```

Use o Lizard 1.26.1 e a seção `statistics.total` de cada relatório JSON do
jscpd para os totais. As linhas contadas pelo jscpd seguem o seu próprio
denominador, enquanto NLOC e CCN vêm do Lizard.
