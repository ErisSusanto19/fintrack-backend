# Finance Tracker - Backend Technical Specification

| **Version** | 1.0 |
| **Status** | Baseline |
| **Last Updated** | August 16, 2025 |
| **Author** | Eris Susanto |

## Table of Contents
1.  [Introduction](#1-introduction)
2.  [System Architecture](#2-system-architecture)
3.  [Data Model & Database Design](#3-data-model--database-design)
4.  [API Specification (v1)](#4-api-specification-v1)
5.  [Security Design](#5-security-design)
6.  [Key Business Logic & Rules](#6-key-business-logic--rules)
7.  [Non-Functional Requirements](#7-non-functional-requirements)
8.  [Appendix](#8-appendix)

---

## 1. Introduction

### 1.1. Document Purpose
This document provides a comprehensive technical specification for the backend services of the Finance Tracker application. It is intended to guide the development process, ensuring all components are built to a consistent standard. This will serve as our map for the project.

### 1.2. Project Goal
The primary objective is to build a robust, scalable, and secure backend foundation using Spring Boot. This system will power a personal finance management application, focusing on data integrity, performance, and a clean, maintainable architecture to support future feature growth.

### 1.3. Core Features
*   User Authentication & Authorization (JWT)
*   Multi-Account Management (Cash, Bank, E-Wallet)
*   Categorization of Income and Expenses
*   Transaction Recording (Income, Expense, Transfer) with Attachments
*   Budgeting per Category or Overall
*   Recurring Transaction Scheduling
*   Reporting & Dashboard Analytics
*   (Optional) Multi-Currency Support

## 2. System Architecture

### 2.1. Architectural Style
The system will be built following the principles of **Clean Architecture** (or a Hexagonal-ish style). This promotes separation of concerns by isolating business logic from external frameworks and interfaces.

*   **Domain/Entities**: Core business objects, independent of any framework.
*   **Application/Services**: Orchestrates business logic and use cases.
*   **Adapters/Frameworks**: The outer layer, including REST Controllers, Database Repositories, and third-party integrations.

### 2.2. Technology Stack
*   **Framework**: Spring Boot 3.x
*   **Language**: Java 17+
*   **Database**: PostgreSQL 15+
*   **Database Migrations**: Flyway
*   **Authentication**: Spring Security 6, JWT
*   **API Documentation**: SpringDoc (OpenAPI 3)
*   **Data Mapping**: MapStruct
*   **Validation**: Jakarta Bean Validation (Hibernate Validator)
*   **Caching (Optional)**: Redis
*   **Build Tool**: Maven / Gradle

### 2.3. Project Structure

```
com.yourcompany.fintrack
├─ api/                 # Controllers, DTOs, Mappers
│  ├─ auth/
│  ├─ account/
│  ├─ transaction/
│  └─ ...
├─ application/         # Service layer (Use Cases)
│  ├─ port/             # Interfaces for repositories (in/out)
│  └─ service/          # Service implementations
├─ domain/              # Core business entities and value objects
├─ infrastructure/      # Implementation of external concerns
│  ├─ config/           # Spring configuration (Security, CORS, etc.)
│  ├─ persistence/      # JPA entities, repositories (implementing ports)
│  ├─ security/         # JWT utilities, UserDetails service
│  ├─ storage/          # S3/Local file storage implementation
│  └─ scheduler/        # Cron jobs for recurring tasks
└─ FinTrackApplication.java
```

## 3. Data Model & Database Design

### 3.1. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS {
        UUID id PK
        string email UK
        string password_hash
        string full_name
        string role
        timestamp created_at
        timestamp updated_at
    }
    ACCOUNTS {
        UUID id PK
        UUID user_id FK
        string name
        string type "CASH, BANK, EWALLET, CREDIT_CARD"
        string currency_code
        numeric initial_balance
        numeric balance "Calculated field"
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at "For soft delete"
    }
    CATEGORIES {
        UUID id PK
        UUID user_id FK
        UUID parent_id FK "For sub-categories"
        string name
        string type "INCOME or EXPENSE"
        string icon_color_hex
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at "For soft delete"
    }
    TRANSACTIONS {
        UUID id PK
        UUID user_id FK
        UUID account_id FK
        UUID category_id FK NULL
        UUID transfer_group_id NULL "Links two transfer transactions"
        string type "REGULAR, TRANSFER, ADJUSTMENT"
        string direction "INCOME, EXPENSE"
        numeric amount
        date transaction_date
        string description
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at "For soft delete"
    }
    ATTACHMENTS {
        UUID id PK
        UUID transaction_id FK
        string file_name
        string mime_type
        string storage_key "Path/URL to file in S3/local"
        long file_size_bytes
        timestamp created_at
    }
    BUDGETS {
        UUID id PK
        UUID user_id FK
        UUID category_id FK NULL "NULL for overall budget"
        integer month "1-12"
        integer year
        numeric amount_limit
        numeric warning_threshold "e.g., 0.8 for 80%"
        UNIQUE(user_id, category_id, month, year)
    }
    RECURRING_TRANSACTIONS {
        UUID id PK
        UUID user_id FK
        UUID account_id FK
        UUID category_id FK
        string direction "INCOME, EXPENSE"
        numeric amount
        string cron_schedule
        date start_date
        date end_date NULL
        string description
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    REFRESH_TOKENS {
        UUID id PK
        UUID user_id FK
        string token_hash UK
        string user_agent
        string ip_address
        timestamp expires_at
        timestamp created_at
        boolean is_revoked
    }

    USERS ||--o{ ACCOUNTS : "manages"
    USERS ||--o{ CATEGORIES : "defines"
    USERS ||--o{ TRANSACTIONS : "performs"
    USERS ||--o{ BUDGETS : "sets"
    USERS ||--o{ RECURRING_TRANSACTIONS : "schedules"
    USERS ||--o{ REFRESH_TOKENS : "owns"
    ACCOUNTS ||--o{ TRANSACTIONS : "has"
    CATEGORIES ||--o{ TRANSACTIONS : "classifies"
    CATEGORIES }o--|| CATEGORIES : "is child of"
    TRANSACTIONS ||--o{ ATTACHMENTS : "includes"
```

### 3.2. Database Specifics
*   **Naming Convention**: Snake case for tables and columns (e.g., `user_id`).
*   **Data Types**: Use `NUMERIC(18, 4)` for financial amounts for precision. `UUID` for primary keys.
*   **Migrations**: Use Flyway for version-controlled schema changes. All changes must be in versioned SQL scripts (e.g., `V1__init_schema.sql`).
*   **Indexing Strategy**:
    *   All foreign keys will be indexed.
    *   Composite index on `transactions(user_id, transaction_date DESC)`.
    *   Composite index on `transactions(user_id, category_id)`.
    *   Index on `refresh_tokens(user_id, is_revoked, expires_at)`.
    *   Consider a GIN index for full-text search on `transactions.description`.

## 4. API Specification (v1)

### 4.1. General Conventions
*   **Base Path**: `/api/v1`
*   **Authentication**: All endpoints (except `/auth/**`) require a `Bearer <JWT>` token in the `Authorization` header.
*   **Pagination**: Use `page` (0-indexed) and `size` query parameters for list endpoints.
*   **Sorting**: Use `sort=field,direction` (e.g., `sort=transactionDate,desc`).
*   **Standard Response Wrapper**:
    ```json
    {
      "data": { ... }, // The actual response payload
      "meta": { "timestamp": "...", "page": 0, "size": 10, "totalElements": 100 }, // Optional metadata
      "error": null // Error object if request fails
    }
    ```

### 4.2. Endpoints

#### 4.2.1. Authentication (`/auth`)
*   `POST /auth/register`: Register a new user.
*   `POST /auth/login`: Authenticate and receive an access token and a refresh token.
*   `POST /auth/refresh`: Obtain a new access token using a valid refresh token.
*   `POST /auth/logout`: Revoke the refresh token used in the request.

#### 4.2.2. Accounts (`/accounts`)
*   `GET /`: List all user accounts with pagination.
*   `POST /`: Create a new account (with an initial balance).
*   `GET /{id}`: Get account details.
*   `PUT /{id}`: Update account details (name, type, etc.).
*   `DELETE /{id}`: Soft-delete (archive) an account.

#### 4.2.3. Transactions (`/transactions`)
*   `GET /`: List transactions with powerful filtering (date range, account, category, direction, amount range).
*   `POST /`: Create a new income or expense transaction.
*   `POST /transfer`: Create a transfer between two user accounts (atomic operation).
*   `GET /{id}`: Get transaction details, including attachments.
*   `PUT /{id}`: Update a transaction.
*   `DELETE /{id}`: Soft-delete a transaction.
*   `POST /{id}/attachments`: Upload an attachment (multipart/form-data).
*   `DELETE /attachments/{attachmentId}`: Delete an attachment.

#### 4.2.4. Categories (`/categories`)
*   `GET /`: List all categories, filterable by `type` (INCOME/EXPENSE).
*   `POST /`: Create a new category (can be a sub-category by providing `parentId`).
*   `PUT /{id}`: Update a category.
*   `DELETE /{id}`: Soft-delete a category.

#### 4.2.5. Budgets (`/budgets`)
*   `GET /`: List budgets for a given period (e.g., `?year=2025&month=8`).
*   `POST /`: Create a new budget.
*   `GET /summary`: Get a summary of spending vs. budget for a given period.
*   `PUT /{id}`: Update a budget.
*   `DELETE /{id}`: Delete a budget.

#### 4.2.6. Reports (`/reports`)
*   `GET /overview`: Get a dashboard overview for a date range (total income/expense, cash flow, top categories).
*   `GET /cashflow-trend`: Get time-series data for cash flow analysis.
*   `GET /category-breakdown`: Get expense breakdown by category for a date range.

## 5. Security Design

*   **Authentication**: Stateless JWT access tokens (short-lived, e.g., 15 mins) and stateful refresh tokens (long-lived, e.g., 30 days) stored hashed in the database.
*   **Refresh Token Management**: Refresh tokens are stored in an `HttpOnly` secure cookie. They are invalidated on logout or can be revoked by the user.
*   **Password Hashing**: BCrypt with a cost factor of 12 will be used for storing passwords.
*   **Data Tenancy**: **Crucially**, all database queries for user-specific data MUST be scoped by the authenticated `user_id`. A user must never be able to access another user's data. This will be enforced at the service or repository layer.
*   **CORS**: Configure a strict CORS policy to only allow requests from the official frontend domain.
*   **Input Validation**: All DTOs will be validated using Jakarta Bean Validation to prevent invalid data and protect against injection attacks.

## 6. Key Business Logic & Rules

*   **Transactional Integrity**: All operations that modify an account's balance (create, update, delete transaction) must be performed within a single database transaction (`@Transactional`). The `ACCOUNTS.balance` field will be updated atomically.
*   **Transfer Logic**: A transfer is an atomic operation that creates two `TRANSACTION` records (an expense from the source account, an income to the destination account) linked by a `transfer_group_id`. If one fails, both are rolled back.
*   **Soft Deletes**: Deleting core entities like transactions, accounts, and categories will use a soft-delete pattern (setting the `deleted_at` timestamp). This preserves historical data and allows for restoration.
*   **Balance Reconciliation**: An internal mechanism (e.g., a scheduled job or an admin endpoint) should be available to reconcile account balances by recalculating them from the transaction history, ensuring data integrity.

## 7. Non-Functional Requirements

*   **Performance**: Endpoints returning lists must be paginated. Heavy query columns must be indexed. High-traffic, read-heavy endpoints (like dashboard reports) should be cached (e.g., using Redis).
*   **Testability**: The application must have a high level of test coverage.
    *   **Unit Tests**: For services and utility classes (JUnit, Mockito).
    *   **Integration Tests**: For API controllers and repositories, using `Testcontainers` to spin up a real PostgreSQL instance.
*   **Observability**:
    *   **Logging**: Structured logging (JSON) will be implemented for easier parsing and analysis.
    *   **Monitoring**: Expose application metrics via Spring Boot Actuator for monitoring with tools like Prometheus.

## 8. Appendix

### 8.1. Enum Definitions

*   **AccountType**: `CASH`, `BANK`, `EWALLET`, `CREDIT_CARD`, `INVESTMENT`
*   **CategoryType**: `INCOME`, `EXPENSE`
*   **TransactionType**: `REGULAR`, `TRANSFER`, `ADJUSTMENT`
*   **TransactionDirection**: `INCOME`, `EXPENSE`