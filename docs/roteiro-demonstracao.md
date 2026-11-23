# Roteiro da demonstração lado a lado do PetClinic

O roteiro prevê **12 min 45 s**. Na tela esquerda, use o legado `v0-legado` (`df1596b`) em `http://localhost:9965/petclinic/`. Na direita, use a versão integrada testada `567c797` em `http://localhost:9966/petclinic/`. As duas instâncias começam com os dados de exemplo do HSQLDB. A revisão `c11b6ad` pode ser usada no lado integrado porque adiciona apenas o rascunho do relatório.

## Preparação do ensaio

Em PowerShell, a partir do repositório, prepare cópias em uma pasta temporária:

```powershell
$demoRoot = Join-Path ([IO.Path]::GetTempPath()) 'petclinic-apresentacao'
New-Item -ItemType Directory -Path $demoRoot -Force | Out-Null
git archive --format=zip --output="$demoRoot/legado.zip" v0-legado
git archive --format=zip --output="$demoRoot/integrado.zip" 567c797
Expand-Archive "$demoRoot/legado.zip" "$demoRoot/legado" -Force
Expand-Archive "$demoRoot/integrado.zip" "$demoRoot/integrado" -Force
$legacyPom = (git show '1710b2a:pom.xml') -join "`n"
[IO.File]::WriteAllText("$demoRoot/legado/pom.xml",
    $legacyPom.Replace('<port>9966</port>', '<port>9965</port>'))
```

A cópia legada recebe somente o POM do ajuste de build #45 e a porta 9965 para a demonstração. O ECJ desse ajuste permite ao Tomcat 7 compilar as JSPs no JDK 8. O código da aplicação e a tag `v0-legado` permanecem como na revisão original. Para reproduzir a medição dos testes da tabela, use cópias dos dois commits com seus POMs originais.

Abra um terminal em cada cópia, selecione o **JDK 8**, confirme com `mvn.cmd -version` e rode `mvn.cmd "-Dmaven.tomcat.address=127.0.0.1" tomcat7:run`. Deixe pré-abertas a busca de tutores, a página do tutor 6 e a tabela de métricas. Guarde a saída dos testes antes de subir o Tomcat, pois `mvn clean` apaga arquivos usados pela instância se for executado no mesmo diretório.

## Apresentação

| Tempo | Quem | O que mostrar e dizer |
|---|---|---|
| 0:00–0:50 | Gabriel Maia (Integrante 1) | Identificar as revisões e abrir as duas páginas iniciais. Explicar que o projeto evoluiu a mesma aplicação Spring MVC e preservou os fluxos de tutores, pets e visitas. |
| 0:50–2:50 | Kezia (Integrante 2) | Mostrar `mvn -version` com JDK 8 e a alteração Java 1.8/ECJ no POM. Explicar que o ajuste permite ao Tomcat 7 compilar as JSPs na cópia legada e deixa visível o problema específico da rota de visitas. |
| 2:50–5:15 | Davi (Integrante 3) | Buscar `Davis` nos dois lados e mostrar Betty e Harold. Em seguida abrir a rota isolada de visitas no legado, identificar o JSP ausente e mostrar onde o histórico e o cadastro de visita ficam na versão integrada. Mostrar os 13 testes de controllers e a complexidade de `processFindForm` de 4 para 2. |
| 5:15–7:15 | Gabriel Maia (Integrante 1) | Mostrar no código as três implementações de persistência no legado e o Spring Data JPA na integrada. Mostrar `petclinic.db`, o script com `<HOST>` e `<SENHA>` e o resultado da execução com MySQL 5.7 em Docker. |
| 7:15–9:15 | Gabriel Torre (Integrante 4) | Mostrar cadastro e validação de pet no estado da revisão apresentada. Vincular as mudanças de domínio à entrega integrada correspondente ou apresentá-las como trabalho planejado. |
| 9:15–11:15 | Leo (Integrante 5) | Mostrar `/vets.html` e `/vets.xml`, com o mesmo conjunto de veterinários. Mostrar JSON somente se o endpoint da #36 estiver disponível na revisão usada e tiver sido testado. |
| 11:15–12:45 | Equipe | Exibir a [tabela de métricas](metricas.md), explicar a contagem de testes e resumir o próximo passo de modularização. |

Há dois integrantes chamados Gabriel. Na tabela, **Gabriel Maia** é o Integrante 1 (abertura e persistência) e **Gabriel Torre** é o Integrante 4 (domínio). Cada integrante apresenta cerca de dois minutos e deve conseguir explicar o próprio módulo sem ler. Se alguém não puder estar presente, o grupo define antes do ensaio quem cobre a parte dele, para o tempo total de 12 min 45 s não mudar.

## Sequências para mostrar na tela

**Tutores e visitas.** Nas duas instâncias, a busca `/owners?lastName=Davis` deve listar Betty e Harold. No legado, `/owners/6/pets/7/visits` retorna 404 ao encaminhar para `visitList.jsp`, que não existe. Na integrada, a mesma URL retorna 404 porque o mapeamento foi removido. No ensaio, só a resposta legada mencionou `visitList.jsp`. Em `/owners/6`, mostre Samantha e seu histórico, depois abra `/owners/6/pets/7/visits/new`, salve uma visita de exemplo e mostre-a após recarregar os detalhes. O ganho demonstrado é a retirada do mapeamento quebrado e a cobertura do fluxo existente.

**Troca de banco sem editar XML ou POM.** Prepare antes da gravação um MySQL 5.7 de teste. Execute `createDB.sql` com `<SENHA>` e com o `<HOST>` que o servidor MySQL identifica para o aplicativo. No Docker com aplicativo no Windows, use o host visto pelo contêiner. Consulte `src/main/resources/db_readme.txt`. Inicie uma sessão separada do Tomcat com `PETCLINIC_DB=mysql`, `PETCLINIC_DB_PASSWORD` e, se necessário, `PETCLINIC_DB_URL`. Mostre o resultado em `/owners/find` e `/vets.html` e confirme no banco a contagem dos 10 tutores. Reserve o startup para o ensaio e use a saída já preparada durante a fala.

**Valores que precisam bater.** O legado tem 13 métodos de teste distintos e 33 execuções, pois dez métodos de serviço rodam em três implementações. A versão integrada tem 48 execuções e 13 testes diretos de controllers de tutores e visitas. A API JSON da #36 e a migração para Spring Boot da #41 seguem em aberto na base `567c797`. Se a base mudar antes da gravação, revise o commit, as rotas e os números exibidos.

## Gravação de segurança

Ensaie a sequência completa com cronômetro, janelas lado a lado e zoom igual. Grave as URLs, a busca, o histórico e o quadro de métricas. Mostre as variáveis de configuração com valores de exemplo e mantenha as credenciais fora da tela. Deixe uma captura ou um trecho gravado da troca para MySQL disponível caso o Docker demore durante a apresentação.
