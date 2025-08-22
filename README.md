# Finance Tracker - Backend Technical Specification

| **Version** | 1.1 (As Built) |
| **Status** | Implemented |
| **Last Updated** | August 21, 2025 |
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
This document provides a comprehensive technical specification for the backend services of the Finance Tracker application. It reflects the "as-built" state of the project, serving as a single source of truth for the current implementation and a guide for future development.

### 1.2. Project Goal
The primary objective is to build a robust, scalable, and secure backend foundation using Spring Boot. This system powers a personal finance management application, focusing on data integrity, performance, and a clean, maintainable architecture.

### 1.3. Core Features
*   User Authentication & Authorization (JWT with Refresh Tokens)
*   Multi-Account Management
*   Categorization of Income and Expenses
*   Transaction Recording (Income, Expense, Transfer)
*   File Attachment Upload (e.g., receipts) linked to Transactions
*   Monthly Budgeting with proactive validation
*   Recurring Transaction Scheduling via CRON jobs
*   Reporting & Dashboard Analytics (Overview, Category Breakdown, Cash Flow Trend)

## 2. System Architecture

### 2.1. Architectural Style
The system is built following the principles of a layered architecture, separating concerns into Presentation (API), Application (Service), Domain, and Infrastructure layers.

*   **API**: Controllers, DTOs, Mappers, and Exception Handlers.
*   **Application**: Service interfaces and implementations containing core business logic.
*   **Domain**: Core business entities and enums.
*   **Infrastructure**: Implementation of external concerns like database persistence (JPA), security (Spring Security), and scheduling.

### 2.2. Technology Stack
*   **Framework**: Spring Boot 3.3.3
*   **Language**: Java 21
*   **Database**: PostgreSQL 15+
*   **Database Migrations**: Flyway
*   **Authentication**: Spring Security 6, JWT (jjwt library)
*   **API Documentation**: SpringDoc OpenAPI 3 (Swagger UI)
*   **Data Mapping**: MapStruct
*   **Validation**: Jakarta Bean Validation (Hibernate Validator)
*   **Caching**: Spring Cache Abstraction with In-Memory provider (ConcurrentMapCache)
*   **Build Tool**: Maven

### 2.3. Project Structure
The project structure reflects the layered architecture:
```
com.eris.fintrack
├─ api/                 # Controllers, DTOs, Mappers, Exceptions
│  ├─ account/
│  ├─ attachment/
│  ├─ auth/
│  ├─ budget/
│  ├─ category/
│  ├─ common/
│  ├─ recurring/
│  ├─ report/
│  └─ transaction/
├─ application/         # Service layer
│  └─ service/
│     ├─ implementation/  # Service class implementations
│     └─ *.java           # Service interfaces
├─ domain/              # Core business entities
│  └─ enums/
├─ infrastructure/      # Implementation of external concerns
│  ├─ config/
│  ├─ persistence/      # JPA Repositories
│  └─ scheduler/
└─ FintrackApplication.java
```

## 3. Data Model & Database Design

### 3.1. Entity-Relationship Diagram (ERD)
This ERD represents the final implemented database schema.

```mermaid
erDiagram
    USERS {
        UUID id PK
        string email UK
        string password_hash
        string full_name
        timestamp created_at
        timestamp updated_at
    }
    ROLES {
        integer id PK
        string name UK "e.g., ROLE_USER"
    }
    USER_ROLES {
        UUID user_id PK, FK
        integer role_id PK, FK
    }
    ACCOUNTS {
        UUID id PK
        UUID user_id FK
        string name
        AccountType type "Enum: CASH, BANK, EWALLET..."
        numeric balance
        timestamp created_at
        timestamp updated_at
    }
    CATEGORIES {
        UUID id PK
        UUID user_id FK
        string name
        TransactionType type "Enum: INCOME, EXPENSE"
        timestamp created_at
        timestamp updated_at
    }
    TRANSACTIONS {
        UUID id PK
        UUID user_id FK
        UUID account_id FK
        UUID category_id FK NULL
        UUID transfer_id NULL "Links two transfer transactions"
        TransactionType type "Enum: INCOME, EXPENSE"
        numeric amount
        date transaction_date
        string description
        timestamp created_at
        timestamp updated_at
    }
    ATTACHMENTS {
        UUID id PK
        UUID transaction_id FK
        string file_name
        string mime_type
        string storage_key "Path/key for external storage"
        long file_size_bytes
        timestamp created_at
    }
    BUDGETS {
        UUID id PK
        UUID user_id FK
        UUID category_id FK NULL "NULL for overall budget"
        integer month
        integer year
        numeric amount_limit
        UNIQUE(user_id, category_id, year, month)
    }
    RECURRING_TRANSACTIONS {
        UUID id PK
        UUID user_id FK
        UUID account_id FK
        UUID category_id FK
        TransactionType type
        numeric amount
        string cron_expression
        date start_date
        date end_date NULL
        string description
        boolean is_active
        date last_execution_date NULL
    }
    REFRESH_TOKENS {
        UUID id PK
        UUID user_id FK
        string token UK
        timestamp expiry_date
    }

    USERS ||--|{ USER_ROLES : "has"
    ROLES ||--|{ USER_ROLES : "is"
    USERS ||--o{ ACCOUNTS : "manages"
    USERS ||--o{ CATEGORIES : "defines"
    USERS ||--o{ TRANSACTIONS : "performs"
    USERS ||--o{ BUDGETS : "sets"
    USERS ||--o{ RECURRING_TRANSACTIONS : "schedules"
    USERS ||--o{ REFRESH_TOKENS : "owns"
    ACCOUNTS ||--o{ TRANSACTIONS : "source of"
    CATEGORIES ||--o{ TRANSACTIONS : "classifies"
    TRANSACTIONS ||--o{ ATTACHMENTS : "includes"
```

