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
