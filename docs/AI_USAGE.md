# Uso de IA no projeto

## Ferramentas usadas

- **Claude Code (Claude Opus 5.5)**: gerou o esqueleto do projeto, os testes e a implementação, executou `.\mvnw test` e fez os commits locais.
- **Claude (chat)**: segundo assistente, atuando como orquestrador (escreveu as instruções de cada etapa para o Claude Code) e como revisor do código gerado.
- **Spring Initializr (start.spring.io)**: usado apenas para obter o Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`) e o `.gitattributes`.

## Convenção de commits e coautoria

- Todos os commits produzidos com o Claude Code trazem o trailer `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`, para registrar de forma auditável o apoio de IA.
- A autoria humana permanece: o Author de cada commit é o desenvolvedor, que aprovou o plano ou as instruções de cada etapa antes da execução e revisou o resultado (testes, saídas e código) antes de liberar o avanço.
- Fluxo: o desenvolvedor aprova o plano, o Claude Code executa e mostra a evidência, o desenvolvedor revisa, e só então o trabalho avança. Cada etapa de TDD (RED, GREEN, REFACTOR) é um commit separado.

## O que funcionou

- Trabalho em ciclos TDD (RED → GREEN → REFACTOR) com um commit por etapa, o que deixa o histórico auditável.
- Na etapa RED, os testes falharam pelo motivo certo (`UnsupportedOperationException` ou asserção), sem erro de compilação.
- Verificação por mutação do `strip()` em `Categoria.deTexto`: removido temporariamente, o caso `" prata "` falhou (`Categoria desconhecida: " prata "`); restaurado, todos os testes voltaram a passar. Isso provou que o teste realmente cobre o tratamento de espaços.
- Os casos de borda da revisão (categoria nula e overflow) entraram test-first: teste falhando primeiro, correção depois.
- Mutation check extra no `PedidoService` (trocar `LocalDate.now(clock)` por `LocalDate.now()`, sem o `Clock`): a mutação sobreviveu porque a data real do sistema coincidia com a do clock fixo dos testes (2026-10-14). O próprio Claude Code detectou e corrigiu antes do commit do GREEN, com testes que usam um clock em 2030-06-15. Lição: um teste que passa por coincidência é um teste frágil.
- Camada web, RED verificando o corpo do `ProblemDetail`: os testes de erro conferem `status` e `detail` no corpo, não só o código HTTP. Sem isso, os casos de JSON malformado e de data malformada teriam nascido verdes, porque o tratamento padrão do Spring já responde `400`. Com a checagem do corpo, eles falharam no RED pelo motivo certo (`No value at JSON path "$.status"`).
- Mutation check do `@Valid`: ao remover o `@Valid`, um POST com corpo `{}` continuava recebendo `400`, porque `Categoria.deTexto(null)` lança `IllegalArgumentException`. A mutação só foi detectada porque o teste confere o mapa `campos` do `ProblemDetail` (erro por campo). Conferir apenas o código HTTP deixaria a mutação passar.
- Smoke test real (aplicação no ar com `mvnw spring-boot:run` e chamadas `curl.exe`, sem MockMvc), em 2026-10-14: as 9 chamadas do fluxo responderam como especificado. Foram POST `201` com `Location`, desconto 25% e valor final 150.00; GET `200`; PATCH `200`; GET de novo sem o pedido entregue; PATCH de novo `409`; categoria "bronze" `400`; campos ausentes `400` com o mapa `campos`; data malformada `400`; id inexistente `404`.

## O que a IA errou

- **Overflow de `int` no cálculo**: a fórmula `Math.min(anos * taxa, 30)` veio da especificação do prompt escrito com o assistente de chat, e o Claude Code a implementou fielmente na etapa GREEN. Com valores grandes (ex.: OURO com 600.000.000 anos), `anos * taxa` estoura `int` e o resultado vira negativo (`-1294967296`). Nem o prompt nem os testes iniciais consideravam valores extremos.
  - **Lição**: especificações em prompt também carregam bugs, e testes de borda são o que os revelam.
- **Teste com `@CsvSource`**: a primeira versão do teste de `deTexto` usava `ignoreLeadingAndTrailingWhitespace = false` e `.trim()` no valor esperado, sem necessidade. O próprio Claude Code corrigiu antes do commit, já que espaços dentro de aspas simples no `@CsvSource` são preservados por padrão.
- **Versão do Spring Boot**: o Spring Initializr só oferecia Spring Boot 4.x, enquanto o projeto pedia 3.x. O `pom.xml` foi escrito manualmente e fixado na 3.5.16 (última 3.x no Maven Central na data).

- **Script do smoke test**: na primeira execução, o script PowerShell escrito pelo Claude Code usava `Write-Output` dentro de uma função. Isso contaminava o valor de retorno e quebrava a leitura do `id`. O POST dessa tentativa chegou a criar um pedido, que aparece nas listagens do smoke test. Foi corrigido com `Write-Host` e o fluxo foi refeito.

## O que revisei e validei

- O overflow foi identificado em uma revisão de código feita com um segundo assistente (Claude, no chat), que atuou como revisor do código gerado pelo Claude Code. Antes da correção, o problema foi confirmado com um teste que falhava (`expected: <30> but was: <-1294967296>`); a correção (cálculo em `long` antes do teto) só entrou depois.
- A ausência de validação de categoria nula (que gerava `NullPointerException`) também foi tratada test-first.
- Conferi que o `pom.xml` usa Spring Boot 3.5.16 e Java 21, e que nenhum arquivo de `target/` foi versionado (`git ls-files`).
- Conferi a saída do `.\mvnw test` em cada etapa (RED falhando pelo motivo certo, GREEN/REFACTOR com todos os testes passando).
