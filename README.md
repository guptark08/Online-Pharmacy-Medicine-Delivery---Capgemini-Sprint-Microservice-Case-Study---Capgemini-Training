# 💊 PharmaCare — Online Pharmacy & Medicine Delivery Platform

> A production-grade, full-stack pharmacy system built with Spring Boot 3 microservices and a React 19 frontend. Covers medicine ordering, prescription management, inventory control, payment processing, and a complete admin panel — all containerised with Docker Compose.

---

## 🚀 What It Does

**PharmaCare** is a working end-to-end online pharmacy. A customer can sign up, browse medicines, upload a prescription, place an order, pay, and track delivery. An admin can review prescriptions, move orders through their lifecycle, manage the medicine catalog, view users, and export sales reports.

**Customer flows**
- Register → email verification → OTP-secured login
- Browse 56+ medicines with search, category filter, and Rx flag
- Upload prescription photos/PDFs for pharmacist review
- Add to cart → checkout (address + delivery slot) → simulated payment
- View order history, track status, cancel or reorder

**Admin flows**
- Dashboard with live revenue, order counts, inventory alerts
- Order lifecycle management (PAID → PACKED → OUT_FOR_DELIVERY → DELIVERED, returns, refunds)
- Prescription review queue with inline image preview (approve/reject + notes)
- Medicine catalog CRUD with stock updates and category management
- User directory (all registered customers and admins)
- Sales reports with CSV/PDF export, inventory reports

---

## 🏗️ Tech Stack

### Backend (7 microservices)

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.11 |
| Cloud | Spring Cloud 2023.x — Gateway, Eureka, OpenFeign, Resilience4j |
| Security | Spring Security 6, JJWT |
| Database | MySQL 8 (one database per service) |
| Caching / Revocation | Redis 7 |
| Messaging | RabbitMQ 3.13 (Transactional Outbox Pattern) |
| ORM | Spring Data JPA / Hibernate |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Email | JavaMailSender + Thymeleaf templates |
| Build | Maven 3.9, multi-stage Docker builds |
| Observability | Prometheus, Grafana, Loki, Zipkin |

### Frontend (React SPA)

| Layer | Technology |
|---|---|
| Framework | React 19, TypeScript ~6 |
| Bundler | Vite 8 |
| Routing | React Router v7 |
| Server state | TanStack Query v5 |
| Client state | Zustand v5 (auth store, persisted) |
| Forms | React Hook Form v7 + Zod v4 |
| Styling | Tailwind CSS v4 |
| HTTP | Axios (JWT interceptor + refresh-queue) |
| Type generation | openapi-typescript (codegen from live Swagger) |

---

## 📂 Project Structure

```
.
├── docker-compose.yml                        # One-command full-stack boot
├── .env                                      # Shared environment variables
├── Online_Pharmacy_and_Delivery_Backend/
│   ├── service-registry/                     # Eureka Server               :8761
│   ├── api-gateway/                          # Spring Cloud Gateway        :8080
│   ├── Auth_Service/                         # Auth, JWT, OTP, addresses   :8081
│   ├── CatalogAndPrescription_Service/       # Medicines, Rx, inventory    :8082
│   ├── OrderAndDelivery_Service/             # Cart, checkout, orders      :8083
│   ├── admin-service/                        # Admin panel backend         :8084
│   ├── email-service/                        # Async email via RabbitMQ    :8085
│   ├── init-databases.sql                    # Creates all 4 MySQL databases
│   ├── seed_medicines.sql                    # Seeds catalog DB (56 medicines)
│   └── seed_admin_db.sql                     # Mirrors catalog data to admin DB
└── Online_Pharmacy_and_Delivery_Frontend/
    ├── src/
    │   ├── app/                              # Router, providers
    │   ├── features/
    │   │   ├── auth/                         # Login (2-step OTP), signup, reset
    │   │   ├── catalog/                      # Medicine browse, detail, search
    │   │   ├── cart/                         # Cart with optimistic updates
    │   │   ├── checkout/                     # Multi-step: address→slot→Rx→payment
    │   │   ├── orders/                       # Order list, detail, cancel, reorder
    │   │   ├── prescriptions/                # Upload + status list
    │   │   ├── home/                         # Role-aware landing page
    │   │   └── admin/                        # Full admin panel (6 pages)
    │   └── shared/                           # Axios client, auth store, UI components
    └── src/shared/types/api/                 # OpenAPI-generated TypeScript types
```

