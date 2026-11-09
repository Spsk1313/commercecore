# CommerceCore — Engineering Project


**Status (2026-11-09):** COM-002 local React → API → PostgreSQL read-path baseline is **Verified** after [PR #7](https://github.com/Spsk1313/commercecore/pull/7) merged. COM-003 Flyway V2 accounts and memberships merged in [PR #8](https://github.com/Spsk1313/commercecore/pull/8) as `fe7ae39`. **[PR #9](https://github.com/Spsk1313/commercecore/pull/9) squash-merged as `9d192d0`** with Flyway V3 email-verification schema and transactional verification logic. Learner-run backend suite: **32 tests passed**; latest PR #9 backend/frontend CI passed at `598ebf3`. There is **no** registration/login API, password hashing implementation, verification email delivery/HTTP endpoint, tenant authorization, production deployment or verified real-user value.

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
- `docs/DECISIONS/ADR-0001-modular-monolith.md` — modular-monolith choice, pending explicit acceptance.
- `docs/DECISIONS/ADR-0006-email-verification-tokens.md` — token/hash/transaction rationale, provisional.
- `docs/QUALITY_GATES.md` — definition of done, verification and release gates.
- `docs/EVIDENCE_LEDGER.md` — links to actual tests, commits, demonstrations, measurements.
- `docs/OPERATIONS.md` — startup, environments, deployments, monitoring, backup and recovery.
- `docs/CHANGELOG.md` — dated, user-visible and architectural changes.
- `docs/VISIBILITY.md` — sanitized case studies, demos, outreach and feedback.

## Source of truth
Once a Git repository exists, these files inside the repo are authoritative for product facts. The coach may keep a **brief mirrored checkpoint** in Software Prep records; it must link to/quote the actual commit and cannot claim to have reviewed local files or CI unless artifacts/logs were supplied or read from connected GitHub. Do not duplicate maintenance for no reason.


## Current verified engineering baseline

- **Frontend:** React 19, TypeScript and Vite 8; PR #7 merged a temporary merchant-ID search with input validation, loading/errors and a local development `/api` proxy. This is a walking-skeleton integration harness, not a storefront or secure merchant dashboard. See [frontend README](frontend/README.md).
- **Backend:** Java 21, Spring Boot 4.1.2, Spring MVC, `JdbcClient`, PostgreSQL 17, Flyway and Testcontainers. V1: merchants; V2 (merged): users and merchant memberships; V3 (merged PR #9): verified-at timestamp and email-verification token records. JPA/Hibernate is **not installed**.
- **Verification foundation merged:** 32 cryptographically random bytes encoded as URL-safe unpadded Base64; SHA-256 of the raw token string stored as a hexadecimal hash (raw token not persisted); repository consumption guarded by expiration/consumed checks; `@Transactional` service atomically marks the user verified. One token row per user. Issuance/persistence in registration, delivery and rotation/resend are **not implemented**.
- **Security boundary:** membership constraints do not authenticate or authorize users. The existing merchant GET is unauthenticated and must remain development-only until its public/private contract is designed.
- **Proof:** 32 backend test executions, zero failures/errors/skips locally on 2026-11-09; PR #9 latest-head backend/frontend GitHub Actions both passed before merge. Full npm development-tooling audit findings remain recorded in [evidence](docs/EVIDENCE_LEDGER.md).

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

Spring Boot connects to PostgreSQL at `jdbc:postgresql://127.0.0.1:5433/commercecore` and runs pending Flyway migrations at startup: V1 merchants, V2 users/memberships, V3 verification tokens (PR #9). Flyway records migration history; do not edit versioned migrations already applied to persistent databases. Earlier local startup and `/actuator/health` HTTP 200 checks are not deployment evidence.

**Isolated tests (Docker required, no `.env` needed):**

```bash
cd backend
env -u COMMERCECORE_DB_PASSWORD ./mvnw test
```


Backend tests use disposable PostgreSQL 17 Testcontainers via Spring `@ServiceConnection` without the local Compose database or `.env`. The latest learner-reported run (2026-11-09) completed **32 tests with zero failures/errors/skips**. Tests cover merchant read, V1/V2 integrity, token format/hash, sequential single-use and expired-token rejection, successful user verification, and rollback when updating an already-verified user fails. Removing the service's `@Transactional` caused its rollback test to fail; restoring it passed. This is not a concurrent-use stress test or an end-to-end signup/email test.

To shut down local PostgreSQL without deleting persistent data, run `docker compose down` from repository root. **Do not use `docker compose down -v` during ordinary shutdown.**


## Merged checkpoints and active review

- [PR #1](https://github.com/Spsk1313/commercecore/pull/1) — backend bootstrap/CI, merged `ba058a1`.
- [PR #2](https://github.com/Spsk1313/commercecore/pull/2) — local PostgreSQL Compose, merged `0ec4cfc`.
- [PR #3](https://github.com/Spsk1313/commercecore/pull/3) — documentation baseline, merged `7088b72`.
- [PR #4](https://github.com/Spsk1313/commercecore/pull/4) — Flyway V1/JDBC/Testcontainers, merged `6e3d326`.
- [PR #6](https://github.com/Spsk1313/commercecore/pull/6) — React/TypeScript frontend bootstrap, merged `aaed304`.
- [PR #7](https://github.com/Spsk1313/commercecore/pull/7) — local browser-to-database merchant read, merged `a42d639`.
- [PR #8](https://github.com/Spsk1313/commercecore/pull/8) — V2 users/memberships, merged `fe7ae39` (latest-head CI passed).
- **Merged:** [PR #9](https://github.com/Spsk1313/commercecore/pull/9) — V3 email-verification foundation, squash commit `9d192d0`; final-head backend/frontend CI passed.

**Next:** account registration with carefully benchmarked Argon2id password hashing, token issuance/persistence/delivery and verification HTTP/resend workflow; then authenticated sessions, atomic store + OWNER membership and negative cross-tenant authorization tests. See the [delivery board](docs/DELIVERY_BOARD.md).

## Product naming
CommerceCore is an independent training project. Inspired by *public* Shopify workflow and engineering literature, not affiliated with or an endorsed Shopify product. No proprietary code or interface assets copied.
