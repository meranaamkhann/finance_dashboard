# Finance Dashboard

🌐 **Live Demo:** https://finance-pro-sibbus.vercel.app/

📖 **API Documentation:** https://finance-pro-e5cl.onrender.com/swagger-ui.html 

A full-stack finance management platform built with **React + Vite** and **Spring Boot**, featuring JWT authentication, role-based access control, workspace collaboration, subscription plans, budgeting, analytics, custom categories, audit logging, and REST APIs.

Built as a production-oriented backend-focused project with a clear separation between frontend, business logic, persistence, authentication, and subscription management.

All financial amounts are displayed in **INR (₹)**.

---

## Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Data JPA, Hibernate, Spring Security, JWT, Maven
- **Database:** H2 (development), PostgreSQL (production)
- **Frontend:** React 18, Vite, Tailwind CSS, React Router, Axios, Lucide React
- **API:** REST, OpenAPI / Swagger
- **DevOps:** Docker, Docker Compose, GitHub Actions
- **Deployment:** Vercel (frontend), Render (backend)

---

## Features

### Authentication & Security

- JWT-based authentication
- BCrypt password hashing
- Stateless authentication
- Role-based access control
- Protected API endpoints
- Authentication filters
- Centralized exception handling
- Request validation
- Secure environment-based configuration

### Financial Records

- Income and expense management
- Create, update, and delete records
- Transaction categories
- Date filtering
- Keyword search
- Sorting
- Pagination
- CSV export
- INR currency formatting

### Dashboard & Analytics

- Income and expense summaries
- Current balance
- Spending breakdown
- Category-wise analysis
- Recent transactions
- Financial health score
- Budget utilization
- Monthly financial insights

### Budgets

- Create category-based budgets
- Define budget periods
- Track spending against budgets
- Remaining budget calculation
- Usage percentage
- Overlapping budget validation
- Budget status indicators

Budget status is calculated automatically:


< 80%       ON_TRACK
80–89%      WARNING
90–99%      CRITICAL
>= 100%     EXCEEDED

---

#  Categories
- System categories
- Workspace-specific custom categories
- Category colors
- Income and expense category types
- Duplicate category prevention
- Protected system categories
- Workspace-level category isolation

# Workspace Collaboration
- Workspace creation
- Workspace ownership
- Member invitations
- Member removal
- Member role management
- Workspace member limits
- Workspace-level access control

Example:

Workspace
├── Owner
├── Members
├── Financial Records
├── Budgets
└── Custom Categories

# Subscription Plans

Finance Dashboard includes subscription-based access control.

Plans support:

- Free plan
- Pro plan
- Team plan
- Monthly billing
- Yearly billing
- Plan-specific features
- Member limits
- Server-side subscription enforcement

Subscription restrictions are enforced by the backend rather than relying only on frontend checks.

For example:

User Request
     |
     v
Authentication
     |
     v
Workspace Validation
     |
     v
Active Subscription
     |
     v
Plan Limit Check
     |
     +---- Allowed ------> Continue
     |
     +---- Limit Reached -> Reject

This prevents users from bypassing plan restrictions by directly calling the API.

# Audit Trail

Important operations are recorded through the audit system.

Audit information includes:

- Action
- User
- Entity
- Entity ID
- Timestamp
- IP address
- Description

# Payments

Razorpay payment infrastructure is integrated into the application architecture for subscription checkout and payment verification.

Current V1 intentionally keeps production payment activation disabled.

The intended flow is:

Pricing
   |
   v
Create Order
   |
   v
Payment Checkout
   |
   v
Payment Verification
   |
   v
Subscription Activation

Production payment activation, custom domain, legal/business configuration, and other SaaS requirements are planned for V2.

# Screenshots

Landing Page

Dashboard

Financial Records

Budgets

Workspace

Categories

Pricing

# Project Layout

finance_dashboard/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finance/dashboard/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── exception/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── scheduler/
│   │   │   │   ├── security/
│   │   │   │   ├── service/
│   │   │   │   └── util/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-dev.properties
│   │   │       └── application-prod.properties
│   │   │
│   │   └── test/
│   │
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
├── .github/
│   └── workflows/
│       └── ci-cd.yml
│
├── docker-compose.yml
└── README.md

#Run It Locally

Backend

Requires Java 17+ and Maven.

cd backend
mvn spring-boot:run

The API starts on:

http://localhost:8080

Swagger:

http://localhost:8080/swagger-ui.html

H2 Console:

http://localhost:8080/h2-console

The development profile uses an in-memory H2 database, so no separate PostgreSQL installation is required for local development.

Frontend

Requires Node.js and npm.

cd frontend
npm install
npm run dev

The frontend starts on:

http://localhost:5173

3Development Accounts

The development profile automatically seeds test accounts.

Username	Password	Role
admin	Admin@1234	ADMIN
analyst	Analyst@1234	ANALYST
viewer	Viewer@1234	VIEWER

These accounts are intended only for local development.

Run the Tests

