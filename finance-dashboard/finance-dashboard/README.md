# 💰 Finance Dashboard API

> A production-grade Spring Boot 3 backend for a personal/organisational finance management system — featuring JWT authentication, role-based access control, budget alerts, recurring transactions, a financial health score engine, CSV exports, an immutable audit trail, and per-IP rate limiting.

---

## ✨ What Makes This Stand Out

| Feature | Details |
|---|---|
| **Financial Health Score** | Composite 0–100 score from 5 signals: savings rate, budget adherence, expense diversity, income stability, positive cash-flow months. Includes grade (A–F) + actionable insights. |
| **Smart Budget Alerts** | Category budgets with configurable warning (80%) / critical (100%) thresholds. Fires in-app notifications in real time when spending crosses a threshold. |
| **Recurring Transaction Engine** | Define DAILY / WEEKLY / MONTHLY / QUARTERLY / YEARLY rules. A daily cron auto-posts transactions, computes `nextExecutionDate`, and notifies the user on execution. |
| **Immutable Audit Trail** | Every mutation (create, update, delete, login, export, budget alert) is logged in a separate `REQUIRES_NEW` transaction with actor, IP, before-state JSON, and after-state JSON. |
| **CSV Export** | Applies the same rich filter set as the list endpoint. Streams a timestamped `.csv` file for download. |
| **Per-IP Rate Limiting** | Token-bucket algorithm via Bucket4j — 100 requests / 60 s per IP. Returns `X-Rate-Limit-Remaining` and `Retry-After` headers. |
| **Response Caching** | Dashboard summary and health score are Spring-cached. Cache is evicted automatically on any record write. |

---

## 🏗️ Architecture

```
src/main/java/com/finance/dashboard/
│
├── config/          SecurityConfig, OpenApiConfig, CacheConfig, DataSeeder
├── controller/      AuthController, UserController, FinancialRecordController,
│                    BudgetController, RecurringTransactionController,
│                    DashboardController, NotificationController, AuditController
├── dto/
│   ├── request/     LoginRequest, CreateUserRequest, UpdateUserRequest,
│   │                CreateRecordRequest, UpdateRecordRequest,
│   │                BudgetRequest, RecurringTransactionRequest
│   └── response/    ApiResponse<T>, PagedResponse<T>, AuthResponse, UserResponse,
│                    FinancialRecordResponse, BudgetResponse,
│                    RecurringTransactionResponse, DashboardSummaryResponse,
│                    MonthlyTrendResponse, WeeklyTrendResponse,
│                    CategorySummaryResponse, FinancialHealthScoreResponse,
│                    NotificationResponse, AuditLogResponse
├── exception/       ResourceNotFoundException, DuplicateResourceException,
│                    BadRequestException, GlobalExceptionHandler
├── model/           User, FinancialRecord, Budget, RecurringTransaction,
│                    AuditLog, Notification
│                    + enums: Role, TransactionType, Category,
│                             RecurringFrequency, AuditAction
├── repository/      UserRepository, FinancialRecordRepository (+Specification),
│                    BudgetRepository, RecurringTransactionRepository,
│                    AuditLogRepository, NotificationRepository
├── security/        JwtUtils, JwtAuthenticationFilter,
│                    RateLimitingFilter (Bucket4j),
│                    UserDetailsImpl, UserDetailsServiceImpl
├── service/         AuthService, UserService, FinancialRecordService,
│                    BudgetService, BudgetAlertService,
│                    RecurringTransactionService, DashboardService,
│                    FinancialHealthScoreService, NotificationService,
│                    AuditService, ExportService
└── util/            SecurityUtils, RecurringUtils
```

### Design Principles

- **Separation of concerns** — controllers only handle HTTP; services own all business logic; repositories own all data access.
- **Soft deletes** — records and users are never physically removed; `deleted=true` / `active=false` flags preserve history and referential integrity.
- **Append-only audit** — `AuditLog` rows are written in `REQUIRES_NEW` transactions and never modified. A rollback in the calling service cannot suppress the audit entry.
- **Consistent API envelope** — every response is wrapped in `ApiResponse<T>` with `success`, `message`, `data`, `errors`, and `timestamp`. Pagination uses `PagedResponse<T>`.
- **Stateless JWT** — no server-side sessions. Each request is authenticated independently by validating the HS256-signed token.
- **Cache-evict on write** — `@Caching(evict = …)` on every write method in `FinancialRecordService` keeps dashboard and health-score caches consistent.

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+

### Run

```bash
git clone https://github.com/meranaamkhann/finance-dashboard.git
cd finance-dashboard
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

### Explore

| URL | What it is |
|---|---|
| http://localhost:8080/swagger-ui.html | Interactive API docs (Swagger UI) |
| http://localhost:8080/h2-console | In-memory database browser |

H2 connection: `jdbc:h2:mem:financedb` · username: `sa` · password: _(blank)_

---

## 🔐 Authentication

All protected endpoints require a **Bearer JWT** in the `Authorization` header.

### Step 1 — Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@1234"
}
```

