# Floricultura API

[![CI](https://github.com/LLSystems/floricultura-api-mini-projeto/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/LLSystems/floricultura-api-mini-projeto/actions/workflows/ci.yml)

Micro-API REST de pedidos de uma floricultura fictícia, desenvolvida como mini-projeto acadêmico (UFG/AKCIT).
Cadastra pedidos com desconto de fidelidade, lista as entregas pendentes de um dia e registra a entrega.

## Tecnologias

- Java 21
- Spring Boot 3.5.x (Spring Web, Spring Data JPA, Bean Validation)
- H2 Database (em memória)
- JUnit 5, Mockito, Spring Boot Test (MockMvc, `@DataJpaTest`, `@WebMvcTest`)
- Maven (via Maven Wrapper, sem instalação prévia)

## Requisitos

- JDK 21 instalado (`java -version` deve mostrar 21)
- Git
- Porta 8080 livre
- Acesso à internet na primeira execução (o wrapper baixa o Maven e as dependências)

Os comandos abaixo são para **PowerShell no Windows**. Em Linux/macOS, use `./mvnw` no lugar de `.\mvnw`.

## Instalação

```powershell
git clone https://github.com/LLSystems/floricultura-api-mini-projeto.git
cd floricultura-api-mini-projeto
.\mvnw test
```

O `.\mvnw test` deve terminar com `Tests run: 63, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`.

> O CI ([GitHub Actions](.github/workflows/ci.yml)) roda os mesmos testes em **Linux** (`ubuntu-latest`, Java 21 Temurin, `./mvnw -B test`) a cada push e pull request na `main`.

## Uso

### Subir a aplicação

```powershell
.\mvnw spring-boot:run
```

A API fica pronta quando o log mostrar `Started FloriculturaApplication`. Ela atende em `http://localhost:8080`.
Para encerrar, use `Ctrl+C` no terminal em que ela está rodando.

### Endpoints

| Método | Rota | Descrição | Respostas |
|---|---|---|---|
| `POST` | `/pedidos` | Cria um pedido (corpo JSON) | `201` + cabeçalho `Location: /pedidos/{id}`; `400` |
| `GET` | `/pedidos/entregas?data=AAAA-MM-DD` | Lista os pedidos **pendentes** com entrega na data. `data` é opcional (padrão: hoje) | `200`; `400` se a data for malformada |
| `PATCH` | `/pedidos/{id}/entregue` | Marca o pedido como entregue | `200`; `404` se não existir; `409` se já estiver entregue |

Corpo do `POST /pedidos` (todos os campos são obrigatórios):

```json
{
  "cliente": "Maria Silva",
  "categoria": "ouro",
  "anosFidelidade": 5,
  "valorBruto": 200.00,
  "dataEntrega": "2026-10-15"
}
```

> **Atenção:** `dataEntrega` precisa ser **hoje ou uma data futura**, senão a API responde `400`. Por isso os exemplos calculam a data na hora.

### Exemplos com `Invoke-RestMethod` (PowerShell)

Em outro terminal, com a aplicação no ar:

```powershell
$amanha = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
$corpo = @{ cliente = "Maria Silva"; categoria = "ouro"; anosFidelidade = 5; valorBruto = 200.00; dataEntrega = $amanha } | ConvertTo-Json

# cria o pedido
$pedido = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/pedidos" -ContentType "application/json" -Body $corpo
$pedido

# lista as entregas pendentes de amanhã
Invoke-RestMethod "http://localhost:8080/pedidos/entregas?data=$amanha"

# marca como entregue
Invoke-RestMethod -Method Patch -Uri "http://localhost:8080/pedidos/$($pedido.id)/entregue"
```

### Exemplos com `curl.exe` (mostram status e cabeçalhos)

No PowerShell 5.1, passar JSON direto na linha de comando do `curl.exe` perde as aspas. Grave o corpo em um arquivo:

```powershell
$amanha = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
@{ cliente = "Maria Silva"; categoria = "ouro"; anosFidelidade = 5; valorBruto = 200.00; dataEntrega = $amanha } |
    ConvertTo-Json | Out-File -Encoding ascii pedido.json

# cria o pedido e guarda o id devolvido (a última linha da resposta é o corpo JSON)
$resposta = curl.exe -s -i -X POST http://localhost:8080/pedidos -H "Content-Type: application/json" --data-binary "@pedido.json"
$resposta
$id = ($resposta[-1] | ConvertFrom-Json).id

curl.exe -s -i "http://localhost:8080/pedidos/entregas?data=$amanha"
curl.exe -s -i -X PATCH "http://localhost:8080/pedidos/$id/entregue"
```

Os ids são sequenciais e o banco começa vazio a cada inicialização. Por isso os exemplos usam o id devolvido pelo POST, em vez de um número fixo.

### Saídas reais (execuções de 2026-10-14, com `dataEntrega` = 2026-10-15)

Criar (`POST /pedidos`, OURO, 5 anos, corpo com `"valorBruto":200`; execução após a normalização de escala, banco recém-iniciado):

```
HTTP/1.1 201
Location: /pedidos/1
{"id":1,"cliente":"Maria Silva","categoria":"OURO","anosFidelidade":5,"valorBruto":200.00,"descontoPercentual":25,"valorFinal":150.00,"dataEntrega":"2026-10-15","status":"PENDENTE"}
```

Os blocos abaixo vêm do smoke test completo, em que o pedido criado recebeu o id 2.

Marcar como entregue (`PATCH /pedidos/2/entregue`):

```
HTTP/1.1 200
{"id":2,"cliente":"Maria Silva","categoria":"OURO","anosFidelidade":5,"valorBruto":200.00,"descontoPercentual":25,"valorFinal":150.00,"dataEntrega":"2026-10-15","status":"ENTREGUE"}
```

Erros (corpo no formato `ProblemDetail`, `Content-Type: application/problem+json`):

```
PATCH /pedidos/2/entregue (de novo)            -> 409
{"type":"about:blank","title":"Pedido já entregue","status":409,"detail":"Pedido já foi entregue: 2","instance":"/pedidos/2/entregue"}

POST /pedidos com "categoria": "bronze"        -> 400
{"type":"about:blank","title":"Requisição inválida","status":400,"detail":"Categoria desconhecida: \"bronze\"","instance":"/pedidos"}

POST /pedidos com "valorBruto": 10.305          -> 400
{"type":"about:blank","title":"Requisição inválida","status":400,"detail":"Valor bruto aceita no máximo 2 casas decimais: 10.305","instance":"/pedidos"}

POST /pedidos com corpo {}                      -> 400
{"type":"about:blank","title":"Dados inválidos","status":400,"detail":"Um ou mais campos obrigatórios estão ausentes ou em branco","instance":"/pedidos","campos":{"cliente":"não deve estar em branco","categoria":"não deve estar em branco","valorBruto":"não deve ser nulo","anosFidelidade":"não deve ser nulo","dataEntrega":"não deve ser nulo"}}

GET /pedidos/entregas?data=20-10-2026           -> 400
{"type":"about:blank","title":"Parâmetro inválido","status":400,"detail":"Valor inválido para o parâmetro 'data': 20-10-2026 (formato esperado: AAAA-MM-DD)","instance":"/pedidos/entregas"}

PATCH /pedidos/999/entregue                     -> 404
{"type":"about:blank","title":"Pedido não encontrado","status":404,"detail":"Pedido não encontrado: 999","instance":"/pedidos/999/entregue"}
```

## Regras de negócio

- **Categorias de fidelidade**: `OURO` (taxa 5) e `PRATA` (taxa 3). O texto é aceito sem diferenciar maiúsculas/minúsculas e ignorando espaços nas pontas (`" Ouro "` vira `OURO`).
- **Desconto percentual** = `anos × taxa`, com **teto de 30%**. Ex.: OURO 5 anos = 25%; OURO 6 anos = 30%; PRATA 3 anos = 9%.
- **Valor final** = `valorBruto × (100 − desconto) / 100`, com 2 casas decimais e arredondamento **HALF_UP**. Ex.: 10,30 com 5% = 9,785, arredondado para 9,79.
- **Validações** (respondem `400`):
  - campos obrigatórios ausentes ou em branco;
  - categoria desconhecida;
  - anos de fidelidade negativos;
  - valor bruto menor ou igual a zero;
  - valor bruto com mais de 2 casas decimais (ex.: `10.305`). Valores com até 2 casas são normalizados para 2 casas, sem arredondamento: `200` e `200.000` viram `200.00`;
  - data de entrega anterior a hoje (hoje é aceito);
  - JSON malformado ou data em formato inválido.
- **Status**: o pedido nasce `PENDENTE` e vai para `ENTREGUE`. Tentar entregar de novo responde `409`; pedido inexistente responde `404`.

## Testes

```powershell
.\mvnw test
```

São 63 testes:

| Tipo | Classe | Testes |
|---|---|---|
| Unitário de domínio (JUnit 5, sem Spring) | `CalculadoraDescontoTest`, `PedidoTest` | 15 + 25 |
| Serviço com Mockito (`Clock` fixo) | `PedidoServiceTest` | 9 |
| Repositório com `@DataJpaTest` (H2) | `PedidoRepositoryTest` | 1 |
| Web com `@WebMvcTest` + `@MockitoBean` | `PedidoControllerTest` | 11 |
| Integração (`@SpringBootTest` + MockMvc, H2 real, sem mocks) | `PedidoApiIntegrationTest` | 1 |
| Contexto da aplicação | `FloriculturaApplicationTests` | 1 |

## Estrutura do projeto

```
src/main/java/br/edu/ufg/floricultura/
├── FloriculturaApplication.java
├── config/       ClockConfig (bean java.time.Clock)
├── controller/   PedidoController
├── domain/       Categoria, CalculadoraDesconto, Pedido, StatusPedido
├── dto/          PedidoRequest, PedidoResponse
├── exception/    GlobalExceptionHandler, PedidoJaEntregueException, PedidoNaoEncontradoException
├── repository/   PedidoRepository
└── service/      PedidoService
src/test/java/br/edu/ufg/floricultura/   (mesma estrutura de pacotes)
docs/AI_USAGE.md                          registro do uso de IA
```

## Decisões de projeto

- **`Clock` injetado**: o "hoje" vem de um bean `java.time.Clock`, e o domínio recebe `hoje` como parâmetro. Assim os testes fixam a data e não dependem do relógio da máquina.
- **Validação em duas camadas, sem duplicação**: o DTO (`PedidoRequest`) só valida presença (`@NotBlank`/`@NotNull`). As regras de negócio (valor > 0, data ≥ hoje, anos ≥ 0, categoria válida) ficam apenas no domínio.
- **Erros padronizados**: um `@RestControllerAdvice` converte as exceções em `ProblemDetail` (RFC 9457) com mensagens em português.
- **TDD com um commit por etapa**: cada funcionalidade tem commits separados de RED, GREEN e REFACTOR. Os testes foram conferidos com *mutation checks* manuais (alterar a regra e confirmar que algum teste falha).

## Limitações

- **H2 em memória**: os dados somem ao reiniciar a aplicação.
- O console do H2 fica ligado em `/h2-console` (JDBC URL `jdbc:h2:mem:floricultura`, usuário `sa`, senha vazia). É útil para estudo, mas não deve ficar exposto em produção.
- Sem autenticação nem autorização.
- Sem paginação na listagem de entregas.
- Domínio fictício e simplificado (sem itens do pedido, endereço ou pagamento).
- Spring Boot 3.5.x por escolha do `pom.xml`. O Spring Initializr já oferece só a 4.x.
- Projeto desenvolvido com apoio de IA generativa; veja [docs/AI_USAGE.md](docs/AI_USAGE.md) para o registro de como a IA foi usada, o que errou e o que foi revisado.

## Próximos passos

- Banco persistente (PostgreSQL, por exemplo) com migrações (Flyway).
- Autenticação (Spring Security).
- Documentação OpenAPI/Swagger (springdoc-openapi).
