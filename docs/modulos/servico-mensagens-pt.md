# Documentação e tradução para o português

Issue [#37](https://github.com/maiatechdev/petclinic/issues/37).

## 1. Problemas encontrados

| Onde | Problema | Smell |
|---|---|---|
| `service/ClinicService.java` | 0 de 8 métodos com Javadoc; o comentário da interface dizia apenas "Mostly used as a facade for all Petclinic controllers" | Comentário ausente |
| `resources/messages/` | Mensagens só em inglês e alemão; não havia `messages_pt.properties` | Funcionalidade ausente |

## 2. Antes

```java
/**
 * Mostly used as a facade for all Petclinic controllers
 *
 * @author Michael Isvy
 */
public interface ClinicService {

    Collection<PetType> findPetTypes() throws DataAccessException;

    Owner findOwnerById(int id) throws DataAccessException;
```

## 3. Depois

```java
/**
 * Finds an owner by id, with the owner's pets already loaded.
 *
 * @param id the owner id
 * @return the owner, or {@code null} if none exists with this id
 * @throws DataAccessException if the query fails
 */
Owner findOwnerById(int id) throws DataAccessException;
```

```properties
welcome=Bem-vindo
duplicate=já está em uso
```

## 4. Explicação técnica

- **Javadoc.** Cada método documenta o contrato, inclusive o que os testes de
  persistência (PR #51) revelaram: `findOwnerById` e `findPetById` devolvem `null` quando o
  registro não existe, e `findOwnerByLastName` busca por prefixo (texto vazio traz todos os
  tutores).
- **Tradução.** `messages_pt.properties` tem as mesmas 8 chaves de `messages.properties`. O
  Spring escolhe o arquivo pelo idioma do navegador (`Accept-Language`), então um navegador em
  português passa a ver as boas-vindas e as mensagens de validação traduzidas, sem mudança de
  configuração.
- **Codificação.** O `ResourceBundleMessageSource` do Java 8 lê arquivos `.properties` em
  ISO-8859-1. Por isso os acentos estão escritos como escapes Unicode (`á` = `á`). Um
  arquivo salvo em UTF-8 com acentos diretos apareceria com caracteres trocados na tela.
- **Fora do escopo.** A troca de idioma por um link na tela e a extração dos textos fixos dos
  JSPs (menu, títulos e botões continuam em inglês) ficaram de fora. Os JSPs serão substituídos
  por Thymeleaf na migração (#41).

## 5. Testes que comprovam o comportamento

`PortugueseMessagesTests` (2 testes):

- as mensagens em português são resolvidas com os acentos corretos (`Bem-vindo`,
  `já está em uso`, `data inválida`). Se o arquivo for salvo em UTF-8 com acentos diretos, o
  teste falha;
- `messages_pt.properties` tem todas as chaves de `messages.properties`.

No Tomcat, um navegador em português (`Accept-Language: pt-BR`) viu "Bem-vindo" na página
inicial e "já está em uso" ao cadastrar um animal com nome repetido; um navegador em inglês
continuou vendo "Welcome". `mvn clean test`: 67 testes antes, 69 depois.

## 6. Métricas

Javadoc contado como em [`docs/metricas.md`](../metricas.md): árvore sintática do Java 8
(`JavacTask.parse` e `DocTrees`), métodos públicos e métodos de interface, sem construtores.

| Métrica | Antes | Depois |
|---|---|---|
| Métodos com Javadoc em `ClinicService` | 0/8 (0%) | 8/8 (100%) |
| Métodos públicos com Javadoc, produção | 34/99 (34,3%) | 42/99 (42,4%) |
| Idiomas com tradução | inglês, alemão | inglês, alemão, português |
| Testes de tradução | 0 | 2 |

