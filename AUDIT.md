# Build Audit

Run on 2026-11-02. The final audit commands were run from the repository root unless noted.

## Final verification

| Command/check | Result |
|---|---|
| `mvn -q -DskipTests package` in `services/user-service` | Passed |
| `mvn -q -DskipTests package` in `services/product-service` | Passed |
| `mvn -q -DskipTests package` in `services/order-service` | Passed |
| `docker compose up --build` | Passed; all seven containers started |
| `GET http://localhost:3000/` | HTTP 200 |
| `GET http://localhost:8081/api-docs` | HTTP 200 |
| `GET http://localhost:8082/api-docs` | HTTP 200 |
| `GET http://localhost:8083/api-docs` | HTTP 200 |
| Backend `mvn test` reactor | Passed; 22 tests |
| Frontend `npm test` (run in `frontend`) | Passed; 23 tests |
| Frontend `npm run build` (run in `frontend`) | Passed |
| `docker compose config --quiet` | Passed |

## Failures encountered and resolutions

| Failure | Resolution |
|---|---|
| Maven reactor could not read the service POMs: `flyway-database-postgresql` had no managed dependency version. | Removed the unversioned dependency from the three service POMs. The configured Flyway Core version includes PostgreSQL support. Individual service package commands then passed. |
| Backend test compilation failed in `UserControllerIT` at the chained `assertThat(...).isTrue()` assertion. | Replaced the assertion with JUnit's `assertTrue(...)`; the full backend unit-test reactor passed. |
| An initial `npm ci` ran from the repository root and failed because no root `package.json` exists. | Ran npm commands from `frontend`, where the frontend manifest lives, and generated `frontend/package-lock.json` for reproducible `npm ci`. |
| Frontend production build failed because Vite could not find `frontend/index.html`. | Added the HTML entry page, React bootstrap, app component, and styles; production build and frontend tests passed. |
| An earlier Compose startup attempt could not connect to the Docker Desktop Linux engine pipe. | Started Compose again once the Docker daemon was available; all images built, containers started, and HTTP smoke checks passed. |

## Changes made to make the project run

The implementation changes span 29 paths; the audit file, shortcut ignore rule, and removed shortcut make 32 changed paths in total.

### Maven and backend services

- `pom.xml` — root Maven reactor with all three service modules.
- `services/user-service/pom.xml` — removed the unversioned Flyway dependency.
- `services/user-service/src/main/java/com/ecommerce/user/config/SecurityConfig.java` — password encoder and HTTP security configuration.
- `services/user-service/src/main/java/com/ecommerce/user/repository/UserRepository.java` — repository required by the user service.
- `services/user-service/src/main/resources/db/migration/V1__create_users.sql` — user schema migration.
- `services/user-service/src/test/java/com/ecommerce/user/integration/UserControllerIT.java` — fixed the test assertion so tests compile.
- `services/product-service/pom.xml` — removed the unversioned Flyway dependency.
- `services/product-service/src/main/java/com/ecommerce/product/ProductServiceApplication.java` — Spring Boot entry point.
- `services/product-service/src/main/resources/application.yml` — service port and database/runtime configuration.
- `services/product-service/src/main/resources/db/migration/V1__create_products.sql` — product schema migration.
- `services/order-service/pom.xml` — dependencies and Spring Boot packaging for order service.
- `services/order-service/src/main/java/com/ecommerce/order/OrderServiceApplication.java` — Spring Boot entry point.
- `services/order-service/src/main/java/com/ecommerce/order/config/OrderServiceConfig.java` — HTTP client bean used by the product client.
- `services/order-service/src/main/java/com/ecommerce/order/controller/OrderController.java` — order REST endpoints.
- `services/order-service/src/main/java/com/ecommerce/order/dto/CreateOrderRequest.java` — nested request validation and minimum quantity validation.
- `services/order-service/src/main/java/com/ecommerce/order/dto/UpdateOrderStatusRequest.java` — validated status update payload.
- `services/order-service/src/main/java/com/ecommerce/order/exception/OrderExceptionHandler.java` — HTTP responses for order errors.
- `services/order-service/src/main/resources/application.yml` — service port, database, and product-service URL configuration.
- `services/order-service/src/main/resources/db/migration/V1__create_orders.sql` — order and order-item schema migration.

### Frontend, development, and documentation

- `frontend/index.html` — Vite HTML entry point.
- `frontend/package-lock.json` — reproducible frontend dependency installation.
- `frontend/src/App.jsx` — storefront catalog and cart summary.
- `frontend/src/main.jsx` — React and Redux application bootstrap.
- `frontend/src/styles.css` — storefront styling and responsive layout.
- `frontend/src/components/product/ProductCard.jsx` — storefront product-card presentation.
- `frontend/src/components/product/ProductList.jsx` — catalog loading, error, empty, and pagination presentation.
- `frontend/vite.config.js` — development proxies aligned with `/api/v1` service routes.
- `.github/workflows/frontend-ci.yml` — npm cache keyed to the lockfile used by `npm ci`.
- `README.md` — Maven reactor and local build instructions.

### Requested repository cleanup

- `.gitignore` — ignore Windows shortcut files with `*.lnk`.
- `services/order-service/src/main/java/com/ecommerce.lnk` — removed stray tracked shortcut.
