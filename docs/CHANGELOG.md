# Changelog — verified engineering changes

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
- Spring Boot database connectivity and Flyway migrations remain pending.

## Change template

Date — Feature/incident ID — What changed — Impact on API/schema/security or user experience — Tests/CI evidence — Commit or release — Limitations and follow-up.

- 2026-10-25: Drafted ADR-0003 (inventory accounting and cancellation/retry semantics); requirements remain proposed, not implemented.

- 2026-10-25: Accepted ADR-0003 V1 logical inventory accounting and concurrent cancel/fulfill state-transition policy. Recorded database atomicity requirement and caveat about physical dispatch. Planning only; no implementation or test evidence.

- 2026-10-25: Accepted ADR-0004 logical tenant-ownership and cart authorization policy from design review, documented composite-FK caveat. Planning only, not deployed/tested.

- 2026-10-25: Product mission and six high-level user capabilities approved during COM-001 inception. Detailed release scope remains draft; planning only.

- 2026-10-25: Accepted COM-001.3 authenticated customer checkout and public browsing (ADR-0005). Recorded extra login friction and deferral of guest checkout. Product design only; no implementation or test evidence.
