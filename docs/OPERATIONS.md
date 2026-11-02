# Operations and reproducibility runbook

**Status (2026-11-01):** [PR #6](https://github.com/Spsk1313/commercecore/pull/6) merged; [PR #7](https://github.com/Spsk1313/commercecore/pull/7) adds a locally verified React → Spring Boot → PostgreSQL merchant-read flow. Initial PR #7 backend/frontend CI passed. No production deployment, secure merchant workflow or tenant authorization. Post-documentation [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41376782880) and [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41376782817) passed at audit head `cd9e723`; verify the latest commit before merge.

## Environments
- **Local:** Java 21 / Spring Boot 4.1.2; PostgreSQL 17 via Docker Compose with loopback host port `127.0.0.1:5433`, database and user `commercecore`, named persistent volume.
- **Automated tests:** PostgreSQL 17 in disposable Testcontainers instances; Docker required. Spring Boot `@ServiceConnection` configures the test datasource independently of the local `.env`.
- **CI:** `.github/workflows/backend-ci.yml` runs Maven tests on Java 21 ([PR #7 run](https://github.com/Spsk1313/commercecore/actions/runs/41375979195) passed on initial head); `.github/workflows/frontend-ci.yml` runs `npm ci`, lint and build with Node.js 22 ([PR #7 run](https://github.com/Spsk1313/commercecore/actions/runs/41375979226) passed on initial head).
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

The test class `persistence/MerchantSchemaIntegrationTest` supplies a PostgreSQL Testcontainers service connection, runs Flyway, verifies the table exists, rejects five invalid-name inputs, and inserts/reads five valid names with test-managed transaction rollback. The later PR #7 backend suite reported **18 executions passed locally**: 11 schema tests, 2 real PostgreSQL repository tests, 2 Mockito service tests and 3 Spring MVC tests. [PR #7 backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41375979195) passed on its initial head. No Compose database or preexisting test records are needed.

Use `docker compose down` (from the repository root) to remove local containers while keeping the named data volume. **Never use `docker compose down -v` for routine shutdown**: it removes the named volume. Changing `.env` later does not rotate credentials already initialized within an existing PostgreSQL data volume.

## Frontend local operations (PRs #6–#7)

From the repository root, with Node.js 22 and npm installed:

```bash
cd frontend
npm ci
npm run dev
# Separate terminal in frontend/: npm run lint && npm run build
```

The Vite development server prints its URL (previously localhost:5173). PR #7 adds a Vite development proxy of `/api` to `http://localhost:8080` and a temporary merchant-ID UI using the Spring Boot endpoint. The Vite proxy is not a production gateway. `npm ci` depends on the committed lockfile. `dist/` and `node_modules/` are ignored. npm audit recorded 12 moderate development dependency findings involving `minimatch@10.2.6` and zero findings with `--omit=dev`; the full-audit risk remains open. See [frontend README](../frontend/README.md).

### Local merchant-read smoke test (development only)

With PostgreSQL, backend and frontend all running, from the repository root:

```bash
# Confirm the Vite proxy reaches the backend's not-found handler.
curl -i http://localhost:5173/api/v1/merchants/999

# Insert a temporary merchant; note the returned generated ID.
docker compose exec -T db psql -U commercecore -d commercecore \
  -c "INSERT INTO merchants (name) VALUES ('Local Test Merchant') RETURNING id;"

# Substitute the generated ID for <id>.
curl -i http://localhost:8080/api/v1/merchants/<id>
# In the browser at localhost:5173, search for that ID and check the display.

# Remove the disposable row once verified.
docker compose exec -T db psql -U commercecore -d commercecore \
  -c "DELETE FROM merchants WHERE id = <id>;"
```

**Security boundary:** This merchant-ID endpoint is unauthenticated and has no tenant/publication policy. Keep it local-only; do not publicly deploy it or insert real customer data. The production frontend requires separate API routing and authorization design.

## Limitations and future operational work
- Local Spring Boot health, narrow merchant-read API and browser-to-database flow were verified manually; no public deployment, protected merchant workflow or complete commerce experience exists.
- Database constraints are tested, but merchant identity/tenant isolation, backup restore, migration downgrade procedures, concurrency and production security remain unverified.
- Define deployment parameters, secret rotation, HTTPS, observability, runbooks and backup/recovery drills before handling real users.

## Incident record template
Time, symptom, blast radius, hypothesis, exact reproducer, logs/metrics, root cause, mitigation, validation, prevention, affected ADR, supporting commits. Synthetic faults must be labeled simulated.
