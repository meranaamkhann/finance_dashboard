# Finance Dashboard v2.0 — Production Grade

> Full-stack personal finance app — Spring Boot 3.2 backend + React 18 + Vite + Tailwind frontend.
> INR currency · JWT auth · RBAC · Budgets · Health Score · Audit Trail

## Project Structure

```
finance_dashboard/              <- your git root
  backend/                      <- Spring Boot 3.2 (Java 17)
    src/main/java/com/finance/dashboard/
      config/    controller/    dto/    exception/
      model/     repository/   scheduler/   security/
      service/   util/
    src/main/resources/
      application.properties
      application-dev.properties   (H2, seeder on)
      application-prod.properties  (PostgreSQL, env-vars)
    pom.xml
    Dockerfile
  frontend/                     <- React 18 + Vite + Tailwind
    src/pages/     src/components/    src/services/
    package.json   vite.config.js     tailwind.config.js
  docker-compose.yml
  .github/workflows/ci-cd.yml
  README.md
```

## Quick Start (Dev)

### Backend
```bash
cd backend
mvn spring-boot:run
# API:    http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

### Frontend (new terminal)
```bash
cd frontend
npm install && npm run dev
# App: http://localhost:5173
```

### Dev credentials (auto-seeded)
| Username | Password     | Role    |
|----------|-------------|---------|
| admin    | Admin@1234   | ADMIN   |
| analyst  | Analyst@1234 | ANALYST |
| viewer   | Viewer@1234  | VIEWER  |

## Docker
```bash
docker-compose up --build
```

## Production Deploy (Render)
1. New Web Service -> connect repo
2. Root directory: `backend`
3. Build: `mvn clean package -DskipTests`
4. Start: `java -jar target/finance-dashboard-2.0.0.jar`
5. Env vars:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL=jdbc:postgresql://...`
   - `DB_USERNAME=...` `DB_PASSWORD=...`
   - `JWT_SECRET=<64-char random string>`

## Author
Asad Khan - github.com/meranaamkhann
