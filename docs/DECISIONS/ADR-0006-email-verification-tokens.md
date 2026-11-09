# ADR-0006 — Opaque single-use email verification tokens

**Status:** Provisional; mechanism merged in PR #9, explicit learner acceptance and delivery-policy review pending  
**Date:** 2026-11-09  
**Scope:** COM-003 identity verification foundation  
**Decider:** Implementing engineer (review/sign-off pending)

## Context

CommerceCore must verify ownership of an account email. A verification link is a bearer secret: anyone with a still-valid raw token may use it. We want short-lived, one-time server-revocable credentials that do not reveal usable tokens if an attacker obtains a read-only database dump, and one transaction that keeps user state consistent with token state. Registration, HTTP delivery and email transport are not yet implemented.

## Implemented mechanism (PR #9)

1. Generate **32 bytes / 256 bits** using Java `SecureRandom`; encode as unpadded URL-safe Base64. The result is an opaque high-entropy token, not a password or JWT.
2. Compute **SHA-256 over the UTF-8 raw token string**, store its lower-case hex digest in `token_hash`, never persist the raw token. Fast hashing is appropriate for a 256-bit random token; **passwords need a dedicated password KDF such as Argon2id**, not SHA-256.
3. Flyway V3 stores `users.email_verified_at` and `email_verification_tokens(user_id, token_hash, created_at, expires_at, consumed_at)`. `user_id` and `token_hash` are unique; one token row per user is supported. The check `expires_at > created_at` enforces timestamp ordering, not current validity.
4. `JdbcVerificationTokenRepository.consume` conditionally sets `consumed_at = now()` where the hash matches, `expires_at > now()` and `consumed_at IS NULL`; `UPDATE ... RETURNING user_id` claims one eligible token.
5. `@Transactional EmailVerificationService.verify(rawToken)` hashes and consumes the token, then marks `email_verified_at` if NULL. A failure to update exactly one user row raises an unchecked exception and **rolls back token consumption**.

## Alternatives and consequences

- **Raw tokens stored in database:** easy lookup, but a read-only database leak exposes usable links; rejected.
- **Slow password-style hashing for token:** unnecessary overhead for uniformly random 256-bit tokens; not selected. Passwords remain a separate security boundary.
- **Stateless signed token/JWT:** complicates revocation, one-time use and resend invalidation without state; not selected for V1.
- **Multiple outstanding tokens per user:** potentially convenient but complicates rotation; V3 chooses a single row. Resend and concurrent issuance behavior require later design.

## Verification and limitations

[PR #9](https://github.com/Spsk1313/commercecore/pull/9) has learner-reported **32 passing local backend tests** covering token format/hash, expiry rejection, sequential single use, successful verification and transaction rollback on a failed account transition. Learner temporarily removed `@Transactional`: rollback test failed; restoring it passed. PR #9 latest-head [backend CI](https://github.com/Spsk1313/commercecore/actions/runs/42701618751) and [frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/42701618610) passed on `598ebf3` before squash merge `9d192d0`. **Not proven:** simultaneous multi-connection consumption, full security audit, email sending, registration, HTTP API, session security or production readiness.

## Open security and product decisions

- Email normalization/validation, generic registration/resend responses against account enumeration, rate limiting and bot abuse controls.
- Pending-account squatting, token TTL/cleanup and retention, resend rotation/replacement, concurrent issuance and delivery failure/retry behavior.
- Secret-safe email links/HTTPS, URL/referrer/history/log leakage prevention, provider credential storage, idempotency and HTTP outcomes for expired/consumed/already-verified tokens.
- Account login/session/CSRF policy and negative authorization coverage.

**Revisit:** Before implementing the registration/verify HTTP and resend workflows. Mark Accepted only after explicit implementing-engineer review; code alone is not a formal approval.
