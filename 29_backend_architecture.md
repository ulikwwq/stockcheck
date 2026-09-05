# Backend Architecture

The backend is a Spring Boot application responsible for the REST API, authentication, authorization, business logic, database access, transactions, inventory, profit, and audit operations.

The backend is the authoritative source of application data and business rules.

## Architecture

The backend follows a layered architecture.

The main layers are:

- Controller.
- Service.
- Repository.
- Entity.
- DTO.
- Mapper.
- Security.
- Exception handling.

Each layer has a defined responsibility.

## Controller Layer

Controllers expose REST API endpoints.

Controllers are responsible for:

- Receiving HTTP requests.
- Validating request DTOs.
- Calling application services.
- Returning HTTP responses.

Controllers must not contain complex business logic.

Controllers must not directly access the database.

## Service Layer

Services contain application and business logic.

Services are responsible for:

- Business validation.
- Authorization-related business checks.
- Tenant ownership checks.
- Coordinating repositories.
- Managing transactions.
- Performing calculations.
- Creating related records.

Important business operations such as Sale creation must be implemented at the service level.

## Repository Layer

Repositories provide database access.

Repositories are responsible for:

- Reading entities.
- Saving entities.
- Updating entities.
- Querying required data.

Repositories must not contain application-level business rules.

Queries must respect Tenant isolation where required.

## Entity Layer

Entities represent persistent database structures.

Entities correspond to the PostgreSQL schema.

Entities should contain persistence relationships and data required by the domain model.

Entities must not be returned directly through API responses when doing so could expose internal or sensitive fields.

## DTO Layer

DTOs define API request and response structures.

Request DTOs contain data accepted from the client.

Response DTOs contain data returned to the client.

DTOs provide a controlled API boundary and prevent accidental exposure of database fields.

Different roles may require different response DTOs.

## Mapper Layer

Mappers convert between:

- Entity and DTO.
- DTO and Entity where appropriate.

Mapping logic should remain separate from controllers and repositories.

Sensitive entity fields must not be accidentally included in response DTOs.

## Security Layer

Spring Security controls authentication and authorization.

The security layer is responsible for:

- Authentication.
- Authenticated user context.
- Role-based access.
- Protected endpoints.

Business-level Tenant ownership checks remain required after authentication.

Authentication alone does not prove access to a specific resource.

## Tenant Context

The authenticated user's Tenant is the authoritative Tenant context.

The backend must obtain the Tenant through the authenticated user and related database relationships.

Client-provided Tenant identifiers must not be trusted for authorization.

Services must verify Tenant ownership before accessing or modifying Tenant-specific resources.

## Transaction Management

Transactional business operations are managed at the service layer.

The Sale operation must execute as one database transaction.

The transaction coordinates:

- Sale.
- SaleItems.
- Inventory.
- StockMovement.
- Profit.
- AuditLog.

If the operation fails, the required database changes must be rolled back.

## Business Logic

Business rules belong in the backend service layer.

Examples include:

- Seller can create Sales.
- Owner can manage Products.
- Seller cannot manage Products.
- Seller cannot access Profit.
- Inventory cannot become negative.
- Product must belong to the authenticated Tenant.
- Sale quantity must be available.
- Historical prices must be stored with SaleItems.

The frontend must not be the authoritative implementation of these rules.

## Authentication Context

Services must obtain the authenticated user from the security context.

The backend must not trust request fields for:

- User ID.
- Seller ID.
- Tenant ID.
- Role.

These values must be derived from the authenticated context and database relationships.

## Error Handling

The backend uses centralized exception handling.

Expected business exceptions should be converted into consistent API error responses.

Unexpected exceptions must be handled safely.

Controllers should not contain repetitive exception-handling logic.

## Validation

Request validation occurs at the API boundary.

Business validation occurs inside the service layer.

Both levels are required.

Example:

Request validation verifies that quantity is a valid positive number.

Business validation verifies that sufficient inventory exists.

## Database Access

The backend communicates with PostgreSQL through the configured persistence layer.

Database access must remain inside repositories and the persistence architecture.

Controllers must never execute database queries directly.

## Security of Responses

Response DTOs must contain only information authorized for the authenticated user.

SELLER responses must not expose:

- Purchase price.
- Profit.
- Other restricted business information.

OWNER responses may contain information permitted by the Owner business rules.

## Feature Separation

Backend functionality is grouped by business feature.

The main features are:

- Auth.
- Tenant.
- User.
- Product.
- Sale.
- Inventory.
- Audit.

Feature-specific logic should remain within its feature package.

Shared functionality belongs in common infrastructure.

## Dependency Direction

The architecture must maintain clear dependencies.

Controllers depend on services.

Services depend on repositories and required supporting components.

Repositories depend on persistence infrastructure.

Entities represent persistent data.

Controllers must not bypass the service layer to access repositories directly for business operations.

## API Boundary

The backend REST API is the only application boundary used by the frontend.

The frontend must not communicate directly with PostgreSQL.

The frontend must not access backend internal classes.

All business operations must pass through the backend API.

## Backend Principle

The backend is the authoritative application layer.

It controls:

- Authentication.
- Authorization.
- Tenant isolation.
- Validation.
- Business rules.
- Financial calculations.
- Inventory.
- Transactions.
- Audit history.
- Database integrity.

The frontend is a client of the backend and must not be treated as a trusted source of business-critical information.