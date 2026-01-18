# VeiculosAPI

API REST para gerenciamento de veículos.

## Requisitos

- Java 21
- Maven 3.9+
- Redis (opcional)

## Quick Start

```bash
./mvnw spring-boot:run
```

API: `http://localhost:8080` | Swagger: `http://localhost:8080/swagger-ui.html`

## Comandos

| Comando                  | Descrição          |
| ------------------------ | ------------------ |
| `./mvnw spring-boot:run` | Executar           |
| `./mvnw test`            | Testes             |
| `./mvnw verify`          | Testes + cobertura |
| `./mvnw clean install`   | Build completo     |

## Autenticação

JWT Bearer Token.

```bash
# Obter token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"admin","senha":"admin"}'
```

| Usuário | Senha | Permissões                    |
| ------- | ----- | ----------------------------- |
| admin   | admin | GET, POST, PUT, PATCH, DELETE |
| user    | user  | GET                           |

## Endpoints

| Método | Endpoint                       | Descrição                 |
| ------ | ------------------------------ | ------------------------- |
| POST   | /auth/login                    | Login                     |
| GET    | /veiculos                      | Listar (paginado)         |
| GET    | /veiculos/{id}                 | Buscar por ID             |
| GET    | /veiculos/relatorios/por-marca | Contagem por marca        |
| POST   | /veiculos                      | Criar (ADMIN)             |
| PUT    | /veiculos/{id}                 | Atualizar (ADMIN)         |
| PATCH  | /veiculos/{id}                 | Atualizar parcial (ADMIN) |
| DELETE | /veiculos/{id}                 | Soft delete (ADMIN)       |

### Filtros

```
GET /veiculos?marca=Toyota&ano=2023&cor=Preto&minPreco=20000&maxPreco=50000&page=0&size=10&sort=marca,asc
```

## Exemplos

```bash
# Login e salvar token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"admin","senha":"admin"}' | jq -r '.token')

# Criar
curl -X POST http://localhost:8080/veiculos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"placa":"ABC1234","marca":"Toyota","modelo":"Corolla","ano":2023,"cor":"Preto","precoUsd":25000}'

# Listar
curl http://localhost:8080/veiculos -H "Authorization: Bearer $TOKEN"

# Buscar
curl http://localhost:8080/veiculos/1 -H "Authorization: Bearer $TOKEN"

# Atualizar
curl -X PUT http://localhost:8080/veiculos/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"placa":"ABC1234","marca":"Toyota","modelo":"Corolla XEi","ano":2024,"cor":"Prata","precoUsd":28000}'

# Remover
curl -X DELETE http://localhost:8080/veiculos/1 -H "Authorization: Bearer $TOKEN"
```

## Regras de Negócio

- **Placa única**: duplicada retorna HTTP 409
- **Soft delete**: DELETE marca `ativo=false`
- **Preço em USD**: armazenado em dólares
- **Conversão USD→BRL**: AwesomeAPI (primária) + Frankfurter (fallback), cache Redis

## Respostas de Erro

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Erro de validação",
  "caminho": "/veiculos",
  "detalhes": ["placa: não pode ser vazio"]
}
```

| Status | Descrição       |
| ------ | --------------- |
| 400    | Validação       |
| 401    | Não autenticado |
| 403    | Sem permissão   |
| 404    | Não encontrado  |
| 409    | Placa duplicada |

## Arquitetura

```
com.tinnova.veiculos
├── dominio          # Entidades, repositórios
├── aplicacao        # Serviços
├── infraestrutura   # Segurança, cache, integrações
└── api              # Controllers, DTOs
```

## Stack

Java 21, Spring Boot 4, Spring Data JPA, H2, Flyway, Spring Security + JWT, Redis, Springdoc OpenAPI, JUnit 5, Mockito, Lombok

## Testes

```bash
./mvnw test                    # Executar
./mvnw verify                  # Com cobertura (target/site/jacoco/index.html)
```