---

## ⚡ Quick Start (Docker Compose)

### Prerequisites

- Docker 24+ and Docker Compose v2
- 4 GB RAM recommended (all services combined)

### 1. Configure environment

Copy the example and fill in your Gmail SMTP credentials:

```bash
cp .env.example .env   # or edit .env directly
```

Required values in `.env`:

```env
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your-app-password    # Gmail → Security → App Passwords
```

Everything else (MySQL root password, RabbitMQ, JWT secret, Redis) has working defaults.

### 2. Start everything

```bash
docker compose up -d
```

On first boot MySQL automatically:
1. Creates all 4 databases (`init-databases.sql`)
2. Seeds 56 medicines + 10 categories into the catalog DB
3. Mirrors the catalog data into the admin DB

### 3. Wait for services to be ready (~60 seconds)

```bash
docker compose logs -f api-gateway   # ready when "Started" appears
```

### 4. Open the app

| URL | Purpose |
|---|---|
| `http://localhost:5173` | React frontend (dev server — see below) |
| `http://localhost:8080/swagger-ui.html` | Swagger UI (all services aggregated) |
| `http://localhost:9090` | Prometheus |
| `http://localhost:3000` | Grafana (admin / admin123) — Prometheus + Loki + Zipkin pre-wired |
| `http://localhost:3100` | Loki |
| `http://localhost:9411` | Zipkin |
| `http://localhost:15672` | RabbitMQ management (pharmacy / pharmacy123) |
| `http://localhost:9000` | SonarQube |
| `http://localhost:8761` | Eureka dashboard |

### 5. Start the frontend dev server

```bash
cd Online_Pharmacy_and_Delivery_Frontend
npm install
npm run dev          # http://localhost:5173
```

### First login

Create an account via the signup page, then verify your email. Or seed a test admin directly:

```bash
docker exec mysql mysql -uroot -proot usersdb -e "
  INSERT IGNORE INTO users (name,email,username,mobile,password,role,status,email_verified)
  VALUES ('Admin','admin@pharmacy.test','admin','9999999999',
    '\$2a\$10\$...bcrypt-of-Admin@1234...',
    'ROLE_ADMIN', b'1', b'1');"
```

Or register normally and update the role:

```bash
# After registering admin@pharmacy.test via signup form:
docker exec mysql mysql -uroot -proot usersdb -e "
  UPDATE users SET role='ROLE_ADMIN', email_verified=b'1' WHERE email='admin@pharmacy.test';"
```

---

## 🔌 API Reference

All routes go through the gateway at `http://localhost:8080`.

### Auth Service `/api/auth/**`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/signup` | Public | Register (name, email, username, mobile, password) |
| POST | `/api/auth/verify-password-then-send-otp` | Public | Login step 1 — verify password, send OTP |
| POST | `/api/auth/verify-login-otp` | Public | Login step 2 — validate OTP, receive JWT |
| GET | `/api/auth/verify-email?token=` | Public | Verify email from link |
| POST | `/api/auth/resend-verification` | Public | Resend verification email |
| POST | `/api/auth/forgot-password` | Public | Request password reset email |
| POST | `/api/auth/reset-password` | Public | Set new password with reset token |
| POST | `/api/auth/refresh` | Public | Rotate access + refresh tokens |
| POST | `/api/auth/logout` | Bearer | Revoke current token |
| GET | `/api/auth/me` | Bearer | Get current user profile |
| GET | `/api/auth/all` | Admin | List all users |
| GET | `/api/auth/users/{id}` | Admin | Get user by ID |
| GET/POST/PUT/DELETE | `/api/address/**` | Bearer | Manage delivery addresses |

