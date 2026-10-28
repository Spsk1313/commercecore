# Operations and reproducibility runbook

**Status (2026-10-27):** Local Spring Boot ↔ PostgreSQL connection, Flyway V1 and Testcontainers tests verified; GitHub Actions backend CI passed. No deployed service or production availability.

## Environments
- **Local:** Java 21 / Spring Boot 4.1.2; PostgreSQL 17 via Docker Compose with loopback host port `127.0.0.1:5433`, database and user `commercecore`, named persistent volume.
- **Automated tests:** PostgreSQL 17 in disposable Testcontainers instances; Docker required. Spring Boot `@ServiceConnection` configures the test datasource independently of the local `.env`.
- **CI:** `.github/workflows/backend-ci.yml` uses Java 21 on an Ubuntu runner and invokes `./mvnw -B test`; [PR #4 run 40616327894](https://github.com/Spsk1313/commercecore/actions/runs/40616327894) succeeded.
- **Production/deployment:** Not implemented. HTTPS, secrets strategy, backup/recovery and secure authorization are open work.

## Local first-time setup and application startup

Execute from repository root unless stated otherwise:

```bash
cp .env.example .env
# Set your own private COMMERCECORE_DB_PASSWORD value in .env.
docker compose config --quiet
docker compose up -d --wait db
docker compose ps

# Export variables into the shell that launches the Java process.
# Source only a .env file you created/trust, using shell-compatible KEY=value syntax.
set -a
. ./.env
set +a

cd backend
./mvnw spring-boot:run
```

`.env` is ignored by Git and consumed by Docker Compose, but it is **not automatically inherited** by an independently launched Maven/Java process. The application's `application.yaml` references `${COMMERCECORE_DB_PASSWORD}`; never commit the real password or print it in logs. This runbook's local URL/username are development-only values.

Spring Boot's JDBC datasource connects to `jdbc:postgresql://127.0.0.1:5433/commercecore`. Flyway discovers `src/main/resources/db/migration/V1__create_merchants.sql`, creates `public.flyway_schema_history` if necessary, and records successful execution. The first migration creates `public.merchants` with generated identity ID and non-null/nonblank/whitespace/control-character name constraints.

### Inspect local PostgreSQL and migrations

In another terminal from repository root:

```bash
docker compose exec -T db psql -U commercecore -d commercecore -c '\\d+ public.merchants'
docker compose exec -T db psql -U commercecore -d commercecore -c \
  "SELECT installed_rank, version, description, success FROM public.flyway_schema_history ORDER BY installed_rank;"
```

The learner observed V1 with `success = true` and inspected all three named merchant-name checks on 2026-10-27. **Do not modify an already-applied versioned migration**; add a new version for schema changes so checksum validation remains meaningful.

### Run isolated integration tests

With Docker Engine available, from `backend/`:

```bash
env -u COMMERCECORE_DB_PASSWORD ./mvnw test
```

The test class `persistence/MerchantSchemaIntegrationTest` supplies a PostgreSQL Testcontainers service connection, runs Flyway, verifies the table exists, rejects five invalid-name inputs, and inserts/reads five valid names with test-managed transaction rollback. Eleven test executions passed locally; [CI on PR #4](https://github.com/Spsk1313/commercecore/actions/runs/40616327894) passed on a GitHub-hosted runner without local credentials. No Compose database or preexisting test records are needed.

Use `docker compose down` (from the repository root) to remove local containers while keeping the named data volume. **Never use `docker compose down -v` for routine shutdown**: it removes the named volume. Changing `.env` later does not rotate credentials already initialized within an existing PostgreSQL data volume.

## Limitations and future operational work
- Local Spring Boot startup and health are verified; no publicly deployed service, HTTP business API or browser-to-database flow exists.
- Database constraints are tested, but merchant identity/tenant isolation, backup restore, migration downgrade procedures, concurrency and production security remain unverified.
- Define deployment parameters, secret rotation, HTTPS, observability, runbooks and backup/recovery drills before handling real users.

## Incident record template
Time, symptom, blast radius, hypothesis, exact reproducer, logs/metrics, root cause, mitigation, validation, prevention, affected ADR, supporting commits. Synthetic faults must be labeled simulated.
