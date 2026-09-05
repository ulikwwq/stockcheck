# StockCheck

A multi-tenant inventory and sales management platform for small and medium-sized businesses.

> **Status: Phase 2 — Core platform functional.**
> The backend implements the full business domain: JWT auth, multi-tenancy,
> shops, categories, products, sellers, sales (with atomic inventory/profit/
> audit handling), stock movements, profit reporting, and audit logs — all
> behind role-based authorization (`SUPER_ADMIN`, `ADMINISTRATOR` = Owner,
> `SELLER`). The frontend is a working React app covering login, the Owner
> dashboard/products/sellers/sales/profit/inventory/audit screens, the
> Seller's product/new-sale/my-sales screens, and the Super Admin's business
> (tenant) management screen. Not yet built: Business Settings, product
> image upload to object storage, pagination/server-side filtering on list
> endpoints, and deployment configs (Vercel/Render).

## Project Overview

StockCheck will eventually let a business owner track products, stock
levels, purchase/sale prices, and profit, while sellers record sales through
a simple interface. The platform is designed to serve many independent
businesses ("tenants") from a single deployment. See the project
specification for the full functional scope.

## Requirements

Install these before you start:

* **Java 25** (JDK) — `java -version`
* **Docker** and **Docker Compose** — `docker --version`
* **Node.js** (LTS) and **npm** — `node -v` / `npm -v`

## Project Structure

```text
stockcheck/
├── backend/          Spring Boot API (Java 25, Gradle)
├── frontend/         React + Vite + TypeScript + Tailwind CSS
├── docker-compose.yml
├── .gitignore
└── README.md
```

## 1. Start PostgreSQL

From the repository root:

```bash
docker compose up -d
```

This starts PostgreSQL 17 in a container named `stockcheck-postgres`, with:

* Database: `stockcheck`
* User / password: `stockcheck` / `stockcheck`
* Host port: **5434** (mapped to the container's internal `5432`)
* Data persisted in the named volume `stockcheck-postgres-data`

No manual database setup is required — the database and user are created
automatically the first time the container starts.

To confirm it's running:

```bash
docker compose ps
```

To stop it (keeping data):

```bash
docker compose down
```

## 2. Start the Backend

The backend reads its database connection from environment variables, with
sensible local defaults if they're not set:

| Variable      | Default                                         |
|---------------|--------------------------------------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5434/stockcheck`     |
| `DB_USERNAME` | `stockcheck`                                      |
| `DB_PASSWORD` | `stockcheck`                                      |

With PostgreSQL already running (step 1), start the API:

```bash
cd backend
./gradlew bootRun
```

On startup, Flyway runs any pending migrations and Hibernate validates the
JPA model against the schema (`ddl-auto: validate` — it never auto-generates
or alters tables). The backend listens on **port 8080**.

Check that it's healthy:

```bash
curl -i http://localhost:8080/actuator/health
```

You should get `HTTP/1.1 200` with a body like `{"status":"UP"}`.

## 3. Start the Frontend

In a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the URL Vite prints (default `http://localhost:5173`). You should see
the StockCheck login page. The frontend talks to the backend via
`VITE_API_BASE_URL` in `frontend/.env` (defaults to
`http://localhost:8080/api/v1`).

### First login

Migration `V5__seed_super_admin.sql` seeds one bootstrap account so the
platform is usable immediately, without needing direct database access:

| Phone          | Password         | Role          |
|----------------|------------------|---------------|
| `+10000000000` | `SuperAdmin123!` | `SUPER_ADMIN` |

Log in as this user, go to **Businesses**, and create your first business
(tenant) — this also creates its Owner account and a default shop in one
step. Log out, then log back in as that Owner to create sellers, products,
and start recording sales. **Rotate or delete the seeded account before any
shared/production deployment.**

## Running Backend Tests / Build

The backend has no embedded/in-memory database — tests run against the
same PostgreSQL instance used for local development (via Flyway, just
like the running app). **Start PostgreSQL first** (step 1 above), then:

```bash
cd backend
./gradlew test    # run tests
./gradlew build   # full build (compiles, tests, packages)
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Building the Frontend for Production

```bash
cd frontend
npm run build
```

This type-checks the project and produces an optimized production bundle in
`frontend/dist/`. It must complete with no errors.

## Resetting the Local Database

To wipe all local data and start from a completely clean database (useful
if migrations get into a bad state):

```bash
docker compose down -v
docker compose up -d
```

The `-v` flag removes the named volume, so the next `up` recreates the
database from scratch and Flyway re-applies all migrations.

## Ports Summary

| Service    | Port                          |
|------------|--------------------------------|
| PostgreSQL | `5434` (host) → `5432` (container) |
| Backend    | `8080`                         |
| Frontend   | `5173` (Vite dev server)       |

## Implementation Notes

* **Authentication & authorization are real and enforced.** Login issues a
  stateless JWT (`backend/.../security/JwtService.java`); every protected
  endpoint requires it (`SecurityConfig.java`) and role checks are enforced
  server-side with `@PreAuthorize` — the frontend's role-based navigation is
  a UX convenience only, never the security boundary.
* **Role naming:** the spec's `OWNER` role is implemented in code as
  `ADMINISTRATOR` (see `com.stockcheck.backend.role.RoleName`). The frontend
  treats them as the same concept. The enum also carries several roles
  (`BUYER`, `MANAGER`, `COURIER`, `WAREHOUSE_OPERATOR`, `ACCOUNTANT`,
  `CONTENT_MANAGER`) that exist in the database `CHECK` constraint but have
  no behavior wired up anywhere yet — only `SUPER_ADMIN`, `ADMINISTRATOR`,
  and `SELLER` are actually used.
* **Data model:** the `categories` / `products` tables from `V1__initial_schema.sql`
  are legacy from the very first migration and are **not** used by the real
  domain model — `V3__product_inventory_schema.sql` defines the actual
  `categories` and `products` tables the application uses. The `V1` tables
  are unreferenced dead weight; removing them needs a follow-up migration.
* **Not yet built:** Business Settings (mentioned in the spec, no
  entity/endpoint exists), product image upload to object storage (the
  frontend accepts a plain image URL instead), pagination/server-side
  filtering on list endpoints (they return full collections), and
  deployment configuration for Vercel/Render.