### Catalog Service `/api/catalog/**`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/catalog/medicines` | Public | Paginated list (search, category, Rx filter) |
| GET | `/api/catalog/medicines/{id}` | Public | Medicine detail |
| GET | `/api/catalog/categories` | Public | All active categories |
| POST | `/api/catalog/prescriptions/upload` | Customer | Upload prescription image/PDF |
| GET | `/api/catalog/prescriptions/my` | Customer | My prescriptions with status |
| GET | `/api/catalog/prescriptions/{id}/file` | Bearer | Serve prescription file (own only for customers) |
| GET | `/api/catalog/prescriptions/{id}/status` | Bearer | Prescription status (used internally by order service) |
| PUT | `/api/catalog/prescriptions/{id}/link-order/{orderId}` | Bearer | Link approved Rx to order |
| POST | `/api/catalog/inventory/reserve` | Bearer | Reserve stock during checkout |

### Order Service `/api/orders/**`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/orders/cart` | Bearer | Get cart contents |
| POST | `/api/orders/cart` | Bearer | Add item to cart |
| PUT | `/api/orders/cart/{itemId}` | Bearer | Update quantity |
| DELETE | `/api/orders/cart/{itemId}` | Bearer | Remove item |
| DELETE | `/api/orders/cart` | Bearer | Clear cart |
| GET | `/api/orders/addresses` | Bearer | List saved addresses |
| POST | `/api/orders/addresses` | Bearer | Add address |
| POST | `/api/orders/checkout/start` | Bearer | Checkout (reserves inventory, creates order) |
| POST | `/api/orders/payments/initiate` | Bearer | Process payment |
| GET | `/api/orders` | Bearer | Order history |
| GET | `/api/orders/{id}` | Bearer | Order detail |
| PUT | `/api/orders/{id}/cancel` | Bearer | Cancel order |
| POST | `/api/orders/{id}/reorder` | Bearer | Re-add order items to cart |

### Admin Service `/api/admin/**`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/admin/dashboard` | Admin | Live KPIs — revenue, order counts, inventory alerts |
| GET | `/api/admin/orders` | Admin | All orders (paginated) |
| GET | `/api/admin/orders/{id}` | Admin | Order detail with items |
| PUT | `/api/admin/orders/{id}/status` | Admin | Update order status |
| PUT | `/api/admin/orders/{id}/cancel` | Admin | Cancel order |
| GET | `/api/admin/prescriptions` | Admin | All prescriptions |
| GET | `/api/admin/prescriptions/pending` | Admin | Pending review queue |
| PUT | `/api/admin/prescriptions/{id}/review` | Admin | Approve or reject prescription |
| GET | `/api/admin/medicines` | Admin | All medicines |
| POST | `/api/admin/medicines` | Admin | Add medicine |
| PUT | `/api/admin/medicines/{id}` | Admin | Update medicine |
| PATCH | `/api/admin/medicines/{id}/stock` | Admin | Update stock count |
| DELETE | `/api/admin/medicines/{id}` | Admin | Soft-delete medicine |
| GET | `/api/admin/categories` | Admin | All categories |
| POST | `/api/admin/categories` | Admin | Add category |
| PUT | `/api/admin/categories/{id}` | Admin | Update category |
| DELETE | `/api/admin/categories/{id}` | Admin | Soft-delete category |
| GET | `/api/admin/reports/sales?startDate=&endDate=` | Admin | Sales report |
| GET | `/api/admin/reports/sales/today` | Admin | Today's sales |
| GET | `/api/admin/reports/sales/this-month` | Admin | This month's sales |
| GET | `/api/admin/reports/inventory` | Admin | Inventory report |
| GET | `/api/admin/reports/export?format=csv\|pdf` | Admin | Download report |

