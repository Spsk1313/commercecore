# Delivery board and change discipline

**Snapshot:** 2026-11-01 (America/Toronto).
**Status:** COM-002 in progress. PRs #1–#5 merged; backend and database integration verified. React/TypeScript frontend scaffold and CI verified on initial PR #6 commit, but PR #6 remains open. No commerce API, connected frontend workflow, deployment or real users.

## Most recent session handoff — 2026-11-01 (Toronto)
- **Current branch/PR:** `feat/react-frontend-bootstrap` / [PR #6](https://github.com/Spsk1313/commercecore/pull/6); submitted head `a1c9097`, based on `main` `8a93c1a`. Documentation changes added later on the same branch; recheck CI at final head.
- **Frontend implemented:** React 19 + TypeScript 6 + Vite 8, npm lockfile, development server, ESLint/build scripts and frontend pull-request workflow; still displays generated Vite starter UI.
- **Verified:** Local `npm ci`, lint and build; [frontend Actions run](https://github.com/Spsk1313/commercecore/actions/runs/41347826942) and [backend Actions run](https://github.com/Spsk1313/commercecore/actions/runs/41347826992) both passed on initial PR commit.
- **Open risk:** `npm audit` showed 12 moderate development-dependency findings involving `minimatch@10.2.6`; `npm audit --omit=dev` reported none. Resolution pending, not claimed secure by default.
- **Next action:** Review docs and PR head checks, update the minimal CommerceCore application shell in a separate implementation change if warranted, then squash-merge frontend bootstrap. Next COM-002 gate is actual browser → API → PostgreSQL proof. No catalog/ProductCard functionality in this bootstrap PR.
- **Ownership:** Learner authored application scaffold and CI with coaching/review; mentor authored documentation updates. No complete commerce flow or real-user validation.

## Previous session handoff — 2026-10-27 (Toronto)
- **Repository:** `main` and `origin/main` synchronized at `6e3d326`; learner showed a clean worktree and only local/remote `main` branches after squash-merging PR #4.
- **Implemented:** Spring Boot 4.1.2 / Java 21 backend, PostgreSQL 17 local Docker Compose volume, JDBC datasource with secret via environment variable, Flyway `V1__create_merchants.sql`, and `MerchantSchemaIntegrationTest` with PostgreSQL Testcontainers. Merchant table currently models only generated ID and display name, not tenant access controls.
- **Verified:** Spring started locally, initialized Hikari connection pool, executed Flyway V1 and wrote successful migration history; learner inspected the table constraints and history via `psql`. Manually verified valid and rejected names. Local `env -u COMMERCECORE_DB_PASSWORD ./mvnw test`: **11 test executions passed** (one schema check, five negative, five positive). [PR #4 GitHub CI](https://github.com/Spsk1313/commercecore/actions/runs/40616327894) passed on a separate runner. See [evidence ledger](EVIDENCE_LEDGER.md).
- **Ownership/learning:** Learner implemented SQL, tests, and ran verification with coaching/review; learned schema migrations, database constraint boundaries, transaction isolation basics and test transaction rollback. Mentor authored this documentation handoff in a separate PR. Delayed independent mastery assessment not yet performed.
- **Next action (one step at a time):** React + TypeScript frontend bootstrap for COM-002, then first browser-to-API-to-PostgreSQL integration. Preserve small checkpoints and CI discipline.
- **Still unverified:** Auth/tenant isolation, product/order/checkout APIs, frontend, E2E, deployment, backup/restore, load/performance and external customer value.

## Backlog & status

| ID      | Capability                                                          | Status      | Owner   | Proof / link                                                 | Next gate                                                                  |
| ------- | ------------------------------------------------------------------- | ----------- | ------- | ------------------------------------------------------------ | -------------------------------------------------------------------------- |
| COM-001 | Feature specification, tenant model, API and concurrency invariants | In Progress | Learner | None                                                         | Design review + ADR                                                        |
| COM-002 | Local full-stack baseline: React, Spring, PostgreSQL, Flyway, CI    | In Progress | Learner | [PR #6](https://github.com/Spsk1313/commercecore/pull/6), [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41347826942); PRs [#1](https://github.com/Spsk1313/commercecore/pull/1), [#2](https://github.com/Spsk1313/commercecore/pull/2), [#4](https://github.com/Spsk1313/commercecore/pull/4); [passing database CI](https://github.com/Spsk1313/commercecore/actions/runs/40616327894) | PR #6 review/merge, then browser → API → DB verification |
| COM-003 | Merchant auth/store isolation                                       | Planned     | Learner | None                                                         | Negative cross-tenant tests                                                |
| COM-004 | Merchant catalog and inventory                                      | Planned     | Learner | None                                                         | API/UI contract + DB constraints                                           |
| COM-005 | Public storefront and cart                                          | Planned     | Learner | None                                                         | End-to-end browser flow                                                    |
| COM-006 | Order transaction/concurrency                                       | Planned     | Learner | None                                                         | Last-unit race test + rollback proof                                       |
| COM-007 | Order fulfillment and replay handling                               | Planned     | Learner | None                                                         | State-transition and idempotency tests                                     |
| COM-008 | Secure deployed MVP, CI, observability                              | Planned     | Learner | None                                                         | Runbook + demo + operational checks                                        |
| COM-009 | Fault/load study and public engineering case study                  | Planned     | Learner | None                                                         | Reproducible benchmark + sanitized post                                    |

**Allowed statuses:** Proposed -> Ready -> In Progress -> In Review -> Verified -> Released; Blocked can occur at any stage. Never mark Verified based only on a plan, code paste, or an unrun test.

## Accepted COM-001 decisions

- 2026-10-25 — ADR-0002: cart does not reserve inventory; atomic allocation at successful order submission for V1; stale-cart checkout rejection is an expected, clearly recoverable UX case. Design decision only, no implementation verified.

## Feature card template (copy under appropriate ID)

- Scenario / user value:
- Acceptance + API/UI contract:
- Invariants, auth scope, edge cases:
- Threat/failure hypotheses:
- Proposed schema/API/transaction boundary:
- Tradeoffs / ADR needed?:
- Code/commit/PR:
- Tests/logs and observed result:
- Reviewer findings / learner fixes:
- Release/demo and observable impact:
- Open risk and next action:

## Documentation changes by change type

- **Every merged meaningful feature or bug fix:** update this board, `CHANGELOG.md`, and actual verification in `EVIDENCE_LEDGER.md`.
- **API/DB/security/architecture change:** update relevant spec and architecture, and add/revise ADR if a tradeoff materially changed.
- **New deployment/operations workflow:** update `OPERATIONS.md` and evidence/rollback documentation.
- **User/test/performance discovery:** add evidence and change related acceptance expectations.
- **Public communication:** update `VISIBILITY.md` with URL, date, truthful claims and any consent.
- **Tiny refactors:** rely on commit/tests; don't create needless documentation churn unless behavior or an invariant changes.

## Weekly checkpoint template

Date/time (within confirmed block) | Last verified commit | Shipped/proven | Defects and risks | Coaching intervention level | Real users/feedback | Public evidence | Next one action. Unverified items remain unverified.

- **COM-001 cancellation design (in review):** ADR-0003 proposed; clarify order transition and stock effects, then approve and proceed. No code, migrations or tests verified.

- **2026-10-25 COM-001.2 tenant review (policy approved):** learner independently identified cross-tenant cart injection and service+DB defenses. Reviewer added authenticated customer/cart ownership, and composite-FK requirement/option for relational enforcement. ADR-0004 accepted at policy level. Implementation, migrations, negative tests and concrete schemas remain unverified. Next COM-002: repository / local PostgreSQL / initial merchant-and-product vertical slice; local-only until authorization is implemented.

- **2026-10-25 COM-001 product-inception checkpoint:** implementing engineer approved the multi-merchant e-commerce product statement and user roles; V1 specific inclusions/exclusions and checkout account requirement are still being decided. No CommerceCore implementation verified.

- **2026-10-25 COM-001.3 (accepted):** implementing engineer selected customer account requirement for checkout, with public browsing. Guest checkout deferred; authentication/session mechanism and cart login UX remain open. ADR-0005. Next discrete engineering step: agree repository organization and initialize version control; no code/CI deployed or verified.
