# Uso de IA no projeto

## Ferramentas usadas

- **Claude Code (Claude Opus 5.5)**: gerou o esqueleto do projeto, os testes e a implementação, executou `.\mvnw test` e fez os commits locais.
- **Claude (chat)**: segundo assistente, atuando como orquestrador (escreveu as instruções de cada etapa para o Claude Code) e como revisor do código gerado.
- **Spring Initializr (start.spring.io)**: usado apenas para obter o Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`) e o `.gitattributes`.
- **Playwright (Chromium headless)**: usado pelo Claude Code na v1.1.0 para verificar a interface web em um navegador real. Foi instalado em uma pasta temporária fora do repositório; o projeto não tem `package.json` nem `node_modules`.

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
- Validação em clone limpo como teste do próprio README: o repositório foi clonado do GitHub para uma pasta nova, e os passos e blocos de código do README foram executados sem alteração (`.\mvnw test`, `.\mvnw spring-boot:run` e as chamadas de exemplo). Isso revelou o id fixo no exemplo do `curl.exe`. Depois da correção, a validação foi refeita em um clone novo e passou.
- O smoke test real revelou uma inconsistência de escala do `valorBruto` que os testes unitários não detectaram: o POST devolvia `"valorBruto":200`, enquanto as listagens (lidas do banco) devolviam `200.00`. Os testes usavam sempre valores com 2 casas (`200.00`), então nunca exercitaram uma entrada sem escala.
- **Interface web (v1.1.0), teste da página com MockMvc e forward**: o Spring Boot serve `static/index.html` em `/` por *forward* interno, e o MockMvc não executa forwards. Em `GET /`, ele devolve `200` com `forwardedUrl("index.html")`, mas com corpo vazio e sem content-type. Um teste que exigisse `text/html` e os ids no corpo de `GET /` falharia mesmo com a página correta. O Claude Code apontou isso no plano, antes de escrever o teste, e o teste foi dividido em duas checagens: `GET /` com o forward e `GET /index.html` com `text/html` e os ids. O `GET /` real ficou coberto pela verificação em navegador.
- **Interface web, verificação em navegador automatizado**: um script Playwright executou 8 passos contra a aplicação no ar, todos OK. Foram eles: abrir `/`; criar pedidos e conferir 25%/R$ 150,00, 9%/R$ 91,00 e o teto 30%/R$ 70,00; entregar e ver sair da lista; `409` tratado na página (pedido entregue por fora, via API, e clique na linha desatualizada); erros por campo com o formulário vazio; cliente `<img src=x onerror=alert(1)>` exibido como texto, sem `<img>` no DOM e sem diálogo; data padrão = amanhã local.
  - O passo da data fixou o relógio do navegador em 14/10/2026 23:30 em São Paulo, que já é 15/10 em UTC. O campo mostrou `2026-10-15`, enquanto `toISOString()` daria `2026-10-16`. Isso prova que o cálculo usa o fuso local.
- **Mutation check que exigiu `clean`**: para o mutation "apagar o `index.html`", foi preciso rodar `mvnw clean test`. Um `mvnw test` simples continuaria encontrando a cópia antiga em `target/classes` e daria um falso "sobreviveu".

## O que a IA errou

- **Overflow de `int` no cálculo**: a fórmula `Math.min(anos * taxa, 30)` veio da especificação do prompt escrito com o assistente de chat, e o Claude Code a implementou fielmente na etapa GREEN. Com valores grandes (ex.: OURO com 600.000.000 anos), `anos * taxa` estoura `int` e o resultado vira negativo (`-1294967296`). Nem o prompt nem os testes iniciais consideravam valores extremos.
  - **Lição**: especificações em prompt também carregam bugs, e testes de borda são o que os revelam.
- **Teste com `@CsvSource`**: a primeira versão do teste de `deTexto` usava `ignoreLeadingAndTrailingWhitespace = false` e `.trim()` no valor esperado, sem necessidade. O próprio Claude Code corrigiu antes do commit, já que espaços dentro de aspas simples no `@CsvSource` são preservados por padrão.
- **Versão do Spring Boot**: o Spring Initializr só oferecia Spring Boot 4.x, enquanto o projeto pedia 3.x. O `pom.xml` foi escrito manualmente e fixado na 3.5.16 (última 3.x no Maven Central na data).
- **Script do smoke test**: na primeira execução, o script PowerShell escrito pelo Claude Code usava `Write-Output` dentro de uma função. Isso contaminava o valor de retorno e quebrava a leitura do `id`. O POST dessa tentativa chegou a criar um pedido, que aparece nas listagens do smoke test. Foi corrigido com `Write-Host` e o fluxo foi refeito.
- **Id fixo no exemplo do README**: o README gerado trazia, no exemplo com `curl.exe`, um id fixo (`PATCH /pedidos/1`). Executado depois do exemplo com `Invoke-RestMethod`, que já entregava o pedido 1, ele respondia `409` em vez de `200`. Isso só foi detectado porque o README foi executado ao pé da letra em um clone limpo do GitHub. O exemplo foi corrigido para usar o id devolvido pelo POST.
- **Interface web, primeira versão do `index.html`**: usava a classe `sr-only` no cabeçalho da coluna de ações sem defini-la no CSS, e tinha um estilo inline desnecessário na mensagem de erro. O próprio Claude Code corrigiu na revisão, antes de rodar os testes e antes do commit.
- **Comando travado na etapa do CI**: um comando de edição do README incluiu, por engano, uma chamada `python` sem utilidade, que travou no atalho do Python do Windows. A tarefa foi interrompida, o Claude Code conferiu que o README não tinha sido alterado e refez a edição com a ferramenta de edição.

## O que revisei e validei

- O overflow foi identificado em uma revisão de código feita com um segundo assistente (Claude, no chat), que atuou como revisor do código gerado pelo Claude Code. Antes da correção, o problema foi confirmado com um teste que falhava (`expected: <30> but was: <-1294967296>`); a correção (cálculo em `long` antes do teto) só entrou depois.
- A ausência de validação de categoria nula (que gerava `NullPointerException`) também foi tratada test-first.
- Escala do `valorBruto`: o smoke test mostrou a diferença entre `200` e `200.00`. O `200` veio da serialização do `ConvertTo-Json` do PowerShell, que escreve `200.00` como `200`, e a API devolvia a escala recebida. No smoke test, o Claude Code atribuiu a diferença apenas ao `ConvertTo-Json`. A classificação como inconsistência da API foi feita na revisão com o segundo assistente (Claude, no chat), e isso levou à correção test-first (RED com `200` → `200.00` falhando, depois GREEN).
  - O primeiro desenho da correção (`setScale(2, HALF_UP)` sobre a entrada) foi trocado. O próprio Claude Code apontou que ele causaria arredondamento duplo (ex.: `10.305` viraria `10.31` antes do desconto). A regra final normaliza sem arredondar valores com até 2 casas significativas (`200` e `200.000` → `200.00`) e rejeita com `400` valores com mais de 2 casas (ex.: `10.305`). Os dois ramos foram conferidos com mutation checks.
- Conferi que o `pom.xml` usa Spring Boot 3.5.16 e Java 21, e que nenhum arquivo de `target/` foi versionado (`git ls-files`).
- Conferi a saída do `.\mvnw test` em cada etapa (RED falhando pelo motivo certo, GREEN/REFACTOR com todos os testes passando).
