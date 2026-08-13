# FinancePro

A production-oriented full-stack financial management platform built
with React and Spring Boot.

🌐 **Live Demo:** https://finance-pro-sibbus.vercel.app/

📖 **API Documentation:**
https://finance-pro-e5cl.onrender.com/swagger-ui.html

FinancePro is a secure multi-user financial management system with
workspace-based access, role-based permissions, financial records,
budgets, recurring transactions, analytics, audit logging, and automated
alerts.

The application is built with a clear separation between the frontend,
backend business logic, persistence, authentication, authorization, and
infrastructure.

All financial amounts are displayed in **INR (₹)**.

------------------------------------------------------------------------

## Features

### Authentication & Security

- JWT-based authentication
- BCrypt password hashing
- Stateless authentication
- Role-based access control
- Protected API endpoints
- Authentication filters
- Request validation
- Centralized exception handling
- Secure environment-based configuration
- Password visibility controls
- Forgot password flow
- Account validation
- Login security alerts
- Account security controls
- Audit trail for important actions

### Financial Records

- Income and expense management
- Create, update, and delete records
- Financial record categorization
- Search and keyword filtering
- Date-range filtering
- Income and expense filtering
- Sorting and pagination
- Tags and descriptions
- Receipt support
- CSV export
- INR currency formatting

### Dashboard & Analytics

- Income and expense summaries
- Current balance
- Savings rate
- Spending breakdown
- Category-wise analysis
- Recent transactions
- Top expense tracking
- Monthly financial trends
- Day-of-week spending analysis
- Financial health score
- Budget utilization
- Financial insights

### Budget Management

- Category-based budgets
- Custom budget periods
- Spending limits
- Budget overlap validation
- Real-time spending progress
- Remaining budget calculation
- Usage percentage
- Budget status indicators
- Warning and critical thresholds
- Exceeded budget detection
- Budget alerts

Budget status is calculated automatically:

``` text
< 80%       ON TRACK
80–89%      WARNING
90–99%      CRITICAL
>= 100%     EXCEEDED
```

### Recurring Transactions

- Recurring income and expenses
- Monthly and quarterly schedules
- Automatic next-run calculation
- Scheduled transaction processing
- Automatic creation of financial records
- Recurring transaction notifications
- Create, update, and delete recurring rules

### Categories

- System categories
- Workspace-specific custom categories
- Category colors
- Income and expense category types
- Duplicate category prevention
- Protected system categories
- Workspace-level category isolation

### Workspace & Team Management

FinancePro supports multiple users working with shared financial data
while maintaining controlled access.

- Workspace creation
- Workspace ownership
- Member invitations
- Member management
- Member removal
- Role management
- Owner / Admin / Analyst / Viewer roles
- Role-aware feature access
- Workspace-level access control
- Workspace data isolation
- Shared financial records
- Controlled access to financial operations

Example:

``` text
Workspace
├── Owner
├── Members
├── Financial Records
├── Budgets
└── Custom Categories
```

### Audit Trail

Important operations are recorded through an append-only audit trail.

Audit information includes:

- Action performed
- User who performed the action
- Entity affected
- Entity ID
- Timestamp
- Request IP information
- Description
- Before/after information where applicable

### Notifications & Email

- Login security alerts
- Budget notifications
- Recurring transaction notifications
- Password recovery emails
- HTML email templates
- Asynchronous email processing

------------------------------------------------------------------------

## Tech Stack

### Backend

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- Maven
- REST APIs
- OpenAPI / Swagger
- H2 for development and testing
- PostgreSQL for production

### Frontend

- React 18
- Vite
- Tailwind CSS
- React Router
- Axios
- Lucide React

### Infrastructure & DevOps

- Docker
- Docker Compose
- GitHub Actions
- PostgreSQL
- Redis
- Render
- Vercel
- Environment-based configuration

------------------------------------------------------------------------

## Screenshots

### Landing Page

<figure>
<img src="docs/screenshots/Landing page.png"
alt="FinancePro Landing Page" />
<figcaption aria-hidden="true">FinancePro Landing Page</figcaption>
</figure>

### Features