Backend tests:

cd backend
mvn clean verify

Frontend production build:

cd frontend
npm run build

The backend test suite covers application services and important business rules.

GitHub Actions also runs automated verification during CI.

Run with Docker
docker-compose up --build

Stop the containers:

docker-compose down

Docker support provides a reproducible environment for local development and deployment.

Environment Variables

Production configuration uses environment variables for sensitive values.

Example:

SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:postgresql://host:5432/finance_dashboard
DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=your_secure_secret

Depending on the enabled production services, additional configuration may be required for:

CORS
Payment provider credentials
Email services
Redis
Frontend API URL

Never commit production secrets or API keys to Git.

API

Major REST API areas include:

/api/auth
/api/records
/api/budgets
/api/categories
/api/workspace
/api/plans
/api/payments

Protected endpoints use JWT Bearer authentication:

Authorization: Bearer <JWT>

Swagger provides interactive API documentation and testing.

Backend Architecture

The backend follows a layered architecture:

Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Database
Controllers

Handle HTTP requests, validation, routing, and API responses.

Services

Contain business logic including:

Financial calculations
Workspace rules
Authorization
Subscription enforcement
Budget validation
Payment logic
Repositories

Handle persistence through Spring Data JPA and Hibernate.

Security

Spring Security and JWT provide authentication and authorization across protected endpoints.

Data Model

The main domain entities include:

User
├── Workspace
├── WorkspaceMember
├── FinancialRecord
├── Budget
├── Subscription
└── AuditLog

Workspace
├── Members
├── Financial Records
└── Custom Categories

Plan
└── Subscription

The application uses workspace and user relationships to maintain data isolation and enforce access rules.

CI/CD

GitHub Actions is configured to validate the application during pushes and pull requests.

The pipeline includes:

Git Push / Pull Request
          |
          v
     GitHub Actions
          |
     +----+----+
     |         |
     v         v
 Backend    Frontend
 Build      Build
     |         |
     +----+----+
          |
          v
     Docker Build

CI helps catch:

Java compilation errors
Backend test failures
Frontend build failures
Packaging issues
Docker build problems

before deployment.

Deployment

The application is designed for separate frontend and backend deployment.

Backend

The Spring Boot backend can be packaged with:

cd backend
mvn clean package

and started with:

java -jar target/finance-dashboard-2.0.0.jar

The production environment uses PostgreSQL and environment-based configuration.

Frontend

Build the production frontend with:

cd frontend
npm run build

The generated frontend can be deployed to Vercel or another static hosting provider.

V1 Status

Finance Dashboard V1 focuses on the core product and backend architecture.

Implemented:

JWT authentication
BCrypt password hashing
Role-based access control
Financial records
Income and expense management
Dashboard analytics
Financial health score
Budgets
Budget utilization tracking
System categories
Custom categories
Workspace collaboration
Member management
Subscription plans
Monthly and yearly plan support
Server-side subscription limits
Audit logging
CSV export
REST APIs
Swagger/OpenAPI
Automated backend testing
GitHub Actions CI
Docker support
Production deployment configuration

The application is currently positioned as a strong portfolio/SDE project and a foundation for a future SaaS release.

Categories
System categories
Workspace-specific custom categories
Category colors
Income and expense category types
Duplicate category prevention
Protected system categories
Workspace-level category isolation
Workspace Collaboration
Workspace creation
Workspace ownership
Member invitations
Member removal
Member role management
Workspace member limits
Workspace-level access control

Example:

Workspace
├── Owner
├── Members
├── Financial Records
├── Budgets
└── Custom Categories
Subscription Plans

Finance Dashboard includes subscription-based access control.

Plans support:

Free plan
Pro plan
Team plan
Monthly billing
Yearly billing
Plan-specific features
Member limits
Server-side subscription enforcement

Subscription restrictions are enforced by the backend rather than relying only on frontend checks.

For example:

User Request
     |
     v
Authentication
     |
     v
Workspace Validation
     |
     v
Active Subscription
     |
     v
Plan Limit Check
     |
     +---- Allowed ------> Continue
     |
     +---- Limit Reached -> Reject

This prevents users from bypassing plan restrictions by directly calling the API.

Audit Trail

Important operations are recorded through the audit system.

Audit information includes:

Action
User
Entity
Entity ID
Timestamp
IP address
Description
Payments

Razorpay payment infrastructure is integrated into the application architecture for subscription checkout and payment verification.

Current V1 intentionally keeps production payment activation disabled.

The intended flow is:

Pricing
   |
   v
Create Order
   |
   v
Payment Checkout
   |
   v
Payment Verification
   |
   v
Subscription Activation

Production payment activation, custom domain, legal/business configuration, and other SaaS requirements are planned for V2.

## Screenshots

### Landing Page

![FinancePro Landing Page](docs/screenshots/landing-page.png)

### Features

![FinancePro Features](docs/screenshots/features.png)

### Authentication

![FinancePro Sign In](docs/screenshots/sign-in.png)

