# CommerceCore — Engineering Project

**Status:** Java/Spring Boot CI and local PostgreSQL infrastructure merged by 2026-10-26. No commerce feature or deployment verified.
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
Requirements: Java 21, Docker Engine (verified 29.7.2), Compose (verified 5.5.0) and the Maven wrapper inside `backend/`.

```bash
cp .env.example .env
# Set COMMERCECORE_DB_PASSWORD in .env locally; never commit the actual .env.
docker compose config --quiet
docker compose up -d --wait db
docker compose ps
docker compose exec -T db psql -U commercecore -d commercecore -c "SELECT current_database(), current_user;"
(cd backend && ./mvnw test)
```

The Spring Boot backend was locally started with `./mvnw spring-boot:run` from `backend/` and returned HTTP 200 at `http://localhost:8080/actuator/health`. To stop PostgreSQL, use `docker compose down` (preserves volume). **Avoid `docker compose down -v`: it deletes the declared database volume.** No Spring Boot database integration, Flyway migrations, frontend or business API has been built.

## Merged checkpoints
- [PR #1](https://github.com/Spsk1313/commercecore/pull/1) — backend bootstrap and CI, merged as `ba058a1`.
- [PR #2](https://github.com/Spsk1313/commercecore/pull/2) — PostgreSQL 17 Compose environment and local persistence experiment, merged as `0ec4cfc`.
- Next: design Flyway migration setup and verify schema history against PostgreSQL.
- See [delivery board](docs/DELIVERY_BOARD.md), [evidence ledger](docs/EVIDENCE_LEDGER.md), and [operations runbook](docs/OPERATIONS.md).

## Product naming
CommerceCore is an independent training project. Inspired by *public* Shopify workflow and engineering literature, not affiliated with or an endorsed Shopify product. No proprietary code or interface assets copied.
