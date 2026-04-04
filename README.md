# Coupon API

API REST em **Java Spring Boot** para gerenciamento de cupons de desconto, com foco em DDD básico, regras de negócio encapsuladas no domínio, testes automatizados e containerização via Docker.

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|------------|---------------|
| Java (JDK) | 17            |
| Maven      | 3.8+          |
| Docker     | 20+           |
| Docker Compose | 2+        |

---

## Execução local (sem Docker)

```bash
# Clonar o repositório
git clone <url-do-repositorio>
cd Desafio-tecnico-Java

# Compilar e executar
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

---

## Execução com Docker

```bash
# Subir a aplicação
docker-compose up --build

# Parar
docker-compose down
```

A aplicação ficará disponível em `http://localhost:8080`.

---

## Variáveis de ambiente

| Variável                      | Descrição                          | Default         |
|-------------------------------|------------------------------------|-----------------|
| `SPRING_PROFILES_ACTIVE`      | Perfil ativo do Spring             | *(não definido)*|
| `SPRING_DATASOURCE_URL`       | URL do banco de dados              | H2 em memória   |
| `SPRING_DATASOURCE_USERNAME`  | Usuário do banco                   | `sa`            |
| `SPRING_DATASOURCE_PASSWORD`  | Senha do banco                     | *(vazio)*       |

> O banco padrão é **H2 em memória** — os dados são perdidos ao reiniciar a aplicação.  
> Console H2 disponível em `http://localhost:8080/h2-console` (apenas para desenvolvimento).

---

## Documentação da API (Swagger)

Acesse `http://localhost:8080/swagger-ui.html` com a aplicação em execução.

---

## Endpoints

### POST `/coupon` — Criar cupom

**Request Body** (`application/json`):

```json
{
  "code": "ABC-123",
  "description": "Cupom de 20% de desconto",
  "discountValue": 0.8,
  "expirationDate": "2026-12-31",
  "published": false
}
```

| Campo            | Tipo      | Obrigatório | Regras                                         |
|------------------|-----------|-------------|------------------------------------------------|
| `code`           | `string`  | Sim           | Alfanumérico; especiais removidos; máx. 6 chars |
| `description`    | `string`  | Sim           | —                                              |
| `discountValue`  | `number`  | Sim           | Mínimo `0.5`                                   |
| `expirationDate` | `string`  | Sim           | ISO 8601 (`yyyy-MM-dd`); deve ser futura       |
| `published`      | `boolean` | Não           | Default `false`                                |

**Response `201 Created`**:

```json
{
  "id": "cef9d1e3-aae5-4ab6-a297-358c6032b1e7",
  "code": "ABC123",
  "description": "Cupom de 20% de desconto",
  "discountValue": 0.8,
  "expirationDate": "2026-12-31",
  "status": "ACTIVE",
  "published": false,
  "redeemed": false
}
```

---

### GET `/coupon/{id}` — Buscar cupom por ID

**Response `200 OK`**: mesmo esquema do POST.

**Response `404 Not Found`** quando o ID não existe ou o cupom foi deletado.

---

### DELETE `/coupon/{id}` — Excluir cupom (soft delete)

**Response `204 No Content`** em caso de sucesso.

**Response `422 Unprocessable Entity`** quando o cupom já foi deletado.

**Response `404 Not Found`** quando o ID não existe, ou não for encontrado.

---

## Testes

```bash
# Executar todos os testes
./mvnw clean test
```

A suíte inclui:
- **Testes de domínio** (`CupomTest`) — validações internas da entidade: criação válida, sanitização do `code`, desconto mínimo, data futura, soft-delete e double-delete
- **Testes de serviço** (`CupomServiceTest`) — lógica de negócio com Mockito: fluxos de criação, busca e exclusão com interações de repositório verificadas
- **Testes de controller** (`CupomControllerTest`) — camada web isolada com `@WebMvcTest`: mapeamento HTTP, serialização de resposta e propagação de exceções para status codes
- **Testes do handler de exceções** (`GlobalExceptionHandlerTest`) — cobre cada `@ExceptionHandler` individualmente: 400 (body inválido e validation), 404, 422 e 500
- **Testes de integração** (`CupomIntegrationTest`) — fluxo HTTP completo com `@SpringBootTest` + H2: POST, GET, DELETE e cenários de erro end-to-end