### Dashboard

![FinancePro Dashboard](docs/screenshots/dashboard.png)

### Financial Analytics

![FinancePro Analytics](docs/screenshots/analytics.png)

![FinancePro Analytics Details](docs/screenshots/analytics-details.png)

### Financial Records

![FinancePro Records](docs/screenshots/records.png)

### Recurring Transactions

![FinancePro Recurring Transactions](docs/screenshots/recurring.png)

### Categories

![FinancePro Categories](docs/screenshots/categories.png)

### Workspace Members

![FinancePro Members](docs/screenshots/members.png)

### Profile & Account

![FinancePro Profile](docs/screenshots/profile.png)


Project Layout
finance_dashboard/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finance/dashboard/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── exception/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── scheduler/
│   │   │   │   ├── security/
│   │   │   │   ├── service/
│   │   │   │   └── util/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-dev.properties
│   │   │       └── application-prod.properties
│   │   │
│   │   └── test/
│   │
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
├── .github/
│   └── workflows/
│       └── ci-cd.yml
│
├── docker-compose.yml
└── README.md
Run It Locally
Backend

Requires Java 17+ and Maven.

cd backend
mvn spring-boot:run

The API starts on:

http://localhost:8080

Swagger:

http://localhost:8080/swagger-ui.html

H2 Console:

http://localhost:8080/h2-console

The development profile uses an in-memory H2 database, so no separate PostgreSQL installation is required for local development.

Frontend

Requires Node.js and npm.

cd frontend
npm install
npm run dev

The frontend starts on:

http://localhost:5173
Development Accounts

The development profile automatically seeds test accounts.

Username	Password	Role
admin	Admin@1234	ADMIN
analyst	Analyst@1234	ANALYST
viewer	Viewer@1234	VIEWER

These accounts are intended only for local development.

Run the Tests

Backend tests:

cd backend
mvn clean verify

Frontend production build:

cd frontend
npm run build

The backend test suite covers application services and important business rules.

GitHub Actions also runs automated verification during CI.

Run with Docker
docker-compose up --build

Stop the containers:

docker-compose down

Docker support provides a reproducible environment for local development and deployment.

Environment Variables

Production configuration uses environment variables for sensitive values.

Example:

SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:postgresql://host:5432/finance_dashboard
DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=your_secure_secret

Depending on the enabled production services, additional configuration may be required for:

CORS
Payment provider credentials
Email services
Redis
Frontend API URL

Never commit production secrets or API keys to Git.

API

Major REST API areas include:

/api/auth
/api/records
/api/budgets
/api/categories
/api/workspace
/api/plans
/api/payments

Protected endpoints use JWT Bearer authentication:

Authorization: Bearer <JWT>

Swagger provides interactive API documentation and testing.

Backend Architecture

The backend follows a layered architecture:

Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Database
Controllers

Handle HTTP requests, validation, routing, and API responses.

Services

Contain business logic including:

Financial calculations
Workspace rules
Authorization
Subscription enforcement
Budget validation
Payment logic
Repositories

Handle persistence through Spring Data JPA and Hibernate.

Security

Spring Security and JWT provide authentication and authorization across protected endpoints.

Data Model

The main domain entities include:

User
├── Workspace
├── WorkspaceMember
├── FinancialRecord
├── Budget
├── Subscription
└── AuditLog

Workspace
├── Members
├── Financial Records
└── Custom Categories

Plan
└── Subscription

The application uses workspace and user relationships to maintain data isolation and enforce access rules.

CI/CD

GitHub Actions is configured to validate the application during pushes and pull requests.

The pipeline includes:

Git Push / Pull Request
          |
          v
     GitHub Actions
          |
     +----+----+
     |         |
     v         v
 Backend    Frontend
 Build      Build
     |         |
     +----+----+
          |
          v
     Docker Build

CI helps catch:

Java compilation errors
Backend test failures
Frontend build failures
Packaging issues
Docker build problems

before deployment.

Deployment

The application is designed for separate frontend and backend deployment.

Backend

The Spring Boot backend can be packaged with:

cd backend
mvn clean package

and started with:

java -jar target/finance-dashboard-2.0.0.jar

The production environment uses PostgreSQL and environment-based configuration.

Frontend

Build the production frontend with:

cd frontend
npm run build

The generated frontend can be deployed to Vercel or another static hosting provider.

V1 Status

Finance Dashboard V1 focuses on the core product and backend architecture.

Implemented:

JWT authentication
BCrypt password hashing
Role-based access control
Financial records
Income and expense management
Dashboard analytics
Financial health score
Budgets
Budget utilization tracking
System categories
Custom categories
Workspace collaboration
Member management
Subscription plans
Monthly and yearly plan support
Server-side subscription limits
Audit logging
CSV export
REST APIs
Swagger/OpenAPI
Automated backend testing
GitHub Actions CI
Docker support
Production deployment configuration

#License
The application is currently positioned as a strong portfolio/SDE project and a foundation for a future SaaS release.