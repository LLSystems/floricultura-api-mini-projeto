# Uso de IA no projeto

## Ferramentas usadas

- **Claude Code (Claude Opus 5.5)**: gerou o esqueleto do projeto, os testes e a implementação, executou `.\mvnw test` e fez os commits locais.
- **Claude (chat)**: segundo assistente, atuando como orquestrador (escreveu as instruções de cada etapa para o Claude Code) e como revisor do código gerado.
- **Spring Initializr (start.spring.io)**: usado apenas para obter o Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`) e o `.gitattributes`.

## O que funcionou

- Trabalho em ciclos TDD (RED → GREEN → REFACTOR) com um commit por etapa, o que deixa o histórico auditável.
- Na etapa RED, os testes falharam pelo motivo certo (`UnsupportedOperationException` ou asserção), sem erro de compilação.
- Verificação por mutação do `strip()` em `Categoria.deTexto`: removido temporariamente, o caso `" prata "` falhou (`Categoria desconhecida: " prata "`); restaurado, todos os testes voltaram a passar. Isso provou que o teste realmente cobre o tratamento de espaços.
- Os casos de borda da revisão (categoria nula e overflow) entraram test-first: teste falhando primeiro, correção depois.

## O que a IA errou

- **Overflow de `int` no cálculo**: a fórmula `Math.min(anos * taxa, 30)` veio da especificação do prompt escrito com o assistente de chat, e o Claude Code a implementou fielmente na etapa GREEN. Com valores grandes (ex.: OURO com 600.000.000 anos), `anos * taxa` estoura `int` e o resultado vira negativo (`-1294967296`). Nem o prompt nem os testes iniciais consideravam valores extremos.
  - **Lição**: especificações em prompt também carregam bugs, e testes de borda são o que os revelam.
- **Teste com `@CsvSource`**: a primeira versão do teste de `deTexto` usava `ignoreLeadingAndTrailingWhitespace = false` e `.trim()` no valor esperado, sem necessidade. O próprio Claude Code corrigiu antes do commit, já que espaços dentro de aspas simples no `@CsvSource` são preservados por padrão.
- **Versão do Spring Boot**: o Spring Initializr só oferecia Spring Boot 4.x, enquanto o projeto pedia 3.x. O `pom.xml` foi escrito manualmente e fixado na 3.5.16 (última 3.x no Maven Central na data).

## O que revisei e validei

- O overflow foi identificado em uma revisão de código feita com um segundo assistente (Claude, no chat), que atuou como revisor do código gerado pelo Claude Code. Antes da correção, o problema foi confirmado com um teste que falhava (`expected: <30> but was: <-1294967296>`); a correção (cálculo em `long` antes do teto) só entrou depois.
- A ausência de validação de categoria nula (que gerava `NullPointerException`) também foi tratada test-first.
- Conferi que o `pom.xml` usa Spring Boot 3.5.16 e Java 21, e que nenhum arquivo de `target/` foi versionado (`git ls-files`).
- Conferi a saída do `.\mvnw test` em cada etapa (RED falhando pelo motivo certo, GREEN/REFACTOR com todos os testes passando).