---

## Decisões técnicas

- **`@SQLRestriction` em vez de `@Where`.**  
  O enunciado sugere `@Where(clause = "deleted = false")`, que está deprecated desde o Hibernate 6.3. Optei por `@SQLRestriction("status <> 'DELETED'")`, que é o substituto direto e compatível com a versão do Hibernate incluída no Spring Boot 3.x.

- **Constructor privado + factory method `Cupom.create()`.**  
  Tornar o construtor de `Cupom` privado garante que toda instanciação passe pelas validações do domínio (data futura, desconto mínimo, sanitização). É a diferença entre poder e precisar validar, se o construtor fosse público, um novo caller no Service ou em testes poderia criar um cupom inválido silenciosamente.

- **Validação em duas camadas com semânticas distintas.**  
  O DTO usa `@NotBlank` e `@Positive` para rejeitar payloads estruturalmente inválidos (campo ausente, valor zero/negativo), retornando 400 com `fieldErrors`. O domínio valida regras de negócio (desconto mínimo de 0.5, data estritamente futura), retornando 422. Essa separação evita que a API exponha detalhes internos do domínio em erros de formato, e vice-versa.

- **`published` como `Boolean` wrapper, não `boolean` primitivo.**  
  Com `boolean`, um campo ausente no JSON é deserializado como `false`, o que coincide com o default esperado, mas esconde a intenção do cliente. Com `Boolean`, o campo ausente chega como `null`, e o Service aplica o default explicitamente via `Boolean.TRUE.equals(dto.published())`. Isso permite distinguir "o cliente enviou `false`" de "o cliente omitiu o campo".

- **Truncamento silencioso do `code` para 6 caracteres, com guard para código vazio.**  
  Em vez de rejeitar códigos com mais de 6 caracteres alfanuméricos (o que quebraria clients que enviam códigos longos), optei por truncar para os primeiros 6. Ao mesmo tempo, adicionei uma validação defensiva: se após a remoção de caracteres especiais o código ficar vazio, uma `BusinessException` é lançada, cenário que a sanitização pura do enunciado não endereçava.

- **HTTP 422 para violações de regra de negócio, 400 para input malformado.**  
  Optei por mapear `BusinessException` para 422 (Unprocessable Entity) em vez de 400 (Bad Request). A distinção semântica: 400 indica que o servidor não conseguiu interpretar a requisição (JSON inválido, campo ausente); 422 indica que o servidor entendeu a requisição, mas as regras de negócio a rejeitam. Respostas de validação do Bean Validation retornam 400 com mapa de `fieldErrors`; respostas de domínio retornam 422 com `message` textual.

- **Dockerfile multi-stage (build + runtime) em vez de single-stage.**  
  O enunciado sugere um Dockerfile simples que copia um JAR pré-compilado. Optei por um build multi-stage: o primeiro estágio usa `maven:3.8.8-eclipse-temurin-17` para compilar dentro do container (com `-DskipTests`), e o segundo copia apenas o JAR para uma imagem `eclipse-temurin:17-jre`. Isso elimina a dependência de um JAR local e reduz a imagem final (JRE em vez de JDK completo).

- **Testes fatiados: `@WebMvcTest` para controllers, `@SpringBootTest` para integração.**  
  Os testes de controller (`CupomControllerTest`, `GlobalExceptionHandlerTest`) usam `@WebMvcTest`, que carrega apenas a camada web com service mockado, que são rápidos e focados no mapeamento HTTP. Os testes de integração (`CupomIntegrationTest`) usam `@SpringBootTest` com H2, percorrendo o stack inteiro (controller → service → domínio → repository). Essa separação permite rodar os testes de contrato sem banco e os de fluxo completo com persistência real.

- **JaCoCo integrado ao ciclo `test` do Maven, sem goal separado.**  
  Configurei o `jacoco-maven-plugin` com dois executions: `prepare-agent` (que instrumenta o bytecode antes dos testes) e `report` vinculado à fase `test`. Com isso, `./mvnw clean test` já gera o relatório HTML em `target/site/jacoco/` sem nenhum comando adicional. A alternativa seria vincular à fase `verify`, mas como o projeto não tem um pipeline de CI configurado, preferir a geração antecipada facilita a inspeção local imediata após cada rodada de testes.
