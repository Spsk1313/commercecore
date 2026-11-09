# Quality gates and Definition of Done


**Status (2026-11-09):** COM-002 local walking skeleton Verified; PR #8 identity/membership migration merged; [PR #9](https://github.com/Spsk1313/commercecore/pull/9) email-verification foundation merged/Verified at the limited workflow level. **32 local backend tests** and final PR #9 backend/frontend CI passed at `598ebf3`; merged as `9d192d0`. These narrow proofs do **not** satisfy identity security or MVP release gates.

## Feature Definition of Done
A feature may be marked **Verified** only when:
1. User-visible behavior and non-goals are defined; API/UI contract and state transitions agreed.
2. Authorized and unauthorized paths considered; untrusted input, tenant scope and relevant secrets respected.
3. Data constraints and transaction/retry/idempotency expectations (if applicable) are explicit.
4. Learner implemented and explained the feature; tests appropriate to its risk ran and results are preserved.
5. Error paths, concurrency/failure cases and client loading/retry behavior are covered where they matter.
6. CI/build state known; change is reviewed; docs and evidence ledger reflect what actually happened.
7. Relevant regressions pass. If a check is intentionally deferred, it is tracked as an open risk, not called finished.

## Test strategy
- Unit tests: domain invariants, normalization, pricing and state transitions.
- API/slice tests: HTTP errors, DTOs, permissions and response contracts.
- PostgreSQL integration with Testcontainers: migrations, constraints, real isolation/transaction behavior.
- Concurrency tests: race on last unit, duplicate request/idempotency, order/stock atomicity.
- Browser E2E: merchant creates product -> storefront order -> fulfillment, including negative UI states.
- Security: cross-tenant resource access, forged IDs, unauthenticated writes, input abuse and secret-handling.
- Mutation/fault drill: purposely broken behavior and proof tests detect it; crashed worker/DB outage only when implemented and safe.
- Verify CI on clean checkout; tests without run evidence are not marked passed.

## Walking skeleton vs product feature

The COM-002 merchant-ID read path may be reviewed as a narrow integration proof with documented local tests, targeted MVC/JDBC checks and passing CI. It is **not** secure merchant functionality or a production release. COM-003 onward needs authenticated ownership, private-resource authorization and negative cross-tenant tests. No need to expand the temporary ID search for its own sake. See [PR #7](https://github.com/Spsk1313/commercecore/pull/7) and [evidence](EVIDENCE_LEDGER.md).


## Identity and verification foundation gates

- **PR #9 local checks:** random-token encoding, SHA-256 hash checks, single-use sequential consumption, expiry rejection, successful user verified-at update, rollback on a forced zero-row user update.
- **Transaction fault experiment:** removing service `@Transactional` caused the rollback test to fail; restoring it passed. Concurrent independent token-claim transactions are untested.
- **Before registration is Verified:** Argon2id password encoding/verification and parameter benchmark, email normalization/validation, duplicate registration/pending account response, abuse controls and rate limits, token issuance/delivery secrecy, resend/rotation/replay, verification API and error behavior, session/CSRF security, failed-delivery behavior and integration tests.
- **Before merchant ownership is Verified:** atomic merchant + OWNER creation, trusted principal, per-tenant authorization and negative cross-tenant tests.
- **Evidence:** green initial-head CI is not proof of later-head CI; open PR does not mean merged/released.

## MVP release checklist
- [ ] Product happy path demonstrated end-to-end.
- [ ] Account registration/password hashing and verified-email lifecycle implemented and tested, with secret-handling and abuse controls.
- [ ] Required login/session, CSRF where applicable, and negative authorization tests passed.
- [ ] Database schema is migrated through Flyway; rollback/backups considered.
- [ ] Concurrent last-unit purchase invariant verified against actual PostgreSQL.
- [ ] No real payment data handled in project V1.
- [ ] CI build/tests passing, minimal dependency and secret checks.
- [ ] External secrets, HTTPS, health/readiness, structured non-sensitive logs.
- [ ] Deployed demo reviewed, known limitations documented.
- [ ] Reproducible performance benchmark with hardware/data/profile recorded.
- [ ] README/runbook enable an engineer to reproduce essential operations.
- [ ] Evidence links and learner technical walkthrough current.

## Scale/optimization rule
Do not optimize without a hypothesis and baseline. Record environment, dataset, workload mix, concurrency, p50/p95/p99 where available, error rate, throughput, DB query plan, resource use, and before/after results. Million-user scale is hypothetical until tested at matching scope. Add cache/queue only with an invalidation/delivery strategy and a failure experiment.
