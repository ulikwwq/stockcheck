# Logging and Monitoring

The system must provide structured and useful logging for development and production troubleshooting.

Logging must support debugging, operational monitoring, and investigation of unexpected application behavior.

## Application Logging

The backend uses the application's configured logging system.

Logs should provide enough information to understand:

- Application startup.
- Application shutdown.
- Important business operations.
- Authentication failures.
- Authorization failures.
- Unexpected exceptions.
- Database or infrastructure failures.

## Log Levels

The application should use appropriate log levels.

ERROR is used for unexpected failures that require investigation.

WARN is used for potentially problematic situations that do not necessarily stop the operation.

INFO is used for important application lifecycle and business events.

DEBUG is used for development and detailed troubleshooting.

Production logging should avoid unnecessary DEBUG output.

## Sensitive Information

Logs must never contain sensitive information.

The system must never log:

- Passwords.
- Password hashes.
- Authentication tokens.
- Access tokens.
- Database passwords.
- Storage credentials.
- Private keys.
- Other application secrets.

Sensitive request fields must be excluded from logs.

## Authentication Logging

Authentication failures may be logged for security monitoring.

Logs should identify enough context to investigate the event without exposing credentials.

Successful authentication may be logged when useful for operational monitoring.

Passwords must never be logged.

## Authorization Logging

Important authorization failures may be logged.

The log should identify:

- User.
- Requested operation.
- Resource type.
- Time.
- Result.

The log must not expose sensitive business data.

## Tenant Context

When logging Tenant-specific operations, the log should include the relevant Tenant identifier when appropriate.

Tenant identifiers may be used for troubleshooting and operational investigation.

The logged Tenant context must correspond to the authenticated user and the affected resource.

## Business Operation Logging

Important business operations may generate application logs.

Examples include:

- Product creation.
- Product updates.
- Inventory adjustments.
- Sale creation.
- Seller creation.
- Important status changes.

AuditLogs remain the authoritative business history for auditable actions.

Application logs are primarily for operational troubleshooting.

## Sale Logging

Sale processing may be logged for operational purposes.

Logs must not expose sensitive financial information unnecessarily.

The application should log enough information to investigate a failed transaction without logging protected purchase prices, authentication secrets, or other sensitive data.

## Error Logging

Unexpected exceptions must be logged with enough technical information for developers to investigate the problem.

The API response must remain safe and must not expose the internal exception details.

The log may contain:

- Exception type.
- Stack trace.
- Request context.
- Operation context.
- Correlation identifier.

Sensitive values must still be excluded.

## Correlation Identifier

Requests should use a correlation or request identifier when appropriate.

The identifier allows related log entries to be connected during troubleshooting.

The identifier may also be returned in API error responses when configured.

## Database Errors

Database failures should be logged appropriately.

Logs must not expose:

- Database passwords.
- Connection credentials.
- Sensitive SQL parameters.
- Other protected configuration.

Database errors returned to clients must be converted into safe API error responses.

## Inventory Failures

Inventory-related failures may be logged.

Examples include:

- Insufficient inventory.
- Concurrent inventory conflict.
- Invalid inventory adjustment.
- Product not available.

Logs should provide enough operational context to investigate the failure.

## Monitoring

The production environment should monitor the health and availability of the backend.

Monitoring should identify important operational problems such as:

- Application downtime.
- Repeated server errors.
- Database connectivity failures.
- High error rates.
- Failed requests.
- Infrastructure failures.

## Health Checks

The backend should provide a health-check mechanism suitable for the deployment environment.

Health information must not expose sensitive configuration.

Health endpoints must not return:

- Database credentials.
- Authentication secrets.
- Storage credentials.
- Internal security information.

## Log Retention

Production logs should be retained according to the operational requirements of the project.

Retention must be long enough to investigate relevant incidents.

Logs should not be retained indefinitely without a business or operational reason.

## Audit Logs vs Application Logs

AuditLogs and application logs have different purposes.

AuditLogs record important business and security actions as historical records.

Application logs support debugging, troubleshooting, and operational monitoring.

Application logs must not be treated as a replacement for AuditLogs.

AuditLogs must not be replaced by ordinary application logging.

## Production Logging

Production logs must be appropriate for a production environment.

They must:

- Avoid sensitive information.
- Avoid excessive debug output.
- Provide useful error context.
- Support troubleshooting.
- Support operational monitoring.

## Frontend Logging

The frontend may log development information when necessary.

Production frontend logging must not expose:

- Authentication credentials.
- Access tokens.
- Sensitive business data.
- Internal application secrets.

Debugging information should not unnecessarily remain enabled in the production user interface.

## Monitoring Principle

Logging and monitoring provide operational visibility without becoming a source of sensitive information.

AuditLogs preserve business accountability.

Application logs support technical troubleshooting.

Monitoring provides visibility into system health and availability.

All three must remain separate and must follow the project's security and Tenant-isolation rules.