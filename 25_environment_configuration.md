# Environment Configuration

The project uses separate configuration for development and production.

Environment-specific values must not be hardcoded into the application source code.

Sensitive values must be provided through environment variables.

## Backend Configuration

The backend configuration is managed by Spring Boot.

The backend must support environment-specific configuration for:

- Application environment.
- PostgreSQL connection.
- Database credentials.
- Authentication configuration.
- CORS configuration.
- File storage configuration.
- Other required application settings.

## Database Configuration

The backend requires PostgreSQL configuration.

The database configuration contains:

- Database URL.
- Database username.
- Database password.

Database credentials must be provided through environment variables.

Database credentials must never be committed to the repository.

## Authentication Configuration

Authentication-related secrets must be provided through environment variables.

Authentication secrets must never be hardcoded.

Authentication configuration must be different between development and production when required.

Secrets must never be returned through API responses or written to logs.

## CORS Configuration

The backend must define the allowed frontend origins through configuration.

Development and production origins must be configurable independently.

Production must allow only the required production frontend origin.

## File Storage Configuration

Product images use the configured object storage system.

Storage configuration must be provided through environment variables.

Storage credentials must never be committed to the repository.

The frontend must receive only the image URL or storage reference required to display the product image.

## Frontend Configuration

The frontend uses Vite environment configuration for non-secret application values.

The frontend must contain the backend API URL required to communicate with the backend.

No secret value may be included in frontend environment variables.

Any value included in a Vite production build must be considered publicly accessible.

## Environment Files

Local development may use environment files such as:

.env

.env.local

Environment files containing secrets must not be committed to Git.

The repository should contain an example configuration file containing variable names without real secrets.

Example:

.env.example

The example file must not contain real passwords, tokens, credentials, or production secrets.

## Development Environment

The development environment is intended for local development and testing.

It uses development configuration for:

- PostgreSQL.
- Backend.
- Frontend.
- Authentication.
- CORS.
- Storage.

Development configuration must not accidentally point to production infrastructure.

## Production Environment

The production environment uses production configuration.

Production secrets must be provided through the deployment environment.

Production configuration must not be stored in the source repository.

Production must use:

- HTTPS.
- Production database.
- Production authentication secrets.
- Production CORS configuration.
- Production storage configuration.

## Configuration Separation

Development and production configuration must remain separate.

Changing the environment must not require modifying application source code.

The application must read environment-specific values from configuration.

## Secret Protection

The following values are sensitive:

- Database password.
- Database credentials.
- Authentication secrets.
- Storage credentials.
- Access tokens.
- Private keys.
- Other security secrets.

Sensitive values must:

- Exist only in secure environment configuration.
- Never be committed to Git.
- Never be included in frontend builds.
- Never be returned by API endpoints.
- Never be written to logs.

## Git Protection

Secret configuration files must be excluded from version control.

The project must use an appropriate `.gitignore` configuration.

The repository should contain only safe example configuration.

## Configuration Validation

The backend should fail clearly during startup when required configuration is missing or invalid.

The application must not silently use insecure default credentials in production.

Required production configuration must be explicitly provided.

## Configuration Principle

Configuration controls the environment.

Code defines application behavior.

Secrets belong to the environment and must never become part of the source code or frontend application.

Development and production must remain isolated while using the same application architecture.