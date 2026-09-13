# Project Overview

This project is a web-based inventory and sales management platform for small and medium-sized businesses.

The main purpose of the platform is to help business owners manage products, inventory, purchase prices, selling prices, sales, and profit in one centralized system.

The platform supports multiple independent businesses using the same application. Each business is represented as a separate Tenant, and each Tenant has completely isolated business data.

The system has three main user roles:

- SUPER_ADMIN
- OWNER
- SELLER

SUPER_ADMIN manages the platform and creates businesses and Owner accounts.

OWNER manages a specific business. The Owner can manage products, inventory, Sellers, sales, profit, audit history, and business settings.

SELLER is an employee who performs sales. Sellers can view available products, view selling prices, and register sales, but they cannot access purchase prices, profit, product management, or business management functions.

The core business workflow is:

SUPER_ADMIN creates a Tenant and an Owner account.

OWNER logs into the system and manages the business.

OWNER creates products and initial inventory.

OWNER creates Seller accounts.

SELLER logs into the system and views available products.

SELLER registers a product sale.

The system creates a Sale and SaleItem.

The system calculates the profit using the historical purchase price and actual selling price.

The system decreases the product inventory.

The system creates a StockMovement.

The system creates an AuditLog.

All important business operations must be recorded and protected.

Every sale must be represented as a real business transaction. Inventory must not be changed independently without recording the corresponding business operation.

The system must preserve historical transaction data. Changes to current product prices must not change previously completed sales.

The system must prevent inventory from becoming negative.

The system must handle concurrent sales correctly so that multiple Sellers cannot successfully sell the same final unit of inventory.

The system must enforce strict Tenant isolation. A user belonging to one Tenant must never be able to access or modify data belonging to another Tenant.

Tenant identification must come from the authenticated security context and must not be trusted from frontend input.

The backend is the final authority for authentication, authorization, Tenant isolation, inventory consistency, and business rules.

The application consists of a frontend, backend, database, and object storage.

The frontend uses React, TypeScript, Vite, Tailwind CSS, and React Router.

The backend uses Java, Spring Boot, Spring Security, Spring Data JPA, Hibernate, Gradle, and Flyway.

The database uses PostgreSQL hosted by Supabase.

Product images are stored using Supabase Storage or an S3-compatible storage implementation.

The frontend is deployed using Vercel.

The backend is deployed using Render.

The application uses a versioned REST API under /api/v1.

The initial production architecture consists of:

- One frontend
- One backend
- One PostgreSQL database
- One storage system
- One application URL
- Multiple independent Tenants

The system is designed as a production-ready multi-tenant application from the beginning. The architecture should not depend on temporary structures that would require a complete redesign later.

The main functional areas are:

- Authentication
- Authorization
- Multi-Tenancy
- User Management
- Product Management
- Seller Management
- Sales
- Inventory
- Profit
- Stock Movements
- Audit Logs
- Business Settings
- Platform Management

The main technical requirements are security, Tenant isolation, data integrity, transaction consistency, historical data preservation, and reliable inventory management.