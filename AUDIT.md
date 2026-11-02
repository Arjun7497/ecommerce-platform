# Build Audit

Run from the repository root on 2026-11-02.

| Command | Result |
|---|---|
| `mvn -q -DskipTests package` in `services/user-service` | Passed |
| `mvn -q -DskipTests package` in `services/product-service` | Passed |
| `mvn -q -DskipTests package` in `services/order-service` | Passed |
| `docker compose up --build` | Passed; all services started |
| `GET http://localhost:3000/` | HTTP 200 |
| `GET http://localhost:8081/api-docs` | HTTP 200 |
| `GET http://localhost:8082/api-docs` | HTTP 200 |
| `GET http://localhost:8083/api-docs` | HTTP 200 |

## Failures

None.
