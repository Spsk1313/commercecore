# Architecture & threat boundaries


**Status (2026-11-09):** PR #7 merchant-read walking skeleton and PR #8 V2 user/membership schema are merged (PR #8 merge `fe7ae39`). [PR #9](https://github.com/Spsk1313/commercecore/pull/9) is **merged** as `9d192d0` for the V3 verification schema and transactional service. Registration, session authentication, merchant authorization, complete commerce workflows and production deployment remain unimplemented.

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


## Implemented physical schema and boundaries

- **V1 (merged):** `merchants(id, name)` with generated identity, required name and nonblank/edge-whitespace/control-character checks.
- **V2 (merged PR #8):** `users(id, email, password_hash)` with a unique index on `lower(email)`; `merchant_memberships(id, user_id, merchant_id, role)` with required FKs, OWNER/STAFF role check and unique (user_id, merchant_id). This is schema integrity, **not** password hashing, principal authentication, registration or authorization.
- **V3 (merged PR #9):** nullable `users.email_verified_at` and `email_verification_tokens(id, user_id, token_hash, created_at, expires_at, consumed_at)`; unique `user_id` allows one stored verification token per user, and unique `token_hash` prevents duplicates. `CHECK (expires_at > created_at)` enforces timestamp ordering; real-time expiry is checked when attempting consumption.
- **Current HTTP read:** `GET /api/v1/merchants/{merchantId}` returns 200 `{id,name}`, 404 `ProblemDetail` if absent, and 400 for nonnumeric input. Temporary React merchant-ID search uses a Vite local `/api` proxy. The endpoint lacks authentication/publication policy and is **not safe to expose publicly without review**.
- **Merchant feature layout:** `merchant/api` → `merchant/application` → `merchant/persistence`. Implemented JDBC repo is behind the merchant query service; there is no merchant write or access control.
- **Identity layout (PR #9):** `identity/application` contains `VerificationTokenGenerator`, `VerificationTokenHasher`, `InvalidTokenException`, `EmailVerificationService`; `identity/persistence` contains `JdbcVerificationTokenRepository` and `JdbcUserRepository`. Generator uses `SecureRandom` 32 bytes, URL-safe Base64 without padding. Hasher uses SHA-256 of raw-token UTF-8 bytes; only lower-case hex digest is persisted.
- **Transaction:** `@Transactional EmailVerificationService.verify(rawToken)` hashes the raw token, conditionally consumes an unexpired, unconsumed token with `UPDATE ... RETURNING user_id`, and marks `email_verified_at` only if NULL. If user update changes anything other than one row, unchecked exception rolls back consumption. The repository uses `now()` for expiry and consumption time. Tests establish sequential single-use, expiry, success and rollback; they do **not** establish simultaneous multi-connection race outcomes.
- **Absent:** issuance/persistence from signup, raw-token email delivery, resend/rotation, verification HTTP controller, password encoder, Spring Security session/login, tenant guard and negative authorization tests. `VerificationTokenGenerator` is currently instantiated in tests; it is not an independently wired registration component.

## Planned domain model and trust boundaries

Merchant is the V1 tenant (one storefront). Products, inventory, carts and orders are merchant-owned; authenticated checkout, non-reserving carts, atomic stock allocation and cancellation/fulfillment rules remain **approved designs, not code** (ADRs 0002–0005). Service authorization must derive the principal from trusted authentication and verify membership/role and resource ownership; client IDs alone confer no privilege.

The token, hash-at-rest and transactional-consumption tradeoffs are recorded in [provisional ADR-0006](DECISIONS/ADR-0006-email-verification-tokens.md). Explicit acceptance and delivery/resend policy review remain pending.

**Next identity/security slices:** input normalization, validation and anti-enumeration/rate limits; Argon2id password hashing and parameter tuning; safe pending-account lifecycle and token issuance with delivery/resend failure policy; HTTP verification and authenticated session/CSRF boundaries; transactional merchant+OWNER creation and cross-tenant negative tests. These are planned, not complete.

## Architecture evolution gate
Propose enhancement -> identify measured bottleneck/failure -> consider cheapest alternative -> record ADR with expected impact -> implement and fault-test -> compare measured before/after -> keep or revert. Examples: index on slow filtered query; cache for demonstrated read pressure with invalidation plan; durable worker for notification retry, then broker only when warranted.


## Evidence and remaining work

- [PR #4](https://github.com/Spsk1313/commercecore/pull/4): V1 Flyway/Testcontainers.
- [PR #7](https://github.com/Spsk1313/commercecore/pull/7): manually verified local browser → API → PostgreSQL read and targeted CI, merged.
- [PR #8](https://github.com/Spsk1313/commercecore/pull/8): V2 schema, six integrity tests, merged; latest-head backend/frontend CI passed.
- [PR #9](https://github.com/Spsk1313/commercecore/pull/9): 32 backend tests locally, including verification service transaction fault experiment; latest-head backend/frontend CI passed before squash merge.
- No automated browser-to-DB E2E, public secure auth, concurrent checkout/token-consumption proof, deployment, backup restore, load baseline, formal threat review or user pilot. Do not imply these are shipped.
