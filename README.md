Finance Dashboard

A full-stack finance management platform built with Java 17, Spring Boot 3.2, React 18, Vite, Tailwind CSS, and JPA/Hibernate.

Finance Dashboard is designed as a production-oriented application rather than a basic CRUD project. It combines financial record management, budgeting, analytics, workspace collaboration, role-based access control, subscription plans, server-side plan enforcement, audit logging, REST APIs, testing, CI/CD, and deployment support.

Overview

Finance Dashboard allows users to manage and analyze their financial activity through a modern web interface backed by a secure Spring Boot REST API.

The application supports both individual and collaborative financial management through a workspace-based architecture.

Core capabilities
JWT authentication
BCrypt password hashing
Role-based access control
Income and expense management
Financial analytics
Budget management
Budget utilization tracking
System and custom categories
Workspace collaboration
Member management
Subscription plans
Server-side subscription limit enforcement
Payment infrastructure
Audit logging
CSV export
REST APIs
OpenAPI / Swagger documentation
Automated tests
GitHub Actions CI/CD
Docker support
Production deployment configuration
System Architecture

The application follows a client-server architecture with a layered Spring Boot backend.

                    Browser
                       |
                       v
              React Frontend
             React + Vite
                       |
                 REST / JSON
                       |
                       v
             Spring Boot API
                       |
          +------------+------------+
          |            |            |
          v            v            v
      Security     Controllers   Validation
          |            |
          |            v
          |         Services
          |            |
          |            v
          |       Repositories
          |            |
          +------------+
                       |
                       v
                Relational DB

The frontend is responsible for presentation and user interaction.

The backend is the authoritative layer for:

Authentication
Authorization
Business rules
Workspace access
Subscription limits
Data validation
Persistence

Frontend restrictions are therefore not treated as security boundaries.

Backend Architecture

The backend follows a layered architecture:

HTTP Request
     |
     v
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
Controller Layer

Controllers expose REST endpoints and handle:

HTTP routing
Request parsing
Validation
HTTP responses
API documentation
Endpoint-level authorization

Controllers delegate business logic to services.

Service Layer

The service layer contains the application's business rules.

Major services include:

AuthService
BudgetService
FinancialRecordService
WorkspaceService
SubscriptionService
CustomCategoryService
AuditService
PaymentService

The service layer handles operations such as:

Checking permissions
Resolving the current user
Resolving the current workspace
Validating subscription limits
Validating business rules
Creating and updating domain objects
Recording audit events

This keeps business logic separate from HTTP and persistence concerns.

Repository Layer

The application uses Spring Data JPA repositories for database access.

Service
   |
   v
Spring Data Repository
   |
   v
Hibernate / JPA
   |
   v
Database

Repositories are responsible for persistence queries while services remain responsible for business logic.

Authentication and Security

Authentication is implemented using Spring Security and JWT.

The authentication flow is:

User
 |
 | Credentials
 v
Login Endpoint
 |
 v
Credential Validation
 |
 v
JWT Generation
 |
 v
Client
 |
 | Authorization: Bearer <JWT>
 v
JWT Authentication Filter
 |
 v
Security Context
 |
 v
Protected Controller

Passwords are hashed using BCrypt and are never stored as plain text.

Protected requests contain:

Authorization: Bearer <token>

The JWT authentication filter validates the token, identifies the user, and establishes the authenticated security context.

Role-Based Access Control

The application uses role-based authorization to control access to protected operations.

Supported roles include:

ADMIN
ANALYST
VIEWER
OWNER

Authorization is enforced by the backend using Spring Security and application-level service checks.

The frontend may hide unavailable actions, but the backend independently validates whether the authenticated user is allowed to perform the operation.

Workspace Architecture

Finance Dashboard uses workspaces as the primary boundary for collaborative financial data.

User
 |
 v
Workspace
 |
 +---- Owner
 |
 +---- Members
 |
 +---- Financial Records
 |
 +---- Custom Categories
 |
 +---- Workspace Data

A workspace contains users with different roles and provides the scope for collaborative operations.

Workspace functionality includes:

Workspace creation
Workspace ownership
Member invitations
Member removal
Member role management
Workspace membership validation
Workspace-level data access

This architecture provides a foundation for multi-user SaaS functionality.

Subscription Architecture

Finance Dashboard includes subscription-aware access control.

Plans contain information such as:

Plan
 |
 +---- Name
 +---- Slug
 +---- Monthly Price
 +---- Yearly Price
 +---- Features
 +---- Maximum Users
 +---- Other Limits

The active subscription is resolved by the backend before performing operations that are subject to plan restrictions.

Server-side enforcement

For example, when adding a workspace member:

Request
   |
   v
Authenticate User
   |
   v
Resolve Workspace
   |
   v
Resolve Active Plan
   |
   v
Check Plan Limit
   |
   +---- Allowed ------> Add Member
   |
   +---- Limit Reached -> Reject Request

