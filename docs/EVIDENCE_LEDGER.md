# Engineering evidence ledger

**Principle:** Evidence beats adjectives. Local application and
infrastructure verification has begun. No production deployment,
performance benchmark, or external-user outcome has been verified.

## Entry template

- **Date/time:**
- **Feature/incident ID:**
- **Claim / invariant:**
- **Independent work vs coaching supplied:**
- **Git commit / PR / diff:**
- **Environment:** OS, Java/Node/PostgreSQL versions, Docker/CI, dataset and resource limits.
- **Proof:** command, raw test/CI log, HTTP response, DB check, screenshot/screencast, trace, or benchmark output.
- **Negative/mutation/fault experiment:** exact induced failure, expected vs observed effect, recovery result.
- **Security and privacy handling:** authorization proof; sanitized outputs; no secrets/customer data.
- **Confidence / limitation:** what this does **not** prove.
- **External reviewer/pilot feedback:** who (with permission), what they actually did/said, and consent to publish.
- **Artifact link and next hypothesis:**

## Evidence score (internal, not public certification)

E0 planned -> E1 code shown -> E2 independently executed/tested -> E3 integrated/CI demonstrated -> E4 deployed and operated -> E5 external use/review with consent and attributable outcome. Each capability can have different levels. Do not infer one level from another.


## Verified implementation evidence

### 2026-11-09 — COM-003 verification foundation (PR #9 open)

