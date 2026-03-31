# 💊 PharmaCare — Online Pharmacy Platform

> A production-grade, microservices-based online pharmacy system built with Spring Boot 3, enabling end-to-end medicine ordering, prescription management, secure authentication, and admin analytics.

---

## 🚀 Project Description

**PharmaCare** is a fully-featured online pharmacy backend system that solves the complexity of managing medicine catalogs, prescription-gated orders, inventory control, and customer authentication — all at microservices scale.

The platform handles:
- **Patient-side flows**: Registration, login (OTP-secured), browsing medicines, uploading prescriptions, placing orders, and tracking deliveries
- **Admin-side flows**: Prescription review queue, order lifecycle management, inventory tracking, and sales reporting
- **Infrastructure concerns**: Distributed JWT auth, async event messaging via RabbitMQ, circuit breakers, rate limiting, and observability via Prometheus + Loki + Zipkin

This project exists to demonstrate a real-world pharmacy backend that goes beyond CRUD — incorporating the Transactional Outbox Pattern, JWT revocation with Redis, per-route rate limiting, and Eureka-based service discovery.

---

## 🧠 Features

### 🔐 Authentication & Security
- Two-factor login: password → OTP sent via email
- Email verification on signup
- Password reset via tokenized email link
- JWT access tokens + refresh token rotation
- JWT revocation support via Redis (JTI + hash blacklisting)
- BCrypt password hashing

### 🏥 Medicine Catalog
- Paginated medicine browsing with keyword search, category filter, and prescription flag
- Category management
- Inventory tracking with reservation and adjustment events
- Prescription upload and pharmacist review workflow

### 🛒 Order & Delivery
- Shopping cart with per-user item management (max 10 quantity per item)
- Checkout with address selection and inventory reservation via Feign client
- Full order lifecycle: PLACED → CONFIRMED → SHIPPED → DELIVERED / CANCELLED
- Simulated payment processing

### 🛡️ Admin Panel
- Order status management and cancellation
- Prescription review queue (approve/reject with notes)
- Sales reports with CSV and PDF export
- Inventory reports
- Dashboard stats aggregated from catalog service

### 🏗️ Infrastructure
- API Gateway: JWT validation, per-route circuit breakers, rate limiting (sliding window, in-memory)
- Eureka Service Registry for service discovery
- RabbitMQ with Transactional Outbox Pattern for reliable event delivery
- Email Service: OTP delivery, login alerts, email verification, password reset
- Swagger UI aggregated at the gateway layer (role-aware doc access)
- Prometheus metrics, Zipkin tracing, Loki logging

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.11 |
| Cloud | Spring Cloud 2023.x (Gateway, Eureka, OpenFeign, Resilience4j) |
| Security | Spring Security 6, JJWT |
| Database | MySQL 8 (per service) |
| Caching | Redis (JWT revocation, OTP storage, rate limiting) |
| Messaging | RabbitMQ |
| ORM | Spring Data JPA / Hibernate |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Email | Spring Mail (JavaMailSender) + Thymeleaf templates |
| Build | Maven 3.9 |
| Containerization | Docker (multi-stage builds, Eclipse Temurin 17 JRE) |
| Observability | Prometheus, Zipkin, Loki (Grafana-compatible) |

---

## 📂 Project Structure

```
OnlinePharmacy/
├── serviceregistry/          # Eureka Server — service discovery hub (port 8761)
├── api-gateway/              # Spring Cloud Gateway — routing, auth, rate limiting (port 8080)
├── Auth/                     # Authentication Service — users, JWT, OTP, passwords (port 8081)
├── Catalog/                  # Catalog & Prescription Service — medicines, inventory, Rx (port 8082)
├── Order/                    # Order & Delivery Service — cart, checkout, orders, payments (port 8083)
├── admin/                    # Admin Service — management, reports, dashboards (port 8084)
└── emailservice/             # Email Notification Service — SMTP delivery via RabbitMQ (port 8085)
```

### Service Breakdown

#### `serviceregistry/`
Vanilla Spring Cloud Netflix Eureka Server. All other services register here and use it for dynamic load-balanced discovery. Secured with basic auth credentials.