---

## 🖥️ Frontend Pages

### Customer

| Route | Page |
|---|---|
| `/` | Dashboard — recent orders, quick links |
| `/catalog` | Medicine browse with sidebar filters and debounced search |
| `/catalog/:id` | Medicine detail with add-to-cart |
| `/cart` | Cart with quantity stepper, clear, proceed to checkout |
| `/checkout` | 4-step: address → slot → prescription (if Rx items) → payment |
| `/orders` | Order history with status badges |
| `/orders/:id` | Order detail — items, totals, cancel / reorder buttons |
| `/prescriptions` | Upload Rx + status list (PENDING / APPROVED / REJECTED) |
| `/login` | 2-step login: password then 6-digit OTP |
| `/signup` | Registration with real-time validation |
| `/forgot-password` | Password reset request |
| `/reset-password` | Set new password via email token |

### Admin (`/admin/*`)

| Route | Page |
|---|---|
| `/admin/dashboard` | KPI cards, recent orders, low-stock + expiry alerts |
| `/admin/orders` | Order table with full status lifecycle buttons |
| `/admin/orders/:id` | Order detail with status transition buttons |
| `/admin/prescriptions` | Pending/All tabs, inline image preview, approve/reject modal |
| `/admin/medicines` | Medicines table, add/edit modal, stock update, category panel |
| `/admin/users` | All registered users with role, verification and status |
| `/admin/reports` | Sales (date range + presets + CSV/PDF export) and inventory |

---

## 📦 Order Status Lifecycle

```
CHECKOUT_STARTED
    │
    ├─ (Rx items) ──► PRESCRIPTION_PENDING ──► PRESCRIPTION_APPROVED ──► PAYMENT_PENDING
    │                                      └──► PRESCRIPTION_REJECTED
    └─ (no Rx) ──────────────────────────────────────────────────────► PAYMENT_PENDING
                                                                              │
                                                              ┌───────────────┤
                                                           PAID          PAYMENT_FAILED
                                                              │                │
                                                           PACKED          (retry)
                                                              │
                                                       OUT_FOR_DELIVERY
                                                              │
                                                          DELIVERED ──► RETURN_REQUESTED
                                                                              │
                                                                       REFUND_INITIATED
                                                                              │
                                                                       REFUND_COMPLETED

Any non-terminal status ──► CUSTOMER_CANCELLED / ADMIN_CANCELLED
```

---

## 🔒 Security

- **Two-factor login**: password verification → OTP sent to verified email
- **Email verification gate**: unverified accounts cannot log in
- **JWT + Refresh tokens**: short-lived access tokens, long-lived refresh tokens stored in Redis
- **Token revocation**: Redis-backed blacklist via JTI (configurable)
- **Prescription ownership**: customers can only access their own prescription files
- **Role-based access**: `CUSTOMER` vs `ADMIN` enforced at gateway (header injection) and service layer (`@PreAuthorize`)
- **Rate limiting**: per-IP sliding window at the gateway
- **Circuit breakers**: Resilience4j on all Feign clients with fallback factories

---

## 📊 Observability

| Tool | URL | What it shows |
|---|---|---|
| Prometheus | `localhost:9090` | Metrics from 6 services (scrapes `/actuator/prometheus`) |
| Grafana | `localhost:3000` | Dashboards — Prometheus (default), Loki, Zipkin auto-provisioned |
| Loki | `localhost:3100` | Structured logs from all services |
| Zipkin | `localhost:9411` | Distributed traces across all 6 services |
| RabbitMQ | `localhost:15672` | Queue health, message rates, consumer status |
| SonarQube | `localhost:9000` | Static code analysis |

Prometheus config (`monitoring/prometheus.yml`) scrapes all 5 application services + itself. Grafana datasources (Prometheus, Loki, Zipkin) are auto-provisioned on startup from `monitoring/grafana/provisioning/`.

---

## 🧪 Testing