- **Code/PR:** [PR #9](https://github.com/Spsk1313/commercecore/pull/9), application head `6e7be22`. V3 `users.email_verified_at` and `email_verification_tokens` with unique account/hash, timestamps and structural expiry check; secure 32-byte random token generation, URL-safe Base64, SHA-256 hashing of the raw token, guarded `UPDATE ... RETURNING` single-use claim, and transactional user verification. No raw token persistence.
- **Learner-run proof:** `env -u COMMERCECORE_DB_PASSWORD ./mvnw test` completed on 2026-11-09 15:56 Toronto with **32 tests, 0 failures/errors/skips**. Token generator/hash unit checks, token repository PostgreSQL single-use/expiry checks, service success and rollback integration tests passed. Earlier versions of suite also passed during implementation.
- **Mutation experiment:** Learner temporarily removed `@Transactional` from `EmailVerificationService.verify`; rollback test failed as expected because token claim persisted after subsequent state update failure. Annotation restored and test passed. This is evidence of transaction atomicity for the tested two-step workflow, not proof of all concurrent interleavings.
- **GitHub CI:** Backend CI and frontend CI **passed** on the PR #9 original application head `6e7be22` (GitHub Actions runs `42698924231` and `42698924048` respectively). Any new documentation commit requires fresh latest-head CI verification; do not attribute earlier runs to a later commit.
- **Ownership:** Learner implemented code and tests and performed local fault experiment with coaching/review; coach maintains project documentation. No independent delayed mastery assessment recorded.
- **Scope:** E2 local transactional workflow and PostgreSQL verification, with E3 reproducibility for initial PR CI checks within their tested scope. No registration/verify HTTP endpoint, secure password storage, email delivery, resend, session authentication, tenant authorization, simultaneous token-claim race test, deployed workflow or external user impact.

### 2026-11-08 — COM-003

### 2026-11-08 — COM-003 user/membership constraints (PR #8 merged)

- **Code/PR:** [PR #8](https://github.com/Spsk1313/commercecore/pull/8), **merged** into `main` as `fe7ae39` after latest-head backend/frontend CI passed (run IDs 42517097705 / 42517097800); initial application head `fafb3bf`; Flyway V2 adds users, functional unique index on `lower(email)`, and merchant memberships with OWNER/STAFF role constraint, non-null role, two foreign keys and unique (user_id, merchant_id).
- **Learner-run verification:** Local `env -u COMMERCECORE_DB_PASSWORD ./mvnw test` on 2026-11-08 reported **24 executions, 0 failures/errors/skips** (18 existing, six new). New Testcontainers PostgreSQL tests reject case-insensitive duplicate email, duplicate user/merchant membership, unsupported role, null role, missing user FK and missing merchant FK. Spring test-managed transactions roll back fixture inserts. [Backend Actions run 42516694613](https://github.com/Spsk1313/commercecore/actions/runs/42516694613) succeeded on initial PR head; [frontend run 42516694574](https://github.com/Spsk1313/commercecore/actions/runs/42516694574) succeeded as unaffected regression check. Those initial runs were followed by successful latest-head PR #8 backend/frontend runs before merge.
- **Learning attribution:** Learner designed and revised the SQL and wrote/refactored the six Java tests with mentor review and debugging guidance. Mentor maintains documentation. Independent delayed transfer assessment not yet performed.
- **Scope/limitation:** E3 for narrowly verified migration and integrity checks with passing latest-head PR CI; not authentication, safe password storage, case/whitespace canonicalization at registration, transactional merchant creation, tenant authorization, frontend product flow, deployment or real-user value. Negative tests assert `DataIntegrityViolationException`, not the precise PostgreSQL constraint identity. Tests using negative IDs rely on default positive generated identities; appropriate for this controlled test fixture.


### 2026-11-01 — COM-002 merchant read vertical slice (PR #7, merged)

- **Code/PR:** [PR #7](https://github.com/Spsk1313/commercecore/pull/7), application head `1d2396c`. Added a JDBC merchant repository, application service and not-found exception, MVC GET endpoint `/api/v1/merchants/{merchantId}`, 200 DTO, 404 ProblemDetail, and nonnumeric 400 handling. Frontend adds a Vite dev `/api` proxy, typed fetch client, temporary merchant-ID input with validation, loading and error states.
- **Learner-reported execution:** `env -u COMMERCECORE_DB_PASSWORD ./mvnw test` — **18 tests executed, zero failures/errors/skips**: 11 schema, 2 JDBC repository, 2 service, 3 MVC. Frontend lint/build green. No automated React component/browser E2E tests.
- **Manually observed:** Against running local Compose PostgreSQL, curl returned HTTP 404 ProblemDetail for ID 999, HTTP 400 for `abc`, and HTTP 200 `{"id":5,"name":"John & Sons"}` after inserting a temporary merchant. Test merchant deleted afterward; database confirmed empty. Vite-proxied curl on port 5173 returned the same 404 ProblemDetail. Learner reported successful browser search, missing-record display and throttled loading indicator.
- **Independent CI:** [Backend Actions run 41375979195](https://github.com/Spsk1313/commercecore/actions/runs/41375979195) and [Frontend Actions run 41375979226](https://github.com/Spsk1313/commercecore/actions/runs/41375979226) both succeeded at initial application head `1d2396c`. **Subsequent documentation-audit head `cd9e723` passed both [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41376782880) and [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41376782817).** Confirm checks on any newer commit before merge.
- **Confidence/scope:** E3 for a narrow *locally integrated read path* with separately verified CI checks. Does **not** prove an automated full-stack E2E test, secure merchant authorization, production operation, catalog/checkout functionality, performance, external use or product value. Unauthenticated merchant-read endpoint must stay local-only until an explicit privacy/publication and authorization policy is implemented. The ID-search screen is an integration harness.
- **Ownership:** Learner wrote application code and ran tests/manual verification with coaching; mentor authored documentation/review. No real-user feedback or deployed release verified. **PR #7 later squash-merged as `a42d639` into `main`, closing COM-002 local integration milestone as Verified.**

### 2026-11-01 — COM-002 React frontend scaffold and pull-request CI

- **Change under review:** [PR #6](https://github.com/Spsk1313/commercecore/pull/6), feature branch `feat/react-frontend-bootstrap`, initial submitted commit `a1c9097`, cleanup commit `d1ecd86`. Added React 19 + TypeScript + Vite 8 scaffold, npm lockfile, ESLint and a separate GitHub Actions frontend workflow. **PR #6 subsequently squash-merged into `main` as `aaed304` on 2026-11-01.**
- **Learner-verified local:** Ubuntu, Node 22.23.2, npm 10.9.8. `npm ci` installed 162 packages; `npm run lint` passed; `npm run build` passed (Vite 8.3.5, 20 modules transformed). Learner previously confirmed local Vite dev server and counter interaction/HMR. Later replaced the Vite demo with a minimal CommerceCore shell, removed unused CSS/assets, and reran lint/build successfully (Vite 8.3.5, 16 modules transformed).
- **Independent PR checks:** [Frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41347826942) job `verify` passed checkout, setup-node 22, `npm ci`, lint and build. [Backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41347826992) job `test` also passed on initial PR head `a1c9097`. After cleanup commit `d1ecd86`, [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41350363240) and [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41350363213) also passed. Documentation-only commits subsequently added require rechecking PR status.
- **Known security finding:** Local `npm audit` returned **12 moderate findings** traced to `minimatch@10.2.6` through ESLint/TypeScript-ESLint development dependencies; `npm audit --omit=dev` returned **zero reported findings**. No forced remediation. Full audit remains non-clean; revisit a compatible fix.
- **Evidence level:** E3 for frontend scaffold lint/build reproducibility on the tested PR commit, **not** for functional product behavior, frontend tests, deployed application or browser-to-database integration. Learner built and configured frontend/CI with mentor review; mentor authored documentation.
- **Outstanding after PR #6:** PR #7 subsequently implemented a local development integration path (see above). Authenticated merchant onboarding, catalog, deployment and real users remain pending.



### 2026-10-27 — COM-002 JDBC, Flyway V1 and PostgreSQL CI integration

- **Changes:** [PR #4](https://github.com/Spsk1313/commercecore/pull/4), merged into `main` as `6e3d326`. Adds Spring Boot JDBC/PostgreSQL/Flyway, datasource password placeholder, `V1__create_merchants.sql`, and Testcontainers-backed `MerchantSchemaIntegrationTest`.
- **Environment:** Java 21, Spring Boot 4.1.2, PostgreSQL 17 (local server log reported 17.10), Maven, Ubuntu/Docker; GitHub-hosted Ubuntu backend CI.
- **Observed local runtime:** Spring Boot started at `localhost:8080`, Hikari established a JDBC connection, Flyway reported applying V1 successfully and creating `public.flyway_schema_history`. Learner queried `\\d+ public.merchants` and history showing version 1, description `create merchants`, and `success = t`.
- **Manual negative/positive data experiments:** `'Café Québec'` inserted successfully within a rolled-back transaction. Whitespace-only, padded names and empty string failed PostgreSQL checks; generated identity sequence values continued to advance despite rollback or rejection.
- **Automated verification:** Local `env -u COMMERCECORE_DB_PASSWORD ./mvnw test` reported **11 executions, 0 failures/errors**: 1 metadata table-existence check, 5 invalid-name cases, 5 accepted-name cases (transactional rollback). GitHub Actions [Backend CI run 40616327894](https://github.com/Spsk1313/commercecore/actions/runs/40616327894) completed successfully on PR #4, exercising Testcontainers PostgreSQL without local `.env`.
- **Evidence level:** E3 for the narrow database-migration and integration-test capability; **not** E3 for complete commerce product functionality. Learner wrote and ran the code with mentor-supplied prompts/examples and review; no independent delayed assessment.
- **Limitations:** V1 merchants table has only ID/name, not authorization or complete tenant schema. Negative tests currently assert Spring's exception type rather than exact PostgreSQL constraint identity. No auth, browser flow, CI deployment, real customer usage, concurrency tests or production restore evidence.

### 2026-10-26 — COM-002 backend bootstrap and CI
- **GitHub:** [PR #1](https://github.com/Spsk1313/commercecore/pull/1), merged commit `ba058a1`; [passing CI run 40262686456](https://github.com/Spsk1313/commercecore/actions/runs/40262686456).
- **User-observed local outputs:** `./mvnw test` ran one passing Spring Boot application-context test; `/actuator/health` returned HTTP 200 and `UP`.
- **Scope:** Backend smoke-level reproducibility/CI (E3 for bootstrap only). Learner implemented and ran checks with code/review suggestions from coach; reviewer verified the PR and GitHub CI result.
- **Unproved:** Commerce features, DB integration, negative security paths, production operation, load results or real users.

### 2026-10-25/26 — COM-002 PostgreSQL development environment

**Status:** Locally verified; [PR #2](https://github.com/Spsk1313/commercecore/pull/2) merged as `0ec4cfc` on 2026-10-26.

**Environment:**

- Ubuntu Linux
- Docker Engine 29.7.2
- Docker Compose 5.5.0
- PostgreSQL 17 (official Docker image)

**Configuration:**

- PostgreSQL managed through Docker Compose.
- Host connection bound to 127.0.0.1:5433.
- Database files stored in a Docker named volume.
- Local database password excluded from Git.

**Verification performed:**

1. `docker compose config --quiet` completed without errors.
2. `docker compose ps` reported PostgreSQL as healthy.
3. SQL query confirmed the database and user were both `commercecore`.
4. Created a temporary `persistence_probe` table and inserted one record.
5. Executed `docker compose down` to remove the container.
6. Recreated the container using `docker compose up -d --wait db`.
7. Queried the table and successfully retrieved the original record.
8. Temporary probe table subsequently removed (learner-reported; cleanup query output not captured).

**Observed result:**
PostgreSQL retained the test record after container replacement. This constitutes local infrastructure evidence (E2), not database integration in CI. [PR #2 backend CI run 40266833274](https://github.com/Spsk1313/commercecore/actions/runs/40266833274) passed without exercising PostgreSQL. Local experiment and setup were learner-executed following coaching; reviewer inspected outputs and the submitted Compose configuration.

**Limitations (as of 2026-10-26; superseded by the 2026-10-27 PR #4 entry above):**

- Spring Boot was not yet connected to PostgreSQL.
- No database integration tests or database CI checks exist yet.
- Backup and disaster recovery have not been tested.
- No production deployment has occurred.

**Next step:**
Introduce Flyway-managed schema migrations and automated database integration verification.
