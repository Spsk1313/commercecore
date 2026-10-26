# CommerceCore — Engineering Project

**Status:** Planning / no implementation or deployment verified as of 2026-10-25.
**Owner:** Learner (sole accountable implementing engineer).
**Coach:** Technical lead/reviewer; not substitute implementation author.
**Target:** A production-minded, independently built, Shopify-inspired commerce vertical slice by **2026-11-30** (scope gates, not a promise of Shopify feature parity).

## Mission
Demonstrate end-to-end engineering judgment on a real merchant/storefront/order/fulfillment workflow. Produce verifiable evidence of correctness, security, reliability, operability and design reasoning; optionally earn real pilot users, never invent them.

## Current scope
Merchant creates a store and product; customer browses, submits pay-on-pickup/simulated order; stock does not oversell under concurrent checkout; merchant manages order and fulfillment. Real card handling and payment processing are excluded from V1.

## Repository navigation
- `docs/PRODUCT_SPEC.md` — product behavior, scope, invariants, acceptance criteria.
- `docs/ARCHITECTURE.md` — proposed modules and actual system shape (keep accurate).
- `docs/DELIVERY_BOARD.md` — authoritative implementation status and next work.
- `docs/DECISIONS/ADR-0001-modular-monolith.md` — reasoned, revisitable choices.
- `docs/QUALITY_GATES.md` — definition of done, verification and release gates.
- `docs/EVIDENCE_LEDGER.md` — links to actual tests, commits, demonstrations, measurements.
- `docs/OPERATIONS.md` — startup, environments, deployments, monitoring, backup and recovery.
- `docs/CHANGELOG.md` — dated, user-visible and architectural changes.
- `docs/VISIBILITY.md` — sanitized case studies, demos, outreach and feedback.

## Source of truth
Once a Git repository exists, these files inside the repo are authoritative for product facts. The coach may keep a **brief mirrored checkpoint** in Software Prep records; it must link to/quote the actual commit and cannot claim to have reviewed local files or CI unless artifacts/logs were supplied or read from connected GitHub. Do not duplicate maintenance for no reason.

## Verified setup
Not yet documented. Add commands only after the learner runs them successfully. Never put secrets in Git.

## Product naming
CommerceCore is an independent training project. Inspired by *public* Shopify workflow and engineering literature, not affiliated with or an endorsed Shopify product. No proprietary code or interface assets copied.