<figure>
<img src="docs/screenshots/features.png" alt="FinancePro Features" />
<figcaption aria-hidden="true">FinancePro Features</figcaption>
</figure>

### Authentication

<figure>
<img src="docs/screenshots/sign-in.png" alt="FinancePro Sign In" />
<figcaption aria-hidden="true">FinancePro Sign In</figcaption>
</figure>

### Dashboard

<figure>
<img src="docs/screenshots/dashboard.png" alt="FinancePro Dashboard" />
<figcaption aria-hidden="true">FinancePro Dashboard</figcaption>
</figure>

### Financial Analytics

<figure>
<img src="docs/screenshots/analytics.png" alt="FinancePro Analytics" />
<figcaption aria-hidden="true">FinancePro Analytics</figcaption>
</figure>

<figure>
<img src="docs/screenshots/analytics-details.png"
alt="FinancePro Analytics Details" />
<figcaption aria-hidden="true">FinancePro Analytics Details</figcaption>
</figure>

### Financial Records

<figure>
<img src="docs/screenshots/records.png" alt="FinancePro Records" />
<figcaption aria-hidden="true">FinancePro Records</figcaption>
</figure>

### Recurring Transactions

<figure>
<img src="docs/screenshots/recurring.png"
alt="FinancePro Recurring Transactions" />
<figcaption aria-hidden="true">FinancePro Recurring
Transactions</figcaption>
</figure>

### Categories

<figure>
<img src="docs/screenshots/categories.png"
alt="FinancePro Categories" />
<figcaption aria-hidden="true">FinancePro Categories</figcaption>
</figure>

### Workspace Members

<figure>
<img src="docs/screenshots/members.png"
alt="FinancePro Workspace Members" />
<figcaption aria-hidden="true">FinancePro Workspace Members</figcaption>
</figure>

### Profile & Account

<figure>
<img src="docs/screenshots/profile.png" alt="FinancePro Profile" />
<figcaption aria-hidden="true">FinancePro Profile</figcaption>
</figure>

------------------------------------------------------------------------

## Architecture

FinancePro follows a layered backend architecture.

``` text
Client
  |
  v
React Frontend
  |
  | REST API
  v
Spring Boot Backend
  |
  +-------------------+
  |                   |
  v                   v
Controller          Security
  |                   |
  v                   v
Service          JWT / Authorization
  |
  v
Repository
  |
  v
PostgreSQL
```

### Controllers

Handle HTTP requests, routing, validation, and API responses.

### Services

Contain business logic including financial calculations, workspace
rules, authorization, budget validation, recurring transaction
processing, analytics, and notification workflows.

### Repositories

Handle persistence using Spring Data JPA and Hibernate.

### Security

Spring Security and JWT provide authentication and authorization across
protected endpoints.

------------------------------------------------------------------------

## Project Structure

``` text
finance_dashboard/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finance/dashboard/
│   │   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── scheduler/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── context/
│   │   ├── pages/
│   │   ├── services/
│   │   └── utils/
│   ├── package.json
│   ├── vite.config.js
│   └── tailwind.config.js
│
├── docs/
│   └── screenshots/
│       ├── landing-page.png
│       ├── features.png
│       ├── sign-in.png
│       ├── dashboard.png
│       ├── analytics.png
│       ├── analytics-details.png
│       ├── records.png
│       ├── recurring.png
│       ├── categories.png
│       ├── members.png
│       └── profile.png
│
├── .github/
│   └── workflows/
│       └── ci-cd.yml
│
├── docker-compose.yml
└── README.md
```

------------------------------------------------------------------------

## Run Locally

### Prerequisites

- Java 17+
- Maven
- Node.js
- npm
- Git

### Backend

``` bash
cd backend
mvn spring-boot:run
```

API:

``` text
http://localhost:8080
```

Swagger:

``` text
http://localhost:8080/swagger-ui.html
```

H2 Console:

``` text
http://localhost:8080/h2-console
```

The development profile uses an in-memory H2 database.

### Frontend

``` bash
cd frontend
npm install
npm run dev
```

Frontend:

``` text
http://localhost:5173
```

------------------------------------------------------------------------

## Development Accounts

The development profile can seed test accounts for local development.