Key files:
- `ServiceRegistryApplication.java` — Main class, `@EnableEurekaServer`
- `application.yml` — Port 8761, Prometheus endpoint, Loki logging

#### `api-gateway/`
Spring Cloud Gateway acting as the single entry point. Performs JWT validation before routing to downstream services, circuit breaking with Resilience4j, and in-memory sliding-window rate limiting per IP per route.

Key files:
- `JwtValidationFilter.java` — Stateless JWT verification; injects `X-User-Id`, `X-Username`, `X-User-Role` headers
- `SimpleRateLimitFilter.java` — Per-IP sliding window rate limiter using `ConcurrentHashMap`
- `GatewaySwaggerDocsController.java` — Aggregates Swagger docs from all services; role-aware access control
- `application.yml` — Route definitions, circuit breaker config, Redis config, CORS

#### `Auth/`
Handles user lifecycle, token issuance, and all verification flows.

Key files:
- `UserController.java` — Signup, get profile, list users (admin), refresh token, logout
- `VerificationController.java` — OTP-gated login, email verification, forgot/reset password
- `JwtService.java` — Token generation with `userId`, `role`, `email` claims + JTI for revocation
- `JwtFilter.java` — Token validation + forwarded identity header consistency check
- `TestDataSeeder.java` — Seeds admin + sample customer accounts on startup (feature-flagged)

#### `Catalog/`
Manages the medicine catalog and prescription submission/review.

Key files:
- `MedicineController.java` — Paginated listing, CRUD (admin-gated)
- `PrescriptionController.java` — Prescription upload and review status lookup
- `InventoryReservationController.java` — Internal API for Order service to reserve/release stock
- `OutboxEventPublisher.java` — Transactional Outbox with exponential backoff retry
- `TestDataSeeder.java` — Seeds medicine categories and sample medicines

#### `Order/`
Manages shopping cart, checkout, order lifecycle, and payment.

Key files:
- `CartController.java` — Full cart CRUD, quantity capped at 10 per item
- `CheckoutController.java` — Orchestrates inventory reservation via Feign + order creation
- `OrderController.java` — Order history, cancellation, status queries
- `CatalogClient.java` / `CatalogFeignClient.java` — Feign-based sync calls to Catalog with fallback
- `AuthFeignClient.java` — User address resolution from Auth service

#### `admin/`
Admin-only aggregation service. Does not own its own data store for orders — reads them via its own DB replica populated via RabbitMQ events (Processed Event pattern).

Key files:
- `AdminOrderController.java` — Order management, prescription review
- `AdminReportController.java` — Sales/inventory reports, CSV/PDF export
- `AdminMedicineController.java` — Medicine and inventory management
- `CrossServiceAnalyticsClient.java` — Calls Catalog for dashboard stats

#### `emailservice/`
Listens to RabbitMQ queues and dispatches emails via SMTP (Gmail). Handles: email verification, OTP delivery, login alerts, password reset.

Key files:
- `EmailEventListener.java` — AMQP message consumer for all email event types
- `EmailSenderService.java` — Renders Thymeleaf templates and sends via JavaMailSender

---

## ⚙️ Setup Instructions

### Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8 (5 separate databases)
- Redis
- RabbitMQ
- Docker (optional, for containerized setup)

### 1. Clone & Extract Services

Each service is an independent Maven project. Extract each zip and open individually or in a mono-repo layout.

### 2. Create Databases

```sql
CREATE DATABASE usersdb;
CREATE DATABASE pharmacy_catalog;
CREATE DATABASE pharmacy_orders;
CREATE DATABASE pharmacy_admin;
-- Email service uses no DB
```

### 3. Environment Variables

Each service reads from environment variables with sensible defaults. The critical ones:

