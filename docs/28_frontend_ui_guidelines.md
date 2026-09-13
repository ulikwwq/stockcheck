# Frontend UI Guidelines

The frontend must provide a clear, consistent, and predictable user interface.

The UI must reflect the defined roles, business processes, and API behavior.

## General Rules

The interface must be:

- Clear.
- Consistent.
- Responsive.
- Accessible.
- Easy to understand.
- Consistent across all application areas.

The UI must not contain business rules that contradict the backend.

## Layout

The application uses a common layout for authenticated users.

The layout may contain:

- Sidebar navigation.
- Header.
- Main content area.
- User information.
- Logout action.

Navigation must depend on the authenticated user's role.

## Owner Navigation

OWNER navigation provides access to authorized areas such as:

- Dashboard.
- Products.
- Sellers.
- Sales.
- Profit.
- Settings.

Only functionality permitted for OWNER should be displayed.

## Seller Navigation

SELLER navigation provides access only to functionality required for selling.

SELLER must not receive navigation items for:

- Product management.
- Seller management.
- Profit.
- Manual inventory management.
- Other restricted Owner functionality.

## Tables

Tables are used for structured business information.

Tables should provide:

- Clear column names.
- Consistent formatting.
- Loading state.
- Empty state.
- Error state.
- Pagination when required.
- Appropriate actions according to the user's role.

Sensitive fields must not be displayed to unauthorized roles.

## Product Table

The Product table may display:

- Product name.
- Category.
- Sale price.
- Available quantity.
- Status.
- Product image.
- Authorized actions.

Purchase price must not be displayed to SELLER.

OWNER can see purchase price where required by the business interface.

## Seller Table

The Seller table is available to OWNER.

It may display:

- Seller information.
- Username.
- Status.
- Creation date.
- Authorized actions.

Sensitive authentication information must never be displayed.

## Sales Table

The Sales table may display:

- Sale identifier.
- Seller.
- Total amount.
- Creation date.
- Authorized actions.

OWNER can access information permitted by the business rules.

SELLER can access only permitted sales information.

## Profit Interface

Profit information is restricted to OWNER.

The interface may display:

- Profit per Sale.
- Total profit.
- Profit statistics.
- Relevant historical information.

SELLER must not receive profit information from the backend and therefore must not display it.

## Forms

Forms must provide:

- Clear labels.
- Required-field indication.
- Validation messages.
- Loading state during submission.
- Success feedback when appropriate.
- Error feedback when submission fails.

Forms must prevent obvious invalid input on the client side.

Backend validation remains authoritative.

## Product Form

The Product form allows authorized OWNER users to enter:

- Product name.
- Description.
- Category.
- Purchase price.
- Sale price.
- Initial quantity.
- Image where supported.

The frontend must not assume that submitted values are valid.

## Seller Form

The Seller creation form allows OWNER to create a Seller.

The frontend must not allow the Owner to choose an arbitrary Tenant or unauthorized role.

The backend determines the Seller's Tenant and role according to the security model.

## Sale Form

The Sale form allows SELLER to:

- Select a Product.
- Enter quantity.
- Enter a custom sale price when permitted.
- Review the transaction.
- Submit the Sale.

The frontend may show temporary calculations for convenience.

The backend remains responsible for the authoritative:

- Inventory validation.
- Selling price.
- Purchase price.
- Profit.
- Total amount.
- Sale result.

## Inventory Feedback

The UI should clearly communicate inventory availability.

If a Sale fails because inventory is insufficient, the frontend should display the backend error in a user-friendly way.

The frontend must not assume that inventory remains available between displaying the quantity and submitting the Sale.

## Confirmation

Destructive or important operations should require appropriate confirmation.

Examples include:

- Changing Product status.
- Changing Seller status.
- Performing inventory adjustments.
- Other important administrative operations.

Confirmation dialogs must clearly explain the operation.

## Notifications

The UI should provide clear feedback for successful and unsuccessful operations.

Examples:

- Product created successfully.
- Product updated successfully.
- Seller created successfully.
- Sale completed successfully.
- Inventory updated successfully.
- Operation failed.

Notifications must not expose sensitive information.

## Loading States

Every asynchronous operation should have an appropriate loading state.

Buttons that submit requests should prevent accidental repeated submissions while the request is in progress.

The UI should clearly indicate when data is being loaded.

## Empty States

When a collection contains no records, the interface should display an informative empty state.

Examples:

- No Products found.
- No Sellers found.
- No Sales found.
- No profit data available.

Empty states should provide an appropriate next action when the user has permission to create the missing resource.

## Error States

Errors must be visually distinguishable from successful states.

The UI should provide understandable messages based on the backend error response.

Technical implementation details must not be shown to users.

## Responsive Design

The interface must remain usable across supported screen sizes.

Important business operations must remain accessible on smaller screens.

Tables may use appropriate responsive behavior when the available width is limited.

## Accessibility

Interactive elements must be accessible.

The UI should use:

- Semantic HTML.
- Accessible labels.
- Keyboard navigation.
- Appropriate focus states.
- Meaningful error messages.

Forms must associate validation messages with the corresponding fields.

## Images

Product images must be displayed consistently.

Images should use appropriate dimensions and object-fit behavior.

Missing or invalid images should have a safe fallback presentation.

The frontend must use the image URL or storage reference returned by the backend.

## Date and Number Formatting

Dates and monetary values must use consistent formatting throughout the application.

Money must be displayed using the application's configured currency and formatting rules.

Quantities must be displayed consistently.

The frontend must not modify authoritative financial values.

## Role Visibility

UI visibility must follow the authenticated role.

OWNER receives Owner functionality.

SELLER receives Seller functionality.

The UI must not expose restricted business information to SELLER.

However, UI visibility is not a security mechanism.

The backend must independently enforce all permissions.

## Navigation Security

Protected routes must require authentication.

Role-specific routes must require the appropriate role.

If the user attempts to access an unauthorized frontend route, the application should redirect or display an appropriate access-denied state.

The backend remains responsible for final authorization.

## Consistency

The same UI patterns must be reused throughout the application.

Buttons, forms, tables, dialogs, notifications, loading states, and error states should behave consistently.

## UI Principle

The frontend should make the defined business processes simple for the user while remaining strictly aligned with the backend API and security model.

The interface must never become an alternative source of truth for authorization, inventory, financial calculations, or Tenant isolation.