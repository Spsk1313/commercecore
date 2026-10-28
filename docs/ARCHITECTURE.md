# Architecture & threat boundaries

**Status:** Proposed architecture with a limited verified database baseline (Flyway V1 `merchants` table, JDBC and Testcontainers CI); module architecture and tenant security remain unimplemented. Updated 2026-10-27.

## Baseline design
- **Frontend:** React + TypeScript; separate merchant dashboard and customer storefront routes; loading/empty/error/conflict states are first-class UX.
- **API:** Java 21 + Spring Boot, modular monolith with explicit identity, catalog/inventory, ordering and fulfillment modules; public read endpoints separated from privileged write endpoints.
- **Persistence:** PostgreSQL; schema owned by Flyway; referential integrity and constraints; JPA repositories in persistence adapters (not exposed in controllers).
- **Delivery:** Git + CI; Dockerized local dependencies; one small deployment with HTTPS, externalized secrets, health checks and structured logs.
- **Async/cache:** No broker/Redis by default. Add only after proving a relevant workflow or bottleneck; record rationale and measured effect.

## Logical request sequence (proposal)
Browser -> HTTP controller -> authentication/authorization -> application service/domain policy -> transaction boundary -> repository -> PostgreSQL -> response DTO -> browser state.

## Operational/trust boundaries
1. Public customer-facing catalogue reads: abuse/rate limits and safe errors considered. Order submission and customer order access require authenticated identities (ADR-0005).
2. Authenticated merchant endpoints: authenticated principal decides store ownership; never trust path or body alone.
3. DB: least-privileged credentials, no secrets committed; constraints prevent invalid writes.
4. Browser: no private secrets in bundles, no trust in client-supplied amounts; avoid sensitive data in analytics/logs.
5. CI/deploy: scoped credentials, secret scanning/dependency checks, controlled migrations and rollback/recovery plan.

## Data model (not finalized)
Current approved V1 conceptual rules: `Merchant` is the tenant and operates one storefront; staff/user identity is distinct from merchant ownership; merchant-owned `Product`, `Cart` and `Order`; product has a V1 inventory record; `CartItem` and `OrderItem` represent lines. A customer can have many historical carts but at most one active cart per merchant. Checkout requires authentication (ADR-0005). Cart contents do not reserve stock; accepted checkout atomically allocates stock (ADR-0002). Order transitions and on-hand/allocated accounting follow ADR-0003. Service authorization must check cart ownership and merchant consistency; relational constraints should help enforce tenant consistency (ADR-0004). Only the initial Flyway V1 `merchants(id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, name VARCHAR(150) NOT NULL)` table and merchant-name check constraints are implemented and PostgreSQL-tested (PR #4). This does **not** implement tenant identity or ownership. All remaining physical tables, authentication/authorization, transactional SQL and HTTP error contracts are still unimplemented. Add specialized entities (e.g., idempotency/outbox/movements) only when implementing requirements that justify them.

## Architecture evolution gate
Propose enhancement -> identify measured bottleneck/failure -> consider cheapest alternative -> record ADR with expected impact -> implement and fault-test -> compare measured before/after -> keep or revert. Examples: index on slow filtered query; cache for demonstrated read pressure with invalidation plan; durable worker for notification retry, then broker only when warranted.

## Evidence TODO
Database migration/integration: [PR #4](https://github.com/Spsk1313/commercecore/pull/4), [passing CI](https://github.com/Spsk1313/commercecore/actions/runs/40616327894). Remaining business modules: unverified. Deployment: unverified. Load baseline: unmeasured. Threat-model review: pending. Diagram: pending learner-authored design.