| Variable | Description | Default |
|---|---|---|
| `JWT_SECRET` | HS256 signing key (min 32 chars) | `qBc8HQhVL9u+uBUPltUg8arrAZFVcSWDocPzkWmAKmc=` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | `root` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PASSWORD` | Redis password | _(empty)_ |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_USERNAME` | RabbitMQ user | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |
| `MAIL_USERNAME` | SMTP Gmail address | _(required)_ |
| `MAIL_PASSWORD` | SMTP Gmail app password | _(required)_ |
| `EUREKA_USERNAME` | Eureka auth user | `eureka` |
| `EUREKA_PASSWORD` | Eureka auth password | `eureka` |
| `EUREKA_SERVER_URL` | Full Eureka URL | `http://eureka:eureka@localhost:8761/eureka` |
| `APP_SEED_ENABLED` | Seed demo data on startup | `true` |
| `JWT_REVOCATION_ENABLED` | Enable Redis JWT blacklist | `false` |
| `LOKI_URL` | Loki push endpoint | `http://localhost:3100/loki/api/v1/push` |
| `ZIPKIN_ENDPOINT` | Zipkin spans endpoint | `http://localhost:9411/api/v2/spans` |

### 4. Start Services (In Order)

```bash
# 1. Service Registry
cd serviceregistry && mvn spring-boot:run

# 2. Auth Service
cd Auth && mvn spring-boot:run

# 3. Catalog Service
cd Catalog && mvn spring-boot:run

# 4. Order Service
cd Order && mvn spring-boot:run

# 5. Admin Service
cd admin && mvn spring-boot:run

# 6. Email Service
cd emailservice && mvn spring-boot:run

# 7. API Gateway (last — after all services are registered)
cd "api gateway" && mvn spring-boot:run
```

### 5. Docker Build (Per Service)

```bash
# Each service has a Dockerfile; pass JAR_NAME as build arg
docker build --build-arg JAR_NAME=auth-service-0.0.1-SNAPSHOT.jar -t pharmacy/auth-service .
```

---

## 🔌 API Endpoints

All routes go through the API Gateway at `http://localhost:8080`. Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

### Auth Service (`/api/auth/**`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/signup` | Public | Register new user |
| POST | `/api/auth/verify-password-then-send-otp` | Public | Step 1 of login: verify password, send OTP |
| POST | `/api/auth/verify-login-otp` | Public | Step 2 of login: validate OTP, get JWT |
| GET | `/api/auth/verify-email` | Public | Verify email via token link |
| POST | `/api/auth/resend-verification` | Public | Resend verification email |
| POST | `/api/auth/forgot-password` | Public | Send password reset email |
| POST | `/api/auth/reset-password` | Public | Reset password with token |
| POST | `/api/auth/refresh` | Public | Refresh access token |
| POST | `/api/auth/logout` | Bearer | Revoke current token |
| GET | `/api/auth/me` | Bearer | Get current user profile |
| GET | `/api/auth/all` | Admin | List all users |
| GET/POST/DELETE | `/api/address/**` | Bearer | Manage delivery addresses |

### Catalog Service (`/api/catalog/**`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/catalog/medicines` | Public | Paginated medicine list (search, filter) |
| GET | `/api/catalog/medicines/{id}` | Public | Medicine detail |
| POST | `/api/catalog/medicines` | Admin | Create medicine |
| PUT | `/api/catalog/medicines/{id}` | Admin | Update medicine |
| DELETE | `/api/catalog/medicines/{id}` | Admin | Soft-delete medicine |
| GET/POST/DELETE | `/api/catalog/categories/**` | Mixed | Category management |
| POST | `/api/catalog/prescriptions` | Bearer | Upload prescription |
| GET | `/api/catalog/prescriptions/{id}` | Bearer | Get prescription status |

### Order Service (`/api/orders/**`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/orders/cart` | Bearer | Get cart contents |
| POST | `/api/orders/cart` | Bearer | Add item to cart |
| PUT | `/api/orders/cart/{itemId}` | Bearer | Update item quantity |
| DELETE | `/api/orders/cart/{itemId}` | Bearer | Remove cart item |
| DELETE | `/api/orders/cart` | Bearer | Clear cart |
| POST | `/api/orders/checkout/start` | Bearer | Initiate checkout |
| GET | `/api/orders` | Bearer | List user orders |
| GET | `/api/orders/{id}` | Bearer | Get order detail |
| POST | `/api/orders/{id}/cancel` | Bearer | Cancel order |
| POST | `/api/orders/payment` | Bearer | Process payment |

