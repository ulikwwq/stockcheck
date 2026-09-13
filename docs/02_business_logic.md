# Business Logic

The platform manages the complete business process from business creation to product sales and inventory updates.

The Super Admin creates a Tenant representing an independent business.

When a Tenant is created, an Owner account is created and assigned to that Tenant.

A default Shop is created for the Tenant.

The Owner receives login credentials and can access the application.

The Owner manages products belonging to the Tenant's Shop.

A product contains a name, description, image, purchase price, default sale price, quantity, and status.

The Owner can create and update products and manage their inventory.

The Owner can create Seller accounts.

A Seller is automatically assigned to the same Tenant as the Owner who created the account.

The Seller can view available products and their selling prices.

When a Seller wants to sell a product, the Seller starts a sale operation.

The system verifies the authenticated Seller.

The system verifies the Seller's Tenant.

The system verifies that the Seller has permission to perform the sale.

The system verifies that the product belongs to the Seller's Tenant.

The system verifies that sufficient inventory is available.

The default selling price is taken from the product.

The Seller may use the default selling price or specify a custom selling price for the specific transaction.

The actual selling price used for the transaction is stored in the SaleItem.

The purchase price used for the transaction is also stored in the SaleItem.

The system creates a Sale.

The system creates one or more SaleItems associated with the Sale.

The system calculates the profit using the purchase price, actual selling price, and quantity.

The system decreases the product inventory.

The system creates a StockMovement representing the inventory change.

The system creates an AuditLog representing the important business action.

The complete sale operation is performed as one atomic transaction.

If any required operation fails, the complete transaction must be rolled back.

A sale must never be created while the corresponding inventory update fails.

Inventory must never become negative.

If the available quantity is lower than the requested sale quantity, the sale must be rejected.

Historical transaction information must remain unchanged after the sale is completed.

Changing the current purchase price or default selling price of a product must not change previously completed SaleItems.

Every important inventory change must be traceable through StockMovement records.

Every important system operation must be traceable through AuditLog records.

The system must preserve enough historical information to determine which user performed an action, what entity was affected, what changed, and when the action occurred.

The business logic must always enforce Tenant isolation.

Users must only operate on resources belonging to their authenticated Tenant.

The frontend must not be trusted to determine the Tenant of an operation.

The backend must determine the Tenant from the authenticated user's security context.

The business logic must enforce role restrictions.

SUPER_ADMIN manages platform-level businesses and Owner accounts.

OWNER manages the business, products, Sellers, inventory, sales, profit, audit history, and business settings.

SELLER performs sales and can access only the information required for their permitted operations.

SELLER must not access purchase prices or profit.

SELLER must not create or modify products.

SELLER must not manually modify inventory.

SELLER must not create other users.

The business logic must maintain data integrity across all related entities.

Sales, inventory changes, stock movements, and audit records must remain consistent.

Concurrent sales must be handled safely so that two Sellers cannot successfully sell the same final unit of inventory.

All business rules must be enforced by the backend.

The frontend may provide user interface restrictions, but frontend restrictions are not considered security or business-rule enforcement.