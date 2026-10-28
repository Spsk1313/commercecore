# Changelog — verified engineering changes

## 2026-10-27 — COM-002 Flyway and PostgreSQL integration tests
- [PR #4](https://github.com/Spsk1313/commercecore/pull/4) squash-merged as `6e3d326`; introduced Spring Boot JDBC/PostgreSQL/Flyway and V1 `merchants` schema with named merchant-name checks.
- Added Testcontainers PostgreSQL integration tests: table-existence verification, five invalid merchant names rejected, five valid names persisted/read in rolled-back transactions.
- Local tests: 11 passed; [GitHub Actions backend CI run 40616327894](https://github.com/Spsk1313/commercecore/actions/runs/40616327894) passed without the developer's `.env`.
- Verified local Spring Boot startup, Flyway migration history and SQL constraints. This is backend/database evidence only; React, commerce APIs, authorization, deployment and real-user value remain unverified.

## 2026-10-26 — COM-002 environment/session handoff
- [PR #1](https://github.com/Spsk1313/commercecore/pull/1) merged as `ba058a1`, establishing the local Spring Boot bootstrap and passing backend CI.
- [PR #2](https://github.com/Spsk1313/commercecore/pull/2) merged as `0ec4cfc`, adding PostgreSQL 17 development infrastructure with locally observed connectivity and persistence.
- Clarified README/runbook startup and teardown, evidence links, and outstanding work. PR #2 CI checks only the backend, not PostgreSQL.
- No merchant/customer business features, migrations, deployed system, performance results, or validated external user outcomes yet.

## 2026-10-25 — Product inception and engineering baseline

- Established initial product mission, module proposal, release-quality gates, delivery board, evidence and public-visibility workflows.
- **No commerce business functionality or production deployment verified yet.**

- Accepted V1 cart/checkout inventory policy in ADR-0002 after UX-versus-availability design review; design only, not yet implemented.

### COM-002 — Backend bootstrap and CI

- Initialized Spring Boot 4.1.2 backend using Java 21 and Maven.
- Added Spring Web MVC and Actuator dependencies.
- Verified successful local Maven build and one application-context test.
- Verified HTTP 200 response from `/actuator/health`.
- Established GitHub Actions backend test workflow.
- Upgraded checkout and setup-java actions to supported major versions.
- CI passed on GitHub.
- Merged PR #1 into main.

### COM-002 — PostgreSQL development infrastructure

- Added Docker Compose configuration for PostgreSQL 17.
- Added environment-variable-based local password configuration.
- Restricted published database port to the local loopback interface.
- Configured persistent database storage through a named volume.
- Added PostgreSQL health checking.
- Verified SQL connectivity and persistence across container recreation.
- Removed temporary persistence-test table.
- At this 2026-10-25/26 checkpoint, Spring Boot database connectivity and Flyway migrations were still pending; completed later in PR #4 (2026-10-27).

## Change template

Date — Feature/incident ID — What changed — Impact on API/schema/security or user experience — Tests/CI evidence — Commit or release — Limitations and follow-up.

- 2026-10-25: Drafted ADR-0003 (inventory accounting and cancellation/retry semantics); requirements remain proposed, not implemented.

- 2026-10-25: Accepted ADR-0003 V1 logical inventory accounting and concurrent cancel/fulfill state-transition policy. Recorded database atomicity requirement and caveat about physical dispatch. Planning only; no implementation or test evidence.

- 2026-10-25: Accepted ADR-0004 logical tenant-ownership and cart authorization policy from design review, documented composite-FK caveat. Planning only, not deployed/tested.

- 2026-10-25: Product mission and six high-level user capabilities approved during COM-001 inception. Detailed release scope remains draft; planning only.

- 2026-10-25: Accepted COM-001.3 authenticated customer checkout and public browsing (ADR-0005). Recorded extra login friction and deferral of guest checkout. Product design only; no implementation or test evidence.
