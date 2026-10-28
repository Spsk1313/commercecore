# CommerceCore — Engineering Project

**Status:** Java 21/Spring Boot backend, local PostgreSQL, Flyway V1 merchant schema and PostgreSQL integration tests verified in GitHub CI as of 2026-10-27. No merchant business API, frontend or deployment verified.
**Owner:** Learner (sole accountable implementing engineer).
**Coach:** Technical lead/reviewer; not substitute implementation author.
**Target:** A production-minded, independently built, Shopify-inspired commerce vertical slice by **2026-11-30** (scope gates, not a promise of Shopify feature parity).

## Mission
Demonstrate end-to-end engineering judgment on a real merchant/storefront/order/fulfillment workflow. Produce verifiable evidence of correctness, security, reliability, operability and design reasoning; optionally earn real pilot users, never invent them.

## Current scope
Merchant creates a store and product; customer browses, submits pay-on-pickup/simulated order; stock does not oversell under concurrent checkout; merchant manages order and fulfillment. Real card handling and payment processing are excluded from V1.

## Repository navigation
- `docs/PRODUCT_SPEC.md` — product behavior, scope, invariants, acceptance criteria.
- `docs/ARCHITECTURE.md` — proposed modules and actual system shape (keep accurate).
- `docs/DELIVERY_BOARD.md` — authoritative implementation status and next work.
- `docs/DECISIONS/ADR-0001-modular-monolith.md` — reasoned, revisitable choices.
- `docs/QUALITY_GATES.md` — definition of done, verification and release gates.
- `docs/EVIDENCE_LEDGER.md` — links to actual tests, commits, demonstrations, measurements.
- `docs/OPERATIONS.md` — startup, environments, deployments, monitoring, backup and recovery.
- `docs/CHANGELOG.md` — dated, user-visible and architectural changes.
- `docs/VISIBILITY.md` — sanitized case studies, demos, outreach and feedback.

## Source of truth
Once a Git repository exists, these files inside the repo are authoritative for product facts. The coach may keep a **brief mirrored checkpoint** in Software Prep records; it must link to/quote the actual commit and cannot claim to have reviewed local files or CI unless artifacts/logs were supplied or read from connected GitHub. Do not duplicate maintenance for no reason.

## Verified local setup

Requirements: Java 21, Docker Engine (locally verified 29.7.2), Docker Compose (locally verified 5.5.0), and the Maven wrapper in `backend/`.

```bash
# From repository root. Create your own private .env once.
cp .env.example .env
# Set COMMERCECORE_DB_PASSWORD privately; never commit .env.

# Start the persistent local development database.
docker compose config --quiet
docker compose up -d --wait db
docker compose ps

# For local application startup only: load variables from your trusted,
# shell-compatible local .env into this shell process.
set -a
. ./.env
set +a
cd backend
./mvnw spring-boot:run
```

Spring Boot connects to PostgreSQL at `jdbc:postgresql://127.0.0.1:5433/commercecore` and runs pending Flyway migrations at startup. V1 creates `merchants` and `flyway_schema_history`; do not edit already-applied migrations. The application startup above was observed locally on 2026-10-27; `/actuator/health` returned HTTP 200 in the earlier backend smoke check. This is a local development configuration, not a production deployment.

**Isolated tests (Docker required, no `.env` needed):**

```bash
cd backend
env -u COMMERCECORE_DB_PASSWORD ./mvnw test
```

The JUnit integration test starts a disposable PostgreSQL 17 Testcontainers instance through `@ServiceConnection`, runs Flyway V1, and checks merchant-table behavior. Eleven test executions passed locally, and [GitHub Actions run #5](https://github.com/Spsk1313/commercecore/actions/runs/40616327894) passed independently on PR #4. Tests do not require a pre-existing Compose database.

To shut down local PostgreSQL without deleting persistent data, run `docker compose down` from repository root. **Do not use `docker compose down -v` during ordinary shutdown.**

## Merged checkpoints
- [PR #1](https://github.com/Spsk1313/commercecore/pull/1) — backend bootstrap and CI, merged `ba058a1`.
- [PR #2](https://github.com/Spsk1313/commercecore/pull/2) — PostgreSQL Compose and persistence verification, merged `0ec4cfc`.
- [PR #3](https://github.com/Spsk1313/commercecore/pull/3) — documentation and session handoff, merged `7088b72`.
- [PR #4](https://github.com/Spsk1313/commercecore/pull/4) — JDBC/Flyway V1 and Testcontainers integration, merged `6e3d326`; passing [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/40616327894).
- **Next COM-002 implementation:** establish React + TypeScript frontend baseline, then verify a browser-to-API-to-PostgreSQL vertical slice. Merchant authentication and business APIs are not yet implemented.
- See [delivery board](docs/DELIVERY_BOARD.md), [evidence ledger](docs/EVIDENCE_LEDGER.md), and [operations runbook](docs/OPERATIONS.md).

## Product naming
CommerceCore is an independent training project. Inspired by *public* Shopify workflow and engineering literature, not affiliated with or an endorsed Shopify product. No proprietary code or interface assets copied.
