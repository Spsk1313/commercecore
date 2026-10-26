# Delivery board and change discipline

**Snapshot:** 2026-10-26.
**Status:** Initial implementation in progress. PRs #1 and #2 merged. Backend CI and local PostgreSQL persistence verified; no commerce business feature or deployment.

## Most recent session handoff — 2026-10-26 (Toronto)
- **Repository:** `main` and `origin/main` synchronized at `0ec4cfc`; clean worktree; merged feature branches removed (learner terminal output).
- **Verified:** Spring Boot 4.1.2/Java 21 startup and context test, HTTP 200 Actuator health; [PR #1 CI](https://github.com/Spsk1313/commercecore/actions/runs/40262686456) passed. PostgreSQL 17 container healthy, successful SQL query and probe-row persistence across recreation; [PR #2 CI](https://github.com/Spsk1313/commercecore/actions/runs/40266833274) passed **backend tests only**. Probe table cleanup was learner-reported.
- **Ownership/learning:** Learner wrote the implementation and executed local verification with coach review and provided commands. No delayed independent mastery assessment has yet been conducted.
- **Next action (one step at a time):** Explain why database schema must be versioned, then inspect current Maven dependencies and plan a first Flyway migration on a fresh feature branch. Implement migration, inspect schema history, then add Spring DB connectivity and isolated integration tests.
- **Not yet achieved:** Flyway/JDBC integration, merchant/product/order schema, React, authorization, deployed demo, production reliability/performance, real users.

## Backlog & status

| ID      | Capability                                                          | Status      | Owner   | Proof / link                                                 | Next gate                                                                  |
| ------- | ------------------------------------------------------------------- | ----------- | ------- | ------------------------------------------------------------ | -------------------------------------------------------------------------- |
| COM-001 | Feature specification, tenant model, API and concurrency invariants | In Progress | Learner | None                                                         | Design review + ADR                                                        |
| COM-002 | Local full-stack baseline: React, Spring, PostgreSQL, Flyway, CI    | In Progress | Learner | PRs [#1](https://github.com/Spsk1313/commercecore/pull/1) and [#2](https://github.com/Spsk1313/commercecore/pull/2) merged; backend CI and local DB tested | Flyway, backend database integration, frontend and end-to-end verification |
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