This is intentionally enforced on the backend.

A frontend-only restriction could be bypassed by sending a request directly to the API.

Financial Records

Financial records represent income and expense transactions.

A record contains information such as:

Financial Record
 |
 +---- Type
 +---- Category
 +---- Amount
 +---- Date
 +---- Description
 +---- User
 +---- Workspace
 +---- Created At

Supported operations include:

Create
Read
Update
Delete
Search
Filter
Sort
Pagination
CSV export

The records API supports filtering by transaction type, date range, and keywords.

Budget Management

Users can create budgets for financial categories and specific periods.

The application calculates:

Spent Amount
Remaining Amount
Usage Percentage
Budget Status

The calculation is conceptually:

Usage % = (Spent / Budget Limit) × 100

Remaining = Budget Limit - Spent

Budget states are determined by utilization:

< 80%       ON_TRACK
80–89%      WARNING
90–99%      CRITICAL
>= 100%     EXCEEDED

The backend also validates overlapping active budgets for the same category and period.

Custom Categories

The application supports both system categories and workspace-specific custom categories.

Categories
    |
    +---- System Categories
    |
    +---- Custom Categories

Custom categories support:

Name
Color
Type
Workspace association
Creator information

System categories are protected from modification and deletion.

Custom categories are scoped to the user's workspace.

Dashboard and Analytics

The dashboard aggregates financial data into higher-level metrics.

Examples include:

Total income
Total expenses
Current balance
Category spending
Budget utilization
Financial health score
Recent transactions

The dashboard uses the application's financial records as its source of truth rather than maintaining separate duplicated financial data.

Audit Trail

Important state-changing operations are recorded through the audit system.

Audit information can include:

Action
Actor
Entity
Entity ID
Timestamp
IP Address
Description

Example flow:

User Action
    |
    v
Service Layer
    |
    +---- Perform Operation
    |
    +---- AuditService.log(...)

This provides traceability for important operations such as budget creation, updates, and deletion.

Payment Architecture

The application contains subscription billing infrastructure using Razorpay.

The intended payment flow is:

Pricing Page
     |
     v
Create Payment Order
     |
     v
Razorpay Checkout
     |
     v
Payment Completed
     |
     v
Backend Verification
     |
     v
Subscription Activation

Payment verification is handled by the backend rather than trusting payment information supplied directly by the frontend.

V1 payment scope

Production payment activation is intentionally deferred in V1.

The current release focuses on the application's subscription architecture and backend plan enforcement without requiring production payment credentials and business/legal configuration.

Production billing activation is planned for V2.

Database Architecture

The application uses JPA/Hibernate for object-relational mapping.

Major domain entities include:

User
Workspace
WorkspaceMember
FinancialRecord
Budget
Plan
Subscription
CustomCategory
AuditLog
Payment

The relationships form the foundation of the application's data model:

User
 |
 +---- Workspace
 |       |
 |       +---- Workspace Members
 |       +---- Financial Records
 |       +---- Custom Categories
 |
 +---- Subscription
 |
 +---- Authentication Data

The development environment uses H2.

Production database configuration is supplied through the production Spring profile and environment variables.

API Design

The backend exposes REST APIs organized around application domains.

Major API areas include:

/api/auth
/api/records
/api/budgets
/api/categories
/api/workspace
/api/plans
/api/payments
/api/users

Protected endpoints use JWT Bearer authentication.

Example:

Authorization: Bearer <JWT>
API Documentation

Swagger / OpenAPI documentation is available when the backend is running.

http://localhost:8080/swagger-ui.html

Swagger provides:

Endpoint documentation
Request schemas
Response schemas
Authentication configuration
Interactive API testing
Validation and Error Handling

The backend uses DTO validation and centralized application exceptions.

Examples of application-level exceptions include:

BadRequestException
ResourceNotFoundException
SubscriptionLimitException

Examples:

Invalid request
      |
      v
BadRequestException
Resource does not exist
      |
      v
ResourceNotFoundException
Subscription limit reached
      |
      v
SubscriptionLimitException

This allows API failures to be represented consistently instead of implementing error handling independently inside every controller.

Security Considerations

The application follows several backend security principles.

Password Security

Passwords are hashed using BCrypt.

Authentication

Protected endpoints require valid JWT authentication.

Authorization

Role, ownership, and workspace access are checked by the backend.

Subscription Enforcement

Plan restrictions are enforced server-side.

Data Isolation

Workspace-related operations are scoped to the authenticated user's workspace.

Secrets

The following must never be committed to source control:

