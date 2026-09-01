# API Conventions

The backend exposes a REST API for communication with the frontend.

The API is the authoritative interface for application data and business operations.

The frontend must communicate with the backend through the defined API.

## API Base Path

All application API endpoints use the base path:

/api/v1

The API version must remain explicit.

Breaking API changes require a new API version.

## HTTP Methods

The API uses standard HTTP methods.

GET is used for retrieving resources.

POST is used for creating resources or executing business operations that create a new result.

PUT is used for replacing or updating resources when appropriate.

PATCH is used for partial updates when appropriate.

DELETE is used only where deletion is explicitly allowed by the business rules.

Historical business records such as completed Sales and StockMovements must not be deleted through normal API operations.

## Resource Naming

API paths use resource-oriented names.

Examples:

/api/v1/products

/api/v1/sellers

/api/v1/sales

/api/v1/inventory

/api/v1/audit-logs

Resource names should use plural nouns.

Paths should remain consistent throughout the application.

## Authentication

Protected API endpoints require authentication.

The backend obtains the authenticated user from the Spring Security security context.

The client must not be trusted to identify the authenticated user through request parameters.

## Authorization

Every protected endpoint must enforce authorization.

Authorization must consider:

- User role.
- User status.
- Tenant ownership.
- Resource ownership.
- Operation permissions.

Frontend route protection does not replace backend authorization.

## Tenant Context

Tenant context is determined by the authenticated user.

The backend must not trust a Tenant ID supplied by the frontend for authorization.

The backend must verify that requested resources belong to the authenticated user's Tenant.

## DTOs

The API uses DTOs for request and response data.

Database entities must not be exposed directly when doing so could reveal internal or sensitive fields.

Request DTOs define the data accepted by the backend.

Response DTOs define the data returned to the frontend.

## Request Validation

All request DTOs must be validated on the backend.

Validation must verify:

- Required fields.
- Data types.
- Value ranges.
- String lengths.
- Quantity values.
- Price values.
- Business-specific requirements.

Client-side validation is supplementary only.

## Response Structure

Successful responses must use a consistent structure appropriate to the endpoint.

Collection endpoints should provide a predictable collection representation.

Single-resource endpoints should return the requested resource representation.

Business-operation endpoints should return the resulting resource or operation result.

## Error Structure

API errors must use a consistent error structure.

An error response should contain:

- HTTP status.
- Error code.
- Human-readable message.
- Timestamp.
- Request or correlation identifier when available.
- Validation details when applicable.

Internal implementation details must not be exposed.

## Pagination

Collection endpoints that can return large amounts of data should support pagination.

Pagination parameters must remain consistent across endpoints.

The API should provide enough information for the frontend to determine:

- Current page.
- Page size.
- Total elements.
- Total pages.

The exact response structure must remain consistent.

## Filtering

Collection endpoints may support filtering where required.

Filtering must be performed by the backend.

Examples include:

- Product status.
- Product category.
- Seller.
- Sale date.
- Inventory state.

Filtering must always respect Tenant isolation and authorization.

## Sorting

Collection endpoints may support sorting where required.

Sorting parameters must be validated against supported fields.

The client must not be able to request arbitrary database fields or expressions.

## Searching

Search functionality must be implemented on the backend.

Search parameters must be validated.

Search results must remain restricted to resources accessible to the authenticated user.

## Product API

Product endpoints support operations required for:

- Creating Products.
- Viewing Products.
- Updating Products.
- Changing Product status.
- Searching Products.
- Filtering Products.

Only authorized OWNER users can perform Product management operations.

SELLER can access only the Product information required for sales.

## Seller API

Seller endpoints support operations required for:

- Creating Sellers.
- Viewing Sellers.
- Updating permitted Seller information.
- Changing Seller status.

Seller management is restricted to authorized OWNER users.

A Seller cannot manage other Sellers.

## Sales API

Sales endpoints support:

- Creating Sales.
- Viewing authorized Sales.
- Viewing Sale details.

When creating a Sale, the backend determines:

- Authenticated Seller.
- Tenant.
- Product ownership.
- Purchase price.
- Actual sale price.
- Profit.
- Inventory availability.

The frontend must not be trusted to provide authoritative values for these fields.

## Inventory API

Inventory endpoints support authorized inventory operations.

OWNER can perform authorized inventory management operations.

SELLER can view inventory required for sales.

Inventory changes must be performed through controlled backend operations.

Direct client modification of Product quantity is not allowed.

## Profit API

Profit endpoints provide profit information only to authorized users.

OWNER can access profit belonging to their Tenant.

SELLER cannot access purchase prices or profit information.

The API must not return restricted fields to SELLER.

## Audit API

Audit endpoints provide authorized audit history.

Audit history must remain restricted according to role and Tenant.

The API must not expose sensitive security information stored in or associated with audit operations.

## Status Codes

The API uses appropriate HTTP status codes.

Common responses include:

200 OK

201 Created

204 No Content

400 Bad Request

401 Unauthorized

403 Forbidden

404 Not Found

409 Conflict

422 Unprocessable Entity

500 Internal Server Error

The same type of operation should use consistent status codes throughout the API.

## Business Errors

Business rule failures must return a predictable error code and appropriate HTTP status.

Examples:

- Insufficient inventory.
- Inactive Product.
- Inactive Seller.
- Unauthorized operation.
- Resource conflict.
- Invalid business state.

The frontend uses these responses to provide meaningful feedback.

## Security

API responses must contain only data the authenticated user is authorized to receive.

Sensitive fields must not be included merely because they exist in the database entity.

The backend must prevent unauthorized access through modified:

- User IDs.
- Seller IDs.
- Tenant IDs.
- Product IDs.
- Sale IDs.
- Other resource identifiers.

## Transactions

Business operations that modify multiple related resources must be transactional.

Sale creation must atomically coordinate:

- Sale.
- SaleItems.
- Inventory.
- StockMovements.
- Profit.
- AuditLogs.

## API Documentation

The API should be documented using the project's configured API documentation approach.

Documentation must describe:

- Endpoints.
- HTTP methods.
- Request parameters.
- Request bodies.
- Response structures.
- Authentication requirements.
- Authorization requirements.
- Error responses.

Documentation must remain synchronized with the implementation.

## API Principle

The REST API is the controlled boundary between the frontend and backend.

The backend owns authentication, authorization, Tenant isolation, validation, business rules, financial calculations, inventory operations, transactions, and data integrity.

The frontend consumes the API and must never be treated as a trusted source of business-critical information.