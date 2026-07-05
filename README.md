# Finance Dashboard v2.0 — Production Grade

> Full-stack personal finance app — **Spring Boot 3.2** backend + **React 18 + Vite + Tailwind** frontend.  
> ₹ INR · Light theme · JWT auth · RBAC · Budgets · Health Score · Audit Trail

---

## 📁 Project Structure

```
finance_dashboard/              ← your git root (clone this)
├── backend/                    ← Spring Boot 3.2 (Java 17)
│   ├── src/
│   │   ├── main/java/com/finance/dashboard/
│   │   │   ├── config/         SecurityConfig, CacheConfig, OpenApiConfig, DataSeeder
│   │   │   ├── controller/     8 REST controllers
│   │   │   ├── dto/            Validated request + response DTOs
│   │   │   ├── exception/      GlobalExceptionHandler + domain exceptions
│   │   │   ├── model/          6 JPA entities + 6 enums
│   │   │   ├── repository/     7 repos (JPQL aggregates + Specification)
│   │   │   ├── scheduler/      RecurringTransactionScheduler, BudgetSweepScheduler
│   │   │   ├── security/       JWT, filters, UserDetails
│   │   │   ├── service/        11 services
│   │   │   └── util/           SecurityUtils, RecurringUtils, IpUtils
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties   (H2, seeder on)
│   │       └── application-prod.properties  (PostgreSQL, env-vars)
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                   ← React 18 + Vite + Tailwind
│   ├── src/
│   │   ├── pages/              Dashboard, Records, Budgets, Recurring,
│   │   │                       Analytics, Notifications, Users, Audit
│   │   ├── components/         Sidebar, Topbar, Modal, Toast, StatCard, Charts
│   │   ├── services/api.js     Axios client with auto-refresh interceptor
│   │   ├── context/            AuthContext (login, logout, role checks)
│   │   └── utils/format.js     INR formatting, categories, color map
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── tailwind.config.js
├── docker-compose.yml
├── .github/workflows/ci-cd.yml
├── .gitignore
└── README.md
```

---

## 🚀 Running Locally (Dev)

### Prerequisites
- Java 17+
- Maven 3.9+
- Node 20+

### 1 — Backend

```bash
cd backend
mvn spring-boot:run
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:financedb`)

### 2 — Frontend (new terminal)

```bash
cd frontend
npm install
npm run dev
```

- App: http://localhost:5173

### Dev credentials (auto-seeded on startup)

| Username | Password | Role | Access |
|---|---|---|---|
| `admin` | `Admin@1234` | ADMIN | Everything |
| `analyst` | `Analyst@1234` | ANALYST | Budgets, analytics, CSV export |
| `viewer` | `Viewer@1234` | VIEWER | Read-only dashboard + records |

---

## 🐳 Docker (Full Stack)

```bash
# From repo root
docker-compose up --build

# Backend  → http://localhost:8080
# Frontend → http://localhost:5173
# Postgres → localhost:5432
```

---

## 🔐 Security Features

| Feature | Detail |
|---|---|
| JWT | Access token (24h) + Refresh token (7d) |
| Auto-refresh | Frontend silently refreshes expired tokens |
| Account Locking | Locked 15 min after 5 failed logins |
| BCrypt | Strength 12 |
| RBAC | VIEWER / ANALYST / ADMIN at path + method level |
| Rate Limiting | Bucket4j: 200 req/min per IP |
| Soft Delete | Users and records never hard-deleted |
| Audit Trail | Every action logged with actor, IP, before/after state |
| Security Headers | CSP, Referrer-Policy, Permissions-Policy (prod) |

---

## 📡 API Endpoints

### Auth (Public)
| Method | Path |
|---|---|
| POST | `/api/auth/login` |
| POST | `/api/auth/refresh` |

### Users
| Method | Path | Role |
|---|---|---|
| GET/PUT | `/api/users/me` | Any |
| POST | `/api/users/me/change-password` | Any |
| POST/GET | `/api/users` | ADMIN |
| GET | `/api/users/{id}` | ADMIN |
| GET | `/api/users/by-role/{role}` | ADMIN |
| PUT/DELETE | `/api/users/{id}` | ADMIN |
| PATCH | `/api/users/{id}/activate` | ADMIN |
| PATCH | `/api/users/{id}/deactivate` | ADMIN |

### Records
| Method | Path | Role |
|---|---|---|
| GET | `/api/records` | Any |
| POST | `/api/records` | ADMIN |
| GET/PUT/DELETE | `/api/records/{id}` | Any/ADMIN |
| GET | `/api/records/export/csv` | ANALYST |

### Budgets & Recurring
| Method | Path | Role |
|---|---|---|
| GET/POST | `/api/budgets` | ANALYST |
| GET/PUT/DELETE | `/api/budgets/{id}` | ANALYST |
| GET/POST | `/api/recurring` | ANALYST |
| GET/PUT/DELETE | `/api/recurring/{id}` | ANALYST |

### Dashboard
| Method | Path | Role |
|---|---|---|
| GET | `/api/dashboard/summary` | Any |
| GET | `/api/dashboard/summary/range?from=&to=` | ANALYST |
| GET | `/api/dashboard/categories` | ANALYST |
| GET | `/api/dashboard/trends/monthly?months=6` | ANALYST |
| GET | `/api/dashboard/trends/weekly?weeks=12` | ANALYST |
| GET | `/api/dashboard/top-expenses` | ANALYST |
| GET | `/api/dashboard/spending-by-day` | ANALYST |
| GET | `/api/dashboard/health-score` | ANALYST |

### Notifications
| Method | Path | Role |
|---|---|---|
| GET | `/api/notifications` | Any |
| GET | `/api/notifications/unread-count` | Any |
| PATCH | `/api/notifications/{id}/read` | Any |
| PATCH | `/api/notifications/mark-all-read` | Any |

### Audit Trail
| Method | Path | Role |
|---|---|---|
| GET | `/api/audit` | ADMIN |
| GET | `/api/audit/by-actor/{username}` | ADMIN |
| GET | `/api/audit/by-entity/{type}/{id}` | ADMIN |
| GET | `/api/audit/by-date-range?from=&to=` | ADMIN |
| GET | `/api/audit/action/{action}` | ADMIN |

---

## 🧠 Financial Health Score (0–100)

| Signal | Max Pts | Formula |
|---|---|---|
| Savings Rate | 30 | net ÷ income; full at 30%+ |
| Budget Adherence | 25 | % budgets below 80% spend |
| Expense Diversity | 20 | HHI index (lower concentration = better) |
| Income Stability | 15 | Low coefficient of variation |
| Positive Cash Flow | 10 | % of last 6 months with income > expense |

**Grade:** A ≥ 85 · B ≥ 70 · C ≥ 55 · D ≥ 40 · F < 40

---

## ☁️ Deploy to Render (Free Tier)

1. Push code to GitHub
2. Go to [render.com](https://render.com) → New Web Service
3. Connect `meranaamkhann/finance_dashboard`
4. Set **Root Directory**: `backend`
5. **Build command**: `mvn clean package -DskipTests`
6. **Start command**: `java -jar target/finance-dashboard-2.0.0.jar`
7. Add environment variables:

```
SPRING_PROFILES_ACTIVE = prod
DB_URL                 = jdbc:postgresql://<render-postgres-host>/financedb
DB_USERNAME            = <username>
DB_PASSWORD            = <password>
JWT_SECRET             = <paste a 64-char random string here>
```

---

## 👤 Author

**Asad Khan** · [github.com/meranaamkhann](https://github.com/meranaamkhann) · khanasad1907@gmail.com
