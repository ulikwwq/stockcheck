# Deployment

The project is deployed as two separate applications:

- Backend
- Frontend

The production environment uses the same application architecture defined for development.

## Backend Deployment

The backend is a Spring Boot application.

The backend is built using Gradle.

The production backend must run using the configured Java version for the project.

The backend exposes the REST API under:

/api/v1

## Frontend Deployment

The frontend is a React application built with Vite.

The production frontend is created using the Vite production build process.

The generated frontend application is deployed to the configured frontend hosting environment.

The frontend communicates with the production backend through the configured API URL.

## Database Deployment

The production database uses PostgreSQL.

Database schema changes are managed through Flyway.

Before the backend uses a new database schema, the required Flyway migrations must be applied.

Production database changes must never depend on manual schema modifications.

## Environment Variables

Environment-specific configuration must be provided through environment variables.

Sensitive values must not be committed to the repository.

Backend configuration includes values such as:

- Database connection information.
- Database credentials.
- Authentication secrets.
- Storage configuration.
- CORS configuration.
- Other required secrets.

Frontend configuration includes the production backend API URL and other non-secret configuration required by the application.

Frontend secrets must never be embedded into the frontend application.

Anything included in the frontend build must be considered publicly accessible.

## Environment Separation

The project has separate environments for development and production.

Development configuration must not use production credentials.

Production configuration must not be committed to the source repository.

Each environment must use its own appropriate configuration.

## Build Process

The backend build process:

1. Install dependencies.
2. Run automated tests.
3. Build the Spring Boot application.
4. Produce the deployable backend artifact.

The frontend build process:

1. Install dependencies.
2. Run frontend tests.
3. Build the Vite application.
4. Produce the production frontend assets.

A production deployment should not proceed when required automated tests fail.

## Database Migration Process

Database migrations must be applied in the correct version order.

The deployment process must ensure that the database schema is compatible with the backend version being deployed.

New migrations must be tested before production deployment.

Applied migrations must not be modified.

## Deployment Order

The deployment process must maintain compatibility between:

- Frontend.
- Backend.
- Database.

When a backend release requires a new database migration, the migration must be applied as part of the deployment process.

The deployment must not leave the backend operating against an incompatible database schema.

## Production Security

Production deployment must use secure configuration.

HTTPS must be used for production communication.

Production credentials must be stored securely.

Secrets must not be included in:

- Source code.
- Git history.
- Frontend source.
- Frontend build output.
- Logs.
- Error responses.

## CORS

The backend must configure CORS according to the production frontend origin.

Development origins must not automatically be allowed in production.

CORS configuration must not be used as a replacement for authentication or authorization.

## Logging

Production logging must provide enough information for troubleshooting and monitoring.

Logs must not contain sensitive information.

The system must never log:

- Passwords.
- Password hashes.
- Authentication secrets.
- Database credentials.
- Access tokens.
- Other sensitive security information.

## Error Handling in Production

Production API responses must not expose internal implementation details.

Stack traces must not be returned to users.

Internal exceptions must be logged securely on the backend.

The frontend must display user-friendly error messages.

## File Storage

Product images are stored using the configured object storage system.

The database stores the corresponding image URL or storage reference.

Storage credentials must be kept in environment configuration.

The frontend must use the configured image URL returned by the backend.

## Health and Availability

The backend should provide a mechanism for determining whether the application is running correctly.

The health mechanism should verify the basic availability of the backend and its required infrastructure according to the deployment configuration.

## Deployment Verification

After deployment, the following areas must be verified:

- Backend is running.
- Frontend is accessible.
- Database connection works.
- Flyway migrations are applied.
- Authentication works.
- Authorization works.
- Tenant isolation works.
- Products can be accessed.
- Sellers can authenticate.
- Sales can be completed.
- Inventory is updated correctly.
- Profit is calculated correctly.
- Audit logs are created.

## Rollback

A deployment rollback must consider the compatibility of:

- Frontend version.
- Backend version.
- Database schema.

Database migrations must not be casually reversed.

If a migration has already been applied, rollback must follow the database migration strategy defined for the project.

## Production Principle

Production deployment must use controlled, reproducible processes.

The same application rules used during development must remain active in production.

Security, Tenant isolation, database integrity, transaction consistency, and historical business data must remain protected after deployment.