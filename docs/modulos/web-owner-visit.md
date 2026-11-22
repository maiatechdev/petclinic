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
As métricas comparativas ficam na
[#31](https://github.com/maiatechdev/petclinic/issues/31).
