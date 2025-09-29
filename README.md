# User Service

Spring Boot (Java 17, Spring Boot 3.x) microservice for managing users.

- REST endpoints to list and create users
- In-memory H2 database initialized with schema.sql and data.sql
- Runs on port 7050 by default
- Configurable via application.properties and overridable with environment variables
- Secured by API key header `X-API-KEY` validated against `API_KEY`
- Integrates with optional external APIs for taxes (impostos) and payments (pagamentos)
- Spring Boot Actuator enabled
- Swagger/OpenAPI documentation available

## Requirements
- Java 17
- Maven 3.8+

## How to run

```bash
# Set an API key (required for all endpoints)
export API_KEY=my-secret

# Optional external integrations
# export TAX_API_ENABLED=true
# export TAX_API_URL="http://localhost:9001/taxes"
# export PAYMENTS_API_ENABLED=true
# export PAYMENTS_API_URL="http://localhost:9002/payments"

# Optional welcome message override
# export WELCOME_MESSAGE="Hello CKAD"

# Start the service
mvn spring-boot:run
```

Service will start on http://localhost:7050

## Endpoints

All endpoints (except Actuator) require header: `X-API-KEY: <your API_KEY>`

- GET `/users`
  - Response
    ```json
    {
      "welcomeMessage": "Hello CKAD",
      "users": [
        {
          "id": 1,
          "name": "Maria Silva",
          "email": "maria@example.com",
          "cpf": "12345678900",
          "impostos": [],
          "pagamentos": []
        }
      ]
    }
    ```

- GET `/users/cpf/{cpf}`
  - Busca um usuário pelo CPF. Retorna 200 com o usuário (inclui `impostos` e `pagamentos`) ou 404 se não encontrado.

- POST `/users`
  - Body
    ```json
    { "name":"Maria Silva", "email":"maria@example.com", "cpf":"12345678900" }
    ```
  - Validations: name not empty, email valid, cpf not empty
  - On validation error returns 400 with details

- Actuator (no API key required):
  - Root: http://localhost:7050/actuator
  - Health: http://localhost:7050/actuator/health
  - Info: http://localhost:7050/actuator/info
  - Env: http://localhost:7050/actuator/env

## Swagger / OpenAPI

Once the app is running, open:

- Swagger UI: http://localhost:7050/swagger-ui/index.html
- OpenAPI JSON: http://localhost:7050/v3/api-docs

Note: Swagger UI pages are accessible without the API key, but API calls from the UI must include `X-API-KEY` in request headers.

## H2 Database & Migrations (Liquibase)

The in-memory H2 database is initialized by Liquibase using the changelog at `classpath:db/changelog/db.changelog-master.yaml`.
It creates the `users` table and seeds a few sample records.

- H2 console: http://localhost:7050/h2-console
  - JDBC URL: `jdbc:h2:mem:usersdb`
  - User: `sa`
  - Password: (empty)

## Curl examples

```bash
API=http://localhost:7050
KEY=my-secret

# List users
curl -H "X-API-KEY: $KEY" "$API/users"

# Get user by CPF
curl -H "X-API-KEY: $KEY" "$API/users/cpf/12345678900"

# Create user
curl -X POST "$API/users" \
  -H "X-API-KEY: $KEY" \
  -H "Content-Type: application/json" \
  -d '{"name":"Ana","email":"ana@example.com","cpf":"11122233344"}'

# Health via Actuator
curl "$API/actuator/health"
```

## Configuration via environment variables

- `WELCOME_MESSAGE` – message returned at GET /users (default: `Hello CKAD`)
- `API_KEY` – required header value for `X-API-KEY` on all endpoints
- `TAX_API_ENABLED` – `true` to enable taxes API calls (default `false`)
- `TAX_API_URL` – optional URL for external taxes API (used only if enabled), called with `?cpf=...`
- `PAYMENTS_API_ENABLED` – `true` to enable payments API calls (default `false`)
- `PAYMENTS_API_URL` – URL for payments API (used only if enabled), called with `?cpf=...`

## Notes

- External API responses are included as arrays in the `impostos` and `pagamentos` fields. If the API returns an object, it is wrapped as a single-element array. If the API is disabled or not configured, the lists are empty.
- Secrets (like API keys) are not logged.


## Versionamento no GitHub

Siga os passos abaixo para versionar este projeto no repositório indicado:

Repositório: https://github.com/ltsiciliano/k8s-cert-lab-user.git

1) Inicialize o Git localmente (se ainda não estiver):

```bash
git init
# defina a branch padrão como main
git branch -M main
```

2) Garanta que artefatos de build não sejam versionados (.gitignore já incluso):

```bash
# se por acaso a pasta target/ já tiver sido adicionada, remova do index
git rm -r --cached target || true
```

3) Aponte o remoto do GitHub (escolha uma das opções):

- HTTPS
```bash
git remote add origin https://github.com/ltsiciliano/k8s-cert-lab-user.git
```
- SSH
```bash
git remote add origin git@github.com:ltsiciliano/k8s-cert-lab-user.git
```

4) Faça o primeiro commit e envie:

```bash
git add .
git commit -m "Initial commit: User Service (Spring Boot 3, Liquibase, Actuator, Swagger)"
# se o repositório remoto estiver vazio, o push funcionará direto
git push -u origin main
```

Caso o repositório remoto já tenha commits, faça um pull com rebase antes do push:

```bash
git pull --rebase origin main
# resolva conflitos (se houver) e então
git push -u origin main
```

5) (Opcional) Verifique o CI do GitHub Actions

Este projeto inclui um workflow em `.github/workflows/ci.yml` que compila e roda os testes em cada push/PR usando Java 17 e Maven.

Se preferir desativar ou customizar, edite/remova esse arquivo.


## Docker

You can build and run the service in a container. The image exposes port 7050 and accepts the same env vars.

```bash
# 1) Build the image (replace with your Docker Hub namespace/repo)
export IMAGE=ltsiciliano/k8s-cert-lab-user:0.1.0

docker build -t $IMAGE .

# 2) Run the container
# API_KEY is required by all endpoints
# Other optional vars: WELCOME_MESSAGE, TAX_API_ENABLED, TAX_API_URL, PAYMENTS_API_ENABLED, PAYMENTS_API_URL, PORT

docker run --rm \
  -p 7050:7050 \
  -e API_KEY=my-secret \
  -e WELCOME_MESSAGE="Hello CKAD" \
  $IMAGE

# 3) Test
curl http://localhost:7050/actuator/health

# 4) Push to Docker Hub (version the image)
# Login first (interactive)
docker login

# Push the version tag
docker push $IMAGE

# Optionally also tag as latest and push
LATEST=${IMAGE%:*}:latest
docker tag $IMAGE $LATEST
docker push $LATEST
```

Notes:
- Default container port is 7050. You can change the container's internal port by setting env PORT, but remember to adjust the host mapping accordingly.
- Keep versions consistent (e.g., 0.1.0, 0.1.1, etc.) when publishing to Docker Hub.
