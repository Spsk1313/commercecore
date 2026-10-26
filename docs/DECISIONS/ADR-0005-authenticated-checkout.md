# ADR-0005 — Authenticated checkout for CommerceCore V1

**Status:** Accepted (product policy; not implemented)
**Date:** 2026-10-25
**Owner:** Implementing engineer; reviewed with engineering coach

## Context
We need a clear customer order-ownership and security boundary for the first end-to-end commerce release. Guest checkout could improve conversion but adds anonymous identity, session lifecycle, order access/recovery, guest-to-account cart merging and extra abuse cases.

## Decision
- Public product/store browsing does not require a customer account.
- Placing an order in V1 requires a successfully authenticated customer.
- Order ownership is derived from authenticated principal on the backend, never trusted from request-supplied customer IDs.
- Customers can only view their own private order data; merchants may manage only orders for their authorized tenant.
- Defer guest checkout and anonymous cart-to-account merging; precise cart login timing is still open.

## Tradeoffs
- Favor simpler initial identity, order history and authorization testing.
- Accept checkout signup/login friction and the possibility of abandoned checkouts; reassess based on actual usage and user research.
- This policy does not predetermine session vs token mechanics or identity-provider choice.

## Verification required before marking implemented
- Unauthenticated checkout is rejected; public catalogue remains accessible.
- Authenticated A cannot order as or view private orders of customer B.
- Merchant A cannot view/fulfill merchant B's orders; denied operations do not mutate state.
- Integration tests and documented HTTP security behavior.

## Follow-up
Decide principal/account and merchant-membership model when designing identity. Integrate implementation and tests into COM-003/COM-006.