### Step 2 — Use the token

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Default seed accounts

| Username | Password      | Role    | Can do |
|----------|---------------|---------|--------|
| admin    | Admin@1234    | ADMIN   | Everything |
| analyst  | Analyst@1234  | ANALYST | Read + analytics + budgets + recurring + export |
| viewer   | Viewer@1234   | VIEWER  | Read records + dashboard summary + notifications |

---

## 🛡️ Role-Based Access Control Matrix

| Endpoint group | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| Login | ✅ | ✅ | ✅ |
| View records / summary | ✅ | ✅ | ✅ |
| View own notifications | ✅ | ✅ | ✅ |
| Full analytics + trends | ❌ | ✅ | ✅ |
| Financial health score | ❌ | ✅ | ✅ |
| CSV export | ❌ | ✅ | ✅ |
| Create / manage budgets | ❌ | ✅ | ✅ |
| Create / manage recurring rules | ❌ | ✅ | ✅ |
| Create / update / delete records | ❌ | ❌ | ✅ |
| User management | ❌ | ❌ | ✅ |
| Audit trail | ❌ | ❌ | ✅ |

---

## 📡 API Reference

### 1. Authentication
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/login | Get JWT token |

### 2. User Management _(ADMIN)_
| Method | Path | Description |
|--------|------|-------------|
| POST   | /api/users | Create user |
| GET    | /api/users | List users (paginated) |
| GET    | /api/users/{id} | Get user by ID |
| GET    | /api/users/by-role/{role} | Filter by role |
| PUT    | /api/users/{id} | Update user |
| PATCH  | /api/users/{id}/activate | Activate user |
| PATCH  | /api/users/{id}/deactivate | Deactivate user |
| DELETE | /api/users/{id} | Soft-delete user |

### 3. Financial Records
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST   | /api/records | ADMIN | Create record |
| GET    | /api/records | ALL  | List with filters |
| GET    | /api/records/{id} | ALL | Get by ID |
| PUT    | /api/records/{id} | ADMIN | Update |
| DELETE | /api/records/{id} | ADMIN | Soft-delete |
| GET    | /api/records/export/csv | ADMIN, ANALYST | CSV download |

**Filter parameters for GET /api/records:**

| Param | Type | Example | Description |
|-------|------|---------|-------------|
| type | enum | `INCOME` / `EXPENSE` | Transaction type |
| category | enum | `SALARY`, `FOOD` | Category |
| dateFrom | ISO date | `2024-01-01` | Start date |
| dateTo | ISO date | `2024-12-31` | End date |
| keyword | string | `rent` | Search in description |
| tags | string | `sip` | Partial match on tags |
| createdById | Long | `1` | Filter by creator |
| sortBy | string | `date`, `amount` | Sort field |
| direction | string | `asc` / `desc` | Sort order |
| page / size | int | `0` / `20` | Pagination |

### 4. Budget Management _(ADMIN, ANALYST)_
| Method | Path | Description |
|--------|------|-------------|
| POST   | /api/budgets | Create budget for category + period |
| GET    | /api/budgets | My active budgets with live spend |
| GET    | /api/budgets/{id} | Get with live spend & status |
| PUT    | /api/budgets/{id} | Update limit or period |
| DELETE | /api/budgets/{id} | Deactivate |

Budget response includes: `spentAmount`, `remainingAmount`, `usagePercent`, `status` (`ON_TRACK` / `WARNING` / `CRITICAL` / `EXCEEDED`).

### 5. Recurring Transactions _(ADMIN, ANALYST)_
| Method | Path | Description |
|--------|------|-------------|
| POST   | /api/recurring | Create rule |
| GET    | /api/recurring | My active rules |
| GET    | /api/recurring/{id} | Get with `nextExecutionDate` |
| PUT    | /api/recurring/{id} | Update rule |
| DELETE | /api/recurring/{id} | Deactivate |

### 6. Dashboard & Analytics
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/dashboard/summary | ALL | Full summary incl. health score |
| GET | /api/dashboard/summary/range | ANALYST, ADMIN | Summary for date range |
| GET | /api/dashboard/categories | ANALYST, ADMIN | Category % breakdown |
| GET | /api/dashboard/trends/monthly | ANALYST, ADMIN | Monthly income vs expense |
| GET | /api/dashboard/trends/weekly | ANALYST, ADMIN | Weekly trends |
| GET | /api/dashboard/health-score | ANALYST, ADMIN | Health score + insights |
| GET | /api/dashboard/top-expenses | ANALYST, ADMIN | Top N categories this month |
| GET | /api/dashboard/spending-by-day | ANALYST, ADMIN | Day-of-week spend pattern |

### 7. Notifications
| Method | Path | Description |
|--------|------|-------------|
| GET    | /api/notifications | Your notifications (supports `?unreadOnly=true`) |
| GET    | /api/notifications/unread-count | Badge count |
| PATCH  | /api/notifications/mark-all-read | Bulk mark read |
| PATCH  | /api/notifications/{id}/read | Mark one read |

