# Architecture & threat boundaries

**Status (2026-11-08):** Local React → Spring Boot → JDBC → PostgreSQL merchant-read baseline Verified after [PR #7 merged](https://github.com/Spsk1313/commercecore/pull/7). [PR #8](https://github.com/Spsk1313/commercecore/pull/8) proposes the user/membership physical schema; authentication, enforced tenant ownership, full commerce modules and production deployment remain unimplemented.

## Baseline design
- **Frontend:** React + TypeScript; separate merchant dashboard and customer storefront routes; loading/empty/error/conflict states are first-class UX.
- **API:** Java 21 + Spring Boot, modular monolith with explicit identity, catalog/inventory, ordering and fulfillment modules; public read endpoints separated from privileged write endpoints.
- **Persistence:** PostgreSQL; schema owned by Flyway; referential integrity and constraints; Spring `JdbcClient` for the implemented merchant read; JPA/Hibernate are not installed. Persistence remains behind an application service.
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
Current approved V1 conceptual rules: `Merchant` is the tenant and operates one storefront; staff/user identity is distinct from merchant ownership; merchant-owned `Product`, `Cart` and `Order`; product has a V1 inventory record; `CartItem` and `OrderItem` represent lines. A customer can have many historical carts but at most one active cart per merchant. Checkout requires authentication (ADR-0005). Cart contents do not reserve stock; accepted checkout atomically allocates stock (ADR-0002). Order transitions and on-hand/allocated accounting follow ADR-0003. Service authorization must check cart ownership and merchant consistency; relational constraints should help enforce tenant consistency (ADR-0004). Flyway V1 `merchants(id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, name VARCHAR(150) NOT NULL)` with name checks was implemented and tested in PR #4. Flyway V2 users and merchant membership constraints are proposed and PostgreSQL-tested in PR #8 (see COM-003 section); this does **not** implement authenticated tenant ownership or authorization. A narrow HTTP GET contract now exists: `GET /api/v1/merchants/{merchantId}` returns `{id,name}` on 200; absent IDs produce 404 `ProblemDetail`; nonnumeric IDs produce 400. The repository maps `Merchant`, the service handles absence, and the controller maps to `MerchantResponse`. A Vite development-only `/api` proxy links the temporary React merchant-ID search to the backend. **No authentication, tenant authorization, publication rules or business write operations exist.** All other physical tables, order transactional SQL and broader API contracts remain unimplemented. Add specialized entities (e.g., idempotency/outbox/movements) only when implementing requirements that justify them.

## COM-003 database identity boundary (PR #8 under review)
- **Users:** `users(id, email, password_hash)` stores login accounts. A case-insensitive unique index on `lower(email)` prevents duplicate email strings differing only by case. Application validation/normalization and secure password-hashing implementation remain pending.
- **Merchant memberships:** `merchant_memberships(id, user_id, merchant_id, role)` relates a user to a business; `user_id` and `merchant_id` are non-null FKs, `role` is required and restricted to `OWNER`/`STAFF`, and `(user_id, merchant_id)` is unique. One person can operate multiple businesses; one business can have multiple staff/owners. Customers do not need a merchant membership simply to browse or check out.
- **Trust boundary:** The relation is persistence data, not authorization middleware. Never trust a client-supplied `user_id`, `merchant_id`, or `role` as proof of privilege; derive the principal from authenticated server-side identity and check membership/role per resource. Account registration, protected merchant APIs, transactional store+OWNER creation and negative cross-tenant tests are separate COM-003 implementation steps.
- **Verification:** Six real-PostgreSQL constraint rejection scenarios added in PR #8; 24 tests passed in the learner's local run and initial GitHub backend CI. No service/API auth or transactional onboarding behavior verified yet.

## Architecture evolution gate
Propose enhancement -> identify measured bottleneck/failure -> consider cheapest alternative -> record ADR with expected impact -> implement and fault-test -> compare measured before/after -> keep or revert. Examples: index on slow filtered query; cache for demonstrated read pressure with invalidation plan; durable worker for notification retry, then broker only when warranted.

## Evidence TODO
Database migration/integration: [PR #4](https://github.com/Spsk1313/commercecore/pull/4). Local merchant-read integration: [PR #7](https://github.com/Spsk1313/commercecore/pull/7) with [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/41375979195) and [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41375979226) passing at its initial head. No automated browser-to-database E2E test; do not expose the unauthenticated endpoint publicly. Remaining business modules: unverified. Deployment: unverified. Load baseline: unmeasured. Threat-model review: pending. Diagram: pending learner-authored design.