Backend unit tests use JUnit 5 + Mockito with H2 in-memory database (MySQL mode). Eureka, RabbitMQ, and tracing are disabled for test runs.

```bash
# Run tests for a specific service
cd Online_Pharmacy_and_Delivery_Backend/Auth_Service && mvn test
cd Online_Pharmacy_and_Delivery_Backend/CatalogAndPrescription_Service && mvn test
cd Online_Pharmacy_and_Delivery_Backend/OrderAndDelivery_Service && mvn test
cd Online_Pharmacy_and_Delivery_Backend/admin-service && mvn test
```

**Test coverage:**
- Auth: `UserServiceTest`, `AuthServiceTest`, `AddressServiceTest`, `JwtUtilTest`
- Catalog: `MedicineServiceTest`, `PrescriptionServiceTest`, `MedicineControllerTest`
- Order: `CartServiceTest`, `CheckoutServiceTest`, `OrderServiceTest`, `PaymentServiceTest`
- Admin: `AdminOrderServiceTest`, `AdminMedicineServiceTest`, `AdminDashboardServiceTest`, `AdminPrescriptionServiceTest`, `AdminCategoryServiceTest`

**Frontend type generation** (regenerate after backend changes):
```bash
cd Online_Pharmacy_and_Delivery_Frontend
npm run codegen        # regenerates all src/shared/types/api/*.d.ts from live Swagger
```

---

## ⚙️ Environment Variables

The `.env` file at the project root is shared by all Docker services.

| Variable | Default | Description |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | `root` | MySQL root password |
| `JWT_SECRET` | (set in .env) | HS256 signing key — change in production |
| `RABBITMQ_DEFAULT_USER` | `pharmacy` | RabbitMQ username |
| `RABBITMQ_DEFAULT_PASS` | `pharmacy123` | RabbitMQ password |
| `MAIL_USERNAME` | _(required)_ | Gmail address for sending emails |
| `MAIL_PASSWORD` | _(required)_ | Gmail App Password |
| `GRAFANA_ADMIN_USER` | `admin` | Grafana admin username |
| `GRAFANA_ADMIN_PASSWORD` | `admin123` | Grafana admin password |
| `ZIPKIN_ENDPOINT` | `http://zipkin:9411/api/v2/spans` | Zipkin trace endpoint |
| `LOKI_URL` | `http://loki:3100/loki/api/v1/push` | Loki log push endpoint |

Frontend variables (`Online_Pharmacy_and_Delivery_Frontend/.env.development`):

| Variable | Default | Description |
|---|---|---|
| `VITE_GATEWAY_URL` | `http://localhost:8080` | API Gateway base URL |

---

## 🚀 Production Deployment

### Rebuild a single service after code changes

```bash
docker compose build auth-service      # rebuild only auth
docker compose up -d --no-deps auth-service   # restart without touching others
```

### Rebuild all services

```bash
docker compose build
docker compose up -d
```

### Database reset (wipe all data)

```bash
docker compose down -v     # removes all volumes including MySQL data
docker compose up -d       # fresh boot re-runs SQL init scripts
```

### Production checklist

- [ ] Set a strong `JWT_SECRET` (minimum 32 random bytes, base64-encoded)
- [ ] Set real `MAIL_USERNAME` + `MAIL_PASSWORD` (Gmail App Password)
- [ ] Change `MYSQL_ROOT_PASSWORD`, `RABBITMQ_DEFAULT_PASS`, `GRAFANA_ADMIN_PASSWORD`
- [ ] Set `SONAR_SEARCH_JAVAOPTS` / web / CE memory according to your server RAM
- [ ] Add Nginx or a cloud load balancer in front of the API Gateway
- [ ] Replace the self-signed Grafana/Prometheus setup with a managed observability stack for high traffic

---

## 📜 License

MIT — see [LICENSE](LICENSE).

---

> Built with ☕ Java + ⚛️ React. Capgemini Sprint Microservice Case Study — Training Project.
