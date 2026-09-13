# Database

The system uses PostgreSQL as the main relational database.

The production database is hosted by Supabase.

The database is shared between multiple Tenants.

Tenant isolation is implemented through entity relationships and backend authorization.

The database stores operational data, transactional data, inventory history, and audit history.

## Core Entities

The main database entities are:

- Tenant
- User
- Shop
- Product
- Sale
- SaleItem
- StockMovement
- AuditLog

## Tenant

The Tenant represents an independent business.

Fields:

- id
- name
- status
- created_at
- updated_at

A Tenant can have multiple Users and Shops.

## User

The User represents an authenticated system user.

Fields:

- id
- tenant_id
- username
- password_hash
- role
- status
- created_at
- updated_at

The tenant_id identifies the Tenant to which the user belongs.

SUPER_ADMIN operates outside the normal Tenant structure.

OWNER and SELLER belong to a specific Tenant.

Passwords are stored only as secure password hashes.

## Shop

The Shop represents a business location or operational shop belonging to a Tenant.

Fields:

- id
- tenant_id
- name
- status
- created_at
- updated_at

A Shop belongs to exactly one Tenant.

A Tenant can contain one or more Shops.

The initial system creates a default Shop when a Tenant is created.

## Product

The Product represents an item managed by a Shop.

Fields:

- id
- shop_id
- name
- description
- image_url
- purchase_price
- default_sale_price
- quantity
- status
- created_at
- updated_at

A Product belongs to one Shop.

The Shop determines the Tenant ownership of the Product.

Purchase price represents the current purchase price.

Default sale price represents the current default selling price.

Quantity represents the current available inventory.

Product status determines whether the product is active or inactive.

## Sale

The Sale represents a completed business transaction.

Fields:

- id
- shop_id
- seller_id
- total_amount
- created_at

A Sale belongs to one Shop.

A Sale is associated with the Seller who performed the transaction.

A Sale can contain one or more SaleItems.

## SaleItem

The SaleItem represents a specific product included in a Sale.

Fields:

- id
- sale_id
- product_id
- quantity
- purchase_price
- sale_price
- profit
- created_at

SaleItem stores the historical purchase price and actual sale price used during the transaction.

The stored prices must not depend on the current Product prices after the sale is completed.

The SaleItem stores the profit calculated for that transaction.

## StockMovement

The StockMovement represents a change in product inventory.

Fields:

- id
- product_id
- type
- quantity_change
- user_id
- created_at

Stock movements provide a historical record of inventory changes.

Examples of movement types include:

- PRODUCT_CREATED
- STOCK_INCREASE
- SALE
- STOCK_ADJUSTMENT

The movement must identify the product, the user responsible for the operation, the quantity change, and the time of the operation.

## AuditLog

The AuditLog represents an important system action.

Fields:

- id
- tenant_id
- user_id
- action
- entity_type
- entity_id
- old_value
- new_value
- created_at

Audit logs are used to determine who performed an action, what entity was affected, what changed, and when the action occurred.

Audit records are historical records and should not be casually deleted.

## Relationships

Tenant has Users.

Tenant has Shops.

Shop has Products.

Shop has Sales.

Sale has SaleItems.

Product has StockMovements.

Tenant has AuditLogs.

SaleItem references Product.

Sale references Seller.

Seller is represented by User with the SELLER role.

## Tenant Ownership

Tenant ownership must be preserved through database relationships.

Users contain tenant_id.

Shops contain tenant_id.

Products belong to Shops.

Sales belong to Shops.

AuditLogs contain tenant_id.

The backend must use these relationships when checking access to data.

## Historical Data

Current Product prices represent the current state of the product.

Historical transaction prices are stored separately in SaleItem.

Changing a Product purchase price must not modify previous SaleItems.

Changing a Product default sale price must not modify previous SaleItems.

Completed sales must preserve their original transaction data.

## Inventory Data

Product.quantity represents the current available inventory.

Inventory changes must be represented by StockMovement records.

A completed sale decreases Product.quantity and creates a corresponding StockMovement.

Inventory must never become negative.

Inventory-related operations must be executed consistently with the corresponding business transaction.

## Data Integrity

The database must enforce appropriate relationships and constraints.

Foreign key relationships must be used where required.

Required fields must not allow invalid null values.

Quantities must not become negative.

Prices must contain valid values.

Usernames must follow the uniqueness rules required by the authentication model.

Tenant relationships must remain consistent.

Historical transaction data must remain intact.

## Database Access

The backend uses:

- Spring Data JPA
- Hibernate
- PostgreSQL

Database schema changes are managed through Flyway migrations.

Production database structure must not be modified manually without a corresponding migration.

## Database Isolation Principle

The database is shared by multiple Tenants.

Data isolation is logical rather than database-per-Tenant isolation.

The backend must ensure that users can only access data belonging to their authenticated Tenant.

Database relationships and backend authorization must work together to maintain Tenant isolation.

The database design must support the complete business workflow while preserving security, historical data, inventory consistency, and transactional integrity.