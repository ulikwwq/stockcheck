# Roles and Permissions

The system contains three user roles.

- SUPER_ADMIN
- OWNER
- SELLER

Every authenticated user belongs to exactly one role.

Every Owner and Seller belongs to exactly one Tenant.

SUPER_ADMIN operates outside the normal Tenant structure.

## SUPER_ADMIN

The Super Admin manages the platform itself.

Responsibilities:

- Create Tenants.
- Create Owner accounts.
- Activate Tenants.
- Suspend Tenants.
- Manage Owner accounts.
- Reset Owner passwords.
- View all Tenants.
- Manage platform-level settings.
- View platform-level logs.

Restrictions:

- Does not automatically act as an Owner inside every Tenant.
- Does not directly manage operational business data unless explicitly implemented.

## OWNER

The Owner manages one specific business.

Responsibilities:

- View products.
- Create products.
- Edit products.
- Manage inventory quantities.
- Set purchase prices.
- Set default selling prices.
- Add product descriptions.
- Add product images.
- Create Seller accounts.
- Manage Seller accounts.
- View sales.
- View profit.
- View inventory.
- View audit history for the Tenant.
- Manage business settings.

Restrictions:

- Cannot create another Owner.
- Cannot create a Super Admin.
- Cannot change Tenant assignment.
- Cannot access another Tenant's data.

## SELLER

The Seller performs sales for one business.

Responsibilities:

- View products.
- View available inventory.
- View selling prices.
- Create sales.
- Use the default selling price.
- Enter a custom selling price for a specific sale.
- View permitted sales information.

Restrictions:

- Cannot view purchase prices.
- Cannot view profit.
- Cannot create products.
- Cannot edit products.
- Cannot manually adjust inventory.
- Cannot create Sellers.
- Cannot create Owners.
- Cannot access business settings.
- Cannot access another Tenant.

## Permission Model

Permissions are enforced by the backend.

Every protected operation must verify:

- Authenticated user.
- User role.
- User Tenant.
- Resource ownership.

Frontend visibility does not replace backend authorization.

Hidden interface elements are only user interface behavior.

The backend remains the final authority for every protected action.

## Role Hierarchy

SUPER_ADMIN

- Platform management.
- Tenant creation.
- Owner management.

OWNER

- Business management.
- Product management.
- Inventory management.
- Seller management.
- Sales visibility.
- Profit visibility.
- Audit visibility.

SELLER

- Product visibility.
- Inventory visibility.
- Sales creation.
- Custom sale pricing for individual transactions.

The permission model is based on role separation and strict Tenant isolation.