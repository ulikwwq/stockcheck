# Testing

The project must include automated tests for the backend and frontend.

Testing is required to verify business logic, security, Tenant isolation, API behavior, inventory consistency, and frontend behavior.

## Backend Testing

The backend uses tests appropriate for:

- Unit testing.
- Service testing.
- Repository testing.
- API testing.
- Security testing.
- Integration testing.

## Unit Tests

Unit tests verify isolated business logic.

Important areas include:

- Profit calculation.
- Product validation.
- Sale validation.
- Inventory validation.
- Role permissions.
- Business rules.
- Error handling.

Unit tests must cover both successful and unsuccessful scenarios.

## Service Tests

Service tests verify application and business logic.

Important scenarios include:

- Creating a Product.
- Updating a Product.
- Creating a Seller.
- Creating a Sale.
- Increasing inventory.
- Adjusting inventory.
- Calculating profit.
- Creating StockMovements.
- Creating AuditLogs.

Services must be tested with valid and invalid inputs.

## Authentication Tests

Authentication tests must verify:

- Successful login.
- Invalid username.
- Invalid password.
- Inactive user.
- Invalid authentication.
- Protected endpoint without authentication.

Passwords must never be exposed in test output.

## Authorization Tests

Authorization tests must verify role restrictions.

Examples:

- OWNER can create Products.
- SELLER cannot create Products.
- OWNER can manage Sellers.
- SELLER cannot manage Sellers.
- OWNER can access profit.
- SELLER cannot access profit.
- SELLER can create Sales.
- Unauthorized users cannot perform protected operations.

## Tenant Isolation Tests

Tenant isolation must be tested explicitly.

Examples:

- Tenant A Owner can access Tenant A Products.
- Tenant A Owner cannot access Tenant B Products.
- Tenant A Owner cannot access Tenant B Sales.
- Tenant A Owner cannot access Tenant B Sellers.
- Tenant A Owner cannot access Tenant B Profit.
- Tenant A Owner cannot access Tenant B AuditLogs.
- Tenant A Seller cannot access Tenant B Products.
- Tenant A Seller cannot create a Sale using a Tenant B Product.

Tests must verify that changing a Tenant ID in the request cannot bypass Tenant isolation.

## Product Tests

Product tests must verify:

- Product creation.
- Required fields.
- Invalid prices.
- Invalid quantities.
- Product editing.
- Product status changes.
- Product retrieval.
- Product search.
- Product filtering.
- Tenant ownership.
- Role restrictions.

## Seller Tests

Seller tests must verify:

- Seller creation.
- Automatic Tenant assignment.
- Automatic SELLER role assignment.
- Seller authentication.
- Seller status.
- Seller permissions.
- Seller Tenant isolation.

The backend must prevent an Owner from creating a Seller with an unauthorized role.

## Sales Tests

Sales tests are critical.

Tests must verify:

- Successful Sale.
- Multiple SaleItems where supported.
- Default sale price.
- Custom sale price.
- Quantity validation.
- Insufficient inventory.
- Product status.
- Tenant ownership.
- Seller ownership.
- Profit calculation.
- Inventory decrease.
- StockMovement creation.
- AuditLog creation.

## Transaction Tests

Tests must verify transaction atomicity.

If any required operation fails, all related database changes must be rolled back.

Examples:

- Sale creation fails → inventory must not decrease.
- Inventory update fails → Sale must not remain.
- SaleItem creation fails → Sale must not remain.
- Required StockMovement creation fails → Sale transaction must not remain.
- Required AuditLog creation fails → Sale transaction must not remain.

## Inventory Tests

Inventory tests must verify:

- Initial quantity.
- Stock increase.
- Stock adjustment.
- Sale decrease.
- Negative inventory prevention.
- Insufficient inventory.
- Correct StockMovement creation.
- Tenant isolation.
- Authorization.

## Concurrency Tests

The system must test concurrent Sales against the same Product.

A concurrency test must verify that simultaneous Sales cannot create negative inventory.

If only one unit is available and multiple Sellers attempt to sell it simultaneously, only valid transactions may succeed.

## Profit Tests

Profit tests must verify:

- Correct profit calculation.
- Correct quantity calculation.
- Default selling price.
- Custom selling price.
- Historical purchase price.
- Historical selling price.
- Historical profit preservation.

Changing current Product prices after a Sale must not change historical profit.

## Audit Tests

Audit tests must verify:

- Correct User ID.
- Correct Tenant ID.
- Correct action.
- Correct entity type.
- Correct entity ID.
- Appropriate old value.
- Appropriate new value.
- Correct transaction behavior.

Sensitive information must never appear in AuditLogs.

## API Tests

API tests must verify:

- Correct HTTP status codes.
- Request validation.
- Response structure.
- Authentication.
- Authorization.
- Tenant isolation.
- Error responses.
- Business errors.

The API must reject malformed and unauthorized requests.

## Frontend Testing

The frontend must be tested for important user-facing behavior.

Tests should verify:

- Login interface.
- Authentication state.
- Role-based navigation.
- Protected routes.
- Product interface.
- Seller interface.
- Sales interface.
- Profit visibility.
- Form validation.
- API error handling.

## Seller Interface Tests

Frontend tests must verify that SELLER does not receive UI controls for unauthorized operations.

Examples:

- No Product creation control.
- No Product editing control.
- No Seller management.
- No purchase price display.
- No profit display.

These tests improve user experience but do not replace backend authorization tests.

## Owner Interface Tests

Frontend tests must verify that OWNER can access:

- Products.
- Product management.
- Sellers.
- Sales.
- Profit.
- Relevant settings.

## Test Data

Tests must use isolated test data.

Test data must not depend on production data.

Tenant-specific tests should create separate Tenants to verify isolation.

## Test Environment

Automated tests must use a dedicated test environment.

Production credentials must never be used for tests.

Test configuration must use separate database and authentication configuration.

## Regression Testing

When a bug is fixed, an automated test should be added when practical.

Important security and business rules must have regression tests.

Examples include:

- Tenant isolation bypass.
- Negative inventory.
- Unauthorized profit access.
- Duplicate sales.
- Historical profit modification.

## Testing Principle

Tests must verify not only that valid operations work, but also that invalid and unauthorized operations fail safely.

Security, Tenant isolation, inventory consistency, transactions, historical data, and business rules are critical testing areas.

A feature is not considered complete until its important success and failure scenarios are covered.