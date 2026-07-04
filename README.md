# Finance Dashboard — Production Grade v2.0

> Full-stack personal finance management app — **Spring Boot 3.2** backend + **React + Vite + Tailwind** frontend.  
> INR (₹) currency · Light theme · JWT auth · RBAC · Budgets · Health Score · Audit Trail

---

## 🚀 Quick Start (Dev — 2 commands)

```bash
# Backend (H2 in-memory, seeded data, Swagger UI)
cd backend
mvn spring-boot:run
# → http://localhost:8080/swagger-ui.html

# Frontend (separate terminal)
cd frontend
npm install && npm run dev
# → http://localhost:5173
```

**Dev credentials** (auto-seeded):

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@1234` | ADMIN — full access |
| `analyst` | `Analyst@1234` | ANALYST — budgets, analytics, CSV |
| `viewer` | `Viewer@1234` | VIEWER — read-only |

---

## 🐳 Docker (Full stack)

```bash
docker-compose up --build
# Backend → http://localhost:8080
# Frontend → http://localhost:5173
```

---

## 📁 Project Structure

```
finance_dashboard/          ← your git root
├── finance-dashboard/
│   └── finance-dashboard/  ← Spring Boot project
│       ├── pom.xml
│       ├── Dockerfile
│       └── src/
│           ├── main/java/com/finance/dashboard/
│           │   ├── config/         SecurityConfig, CacheConfig, DataSeeder, OpenApiConfig
│           │   ├── controller/     8 REST controllers
│           │   ├── dto/            Request + Response DTOs (fully validated)
│           │   ├── exception/      GlobalExceptionHandler + domain exceptions
│           │   ├── model/          6 JPA entities + 6 enums
│           │   ├── repository/     7 JPA repos with JPQL aggregates
│           │   ├── scheduler/      Recurring auto-poster + Budget sweep
│           │   ├── security/       JWT, filters, UserDetails
│           │   ├── service/        11 services
│           │   └── util/           SecurityUtils, RecurringUtils, IpUtils
│           └── test/
├── frontend/               ← React + Vite + Tailwind
│   ├── src/
│   │   ├── pages/          Dashboard, Records, Budgets, Recurring, Analytics, Users, Audit, Notifications
│   │   ├── components/     UI kit (Modal, Toast, StatCard, ProgressBar, Charts)
│   │   ├── services/       Axios API client with auto-refresh interceptor
│   │   ├── context/        AuthContext (login, logout, role checks)
│   │   └── utils/          format.js (INR, dates, categories, colors)
├── docker-compose.yml
├── .github/workflows/ci-cd.yml
└── README.md
```

---

## 🔐 Security Features

| Feature | Detail |
|---|---|
| JWT Auth | Access token (24h) + Refresh token (7d); refresh tokens blocked on protected routes |
| Account Locking | Auto-lock after 5 failed logins for 15 minutes |
| BCrypt | Strength 12 |
| RBAC | VIEWER / ANALYST / ADMIN — enforced at path + method level |
| Rate Limiting | Bucket4j per-IP: 200 req/min (configurable) |
| Soft Delete | Users and records never hard-deleted |
| CORS | Configurable allowed origins |
| Security Headers | CSP, Referrer-Policy, Permissions-Policy |
| Audit Trail | Every create/update/delete logged with before/after state + IP |

---

## 📊 API Endpoints

| Group | Endpoints | Min Role |
|---|---|---|
| Auth | `POST /api/auth/login`, `POST /api/auth/refresh` | Public |
| Records | `GET/POST/PUT/DELETE /api/records`, `GET /api/records/export/csv` | VIEWER / ADMIN write |
| Budgets | `GET/POST/PUT/DELETE /api/budgets` | ANALYST |
| Recurring | `GET/POST/PUT/DELETE /api/recurring` | ANALYST |
| Dashboard | `GET /api/dashboard/summary`, `/categories`, `/trends/monthly`, `/health-score` etc. | VIEWER / ANALYST |
| Notifications | `GET/PATCH /api/notifications` | VIEWER |
| Users | `GET/POST/PUT/DELETE /api/users`, `GET/PUT /api/users/me` | ADMIN / self |
| Audit | `GET /api/audit` | ADMIN |

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 🧠 Financial Health Score

5-signal algorithm (0–100 pts, grade A–F):

| Signal | Max | How |
|---|---|---|
| Savings Rate | 30 | 20%+ savings = full marks |
| Expense Consistency | 20 | Low coefficient of variation |
| Income Consistency | 20 | Low coefficient of variation |
| Positive Net Months | 20 | % of last 6 months with income > expense |
| Expense-to-Income | 10 | Lower ratio = better |

---

## ☁️ Production Deploy (Render / Railway)

Required environment variables:

```
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://<host>/<db>
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=<64+ char random string>
```

---

## 👤 Author

**Asad Khan** · [github.com/meranaamkhann](https://github.com/meranaamkhann) · khanasad1907@gmail.com
