# Delivery board and change discipline

**Snapshot:** 2026-11-01 (America/Toronto).
**Status:** COM-002 In Review. PRs #1–#6 merged. [PR #7](https://github.com/Spsk1313/commercecore/pull/7) implements a local merchant-read browser → Spring Boot → PostgreSQL flow. Backend and frontend CI passed at the audited documentation head `cd9e723` (see evidence ledger). This is a development integration harness, not authenticated merchant functionality, deployment or verified user value.

## Most recent session handoff — 2026-11-01 (Toronto)
- **Current branch/PR:** `feat/merchant-read-vertical-slice` / [PR #7](https://github.com/Spsk1313/commercecore/pull/7), based on merged PR #6 (`main` at `aaed304`). Initial code head `1d2396c`; documentation-review head `cd9e723` passed backend/frontend CI. After any subsequent commit, verify checks at that new head before merging.
- **Implemented:** React 19 + TypeScript/Vite with `/api` development proxy and temporary merchant-ID lookup (validation, loading, success/error); Spring Boot MVC controller, `MerchantResponse`, service, JDBC repository and 404 ProblemDetail handler. No protected merchant management or product workflow.
- **Verified:** Learner reported 18 passing backend test executions and green frontend lint/build; tested 200/404/400 over live Spring Boot, retrieved a disposable merchant inserted in PostgreSQL, verified Vite proxy and browser lookup/loading, then removed test data. [PR #7 backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41375979195) and [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41375979226) passed on initial head.
- **Open risk:** `npm audit` showed 12 moderate development-dependency findings involving `minimatch@10.2.6`; `npm audit --omit=dev` reported none. Resolution pending, not claimed secure by default.
- **Next action:** Verify PR #7 latest-head CI and squash-merge, then prioritize COM-003 merchant identity, secure store onboarding and cross-tenant authorization. Do not polish the temporary ID-search UI as a product feature.
- **Ownership:** Learner authored and manually verified application behavior with coaching; mentor maintains documentation. No complete commerce flow or real-user validation.

## Previous session handoff — 2026-10-27 (Toronto)
- **Repository:** `main` and `origin/main` synchronized at `6e3d326`; learner showed a clean worktree and only local/remote `main` branches after squash-merging PR #4.
- **Implemented:** Spring Boot 4.1.2 / Java 21 backend, PostgreSQL 17 local Docker Compose volume, JDBC datasource with secret via environment variable, Flyway `V1__create_merchants.sql`, and `MerchantSchemaIntegrationTest` with PostgreSQL Testcontainers. Merchant table currently models only generated ID and display name, not tenant access controls.
- **Verified:** Spring started locally, initialized Hikari connection pool, executed Flyway V1 and wrote successful migration history; learner inspected the table constraints and history via `psql`. Manually verified valid and rejected names. Local `env -u COMMERCECORE_DB_PASSWORD ./mvnw test`: **11 test executions passed** (one schema check, five negative, five positive). [PR #4 GitHub CI](https://github.com/Spsk1313/commercecore/actions/runs/40616327894) passed on a separate runner. See [evidence ledger](EVIDENCE_LEDGER.md).
- **Ownership/learning:** Learner implemented SQL, tests, and ran verification with coaching/review; learned schema migrations, database constraint boundaries, transaction isolation basics and test transaction rollback. Mentor authored this documentation handoff in a separate PR. Delayed independent mastery assessment not yet performed.
- **Historical next action at 2026-10-27:** React bootstrap followed by browser-to-database integration, both subsequently executed locally; current action is PR #7 review/merge.
- **Still unverified:** Auth/tenant isolation, product/order/checkout APIs, automated browser E2E, deployment, backup/restore, load/performance and external customer value. Frontend integration was subsequently verified locally in PR #7.

## Backlog & status

| ID      | Capability                                                          | Status      | Owner   | Proof / link                                                 | Next gate                                                                  |
| ------- | ------------------------------------------------------------------- | ----------- | ------- | ------------------------------------------------------------ | -------------------------------------------------------------------------- |
| COM-001 | Feature specification, tenant model, API and concurrency invariants | In Progress | Learner | [Product spec](PRODUCT_SPEC.md); [ADRs 0002–0005](DECISIONS/) accepted at policy level; [ADR-0001](DECISIONS/ADR-0001-modular-monolith.md) remains proposed | Approve/revise ADR-0001; agree COM-003 identity/store/authorization boundary and essential V1 scope; defer detailed later-feature contracts until their feature work |
| COM-002 | Local full-stack baseline: React, Spring, PostgreSQL, Flyway, CI    | In Review | Learner | [PR #6 merged](https://github.com/Spsk1313/commercecore/pull/6); [PR #7 open](https://github.com/Spsk1313/commercecore/pull/7); 18 local backend tests; [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41376782880) and [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41376782817) passed at `cd9e723` | Confirm latest-head checks and squash-merge PR #7; then mark baseline Verified (not Released) |
| COM-003 | Merchant auth/store isolation                                       | Planned     | Learner | None                                                         | Negative cross-tenant tests                                                |
| COM-004 | Merchant catalog and inventory                                      | Planned     | Learner | None                                                         | API/UI contract + DB constraints                                           |
| COM-005 | Public storefront and cart                                          | Planned     | Learner | None                                                         | End-to-end browser flow                                                    |
| COM-006 | Order transaction/concurrency                                       | Planned     | Learner | None                                                         | Last-unit race test + rollback proof                                       |
| COM-007 | Order fulfillment and replay handling                               | Planned     | Learner | None                                                         | State-transition and idempotency tests                                     |
| COM-008 | Secure deployed MVP, CI, observability                              | Planned     | Learner | None                                                         | Runbook + demo + operational checks                                        |
| COM-009 | Fault/load study and public engineering case study                  | Planned     | Learner | None                                                         | Reproducible benchmark + sanitized post                                    |

**Allowed statuses:** Proposed -> Ready -> In Progress -> In Review -> Verified -> Released; Blocked can occur at any stage. Never mark Verified based only on a plan, code paste, or an unrun test.

## Milestone closure criteria (scope-limited)

- **COM-001 (architecture/product contracts):** Close when the modular-monolith decision (ADR-0001) is expressly accepted or revised, a feasible V1 capability boundary is agreed, and COM-003 identity/store ownership/authorization assumptions are specific enough to implement securely. Detailed checkout locking, payment, order status HTTP payloads and later-feature decisions can remain open and be decided with their own stories; record each remaining question and owner. Approved ADR-0002–0005 are **design agreements**, not implemented code.
- **COM-002 (local development foundation):** Baseline complete at the capability level after reproducible Spring Boot/React/PostgreSQL/Flyway/Compose workflows, passing targeted backend and frontend CI, and verified local browser-to-database integration. PR #7 remains **In Review** until merged; after green latest-head checks and squash merge, mark COM-002 **Verified**, not **Released**. Authentication, public deployment and commerce user value belong to later milestones.

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

- **Historical COM-001 cancellation design:** ADR-0003 was proposed and subsequently accepted for V1 logical policy on 2026-10-25; code and tests remain pending.

- **2026-10-25 COM-001.2 tenant review (historical):** ADR-0004 accepted at policy level; tenant implementation, negative security tests and ownership schema remain outstanding. Current PR #7 merchant-read API is development-only until its visibility/authorization contract is defined.

- **2026-10-25 COM-001 product-inception checkpoint (historical):** Product direction and roles approved; the checkout-account requirement was subsequently accepted in ADR-0005. This entry describes that earlier design checkpoint.

- **2026-10-25 COM-001.3 (accepted):** Customer login required at checkout, public browsing retained, guest checkout deferred (ADR-0005). Auth/session choice and cart UX remain open; historical repo setup tasks were subsequently completed.