### 8. Audit Trail _(ADMIN)_
| Method | Path | Description |
|--------|------|-------------|
| GET    | /api/audit | All events (paginated) |
| GET    | /api/audit/by-actor/{username} | Events by actor |
| GET    | /api/audit/by-entity/{type}/{id} | Full history of an entity |
| GET    | /api/audit/by-date-range | Events in datetime range |

---

## 📊 Financial Health Score

The health score is a composite **0–100** metric computed from five signals:

| Signal | Weight | Measures |
|--------|--------|---------|
| Savings rate | 30 pts | net income / total income (full points at ≥ 30%) |
| Budget adherence | 25 pts | % of active budgets currently on track |
| Expense diversity | 20 pts | Herfindahl–Hirschman Index across expense categories |
| Income stability | 15 pts | Coefficient of variation in monthly income (lower = better) |
| Positive cash flow | 10 pts | Months with net > 0 over last 6 months |

**Grade mapping:** A (85+) · B (70+) · C (55+) · D (40+) · F (<40)

Each response includes personalised `insights` — actionable recommendations based on the weakest signals.

---

## 🔁 Recurring Transaction Scheduler

The scheduler fires daily at **01:00** (configurable via `app.scheduler.recurring-cron`).

For each active rule where `nextExecutionDate <= today`:
1. Auto-posts a `FinancialRecord` with description `[Auto] <rule name>`
2. Sets `lastExecutedDate = today`
3. Sends an in-app notification to the owner

The rule response always includes `nextExecutionDate` computed live from `RecurringUtils`.

---

## 🔔 Budget Alert System

When a new expense record is created, `BudgetAlertService.evaluate()` runs synchronously:
- Finds active budgets for the user + category covering today's date
- Computes `spentAmount` against `limitAmount`
- If usage ≥ **80%** → `BUDGET_WARNING` notification
- If usage ≥ **100%** → `BUDGET_CRITICAL` notification

A nightly batch sweep (`evaluateAllActive`) also runs to catch threshold crossings from bulk imports.

---

## 🌐 Standard Response Format

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "timestamp": "2024-04-01T10:00:00"
}
```

**Validation error:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "amount": "Amount must be greater than zero",
    "date": "Date cannot be in the future"
  },
  "timestamp": "2024-04-01T10:00:00"
}
```

**Paginated data:**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 47,
    "totalPages": 3,
    "last": false,
    "first": true
  }
}
```

---

## ⚙️ Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | HTTP port |
| `app.jwt.secret` | (set) | Base64-encoded HS256 secret |
| `app.jwt.expiration-ms` | 86400000 | Token TTL (24 h) |
| `app.rate-limit.capacity` | 100 | Max tokens per IP bucket |
| `app.rate-limit.refill-tokens` | 100 | Tokens added each refill cycle |
| `app.rate-limit.refill-seconds` | 60 | Refill interval in seconds |
| `app.scheduler.recurring-cron` | `0 0 1 * * *` | Cron for recurring job (01:00 daily) |
| `app.budget.warning-threshold` | 80 | % spend to trigger warning alert |
| `app.budget.critical-threshold` | 100 | % spend to trigger critical alert |

---

## 🗃️ Switching to PostgreSQL

The project uses H2 in-memory for zero-setup development. To use PostgreSQL:

1. Replace H2 dependency in `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financedb
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

---

## 🧪 Running Tests

```bash
mvn test
```

| Test class | Type | Covers |
|---|---|---|
| `AuthControllerIntegrationTest` | Integration | Login flows, validation, role in response |
| `FinancialRecordControllerIntegrationTest` | Integration | RBAC, filtering, CSV export, validation |
| `DashboardControllerIntegrationTest` | Integration | All dashboard endpoints, role restrictions |
| `UserServiceTest` | Unit | CRUD, duplicate checks, soft-delete |
| `FinancialRecordServiceTest` | Unit | Create, read, update, soft-delete |
| `RecurringUtilsTest` | Unit | All frequencies, isDueToday edge cases |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 + JJWT 0.11 |
| Rate Limiting | Bucket4j 8.7 |
| Persistence | Spring Data JPA + H2 (swappable to PostgreSQL) |
| Validation | Jakarta Bean Validation |
| CSV Export | OpenCSV 5.9 |
| Caching | Spring Cache (ConcurrentMapCache) |
| Scheduling | Spring `@Scheduled` |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Build | Maven 3.9 |
| Tests | JUnit 5 + Mockito + MockMvc |

---

## 📁 Pushing to GitHub

```bash
cd finance-dashboard

git init
git add .
git commit -m "feat: initial release — Finance Dashboard API v1.0.0"
git branch -M main
git remote add origin https://github.com/meranaamkhann/finance-dashboard.git
git push -u origin main
```

> You'll need a [Personal Access Token](https://github.com/settings/tokens) with `repo` scope as your password when prompted.