| Username | Password     | Role    |
|----------|--------------|---------|
| admin    | Admin@1234   | ADMIN   |
| analyst  | Analyst@1234 | ANALYST |
| viewer   | Viewer@1234  | VIEWER  |

These accounts are intended only for local development.

------------------------------------------------------------------------

## Testing

### Backend

``` bash
cd backend
mvn clean verify
```

### Frontend Production Build

``` bash
cd frontend
npm run build
```

GitHub Actions also performs automated verification during CI.

------------------------------------------------------------------------

## Docker

Build and start the containers:

``` bash
docker-compose up --build
```

Stop the containers:

``` bash
docker-compose down
```

------------------------------------------------------------------------

## Environment Variables

Production configuration uses environment variables for sensitive
values.

``` env
SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:postgresql://host:5432/finance_dashboard
DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=your_secure_secret
```

Additional configuration may be required for:

``` text
CORS
Email services
Redis
Frontend API URL
```

Never commit production secrets or API keys to Git.

------------------------------------------------------------------------

## REST API

Major API areas include:

``` text
/api/auth
/api/records
/api/budgets
/api/categories
/api/workspace
```

Protected endpoints use:

``` http
Authorization: Bearer <JWT>
```

Swagger provides interactive API documentation.

Production API documentation:

https://finance-pro-e5cl.onrender.com/swagger-ui.html

------------------------------------------------------------------------

## Data Model

``` text
User
├── Workspace
├── WorkspaceMember
├── FinancialRecord
├── Budget
└── AuditLog

Workspace
├── Members
├── Financial Records
├── Budgets
└── Custom Categories
```

Workspace and user relationships maintain data isolation and enforce
access rules.

------------------------------------------------------------------------

## CI/CD

GitHub Actions validates the application during pushes and pull
requests.

``` text
Git Push / Pull Request
          |
          v
    GitHub Actions
          |
     +----+----+
     |         |
     v         v
 Backend    Frontend
  Build       Build
     |         |
     +----+----+
          |
          v
      Docker Build
```

CI helps catch:

- Java compilation errors
- Backend test failures
- Frontend build failures
- Packaging issues
- Docker build problems

before deployment.

------------------------------------------------------------------------

## Deployment

The application is designed for separate frontend and backend
deployment.

### Backend

``` bash
cd backend
mvn clean package
```

Run the packaged application:

``` bash
java -jar target/finance-dashboard-2.0.0.jar
```

The production environment uses PostgreSQL and environment-based
configuration.

### Frontend

``` bash
cd frontend
npm run build
```

The generated frontend can be deployed to Vercel or another static
hosting provider.

### Current Deployment

Frontend: https://finance-pro-sibbus.vercel.app/

Backend: https://finance-pro-e5cl.onrender.com/

Swagger: https://finance-pro-e5cl.onrender.com/swagger-ui.html

------------------------------------------------------------------------

## V1 Status

FinancePro V1 focuses on a complete multi-user financial management
platform with a strong backend architecture.

### Implemented

- JWT authentication
- BCrypt password hashing
- Role-based access control
- Protected REST APIs
- Financial records
- Income and expense management
- Search and filtering
- CSV export
- Dashboard analytics
- Financial health score
- Budget management
- Budget utilization tracking
- Budget alerts
- System categories
- Custom categories
- Workspace collaboration
- Member management
- Role-aware access
- Recurring transactions
- Scheduled transaction processing
- Audit logging
- Notifications
- Swagger/OpenAPI documentation
- Automated backend testing
- GitHub Actions CI
- Docker support
- PostgreSQL production configuration
- Production frontend deployment
- Production backend deployment

The current version is positioned as a strong full-stack/SDE project and
a foundation for a larger financial management product.

------------------------------------------------------------------------

## What I Learned

Building FinancePro required working across:

- Authentication and authorization
- JWT security
- Database relationships
- Workspace-level data isolation
- Role-based permissions
- Financial calculations
- Scheduled jobs
- Backend validation
- REST API design
- Frontend/backend communication
- Testing
- Docker
- CI/CD
- Production deployment

The project helped me understand how the backend, frontend, database,
security, and deployment layers work together as one system.

------------------------------------------------------------------------

## License

This project is currently maintained as a portfolio and learning
project.
