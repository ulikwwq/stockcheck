# Security

The system must protect authentication, authorization, Tenant isolation, business data, and sensitive information.

Security is enforced primarily by the backend.

The frontend is not a security boundary.

## Authentication Security

Users authenticate using username and password.

Passwords must never be stored as plaintext.

Only secure password hashes are stored in the database.

Passwords and password hashes must never be returned by the API.

Passwords must never be written to logs.

Authentication must be handled by Spring Security.

## Authorization Security

Every protected backend operation must verify authorization.

Authorization must consider:

- Authenticated user.
- User role.
- User Tenant.
- Resource ownership.

The backend must reject unauthorized operations even when the frontend does not expose the corresponding action.

## Tenant Isolation

Tenant isolation is mandatory.

A user belonging to one Tenant must never access another Tenant's protected resources.

Tenant isolation applies to:

- Users.
- Shops.
- Products.
- Sales.
- SaleItems.
- Inventory.
- StockMovements.
- Profit.
- AuditLogs.

The authenticated user's Tenant is the source of truth.

A client-provided Tenant ID must never override the authenticated Tenant context.

## Resource Access

Resource identifiers supplied by the client must not be considered proof of ownership.

Before accessing or modifying a resource, the backend must verify that the resource belongs to the authenticated user's Tenant and that the user's role permits the operation.

This applies to every protected resource.

## Role Security

The system uses:

- SUPER_ADMIN
- OWNER
- SELLER

The backend must enforce role permissions.

SELLER must not gain access to:

- Purchase prices.
- Profit.
- Product management.
- Seller management.
- Manual inventory management.

OWNER must not gain access to another Tenant.

SUPER_ADMIN has platform-level permissions according to the platform authorization model.

## Sensitive Business Data

The following information is considered sensitive:

- Purchase prices.
- Profit.
- Business information.
- Inventory information.
- Sales information.
- Audit history.
- Authentication information.

Sensitive information must only be returned to authorized users.

SELLER API responses must not contain purchase prices or profit.

## API Security

All protected API endpoints must require authentication.

API requests must be validated by the backend.

The backend must not trust client-provided:

- User IDs.
- Seller IDs.
- Tenant IDs.
- Roles.
- Permissions.
- Profit values.
- Inventory values.
- Sale totals.

Authoritative values must be determined by the backend.

## Input Validation

All external input must be validated.

Validation applies to:

- Authentication requests.
- Product data.
- Seller data.
- Sale data.
- Inventory operations.
- Settings.
- Other API requests.

The backend must never assume that frontend validation is sufficient.

## Financial Data Integrity

The backend must calculate authoritative financial values.

The client must not be trusted to provide:

- Final sale total.
- Purchase price.
- Profit.

Historical purchase price and actual sale price must be stored in SaleItem.

Historical profit must remain protected from later Product price changes.

## Inventory Security

Inventory operations must be authorized and transactional.

SELLER cannot manually modify inventory.

Inventory must never become negative.

Concurrent sales must be protected against race conditions.

The backend must validate available inventory immediately before committing the transaction.

## Transaction Security

Critical business operations must be transactional.

The Sale operation must maintain consistency between:

- Sale.
- SaleItems.
- Inventory.
- StockMovements.
- Profit.
- AuditLogs.

A failed operation must not leave partial business data.

## Audit Security

Important actions must be recorded in AuditLogs.

Audit logs must identify:

- User.
- Tenant.
- Action.
- Entity.
- Time.

Audit logs must not contain passwords, password hashes, tokens, credentials, or other sensitive security secrets.

Audit history must be protected from unauthorized access.

## Secret Management

Sensitive configuration must be provided through environment variables.

Secrets must not be hardcoded in source code.

Secrets must not be committed to Git.

Secrets must not be included in frontend code.

Secrets must not be written to logs.

Sensitive configuration includes:

- Database credentials.
- Authentication secrets.
- Storage credentials.
- Other private application configuration.

## HTTPS

Production communication must use HTTPS.

Authentication credentials and protected API communication must not be transmitted over insecure production connections.

## CORS

CORS must be configured to allow only the required frontend origins.

Development origins must not automatically be allowed in production.

CORS is not a replacement for authentication or authorization.

## Error Security

Error responses must not expose internal implementation details.

The API must not return:

- Stack traces.
- SQL queries.
- Database credentials.
- Internal filesystem information.
- Authentication secrets.
- Password information.

Unexpected errors must be handled centrally.

## Logging Security

Application logs must be safe for production use.

Logs must not contain:

- Passwords.
- Password hashes.
- Authentication tokens.
- Database credentials.
- Storage credentials.
- Other sensitive secrets.

Logs may contain identifiers required for troubleshooting when those identifiers do not expose sensitive information.

## Frontend Security

The frontend may hide UI elements based on role.

However, hiding an element is not authorization.

The frontend must never be trusted to enforce:

- Tenant isolation.
- Role permissions.
- Financial permissions.
- Inventory permissions.
- API access.

Every protected operation must be independently validated by the backend.

## File Upload Security

Product images must be validated before storage.

The backend must validate uploaded files according to the supported image requirements.

The system must not blindly trust the filename or MIME type supplied by the client.

Uploaded files must not be used to execute server-side code.

Storage credentials must remain private.

## Data Exposure

API responses must contain only information required by the client and permitted by the authenticated user's role.

Database entities must not be returned directly when doing so could expose sensitive fields.

DTOs must control the information exposed through the API.

## Security Testing

Security must be covered by automated tests.

Tests must verify:

- Authentication.
- Authorization.
- Tenant isolation.
- Role restrictions.
- Sensitive field protection.
- Inventory protection.
- API access control.
- Resource ownership.
- Error information exposure.

Security regressions must be treated as critical defects.

## Security Principle

Security must be enforced at every backend boundary.

The system must assume that the client can be modified or manipulated.

The backend must independently verify identity, permissions, Tenant ownership, business rules, inventory, and financial values.

No frontend restriction, request parameter, or client-side calculation may be treated as a trusted security mechanism.