Database credentials
JWT secrets
Razorpay credentials
API keys
Production environment variables
Technology Stack
Backend
Technology	Purpose
Java 17	Primary language
Spring Boot 3.2.5	Backend framework
Spring Security	Authentication and authorization
JWT	Stateless authentication
Spring Data JPA	Data access
Hibernate	ORM
H2	Development and testing
PostgreSQL / MySQL	Production database configuration
Maven	Build system
OpenAPI / Swagger	API documentation
Lombok	Boilerplate reduction
Frontend
Technology	Purpose
React 18	Frontend framework
Vite	Build tooling
Tailwind CSS	Styling
React Router	Client-side routing
Axios	HTTP client
Lucide React	UI icons
DevOps
Technology	Purpose
Git	Version control
GitHub	Repository hosting
GitHub Actions	CI/CD
Docker	Containerization
Docker Compose	Local environment
Render	Backend deployment
Vercel	Frontend deployment
Project Structure
finance_dashboard/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finance/dashboard/
│   │   │   │
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── scheduler/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── util/
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   │
│   ├── src/test/
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
│   │
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
Local Development
Prerequisites

Install:

Java 17+
Maven
Node.js
npm
Git
Docker (optional)
Clone
git clone https://github.com/meranaamkhann/finance_dashboard.git
cd finance_dashboard
Backend
cd backend
mvn spring-boot:run

Backend:

http://localhost:8080

Swagger:

http://localhost:8080/swagger-ui.html

H2 Console:

http://localhost:8080/h2-console
Frontend

Open another terminal:

cd frontend
npm install
npm run dev

Frontend:

http://localhost:5173
Development Database

The development profile uses an in-memory H2 database.

Example:

jdbc:h2:mem:financedb

Because the database is in-memory, development data is reset when the backend restarts.

This configuration is intended for local development and testing.

Configuration

Production configuration should be supplied through environment variables.

Example:

SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:postgresql://host:5432/finance_dashboard
DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=your-secure-secret

Never commit production credentials or secrets to the repository.

Testing

Run backend verification:

cd backend
mvn clean verify

This runs the Maven verification lifecycle, including compilation and automated tests.

Build the frontend:

cd frontend
npm run build
Docker

Build and run the application using Docker Compose:

docker-compose up --build

Stop the environment:

docker-compose down
CI/CD

GitHub Actions is used to automatically validate repository changes.

The CI pipeline validates the application through steps such as:

Git Push / Pull Request
        |
        v
Checkout
        |
        +--------------------+
        |                    |
        v                    v
Maven Verification      Frontend Build
        |                    |
        +---------+----------+
                  |
                  v
             Docker Build

CI helps detect:

Java compilation errors
Backend test failures
Frontend build failures
Packaging issues
Docker build problems

before deployment.

Deployment

The application is designed for separate frontend and backend deployment.

Backend

Build the Spring Boot application:

cd backend
mvn clean package

Run the generated JAR:

java -jar target/finance-dashboard-2.0.0.jar

Production deployment requires:

Production database
Environment variables
Secure JWT secret
CORS configuration
Frontend API configuration
Production payment configuration when billing is activated
Frontend

Build the production frontend:

cd frontend
npm run build

The generated build can be deployed through a static hosting provider.

Engineering Decisions
Layered Backend Architecture

The application separates HTTP handling, business logic, and persistence:

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

This makes business rules easier to test, maintain, and extend.

Backend as the Security Boundary

Frontend checks are not considered sufficient for security-sensitive operations.

For example:

Frontend
   |
   | "User can add member"
   v
Backend
   |
   +---- Is authenticated?
   +---- Is authorized?
   +---- Correct workspace?
   +---- Subscription allows operation?
   |
   v
Perform Operation

This approach prevents client-side manipulation from bypassing application rules.

Workspace-Based Isolation

Workspace ownership provides a consistent boundary for collaborative data.

User
 |
 v
Workspace
 |
 +---- Members
 +---- Records
 +---- Categories
 +---- Budgets

This architecture also provides a foundation for future SaaS expansion.

Centralized Subscription Logic

Subscription rules are handled through backend services rather than duplicated throughout controllers and frontend components.

This makes plan enforcement easier to maintain as additional limits and features are introduced.

V1 Scope

Version 1 focuses on completing the core finance platform and establishing a strong backend foundation.

Included in V1:

Authentication
JWT security
Role-based authorization
Financial records
Budgets
Analytics
Financial health score
Custom categories
Workspace collaboration
Subscription plans
Server-side subscription enforcement
Audit logging
CSV export
API documentation
Automated testing
CI/CD
Docker support
Deployment preparation

Production payment activation is intentionally deferred to V2.

V2 Roadmap

Planned V2 improvements include:

Production Razorpay activation
Custom domain
Production business and legal configuration
Additional analytics
Advanced reporting
Improved onboarding
Additional subscription capabilities
Performance optimization
Product improvements based on real user feedback and demand
Screenshots

Recommended screenshots:

docs/
└── screenshots/
    ├── landing.png
    ├── dashboard.png
    ├── records.png
    ├── budgets.png
    ├── workspace.png
    └── pricing.png

Example:

![Dashboard](docs/screenshots/dashboard.png)
License

This project is licensed under the MIT License.