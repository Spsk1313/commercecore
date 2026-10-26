# Operations and reproducibility runbook

**Status:** TEMPLATE / no deployed service yet. Do not publish invented deployment URLs or run commands as successful.

## Environments
- Local: development environment (to document after setup).
- Test: isolated disposable PostgreSQL via Testcontainers (to implement).
- Deployment: TBD; HTTPS, secrets, budgets and recovery plan required before real users.

## Fill in as verified
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