### Admin Service (`/api/admin/**`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/admin/dashboard` | Admin | Dashboard stats |
| GET/PUT | `/api/admin/orders/**` | Admin | Order management |
| GET/PUT | `/api/admin/prescriptions/**` | Admin | Prescription review queue |
| GET/POST/PUT/DELETE | `/api/admin/medicines/**` | Admin | Medicine & inventory management |
| GET | `/api/admin/reports/sales` | Admin | Sales report by date range |
| GET | `/api/admin/reports/inventory` | Admin | Inventory status report |
| GET | `/api/admin/reports/export` | Admin | Export report as CSV or PDF |

---

## 🧪 Testing

Unit tests exist for Auth, Catalog, and Order services using JUnit 5 + Mockito.

```bash
# Run tests for a specific service
cd Auth && mvn test

cd Catalog && mvn test

cd Order && mvn test
```

Test configurations use an H2 in-memory database (MySQL mode) and disable Eureka, RabbitMQ, and tracing for fast, isolated runs via `src/test/resources/application-test.properties`.

**Test coverage areas:**
- `UserServiceTest`, `AuthServiceTest`, `AddressServiceTest` (Auth)
- `MedicineServiceTest`, `PrescriptionServiceTest`, `MedicineControllerTest`, `CategoryControllerTest` (Catalog)
- `CartServiceTest`, `CheckoutServiceTest`, `OrderServiceTest`, `PaymentServiceTest` (Order)

---

## 🚀 Deployment

### Docker (Single Service)

```bash
# Build image (JAR_NAME must match Maven output)
docker build \
  --build-arg JAR_NAME=auth-service-0.0.1-SNAPSHOT.jar \
  -t pharmacy/auth-service:latest .

# Run with environment overrides
docker run -d \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secret \
  -e JWT_SECRET=your-secret-key-here \
  -e EUREKA_SERVER_URL=http://eureka:eureka@registry:8761/eureka \
  -p 8081:8081 \
  pharmacy/auth-service:latest
```

### Recommended Production Stack

```
Load Balancer (Nginx / AWS ALB)
        │
   API Gateway (:8080)
        │
 ┌──────┼──────────────────────┐
Auth  Catalog  Order  Admin  Email
 │      │       │       │
MySQL  MySQL  MySQL  MySQL

Shared Infrastructure:
  Redis Cluster (JWT revocation, rate limiting)
  RabbitMQ Cluster (event messaging)
  Eureka HA pair (service discovery)
  Prometheus + Grafana (metrics)
  Zipkin (distributed tracing)
  Loki + Grafana (log aggregation)
```

### Environment Best Practices for Production

- Always set `JWT_SECRET` to a randomly generated 256-bit key
- Set `JWT_REVOCATION_ENABLED=true` and provision Redis
- Set `APP_SEED_ENABLED=false` to disable test data seeder
- Configure `LOKI_URL` and `ZIPKIN_ENDPOINT` for observability
- Use separate MySQL databases per service with dedicated credentials

---

## 🤝 Contribution Guide

1. **Fork** the repository and create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Follow the coding standards:**
   - Use Lombok for boilerplate reduction
   - Use `@Valid` on all request bodies
   - Return consistent `ApiResponse<T>` wrappers
   - Add OpenAPI `@Operation` annotations to all endpoints
   - Use environment-variable configuration; never hardcode secrets

3. **Test your changes:**
   ```bash
   mvn test
   ```
   New features must include unit tests.

4. **Submit a Pull Request** with a clear description of:
   - What problem it solves
   - What services are affected
   - Whether it introduces new environment variables

5. **Code review checklist:**
   - [ ] No hardcoded credentials or URLs
   - [ ] Input validation present
   - [ ] Appropriate role-based access control
   - [ ] New endpoints documented in Swagger
   - [ ] Tests included

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

> Built with ☕ and Spring Boot. Not for prescription dispensing without proper licensing.
