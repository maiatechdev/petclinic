# Relatório técnico: estrutura, responsáveis e estado

Mapa para montar o relatório final (ABNT, mínimo de 8 páginas). Serve para ver quem escreve o quê, o que já existe e onde estão os dados de cada parte. **Cada pessoa escreve a própria seção com as próprias palavras**: os arquivos em `docs/` são fonte de dados, não texto para copiar.

Entrega: **segunda 23/11, 23:59**. Rascunhos até 12h, versão revisada às 18h.

## 1. Estrutura pedida pela proposta

A proposta (Etapa 4) exige: capa e introdução, fundamentação teórica, desenvolvimento (diagnóstico do sistema e refatoração aplicada) e conclusão com referências. Pela matriz de avaliação, também pesa a **viabilidade e análise crítica** (visão de negócio, prazo e riscos), então ela ganha seção própria.

## 2. Sumário proposto, com numeração única

| Seção | Título | Quem | Páginas (meta) |
|---|---|---|---|
| Capa | Capa e folha de rosto | Gabriel Maia (1) | 1 |
| 1 | Introdução (contexto, objetivos, metodologia) | Gabriel Maia (1) | 1 |
| 2 | Fundamentação teórica | Kezia (2) | 1,5 |
| 3 | O sistema legado (ficha técnica e requisitos) | Gabriel Maia (1) | 1 |
| 4 | Diagnóstico: 4.1 arquitetura atual e 4.2 débito técnico | Davi (3) e Gabriel Torre (4) | 2 |
| 5 | Refatoração aplicada (subseções abaixo) | todos | 4 |
| 5.1 | Persistência e configuração | Gabriel Maia (1) | |
| 5.2 | Controllers de tutores e visitas | Davi (3) | |
| 5.3 | Domínio e validação de animais | Gabriel Torre (4) | |
| 5.4 | Plataforma (Java 8, pom, datas) | Kezia (2) | |
| 5.5 | Veterinários, documentação e mensagens | Leo (5) | |
| 6 | Planos de evolução (modularização e atualização tecnológica) | Davi (3) e Kezia (2) | 1 |
| 7 | Viabilidade e análise crítica | Leo (5) | 1 |
| 8 | Conclusão | Leo (5) | 0,5 |
| Ref. | Referências (lista única, em ordem alfabética) | todos | 1 |

Soma da meta: cerca de 13 páginas, folgada em relação ao mínimo de 8 para absorver o que for cortado.

## 3. Estado dos rascunhos