## 4. API Specification (v1)

### 4.1. General Conventions
*   **Base Path**: `/api/v1`
*   **Authentication**: All endpoints (except `/auth/**`, `/metadata/**`, and Swagger UI) require a `Bearer <JWT>` token.
*   **Pagination**: Supported on transaction list endpoint via `page`, `size`, and `sort` parameters.
*   **Standard Response Wrapper**: All responses are wrapped in a consistent JSON structure.
    ```json
    {
      "success": true, // boolean indicating outcome
      "data": { ... }, // The actual response payload (null on failure)
      "error": null // Error object on failure (null on success)
    }
    ```
    Error object structure:
    ```json
    { "code": "ERROR_CODE", "message": "A descriptive message." }
    ```

### 4.2. API Documentation (Swagger)
The API is self-documented using SpringDoc and OpenAPI 3. The interactive Swagger UI is available when the application is running at:
*   **URL**: `http://localhost:8080/swagger-ui.html`

### 4.3. Endpoints
A summary of key implemented endpoints:
*   **Auth (`/auth`):** `POST /register`, `POST /login`, `POST /refresh`, `POST /logout`
*   **Accounts (`/accounts`):** Full CRUD (`GET`, `POST`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`)
*   **Categories (`/categories`):** Full CRUD (`GET`, `POST`, `GET /{id}`, `DELETE /{id}`)
*   **Transactions (`/transactions`):** Full CRUD, transfer, and attachment management.
    *   `GET /`, `POST /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`
    *   `POST /transfer`
    *   `POST /{id}/attachments`, `DELETE /{transactionId}/attachments/{attachmentId}`
*   **Budgets (`/budgets`):** CRUD for monthly budgets (`GET`, `POST`, `GET /{id}`, `DELETE /{id}`)
*   **Recurring Transactions (`/recurring-transactions`):** Full CRUD for managing scheduled transaction templates.
*   **Reports (`/reports`):**
    *   `GET /overview?year=&month=`
    *   `GET /category-breakdown?year=&month=`
    *   `GET /cashflow-trend?startDate=&endDate=`
*   **Metadata (`/metadata`):** Endpoints to fetch enum values (e.g., `GET /account-types`).

## 5. Security Design
*   **Authentication**: Stateless JWT access tokens (short-lived) and persistent refresh tokens (long-lived).
*   **Refresh Token Management**: Refresh tokens are stored in the database, linked to a user, and can be invalidated upon logout.
*   **Password Hashing**: BCrypt.
*   **Data Tenancy**: All business logic in the service layer enforces that users can only access their own data. Any attempt to access another user's resources results in a `ForbiddenException`.

## 6. Key Business Logic & Rules
*   **Transactional Integrity**: All operations modifying account balances are atomic (`@Transactional`).
*   **Transfer Logic**: A transfer creates two linked transaction records (one `EXPENSE`, one `INCOME`) with a shared `transfer_id`.
*   **Deletion Strategy**: The current implementation uses **hard deletes** (`DELETE FROM table`). Foreign key constraints (`ON DELETE CASCADE`) ensure data integrity.
*   **Budget Validation**: New expense transactions are validated against relevant budgets for the period. If a transaction exceeds the budget, it is rejected with a `BadRequestException`.
*   **Attachment Storage**: Attachment files are handled by an abstracted `FileStorageService`, with the primary implementation using a cloud provider (Cloudinary). The database only stores metadata.

## 7. Non-Functional Requirements
*   **Performance**:
    *   **Indexing**: Key foreign keys and columns used in filtering are indexed.
    *   **Caching**: Read-heavy, computationally expensive report endpoints (e.g., category breakdown) are cached using Spring's in-memory cache to reduce database load. The cache is manually evicted upon any data modification in the `Transaction` table.
*   **Testability**:
    *   **Unit Tests**: The application has a suite of unit tests for the service layer (e.g., `TransactionService`, `ReportService`, `Scheduler`) using JUnit 5 and Mockito to ensure business logic is correct.
    *   **Integration Tests**: A future goal is to implement end-to-end integration tests.

## 8. Appendix

### 8.1. Implemented Enums
*   **`AccountType`**: `CASH`, `BANK`, `EWALLET`, `CREDIT_CARD`, `INVESTMENT`
*   **`TransactionType`**: `INCOME`, `EXPENSE`