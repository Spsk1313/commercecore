# Operations and reproducibility runbook

**Status:** Local backend and PostgreSQL environment verified as of 2026-10-26. No deployed service or production availability.

## Environments
- Local: Java 21 / Spring Boot 4.1.2 backend; PostgreSQL 17 Docker Compose service, not yet connected to the backend.
- Test: isolated disposable PostgreSQL via Testcontainers (to implement).
- Deployment: TBD; HTTPS, secrets, budgets and recovery plan required before real users.

## Verified local operating procedure
From the repository root:
```bash
cp .env.example .env
# Edit .env with a private COMMERCECORE_DB_PASSWORD.
docker compose config --quiet
docker compose up -d --wait db
docker compose ps
docker compose exec -T db psql -U commercecore -d commercecore -c "SELECT current_database(), current_user;"
(cd backend && ./mvnw test)
```

The database is exposed only on `127.0.0.1:5433` (container port 5432); its database and user names are `commercecore`. Keep `.env` out of Git. PostgreSQL initializes its password on an empty data directory; editing `.env` afterward does **not** rotate the user's password inside an existing volume.

Use `docker compose down` to remove the local container while preserving its named data volume. **Do not use `docker compose down -v` for normal shutdown**, as it removes the volume. A probe-row experiment demonstrated persistence across container recreation, **not** backup or major-version upgrade safety.

Running `./mvnw spring-boot:run` from `backend/` and requesting `http://localhost:8080/actuator/health` gave HTTP 200 locally. Existing GitHub Actions run Maven backend tests only; CI does not yet start PostgreSQL or check Flyway migrations.

## Fill in as later capabilities are verified
- Prerequisites and version pins (Java/Node/Postgres/Docker).
- Exact bootstrap commands / migrations, and how to verify schema history.
- Configuration variables with **names only**; never passwords or tokens.
- How to reproduce a request from browser to database and inspect logs/traces.
- CI pipeline and dependency scanning; local parity limits.
- Health/readiness behavior; what fails when DB is down.
- Monitoring: log fields (trace/request ID, endpoint, status, latency; omit private data), error counters, latency distributions, DB pool and queue metrics only when implemented.
- Backup strategy, restore drill, rollback/migration compatibility and incident recovery.
- Capacity baseline and load-test methodology.

## Incident record template
Time, symptom, blast radius, hypothesis, exact reproducer, logs/metrics, root cause, mitigation, validation, prevention, affected ADR, supporting commits. Synthetic faults must be labeled simulated.