| Parte | Arquivo | Estado |
|---|---|---|
| Capa, 1 e 3, e 5.1 | `docs/relatorio/integrante-1-introducao-e-persistencia.md` | PR #58 aberto, a reescrever com as palavras do autor |
| 4.1, 5.2 e 6 (modularização) | `docs/relatorio/integrante-3-arquitetura-e-web.md` | Na `main` (PR #52) |
| 2 (fundamentação) | a criar | Sem rascunho |
| 4.2 e 5.3 | a criar | Sem rascunho |
| 5.4 | `docs/modulos/plataforma.md` | PR #66 aberto (dados). O texto do relatório ainda não existe |
| 5.5 | `docs/modulos/vet.md` (PR #62) e `docs/modulos/servico-mensagens-pt.md` (PR #68) | Dados nos PRs abertos. O texto do relatório não existe |
| 6 (atualização tecnológica) | `docs/plano-atualizacao.md` | PR #57 aberto |
| 7 e 8 | a criar | Sem rascunho |

## 4. Onde estão os dados de cada seção

**2. Fundamentação teórica (Kezia).** Conceitos: evolução e manutenção de software, reengenharia e engenharia reversa, refatoração, código legado e testes de caracterização, *code smells* e débito técnico, complexidade ciclomática. Autores da proposta: Sommerville (2019), Fowler (2020), Feathers (2013), Martin (2011) e McCabe (1976). Explicar cada conceito e ligar ao que o grupo fez (por exemplo, "testes de caracterização" aparecem em todos os módulos).

**4.2 Débito técnico (Gabriel Torre).** Dados: o relatório da Etapa 2 (acoplamento, duplicação, complexidade, documentação e manutenibilidade) e `docs/metricas.md` (NLOC, CCN, duplicação, Javadoc e testes antes e depois). Contar o que o grupo encontrou e como mediu.

**5.3 Domínio e validação (Gabriel Torre).** Dados: PRs #54 (testes de `PetController` e `PetValidator`) e o PR da #32. Pontos que valem registro: a regra de nome duplicado e de tipo obrigatório só vale no **cadastro**, e não na edição; o `PetValidator.validate` tem a maior complexidade do projeto (CCN 7). A refatoração da #33 ainda não foi feita, então a seção descreve o diagnóstico, os testes e o que ficou como trabalho futuro.

**5.4 Plataforma (Kezia).** Dados: `docs/modulos/plataforma.md` (PR #66) e `docs/modulos/plataforma-datas.md`. Pontos: Java 1.6 para 1.8, ECJ no plugin do Tomcat, driver MySQL, Mockito, Joda-Time fora, e o defeito de fuso (a data 2012-09-04 aparecia como 09-03).

**5.5 Veterinários, documentação e mensagens (Leo).** Dados: `docs/modulos/vet.md`, os testes de `VetControllerTests` e de cache, `docs/modulos/servico-mensagens-pt.md`, `messages_pt.properties` e o Javadoc do `ClinicService`. Pontos: o cache `@Cacheable` nunca tinha sido testado; o código morto do `VetsAtomView`; Javadoc de 17,7% para 29,5% (ver `metricas.md`, e o valor atualizado depois do #68).

**6. Planos de evolução (Davi e Kezia).** Dados: `docs/plano-modularizacao.md` e `docs/plano-atualizacao.md` (E0 a E8, riscos e estimativas). A migração para Spring Boot **não foi executada**: ver a decisão registrada na issue #41.

**7. Viabilidade e análise crítica (Leo).** Critério de avaliação com peso próprio. Responder: valor de negócio (por que modernizar), prazo (o que coube em quantos dias e o que ficou de fora), riscos e como foram tratados, e uma visão crítica honesta do que funcionou e do que não funcionou. Dados: seção de riscos do `plano-atualizacao.md`, a tabela de resultados de `metricas.md`, a decisão da #41 e `docs/contribuicoes.md` (PR #60, mostra como o trabalho foi dividido).

**8. Conclusão (Leo).** Retomar os objetivos da introdução e dizer, com números, o que foi atingido. Fechar com o trabalho futuro.

## 5. Padrões para unificar na consolidação

- **Termos:** usar "tutor" (não *owner*), "animal" (não *pet*) e "visita", como na Etapa 2. Nomes de classes e métodos continuam em inglês, em fonte monoespaçada.
- **Quadros e figuras:** numeração contínua no relatório todo (Quadro 1, 2, 3...), título acima, e **Fonte** abaixo ("Fonte: elaborado pelos autores (2026)" ou a ferramenta usada). Hoje cada rascunho começa a numeração do zero.
- **Medidas:** usar os números de `docs/metricas.md` e citar o PR de onde vêm. Total de testes por ponto no tempo: 33 (legado), 46, 26, 31, 43, 53, 67, e o valor final a confirmar com `mvn clean test` na `main`.
- **Citações:** só citar um autor com página se o texto passar a citá-lo diretamente. Paráfrases levam (AUTOR, ano).
- **PRs e commits:** citar como evidência de participação no apêndice, ou em nota, e não no corpo do texto.

## 6. Formatação ABNT (aplicar no documento final)

- [ ] Papel A4, margens de 3 cm (esquerda e superior) e 2 cm (direita e inferior).
- [ ] Fonte 12 (Times New Roman ou Arial), espaçamento 1,5 entre linhas, citações longas e legendas em fonte menor.
- [ ] Numeração de páginas no canto superior direito, a partir da introdução.
- [ ] Títulos de seção numerados e em caixa alta nas seções primárias.
- [ ] Capa com instituição, curso, integrantes, título, disciplina, professora, cidade e ano.
- [ ] Referências alinhadas à esquerda, em ordem alfabética, formato ABNT.

## 7. Como consolidar

1. Cada integrante termina a sua seção (neste repositório, em `docs/relatorio/`, ou direto no documento final).
2. Um único documento (Word ou Google Docs) recebe as seções na ordem do sumário. Quem monta é o Gabriel Maia.
3. Revisão cruzada: cada seção é lida por outro integrante, para conferir números, termos e ortografia.
4. Exportar em PDF e conferir o número de páginas e a formatação.
5. Fazer a entrega e anexar, se pedido, `docs/contribuicoes.md` como evidência de participação.
