# Changelog — verified engineering changes

## 2026-11-09 — COM-003 email-verification foundation ([PR #9 open](https://github.com/Spsk1313/commercecore/pull/9))
- Added Flyway V3: optional `users.email_verified_at`, one-token-row-per-user `email_verification_tokens`, unique SHA-256 hash, timestamp and expiry-order constraints. V1/V2 applied migrations are unchanged.
- Implemented `VerificationTokenGenerator` (32 random bytes / URL-safe Base64), `VerificationTokenHasher` (SHA-256 of raw-token UTF-8), guarded JDBC token consumption, verified-at user update, and `@Transactional EmailVerificationService.verify`.
- Added unit/PostgreSQL integration coverage for token generation/hash, sequential single-use and expiry, successful verification and rollback. Learner's final local run: **32 passing tests, 0 failures/errors/skips**. A deliberate removal of `@Transactional` failed the rollback test; restoring it passed.
- Initial PR #9 backend/frontend GitHub Actions passed on application head `6e7be22`. **PR remains open; documentation-updated latest-head CI must be rechecked.**
- Added [provisional ADR-0006](DECISIONS/ADR-0006-email-verification-tokens.md) for token/hash/transaction choices and unresolved delivery/abuse policy; explicit acceptance pending.
- **Not yet built:** signup/login API, Argon2id password storage, token issuance/email delivery, HTTP verification/resend, merchant authorization, public deployment or external users.

## 2026-11-08 — COM-003 identity and membership schema (PR #8 merged)
- [PR #8](https://github.com/Spsk1313/commercecore/pull/8) adds Flyway V2 users and merchant memberships with case-insensitive unique email index, required OWNER/STAFF role, foreign keys and per-user/per-merchant uniqueness. Existing V1 merchant schema remains untouched.
- Six new PostgreSQL Testcontainers negative-invariant tests were authored by the learner. Local Maven suite reported **24 tests, 0 failures/errors/skips** on 2026-11-08. [Backend CI](https://github.com/Spsk1313/commercecore/actions/runs/42516694613) and [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/42516694574) passed on initial PR head `fafb3bf`. Check CI again for later documentation commits.
- **Merge:** PR #8 squash-merged into `main` as `fe7ae39` after passing latest-head [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/42517097705) and [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/42517097800). The schema did not implement registration, validation, password hashing, login/session authentication, onboarding transaction or merchant authorization.


## 2026-11-01 — COM-002 merchant read integration (PR #7 merged)
- [PR #7](https://github.com/Spsk1313/commercecore/pull/7) adds JDBC merchant lookup, application service, 200 response DTO, 404 ProblemDetail and numeric path binding (400 on invalid values).
- Added 2 PostgreSQL-backed repository tests, 2 Mockito service tests and 3 MVC tests. **18 backend test executions passed locally**; [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41375979195) passed on the initial application head.
- Added local Vite `/api` proxy, TypeScript API client and temporary React ID search with validation, loading and error states. Manual browser-to-API-to-PostgreSQL smoke check succeeded. [Frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41375979226) passed on the initial head.
- **Not a production feature:** Endpoint is unauthenticated; no tenant isolation, automated browser E2E, store onboarding, deployed app or real-user benefit. **PR #7 subsequently squash-merged into `main` as `a42d639`.** COM-002 is Verified as a local integration baseline, not Released.

## 2026-11-01 — COM-002 frontend bootstrap (PR #6 merged)
- [PR #6](https://github.com/Spsk1313/commercecore/pull/6) adds React/TypeScript/Vite scaffold, lockfile, lint/build scripts and frontend GitHub Actions CI.
- Initial commit `a1c9097` passed independent [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41347826942) (`npm ci`, ESLint, TypeScript/Vite build) and [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41347826992); local clean install, lint and build also passed.
- Documented local development instructions and the open npm development-tooling advisory: 12 moderate full-audit findings, zero findings from `npm audit --omit=dev` at the recorded checkpoint.
- Learner replaced the Vite starter screen/assets with a minimal CommerceCore shell in commit `d1ecd86`; local lint/build passed, followed by [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41350363240) and [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41350363213) passes. No commerce-facing features, API integration, frontend automated tests, deployment or users. **PR #6 squash-merged as `aaed304` on 2026-11-01.**

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
