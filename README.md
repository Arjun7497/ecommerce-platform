# E-Commerce Platform

Full-stack e-commerce application built with React, Java Spring Boot microservices, PostgreSQL, tested with TDD & BDD, and deployed to AWS via GitHub Actions.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [AWS & Terraform Cost Guide](#aws--terraform-cost-guide)
  - [Terraform — Free](#terraform--free)
  - [AWS Cost Breakdown](#aws-cost-breakdown-for-this-project)
  - [AWS Free Tier](#aws-free-tier-first-12-months)
  - [Cost by Scenario](#cost-by-usage-scenario)
  - [How to Reduce AWS Costs](#how-to-reduce-aws-costs)
  - [Set a Billing Alert](#set-a-billing-alert-do-this-before-deploying-anything)
  - [Monitor Current Spend](#monitor-current-spend)
  - [Recommended Strategy](#recommended-strategy-for-this-project)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Open in VS Code](#open-in-vs-code)
- [VS Code Extensions](#vs-code-extensions)
- [Running Locally](#running-locally)
  - [Option A — Docker Compose (Full Stack)](#option-a--docker-compose-full-stack)
  - [Option B — Run Services Individually](#option-b--run-services-individually)
- [Running Tests](#running-tests)
  - [TDD — Unit Tests](#tdd--unit-tests)
  - [Integration Tests](#integration-tests)
  - [BDD — Cucumber Tests](#bdd--cucumber-tests)
  - [Frontend Unit Tests](#frontend-unit-tests)
  - [Frontend E2E BDD Tests](#frontend-e2e-bdd-tests)
  - [Run All Tests at Once](#run-all-tests-at-once)
- [Testing APIs with REST Client](#testing-apis-with-rest-client)
- [Query the Database in VS Code](#query-the-database-in-vs-code)
- [VS Code Tasks Reference](#vs-code-tasks-reference)
- [VS Code Launch Configurations](#vs-code-launch-configurations)
- [Smoke Test — Full User Journey](#smoke-test--full-user-journey)
- [CI/CD Pipeline](#cicd-pipeline)
- [AWS Deployment](#aws-deployment)
- [Common Issues & Fixes](#common-issues--fixes)
- [API Reference](#api-reference)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        React Frontend                        │
│                     (Vite + Redux Toolkit)                   │
│                        :3000 / :5173                         │
└───────────────┬─────────────┬──────────────┬────────────────┘
                │             │              │
        /api/v1/users  /api/v1/products  /api/v1/orders
                │             │              │
┌───────────────▼──┐ ┌────────▼──────┐ ┌────▼──────────────┐
│   user-service   │ │product-service│ │  order-service     │
│  Spring Boot :8081│ │Spring Boot:8082│ │ Spring Boot :8083  │
│  JWT Auth        │ │ CRUD + Search  │ │ Calls product-svc  │
└───────────┬──────┘ └───────┬───────┘ └──────────┬─────────┘
            │                │                     │
     ┌──────▼──────┐  ┌──────▼──────┐  ┌──────────▼──────┐
     │  userdb     │  │  productdb  │  │    orderdb       │
     │ Postgres    │  │  Postgres   │  │    Postgres      │
     │  :5432      │  │   :5433     │  │     :5434        │
     └─────────────┘  └─────────────┘  └─────────────────┘
```

Each microservice has its **own database** (database-per-service pattern). The order-service calls product-service via HTTP to validate stock before placing an order.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, Vite, Redux Toolkit, React Hook Form, Zod, Axios, Recharts |
| Backend | Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | PostgreSQL 16, Flyway migrations |
| Auth | JWT (jjwt 0.12) |
| Unit Tests (Backend) | JUnit 5, Mockito, AssertJ |
| Integration Tests | Testcontainers (real Postgres) |
| BDD (Backend) | Cucumber 7, Gherkin feature files |
| Unit Tests (Frontend) | Vitest, React Testing Library, MSW (Mock Service Worker) |
| E2E BDD (Frontend) | Cypress 13, Cucumber preprocessor |
| Coverage | JaCoCo (80% line threshold), Vitest coverage-v8 |
| Containerization | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Cloud | AWS ECS Fargate, ECR, RDS PostgreSQL, ALB |
| Infrastructure as Code | Terraform |

---

## Project Structure

```
ecommerce-platform/
├── .github/
│   └── workflows/
│       ├── user-service-ci.yml        # 5-stage pipeline: test → build → deploy
│       ├── product-service-ci.yml
│       └── frontend-ci.yml
│
├── .vscode/
│   ├── launch.json                    # Run/debug configs for all services
│   ├── tasks.json                     # All task shortcuts
│   ├── settings.json                  # Workspace settings
│   ├── extensions.json                # Recommended extensions
│   └── http/
│       ├── users.http                 # REST Client — User API
│       ├── products.http              # REST Client — Product API
│       └── orders.http                # REST Client — Order API
│
├── services/
│   ├── user-service/                  # Port 8081
│   │   ├── src/main/java/com/ecommerce/user/
│   │   │   ├── model/User.java
│   │   │   ├── dto/                   # RegisterRequest, LoginRequest, UserResponse, AuthResponse
│   │   │   ├── repository/UserRepository.java
│   │   │   ├── service/UserService.java
│   │   │   ├── service/JwtService.java
│   │   │   ├── controller/UserController.java
│   │   │   └── exception/             # GlobalExceptionHandler, custom exceptions
│   │   └── src/test/
│   │       ├── unit/
│   │       │   ├── UserServiceTest.java      # 8 tests — TDD, Mockito
│   │       │   └── UserControllerTest.java   # 4 tests — @WebMvcTest slice
│   │       ├── integration/
│   │       │   └── UserControllerIT.java     # 3 tests — Testcontainers
│   │       └── bdd/
│   │           ├── CucumberRunner.java
│   │           ├── steps/UserSteps.java
│   │           └── features/user_registration.feature   # 6 scenarios
│   │
│   ├── product-service/               # Port 8082
│   │   ├── src/main/java/com/ecommerce/product/
│   │   │   ├── model/Product.java
│   │   │   ├── dto/                   # ProductRequest, ProductResponse
│   │   │   ├── repository/ProductRepository.java
│   │   │   ├── service/ProductService.java
│   │   │   └── controller/ProductController.java
│   │   └── src/test/
│   │       ├── unit/ProductServiceTest.java   # 5 tests
│   │       └── features/product_management.feature   # 6 scenarios
│   │
│   └── order-service/                 # Port 8083
│       ├── src/main/java/com/ecommerce/order/
│       │   ├── model/Order.java, OrderItem.java
│       │   ├── dto/                   # CreateOrderRequest, OrderResponse
│       │   ├── client/ProductClient.java      # HTTP client to product-service
│       │   ├── repository/OrderRepository.java
│       │   ├── service/OrderService.java
│       │   └── controller/OrderController.java
│       └── src/test/
│           ├── unit/OrderServiceTest.java     # 4 tests
│           └── features/order_placement.feature   # 5 scenarios
│
├── frontend/                          # Port 5173 (dev) / 3000 (docker)
│   ├── src/
│   │   ├── components/product/
│   │   │   ├── ProductCard.jsx
│   │   │   └── ProductList.jsx
│   │   ├── store/slices/
│   │   │   ├── cartSlice.js           # Redux — cart state
│   │   │   └── authSlice.js           # Redux — auth state + async thunks
│   │   ├── services/api.js            # Axios clients for all 3 services
│   │   ├── mocks/
│   │   │   ├── handlers.js            # MSW mock API handlers
│   │   │   └── server.js              # MSW test server
│   │   └── test-setup.js
│   ├── src/__tests__/
│   │   ├── components/
│   │   │   ├── ProductCard.test.jsx   # 9 tests
│   │   │   └── ProductList.test.jsx   # 5 tests
│   │   └── hooks/
│   │       └── useCart.test.js        # 8 tests — Redux slice
│   └── cypress/
│       ├── e2e/shopping.feature       # 8 BDD scenarios
│       └── support/step_definitions/shopping.js
│
├── infrastructure/
│   └── terraform/
│       ├── main.tf                    # VPC, ECR, RDS, ECS, ALB
│       └── variables.tf
│
├── docker-compose.yml                 # Full local stack
└── ecommerce.code-workspace           # VS Code workspace file
```

---

## Prerequisites

Install these before running anything:

| Tool | Minimum Version | Download |
|---|---|---|
| Java JDK | 21 | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org/download.cgi |
| Node.js | 20+ | https://nodejs.org |
| Docker Desktop | Latest | https://www.docker.com/products/docker-desktop |
| Git | Latest | https://git-scm.com |
| VS Code | Latest | https://code.visualstudio.com |

**Verify all installs:**
```bash
java -version        # Should show: openjdk 21...
mvn -version         # Should show: Apache Maven 3.9...
node -version        # Should show: v20...
docker -version      # Should show: Docker version 24+...
docker compose version  # Should show: Docker Compose version v2...
```

---

## Open in VS Code

**First time:**
```
File → Open Workspace from File
→ Browse to: C:\Users\785368\ecommerce-platform\
→ Select:    ecommerce.code-workspace
→ Click:     Open
```

**After first time** (VS Code remembers it):
```
File → Open Recent → ecommerce-platform (Workspace)
```

**From terminal:**
```bash
code C:\Users\785368\ecommerce-platform\ecommerce.code-workspace
```

When VS Code opens the workspace it will ask to **install recommended extensions** — click **Install All**.

---

## VS Code Extensions

These are auto-recommended when you open the workspace. Install all of them.

| Extension | What it does |
|---|---|
| **Extension Pack for Java** | Java language support, debugger, refactoring |
| **Spring Boot Dashboard** | One-click start/stop for all Spring Boot services |
| **Maven for Java** | Run Maven goals from the sidebar |
| **Java Test Runner** | Run/debug JUnit tests with inline ▶ buttons |
| **Spring Boot Tools** | Auto-completion for `application.yml` |
| **Cucumber** | Syntax highlighting + navigation for `.feature` files |
| **REST Client** | Send HTTP requests from `.http` files without Postman |
| **Docker** | Manage containers, images, volumes from sidebar |
| **SQLTools + PostgreSQL Driver** | Query all 3 Postgres databases inside VS Code |
| **ESLint** | JavaScript linting |
| **Prettier** | Code formatting |
| **Tailwind CSS IntelliSense** | Tailwind class auto-complete in JSX |
| **GitHub Actions** | Syntax help for workflow YAML files |
| **Terraform** | IaC syntax support |
| **Material Icon Theme** | File icons for better navigation |

---

## Running Locally

### Option A — Docker Compose (Full Stack)

Starts everything in one command: 3 databases + 3 Java services + React frontend.

```bash
cd C:\Users\785368\ecommerce-platform

# Build and start all containers
docker compose up --build

# OR start in background (detached)
docker compose up --build -d
```

Wait for all services to report healthy, then open:

| URL | Service |
|---|---|
| http://localhost:3000 | React Frontend |
| http://localhost:8081/swagger-ui.html | User Service API Docs |
| http://localhost:8082/swagger-ui.html | Product Service API Docs |
| http://localhost:8083/swagger-ui.html | Order Service API Docs |

**Stop everything:**
```bash
docker compose down

# Also wipe all database data (fresh start):
docker compose down -v
```

---

### Option B — Run Services Individually

Better for active development — lets you restart individual services and see logs clearly.

#### Step 1 — Start only the databases
```bash
cd C:\Users\785368\ecommerce-platform
docker compose up postgres-user postgres-product postgres-order -d --wait
```

Verify databases are healthy:
```bash
docker compose ps
# All 3 should show status: healthy
```

#### Step 2 — Start User Service

Open a **new terminal** in VS Code (`Ctrl+Shift+`` `):
```bash
cd services\user-service
mvn spring-boot:run
```

Wait for: `Started UserServiceApplication in X.XXX seconds`

Or use **Spring Boot Dashboard**: click ▶ next to `user-service`

#### Step 3 — Start Product Service

Open another new terminal:
```bash
cd services\product-service
mvn spring-boot:run
```

#### Step 4 — Start Order Service

Open another new terminal:
```bash
cd services\order-service
mvn spring-boot:run
```

#### Step 5 — Start React Frontend

Open another new terminal:
```bash
cd frontend
npm install        # only needed first time
npm run dev
```

Open http://localhost:5173

#### Step 6 — Launch from VS Code Run Panel (Alternative to steps 2-5)

Press `F5` or go to **Run and Debug** (`Ctrl+Shift+D`), then select from the dropdown:

- `▶ User Service` — starts user service with debugger attached
- `▶ Product Service`
- `▶ Order Service`
- `🚀 Launch All Services` — starts all 3 Java services simultaneously
- `🚀 Launch Full Stack` — starts all 3 Java services + opens React in Chrome
- `🌐 Debug React Frontend` — opens Chrome with source maps attached
- `🧪 Debug Vitest Tests` — runs frontend tests in debug mode

---

## Running Tests

### TDD — Unit Tests

Unit tests use **JUnit 5 + Mockito** (backend) and **Vitest + React Testing Library** (frontend). They run in milliseconds with no database or Docker needed.

**From VS Code editor (inline):**
Open any `*Test.java` file → click the ▶ button next to any `@Test` method or class.

**From terminal:**
```bash
# User service
cd services\user-service
mvn test -Dtest="**/*Test" --no-transfer-progress

# Product service
cd services\product-service
mvn test -Dtest="**/*Test" --no-transfer-progress

# Order service
cd services\order-service
mvn test -Dtest="**/*Test" --no-transfer-progress
```

**From VS Code Task Runner** (`Ctrl+Shift+P` → Tasks: Run Task):
- `user: Unit Tests`
- `product: Unit Tests`
- `order: Unit Tests`
- `ALL: Backend Unit Tests` — runs all 3 in parallel

**Expected output:**
```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0  [user-service]
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0  [product-service]
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0  [order-service]
BUILD SUCCESS
```

---

### Integration Tests

Integration tests use **Testcontainers** — they automatically spin up a real Postgres Docker container. **Docker Desktop must be running.**

```bash
cd services\user-service
mvn verify -Dtest="**/*IT" --no-transfer-progress
```

First run downloads the `postgres:16-alpine` image (once only). Subsequent runs are fast.

**From VS Code Task Runner:**
- `user: Integration Tests`
- `product: Integration Tests`

---

### BDD — Cucumber Tests

Cucumber reads `.feature` files written in plain English (Gherkin) and maps them to Java step definitions.

```bash
cd services\user-service
mvn test -Dtest="CucumberRunner" --no-transfer-progress
```

**View the HTML report after running:**
```bash
# Opens in browser
start target\cucumber-reports\cucumber.html
```

Or use VS Code Task: `user: Open Cucumber Report`

**From VS Code Task Runner:**
- `user: BDD Tests (Cucumber)`
- `product: BDD Tests (Cucumber)`

**Feature files location:**
```
services/user-service/src/test/resources/features/user_registration.feature
services/product-service/src/test/resources/features/product_management.feature
services/order-service/src/test/resources/features/order_placement.feature
```

---

### Frontend Unit Tests

Frontend tests use **Vitest + React Testing Library + MSW** (Mock Service Worker intercepts API calls at the network level).

```bash
cd frontend
npm install        # first time only
npm test           # run once
npm run test:watch # re-run on every file save (development mode)
```

**With coverage report:**
```bash
npm run test:coverage
start coverage\index.html   # open the HTML report
```

**From VS Code Task Runner:**
- `frontend: Unit Tests`
- `frontend: Unit Tests Watch`
- `frontend: Unit Tests + Coverage`
- `frontend: Open Coverage Report`

**Tests breakdown:**
```
src/__tests__/components/ProductCard.test.jsx   — 9 tests
src/__tests__/components/ProductList.test.jsx   — 5 tests
src/__tests__/hooks/useCart.test.js             — 8 tests
```

---

### Frontend E2E BDD Tests

E2E tests use **Cypress + Cucumber preprocessor**. They open a real browser and test the full user journey. The frontend dev server must be running first.

**Step 1 — Make sure dev server is running:**
```bash
cd frontend && npm run dev
```

**Step 2 — Interactive mode (see the browser, great for writing/debugging tests):**
```bash
cd frontend
npx cypress open
# Select: E2E Testing → Chrome → shopping.feature
```

**Step 2 — Headless mode (CI style, faster):**
```bash
cd frontend
npm run test:e2e
```

**From VS Code Task Runner:**
- `frontend: Cypress Open (Interactive)` — opens Cypress GUI
- `frontend: Cypress Run (Headless BDD)` — runs all scenarios headless

**Feature file:**
```
frontend/cypress/e2e/shopping.feature   — 8 scenarios
```

---

### Run All Tests at Once

```bash
# All backend services in parallel
cd C:\Users\785368\ecommerce-platform

# Terminal 1
cd services\user-service && mvn verify --no-transfer-progress

# Terminal 2
cd services\product-service && mvn verify --no-transfer-progress

# Terminal 3
cd services\order-service && mvn verify --no-transfer-progress

# Terminal 4 (frontend)
cd frontend && npm run test:coverage
```

**Via VS Code Task Runner** (single action, runs in parallel):
`Ctrl+Shift+P` → Tasks: Run Task → `ALL: Backend Full Test Suite`

---

## Testing APIs with REST Client

The `.vscode/http/` files let you test all APIs directly inside VS Code without Postman.

**How to use:**
1. Open `.vscode/http/users.http`
2. You'll see **Send Request** links above each HTTP call
3. Click to fire — response appears in a side panel

**Files:**
- `.vscode/http/users.http` — register, login, get user
- `.vscode/http/products.http` — create, list, search, update, delete products
- `.vscode/http/orders.http` — place order, get order, update status

**Tip:** The files use `@name` annotations so responses can be chained — e.g. the product ID from `createProduct` is automatically used in the `GET` request below it.

---

## Query the Database in VS Code

The **SQLTools** extension connects to all 3 databases from inside VS Code.

**Connections are pre-configured in `.vscode/settings.json`:**

| Connection Name | Host | Port | Database |
|---|---|---|---|
| User DB (local) | localhost | 5432 | userdb |
| Product DB (local) | localhost | 5433 | productdb |
| Order DB (local) | localhost | 5434 | orderdb |

**How to use:**
1. Click the database icon in the left sidebar (SQLTools)
2. Click the connection name → **Connect**
3. Click **New SQL File** and run queries:

```sql
-- Check registered users
SELECT id, email, first_name, last_name, role, created_at FROM users;

-- Check products
SELECT id, name, price, stock_quantity, category, active FROM products;

-- Check orders with items
SELECT o.id, o.status, o.total_amount, oi.product_name, oi.quantity
FROM orders o
JOIN order_items oi ON oi.order_id = o.id;
```

---

## VS Code Tasks Reference

Access all tasks via `Ctrl+Shift+P` → **Tasks: Run Task**

### Docker Tasks
| Task | Description |
|---|---|
| `Start Databases` | Starts only the 3 Postgres containers |
| `Start All (Docker Compose)` | Starts full stack with Docker |
| `Stop All (Docker Compose)` | Stops all containers |
| `Stop All + Wipe Volumes` | Stops containers and deletes all DB data |

### Backend Test Tasks
| Task | Description |
|---|---|
| `user: Unit Tests` | JUnit 5 unit tests for user-service |
| `user: Integration Tests` | Testcontainers IT tests |
| `user: BDD Tests (Cucumber)` | Cucumber scenarios |
| `user: All Tests + Coverage` | Full suite + JaCoCo report |
| `user: Open Coverage Report` | Opens JaCoCo HTML in browser |
| `user: Open Cucumber Report` | Opens Cucumber HTML in browser |
| `product: Unit Tests` | Unit tests for product-service |
| `product: Integration Tests` | IT tests for product-service |
| `product: BDD Tests (Cucumber)` | Cucumber scenarios |
| `product: All Tests + Coverage` | Full suite |
| `order: Unit Tests` | Unit tests for order-service |
| `order: All Tests + Coverage` | Full suite |
| `ALL: Backend Unit Tests` | All 3 services unit tests in parallel |
| `ALL: Backend Full Test Suite` | All 3 services full suite in parallel |

### Frontend Tasks
| Task | Description |
|---|---|
| `Start Frontend Dev Server` | Starts Vite dev server on :5173 |
| `frontend: Install Dependencies` | Runs npm install |
| `frontend: Unit Tests` | Runs Vitest once |
| `frontend: Unit Tests Watch` | Vitest in watch mode |
| `frontend: Unit Tests + Coverage` | Vitest with coverage report |
| `frontend: Open Coverage Report` | Opens coverage HTML in browser |
| `frontend: Cypress Open (Interactive)` | Cypress GUI with browser |
| `frontend: Cypress Run (Headless BDD)` | Headless Cypress |
| `frontend: Build` | Production build |

---

## VS Code Launch Configurations

Access via **Run and Debug** panel (`Ctrl+Shift+D`) or press `F5`.

| Configuration | Description |
|---|---|
| `▶ User Service` | Runs user-service with env vars pre-set |
| `▶ Product Service` | Runs product-service |
| `▶ Order Service` | Runs order-service |
| `🐛 Debug User Service` | Attaches debugger to port 5005 |
| `🐛 Debug Product Service` | Attaches debugger to port 5006 |
| `🐛 Debug Order Service` | Attaches debugger to port 5007 |
| `🌐 Debug React Frontend` | Opens Chrome with source maps |
| `🧪 Debug Vitest Tests` | Runs frontend tests in debug mode |
| `🚀 Launch All Services` | Compound: starts all 3 Java services |
| `🚀 Launch Full Stack` | Compound: all 3 Java services + React |

---

## Smoke Test — Full User Journey

Once all services are running, test the complete flow end-to-end:

```bash
# 1 — Register a user
curl -X POST http://localhost:8081/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"me@test.com\",\"password\":\"password123\",\"firstName\":\"Me\",\"lastName\":\"Test\"}"

# 2 — Login (copy the token from response)
curl -X POST http://localhost:8081/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"me@test.com\",\"password\":\"password123\"}"

# 3 — Create a product (copy the id from response)
curl -X POST http://localhost:8082/api/v1/products \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Laptop\",\"description\":\"Fast laptop\",\"price\":999.99,\"stockQuantity\":10,\"category\":\"Electronics\"}"

# 4 — List products
curl http://localhost:8082/api/v1/products

# 5 — Place an order (replace USER_ID and PRODUCT_ID with actual UUIDs from above)
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"USER_ID\",\"shippingAddress\":\"123 Main St\",\"items\":[{\"productId\":\"PRODUCT_ID\",\"quantity\":2}]}"
```

Or use the REST Client files in `.vscode/http/` — faster and no copy-paste of UUIDs.

---

## CI/CD Pipeline

Each service has a dedicated GitHub Actions workflow that runs on push to `main` or `develop`.

**Pipeline stages (in order):**

```
Push to main
    │
    ├─ 1. Unit Tests          (JUnit 5 / Vitest)
    │       └── fast, no DB, run in parallel per service
    │
    ├─ 2. Integration Tests   (Testcontainers / Cypress)
    │       └── needs unit tests to pass first
    │
    ├─ 3. BDD Tests           (Cucumber / Cypress E2E)
    │       └── runs in parallel with integration tests
    │
    ├─ 4. Build & Push        (Docker → ECR)
    │       └── only on main branch, needs all tests green
    │
    └─ 5. Deploy to ECS       (rolling update, waits for stability)
```

**Workflow files:**
```
.github/workflows/user-service-ci.yml
.github/workflows/product-service-ci.yml
.github/workflows/frontend-ci.yml
```

**GitHub Secrets required** (Settings → Secrets and variables → Actions):
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

---

## AWS Deployment

### Infrastructure (Terraform)

```bash
cd infrastructure/terraform

# First time setup
terraform init

# Preview changes
terraform plan -var="db_username=admin" -var="db_password=yourpassword"

# Apply
terraform apply -var="db_username=admin" -var="db_password=yourpassword"
```

### Resources created by Terraform
| Resource | Description |
|---|---|
| VPC | 2 AZs, public + private subnets, NAT gateway |
| ECR | 4 repositories (user, product, order, frontend) |
| RDS | PostgreSQL 16, db.t3.micro, private subnet |
| ECS Cluster | Fargate, Container Insights enabled |
| ALB | Application Load Balancer, public, HTTP/HTTPS |

### Manual deploy (without GitHub Actions)
```bash
# Build and push user-service to ECR
cd services/user-service
mvn package -DskipTests
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ECR_URL>
docker build -t ecommerce-user-service .
docker tag ecommerce-user-service:latest <ECR_URL>/ecommerce-user-service:latest
docker push <ECR_URL>/ecommerce-user-service:latest

# Force new ECS deployment
aws ecs update-service --cluster ecommerce-cluster --service user-service --force-new-deployment
```

---

## Common Issues & Fixes

| Problem | Cause | Fix |
|---|---|---|
| `Port 5432 already in use` | Another Postgres is running | Stop it or change ports in `docker-compose.yml` |
| `Docker daemon not running` | Docker Desktop not started | Open Docker Desktop and wait for it to fully start |
| Testcontainers fails | Docker not running | Start Docker Desktop |
| `mvn: command not found` | Maven not on PATH | Add `M2_HOME/bin` to system PATH |
| `java: command not found` | Java not on PATH | Add JDK `bin` folder to system PATH |
| Spring Boot fails to connect to DB | DB not healthy yet | Wait for `docker compose ps` to show `healthy` |
| Flyway migration error | Stale schema from previous run | Run `docker compose down -v` to wipe DB volumes |
| Frontend `npm install` fails | Corrupted `node_modules` | Delete `frontend/node_modules` folder and retry |
| Cypress tests fail | Dev server not running | Start `npm run dev` first |
| Port 8081/8082/8083 in use | Previous service still running | Kill the process: `netstat -ano \| findstr :8081` then `taskkill /PID <PID> /F` |
| `JWT_SECRET` error on startup | Missing env var | It is pre-set in `.vscode/launch.json` for local runs |

---

## API Reference

### User Service — http://localhost:8081

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/users/register` | Register new user |
| POST | `/api/v1/users/login` | Login, returns JWT |
| GET | `/api/v1/users/{id}` | Get user by ID |
| GET | `/swagger-ui.html` | Full API documentation |

### Product Service — http://localhost:8082

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/products` | Create product |
| GET | `/api/v1/products` | List all (paginated) |
| GET | `/api/v1/products/{id}` | Get by ID |
| GET | `/api/v1/products/category/{cat}` | Filter by category |
| GET | `/api/v1/products/search?keyword=x` | Search by keyword |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Soft delete |
| GET | `/swagger-ui.html` | Full API documentation |

### Order Service — http://localhost:8083

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/orders` | Place an order |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| GET | `/api/v1/orders/user/{userId}` | Get orders by user |
| PATCH | `/api/v1/orders/{id}/status` | Update order status |
| GET | `/swagger-ui.html` | Full API documentation |

---

## AWS & Terraform Cost Guide

### Terraform — Free

Terraform itself is **free and open source**. You only pay for the AWS resources it creates, never for the tool itself.

---

### AWS Cost Breakdown for This Project

#### Resources Defined in Terraform

| AWS Resource | Configuration | Est. Cost/Month |
|---|---|---|
| **NAT Gateway** | 1 gateway, single AZ | ~$32 |
| **RDS PostgreSQL** | db.t3.micro, 20GB storage | ~$13 |
| **ALB** | Application Load Balancer | ~$6 |
| **ECS Fargate** | 4 tasks × 0.25 vCPU / 0.5GB RAM | ~$37 |
| **ECR** | 4 repos, ~1GB images | ~$0.10 |
| **CloudWatch Logs** | Container Insights enabled | ~$3 |
| **VPC, Subnets, IGW** | Networking baseline | Free |
| **Total (estimated)** | | **~$91/month** |

> **The NAT Gateway is the most expensive item** — it charges $0.045/hour (~$32/month) just for existing, even when nothing is running through it.

---

### AWS Free Tier (First 12 Months)

If your AWS account is under 12 months old, some resources are covered:

| Resource | Free Tier Allowance | Covered? |
|---|---|---|
| RDS db.t3.micro | 750 hours/month (runs 24/7) | Yes |
| ALB | 750 hours/month | Yes |
| ECR | 500MB storage/month | Yes |
| ECS Control Plane | Always free | Yes |
| ECS Fargate Compute | Not included | **No** |
| NAT Gateway | Not included | **No** |

**Estimated cost on a free tier account: ~$40-50/month** (NAT Gateway + Fargate still charge).

---

### Cost by Usage Scenario

| Scenario | Monthly Cost | Best For |
|---|---|---|
| Local only (Docker Compose) | **$0** | Day-to-day development |
| AWS Free Tier account | **~$40** | First-time AWS demo |
| Full AWS, no free tier | **~$91** | Production-like setup |
| Optimized AWS (see tips below) | **~$20-30** | Portfolio showcase |
| Free cloud alternatives | **$0** | Portfolio on a budget |

---

### How to Reduce AWS Costs

#### Option 1 — Remove NAT Gateway (saves ~$32/month)

Edit `infrastructure/terraform/main.tf`:
```hcl
module "vpc" {
  # ...
  enable_nat_gateway = false
  single_nat_gateway = false
}
```

Then move ECS tasks to public subnets instead of private ones. Acceptable for a demo/portfolio project — not recommended for production.

---

#### Option 2 — Use Fargate Spot Instances (saves ~60% on compute)

Add to your ECS service definitions in Terraform:
```hcl
capacity_provider_strategy {
  capacity_provider = "FARGATE_SPOT"
  weight            = 1
}
```

Fargate Spot costs ~70% less than standard Fargate. Tasks may be interrupted occasionally — fine for portfolio demos, not for production.

---

#### Option 3 — Scale to Zero When Not Demoing

Stop all ECS tasks when not in use (compute stops, but RDS and NAT Gateway still charge):
```bash
# Scale down — no running tasks, no compute cost
aws ecs update-service --cluster ecommerce-cluster --service user-service --desired-count 0
aws ecs update-service --cluster ecommerce-cluster --service product-service --desired-count 0
aws ecs update-service --cluster ecommerce-cluster --service order-service --desired-count 0
aws ecs update-service --cluster ecommerce-cluster --service frontend-service --desired-count 0

# Scale back up before a demo
aws ecs update-service --cluster ecommerce-cluster --service user-service --desired-count 1
aws ecs update-service --cluster ecommerce-cluster --service product-service --desired-count 1
aws ecs update-service --cluster ecommerce-cluster --service order-service --desired-count 1
aws ecs update-service --cluster ecommerce-cluster --service frontend-service --desired-count 1
```

Also stop RDS when not needed (saves ~$13/month):
```bash
# Stop RDS instance
aws rds stop-db-instance --db-instance-identifier ecommerce-postgres

# Start RDS before demo (takes 2-3 minutes to become available)
aws rds start-db-instance --db-instance-identifier ecommerce-postgres
```

---

#### Option 4 — Destroy Everything When Not Needed (cheapest AWS option)

Tear down all infrastructure with one command:
```bash
cd infrastructure/terraform
terraform destroy -var="db_username=admin" -var="db_password=yourpassword"
```

Recreate when needed (takes ~10-15 minutes to provision):
```bash
terraform apply -var="db_username=admin" -var="db_password=yourpassword"
```

**Cost while destroyed: $0.**

---

#### Option 5 — Use Free Cloud Alternatives (Recommended for Portfolio)

You don't need AWS to showcase a portfolio project. These free platforms demonstrate the same Docker + CI/CD + cloud deployment skills at zero cost:

| AWS Service | Free Alternative | Notes |
|---|---|---|
| ECS Fargate | **Railway.app** | Free tier, Docker-native, deploy from GitHub |
| ECS Fargate | **Render.com** | Free tier, auto-deploy on push |
| RDS PostgreSQL | **Supabase** | Free PostgreSQL, 500MB included |
| RDS PostgreSQL | **Neon.tech** | Free serverless PostgreSQL |
| ECR | **Docker Hub** | Free public image repositories |
| ALB | Included in Railway/Render | Built-in routing and HTTPS |
| GitHub Actions CI | Same — no change | Free 2000 minutes/month |

**Total cost with free alternatives: $0/month** while still demonstrating Docker, CI/CD pipelines, microservices, and cloud deployment to interviewers.

---

### Set a Billing Alert (Do This Before Deploying Anything)

Always configure a cost alert before touching AWS so you are never surprised by an unexpected bill.

**Via AWS Console:**
1. Go to **AWS Console → Billing → Budgets → Create Budget**
2. Choose **Cost Budget**
3. Set monthly budget amount: `$10`
4. Add your email address
5. Alert threshold: `80% of budget`

**Via AWS CLI:**
```bash
aws budgets create-budget \
  --account-id $(aws sts get-caller-identity --query Account --output text) \
  --budget '{
    "BudgetName": "ecommerce-monthly-alert",
    "BudgetLimit": {"Amount": "10", "Unit": "USD"},
    "TimeUnit": "MONTHLY",
    "BudgetType": "COST"
  }' \
  --notifications-with-subscribers '[{
    "Notification": {
      "NotificationType": "ACTUAL",
      "ComparisonOperator": "GREATER_THAN",
      "Threshold": 80
    },
    "Subscribers": [{
      "SubscriptionType": "EMAIL",
      "Address": "your-email@example.com"
    }]
  }]'
```

---

### Monitor Current Spend

Check your AWS bill at any time from the terminal:
```bash
# Total spend for the current month
aws ce get-cost-and-usage \
  --time-period Start=$(date +%Y-%m-01),End=$(date +%Y-%m-%d) \
  --granularity MONTHLY \
  --metrics BlendedCost \
  --query "ResultsByTime[0].Total.BlendedCost"

# Spend broken down by service
aws ce get-cost-and-usage \
  --time-period Start=$(date +%Y-%m-01),End=$(date +%Y-%m-%d) \
  --granularity MONTHLY \
  --metrics BlendedCost \
  --group-by Type=DIMENSION,Key=SERVICE \
  --query "ResultsByTime[0].Groups[*].[Keys[0],Metrics.BlendedCost.Amount]" \
  --output table
```

---

### Recommended Strategy for This Project

Since this is a **portfolio and learning project**, follow this approach to keep costs near $0:

| Activity | Where to Run | Cost |
|---|---|---|
| Daily development | Local — `docker compose up` | $0 |
| Running all tests | Local — Maven + Vitest | $0 |
| CI pipeline (test + build stages) | GitHub Actions — free 2000 min/month | $0 |
| Permanent demo URL | Railway.app or Render.com free tier | $0 |
| Live AWS demo for an interview | Deploy just before, `terraform destroy` after | ~$1-2 |

**Always set a $10 billing alert as a safety net regardless of your approach.**
