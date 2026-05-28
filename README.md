# LedgerView — Backend

REST API for [LedgerView](https://ledger-view.github.io/ledgerview), a personal finance tracker.
**Frontend:** https://github.com/ledger-view/ledgerview

**Techtack:**

- Java 21
- Spring Boot 3.5
- PostgreSQL 15
- Keycloak 24
- Flyway

---

## Features

- **Accounts** — multiple accounts with different currencies (USD, EUR, BTC, …); balance maintained automatically on
  every transaction
- **Transactions** — income/expense tracking with per-account currency; full CRUD with balance reconciliation on update
  and delete
- **Categories** — custom categories with a configurable color palette; transaction count included in list response
- **Dashboard** — monthly summary (income, expenses, net flow) and expenses by category; all figures grouped per
  currency — no cross-currency aggregation
- **Multi-currency** — fiat and crypto currencies configurable in `application.yml`; invalid currency rejected with
  `400`

---

## Running in dev mode

### Prerequisites

- Java 21
- Docker + Docker Compose

### Start infrastructure

```bash
docker compose -f devenv/docker-compose.yml up -d
```

Starts PostgreSQL and Keycloak. Keycloak admin console at `http://localhost:8180`.

### Run the app

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=localdev
```

- `localdev` — connects to local Docker DB, loads seed data
- `unsecured` — skips JWT auth; all requests treated as the dev user

API available at `http://localhost:8080/api`.

### Run tests

```bash
./mvnw test
```

Tests use Testcontainers — no local Postgres needed.
