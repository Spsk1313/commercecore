# ADR-0002 — Cart does not allocate inventory; V1 order submission does

**Status:** Accepted for CommerceCore V1 product behavior (implementation pending)
**Date:** 2026-10-25
**Decider:** Implementing engineer, after staff-engineer review

## Context
Customers may abandon carts. Holding stock at add-to-cart can starve ready-to-buy customers and requires expiring holds, background release jobs, and safe expiry-vs-checkout coordination. The tradeoff is that an item visible in the cart may sell out before checkout completes.

## Decision
- A cart represents intent, not a stock reservation or promise of fulfillment.
- Availability is checked at cart display/checkout for UX, but every final V1 order attempt must atomically validate and allocate finite stock with order creation in PostgreSQL.
- Concurrent attempts for the last unit may both reach checkout. At most one may create a successful order; stock cannot become negative; the other gets a clear stock-conflict response and action to update the cart.
- A cart quantity is not a guarantee of purchase. Display this honestly, keep cart totals as estimates until server-authoritative order creation, and revalidate inventory and prices at the final commit boundary.
- In V1, simulated/pay-on-pickup orders allocate stock at successful order creation; actual payment capture/reservations are out of scope. Revisit this policy if real payment processing or short-lived reservations become a validated requirement.

## Alternatives
1. Hold at add-to-cart with TTL: earlier buyer certainty but inventory hoarding, resource starvation, persistence and expiry race complexity.
2. Allocate on successful checkout: simpler and fairer to ready buyers but a late stock-out is possible (chosen).

## Consequences / UX mitigation
Validate availability at checkout entry and on relevant steps, show clear out-of-stock errors, offer cart quantity adjustment and recovery, and never suggest cart additions reserve items. The final check/atomic operation remains authoritative even if earlier checks passed. Separate truly scarce-event/ticket inventory as a future product-specific policy.

## Evidence and verification required
- Unit/integration/concurrent purchase tests against PostgreSQL and at least one observed controlled stock race.
- UI scenario showing a stale cart and clear, recoverable checkout rejection.
- Latency/locks and unsuccessful checkout rate measured when deployed.

## Reference
Shopify Help Center > Checkout: checkout steps recheck inventory; hold when payment information is submitted; inventory hold released on payment failure. This is a reference for the product tradeoff, not a claim of identical internal implementation.
