# Frontend Architecture

The frontend is a React application built with Vite and TypeScript.

The frontend is responsible for the user interface, navigation, forms, local UI state, API communication, and presentation of backend data.

The backend remains the authoritative source for authentication, authorization, Tenant isolation, business rules, inventory, profit, and financial data.

## Application Structure

The frontend uses a feature-oriented structure.

The main structure is:

frontend/

- src/
  - app/
  - components/
  - features/
    - auth/
    - products/
    - sellers/
    - sales/
    - profit/
    - settings/
    - admin/
  - services/
  - types/
  - assets/
  - main.tsx

## App Layer

The app layer contains application-wide configuration.

It is responsible for:

- Application initialization.
- Routing.
- Global providers.
- Authentication state.
- Protected routes.
- Application-level layouts.

Business-specific functionality should remain inside feature modules.

## Feature Layer

Each business area is organized as a separate feature.

Features include:

- auth
- products
- sellers
- sales
- profit
- settings
- admin

A feature contains the UI and logic directly related to that business area.

Feature modules should remain as independent as practical.

## Components

Reusable UI components are stored in the components directory.

Components should provide reusable presentation and interaction behavior.

Examples include:

- Buttons.
- Inputs.
- Selects.
- Tables.
- Dialogs.
- Modals.
- Forms.
- Loading states.
- Error states.
- Navigation components.

Business-specific behavior should not be unnecessarily placed inside generic components.

## Services

The services layer handles communication with the backend API.

The frontend uses the configured API client to communicate with:

/api/v1

Services are responsible for:

- Sending API requests.
- Handling authentication requests.
- Processing API responses.
- Handling API errors.
- Providing feature-specific API operations where appropriate.

The frontend must not directly access the database.

## Types

TypeScript types represent frontend data structures and API contracts.

Types should correspond to the data actually returned by the backend.

Sensitive backend fields must not be assumed to exist in frontend models when the authenticated user is not authorized to receive them.

## Routing

React Router manages frontend navigation.

Routes are organized according to application functionality.

Protected routes require authentication.

Role-specific routes require the appropriate authenticated role.

Examples:

OWNER routes include:

- Dashboard.
- Products.
- Sellers.
- Sales.
- Profit.
- Settings.

SELLER routes include only functionality required for:

- Viewing permitted Products.
- Creating Sales.
- Viewing permitted Sales.

## Protected Routes

Protected routes verify that the user is authenticated before rendering protected application areas.

If the user is not authenticated, the frontend redirects to the authentication interface.

Role-specific routes must also verify the locally known user role before displaying the corresponding interface.

Frontend route protection is for user experience.

Backend authorization remains mandatory.

## Authentication State

The frontend maintains the authenticated user's application state.

Authentication state contains only the information required by the frontend.

The frontend must not store unnecessary sensitive information.

The frontend must not store passwords.

## Role-Based UI

The interface changes according to the authenticated user's role.

OWNER receives controls for Owner functionality.

SELLER receives only controls required for Seller functionality.

Examples of Seller restrictions:

- No Product management controls.
- No Seller management.
- No purchase price.
- No profit information.
- No manual inventory adjustment controls.

These UI restrictions do not replace backend authorization.

## Product Interface

The Product feature provides the interface required for Product operations.

OWNER can:

- Create Products.
- Edit Products.
- Change Product status.
- View Product information.
- Search Products.
- Filter Products.

SELLER can view Product information required for sales.

Sensitive financial fields must not be displayed to SELLER.

## Seller Interface

The Seller feature provides the interface required for Owner Seller management.

OWNER can:

- Create Sellers.
- View Sellers.
- Manage permitted Seller information.
- Change Seller status.

SELLER does not manage other Sellers.

## Sales Interface

The Sales feature provides the interface for creating and viewing Sales.

The Seller selects Products and quantities.

The frontend may calculate temporary display values for user experience.

The backend remains responsible for:

- Product validation.
- Inventory validation.
- Actual selling price.
- Purchase price.
- Profit.
- Final total.
- Sale creation.

The frontend must use the backend result as the authoritative transaction result.

## Profit Interface

The Profit feature is available only to authorized OWNER users.

The interface may display:

- Sale profit.
- Total profit.
- Profit-related statistics.

SELLER must not receive or display profit information.

The frontend must not attempt to calculate or reconstruct restricted profit information.

## Inventory Interface

Inventory information is displayed according to role permissions.

OWNER can access authorized inventory management functionality.

SELLER can view available inventory required for sales.

Inventory changes are performed through backend operations.

The frontend must not directly modify Product quantities.

## Error Handling

The frontend must handle API errors consistently.

The interface should provide clear feedback for:

- Invalid input.
- Authentication failure.
- Unauthorized access.
- Missing resources.
- Insufficient inventory.
- Business conflicts.
- Server errors.

Raw backend stack traces and internal errors must never be displayed.

## Loading States

API-driven interfaces must provide appropriate loading states.

The user should receive feedback while:

- Loading Products.
- Loading Sellers.
- Loading Sales.
- Creating Sales.
- Updating Products.
- Updating inventory.
- Performing other asynchronous operations.

## Empty States

The interface should provide appropriate empty states when no data is available.

Examples:

- No Products.
- No Sellers.
- No Sales.
- No search results.
- No profit data.

Empty states must not be confused with API errors.

## Forms

Forms must provide client-side validation for user experience.

Validation may check:

- Required fields.
- Basic formats.
- Quantity values.
- Price values.
- Input length.

The backend performs the authoritative validation.

## API Data

The frontend must use backend responses as the source of truth.

The frontend must not assume that:

- Inventory is available.
- A Product belongs to the current Tenant.
- A Sale was completed.
- A price is valid.
- A profit value is correct.

These values must be confirmed by the backend.

## State Management

Application state should be kept as simple as possible.

Local component state should be used for local UI behavior.

Shared application state should be used only when multiple parts of the application require the same information.

Server data should be handled through the project's configured API/data-fetching approach.

The frontend must avoid unnecessary duplication of backend state.

## Security Boundary

The frontend is not a security boundary.

Users can inspect and modify frontend code and network requests.

Therefore, the frontend must never be trusted to enforce:

- Authentication.
- Authorization.
- Tenant isolation.
- Inventory rules.
- Profit permissions.
- Financial calculations.

All security-sensitive rules must be enforced by the backend.

## Frontend Principle

The frontend provides the user experience and communicates with the REST API.

It presents data according to the authenticated user's role and provides appropriate interaction flows.

The backend remains the single authoritative source for security, business logic, financial calculations, inventory, transactions, and Tenant isolation.