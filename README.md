# Finance Dashboard

A production-oriented full-stack finance management platform built with Java, Spring Boot, React, and PostgreSQL/MySQL.

Finance Dashboard provides a secure environment for managing financial records, budgets, analytics, categories, workspaces, and subscription-based access. The project is designed with a strong backend architecture, role-based security, server-side business rules, automated testing, CI/CD, and deployment support.

## Live Application

Frontend: [Add Live URL]

Backend API: [Add Backend URL]

API Documentation: [Add Swagger URL]

---

## Features

### Authentication and Security

- JWT-based stateless authentication
- BCrypt password hashing
- Spring Security integration
- Role-based access control
- Protected REST endpoints
- Authentication filters
- Request validation
- Centralized exception handling
- Environment-based configuration for sensitive values

### Financial Management

- Create, update, and delete financial records
- Income and expense transactions
- Categorized transactions
- Date-based filtering
- Keyword search
- Sorting and pagination
- Financial summaries
- CSV export

### Budget Management

- Create category-based budgets
- Define budget periods
- Track spending against budgets
- Calculate remaining budget
- Calculate budget utilization
- Detect overlapping active budgets
- Budget status tracking

Budget status is calculated based on utilization:

```text
< 80%       ON_TRACK
80–89%      WARNING
90–99%      CRITICAL
>= 100%     EXCEEDED
Categories

The application supports both system-defined and workspace-specific categories.

System Categories

System categories are seeded by the backend and protected from modification or deletion.

Custom Categories

Users with the required permissions can create workspace-specific categories with:

Category name
Category type
Category color
Workspace association

Custom categories remain scoped to their workspace.

Dashboard and Analytics

The dashboard provides an overview of financial activity, including:

Total income
Total expenses
Current balance
Spending breakdown
Category-level analysis
Budget utilization
Recent transactions
Financial health indicators
Workspace Management

Finance Dashboard uses a workspace-based architecture for collaboration.

Workspace functionality includes:

Workspace creation
Workspace ownership
Member invitations
Member removal
Member role management
Workspace membership validation
Workspace-level access control

Example:

Workspace
├── Owner
├── Members
├── Financial Records
├── Budgets
└── Custom Categories
Subscription Plans

The application includes subscription-aware functionality with multiple plans.

Plans can define:

Monthly pricing
Yearly pricing
Feature availability
Maximum workspace members
Other plan-specific limits

Subscription restrictions are enforced by the backend.

This prevents users from bypassing plan restrictions by modifying frontend code or directly calling the REST API.

Example:

Client Request
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
      +---- Allowed ------> Operation
      |
      +---- Limit Reached -> Subscription Error
Audit Trail

Important application actions are recorded through the audit system.

Audit records can contain:

Action
User
Entity
Entity ID
Timestamp
IP address
Description

This provides traceability for important operations such as budget creation, modification, and deletion.

Payments

The application contains the backend architecture required for subscription payment processing through Razorpay.

The intended flow is:

Pricing Page
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

Production payment activation is intentionally deferred to V2 while the core V1 product and subscription architecture are completed.

Architecture

Finance Dashboard follows a layered backend architecture.

                    React Frontend
                          |
                          | REST / JSON
                          v
                 Spring Boot Backend
                          |
             +------------+------------+
             |            |            |
             v            v            v
        Controllers    Security     Validation
             |
             v
          Services
             |
             v
        Repositories
             |
             v
       JPA / Hibernate
             |
             v
       Relational DB
Backend Layers

Controller Layer

Responsible for:

REST endpoints
Request handling
Request validation
Response generation
API documentation

Service Layer

Responsible for:

Business logic
Authorization checks
Subscription rules
Workspace rules
Financial calculations
Transaction management

Repository Layer

Responsible for:

Database access
Query execution
Entity persistence

Security Layer

Responsible for:

JWT authentication
Security context
Role-based authorization
Protected endpoints
Technology Stack
Backend
Technology	Purpose
Java 17	Primary programming language
Spring Boot 3.2	Backend framework
Spring Security	Authentication and authorization
JWT	Stateless authentication
Spring Data JPA	Persistence layer
Hibernate	ORM
Maven	Build and dependency management
H2	Development database
PostgreSQL	Production database
OpenAPI / Swagger	API documentation
Lombok	Boilerplate reduction
Frontend
Technology	Purpose
React 18	UI framework
Vite	Frontend build tool
Tailwind CSS	Styling
React Router	Client-side routing
Axios	API communication
Lucide React	Icons
DevOps
Technology	Purpose
Git	Version control
GitHub	Source control
GitHub Actions	CI/CD
Docker	Containerization
Docker Compose	Local container orchestration
Render	Backend deployment
Vercel	Frontend deployment
Project Structure
finance_dashboard/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finance/dashboard/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   └── response/
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
API

The backend exposes RESTful APIs organized around the application's major domains.

/api/auth
/api/records
/api/budgets
/api/categories
/api/workspace
/api/plans
/api/payments

Protected endpoints use JWT Bearer authentication:

Authorization: Bearer <JWT>

The backend uses DTOs for request and response handling to keep the API contract separate from persistence entities.

API Documentation

Swagger / OpenAPI documentation is available when the backend is running:

http://localhost:8080/swagger-ui.html

Swagger provides interactive documentation for:

REST endpoints
Request models
Response models
Authentication
API testing
Local Development
Prerequisites

Make sure the following are installed:

Java 17+
Maven
Node.js
npm
Git

Docker is optional for local development.

Clone the Repository
git clone https://github.com/meranaamkhann/finance_dashboard.git
cd finance_dashboard
Start the Backend
cd backend
mvn spring-boot:run

Backend:

http://localhost:8080

Swagger:

http://localhost:8080/swagger-ui.html

H2 Console:

http://localhost:8080/h2-console
Start the Frontend

Open another terminal:

cd frontend
npm install
npm run dev

Frontend:

http://localhost:5173
Development Environment

The development profile uses an in-memory H2 database.

jdbc:h2:mem:financedb

Development data is reset when the backend restarts.

This environment is intended for local development and testing rather than production persistence.

Development Accounts

The development environment automatically seeds test accounts.

Username	Password	Role
admin	Admin@1234	ADMIN
analyst	Analyst@1234	ANALYST
viewer	Viewer@1234	VIEWER

These credentials are intended only for local development.

Testing

Run the backend test suite with:

cd backend
mvn clean verify

Build the frontend:

cd frontend
npm run build

The CI pipeline also performs automated validation of the project.

Docker

Build and start the application:

docker-compose up --build

Stop the containers:

docker-compose down

Docker support provides a reproducible environment for local development and deployment workflows.

CI/CD

GitHub Actions is used to automatically validate changes.

The CI workflow performs automated build and verification steps for the application.

General workflow:

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
       Docker

CI helps identify:

Java compilation errors
Backend test failures
Frontend build failures
Packaging issues
Docker build problems

before deployment.

Production Configuration

Production configuration is separated from development configuration.

Sensitive values should be supplied through environment variables.

Example:

SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:postgresql://host:5432/finance_dashboard
DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=your_secure_secret

Production secrets must never be committed to Git.

Deployment

The application is designed for separate frontend and backend deployment.

Backend

Build the Spring Boot application:

cd backend
mvn clean package

Run the generated JAR:

java -jar target/finance-dashboard-2.0.0.jar
Frontend

Create a production build:

cd frontend
npm run build

The generated frontend can be deployed to a static hosting platform.

Security

Security is implemented primarily on the backend.

Key security measures include:

JWT authentication
BCrypt password hashing
Spring Security
Role-based authorization
Workspace access validation
Server-side subscription enforcement
DTO validation
Centralized exception handling
Environment-based secrets

The frontend is not treated as a trusted security boundary.

For example, even if a user manually sends a request to an endpoint, the backend still verifies authentication, authorization, workspace access, and subscription restrictions before performing the operation.

V1 Scope

Finance Dashboard V1 focuses on building a strong full-stack foundation and a production-oriented backend architecture.

V1 includes:

Authentication and authorization
JWT security
Financial record management
Income and expense tracking
Budgets
Budget utilization tracking
Financial analytics
Financial health tracking
System and custom categories
Workspace collaboration
Member management
Subscription plans
Server-side plan enforcement
Audit logging
CSV export
REST APIs
Swagger/OpenAPI documentation
Automated testing
GitHub Actions CI/CD
Docker support
Deployment-ready architecture

Production payment activation is intentionally deferred to V2.

V2 Roadmap

Planned improvements for future versions include:

Production Razorpay activation
Custom domain
Production legal and business configuration
Additional subscription features
Advanced financial analytics
Improved onboarding
Further frontend and UX improvements
Performance optimization
Additional SaaS capabilities
Product improvements based on real user feedback and demand
License

This project is licensed under the MIT License.