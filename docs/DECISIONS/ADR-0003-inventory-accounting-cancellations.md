# ADR-0003 — Inventory accounting and cancellation semantics (ACCEPTED V1 LOGICAL POLICY)

**Date:** 2026-10-25
**Status:** Accepted for V1 business invariants and concurrency behavior after implementing engineer review; no code implemented. Physical dispatch choreography and HTTP response details remain open.
**Context:** COM-001 architecture workshop, scenario of two mugs allocated from five and subsequent merchant cancellation.

## Problem
A single `quantity` obscures physical stock, availability, and order allocations. Blindly increasing saleable stock on every cancellation could oversell or incorrectly restore damaged/missing units. Repeated or concurrent cancellation must not release an allocation twice.

## Accepted baseline policy
- Represent `on_hand`, `allocated`, and derived `available = on_hand - allocated` for one stock location in V1. This is a simplified accounting model, not a full warehouse ledger.
- At accepted simulated/pay-on-pickup checkout: allocate requested quantity and create an order in one transaction. Physical on-hand remains unchanged until fulfillment or inventory adjustment.
- Ordinary pre-fulfillment cancellation: mark eligible order `CANCELLED`, release its still-active allocation once in the same transaction. Derived availability increases accordingly.
- Known lost/damaged/missing stock: use an explicit, authorized, audited inventory adjustment to reconcile physical on-hand. Cancellation alone is not evidence that physical units are saleable.
- Reject or safely report cancellation for ineligible order states (especially fulfilled orders); V1 refunds/returns are separate workflows and not silently implemented as cancellation.
- Retry/cancel race safety comes from guarded state transitions and transactional database guarantees. An HTTP idempotency key may assist retries but must not be the sole prevention of double inventory release. Equivalent repeated cancellations can return the existing cancelled state without a second release; exact HTTP response contract still open.
- Invariants: `on_hand >= 0`, `allocated >= 0`, `available >= 0` (assuming no oversell); no order allocation released more than once; order transition and corresponding inventory movement commit or roll back together; tenant authorization applies.

## Accepted order-state transition policy (2026-10-25)
- `CONFIRMED -> CANCELLED` and `CONFIRMED -> FULFILLED` are mutually exclusive terminal transitions in the initial model; neither `CANCELLED -> FULFILLED` nor the reverse is valid.
- Under competing cancellation and fulfillment database transactions, whichever *valid transaction commits* first determines the result. The other must observe transition failure and must not apply stock effects.
- Atomic predicate / compare-and-set style update example: `UPDATE orders SET status = 'FULFILLED' WHERE id = ? AND status = 'CONFIRMED'`; check affected row count, with order state transition and inventory movement in the same DB transaction.
- A plain check-then-save in separate steps is insufficient because both callers may read stale `CONFIRMED`.
- A database commit cannot atomically undo a physical dispatch; operational dispatch state/hand-off policy needs further design before claiming physical fulfillment guarantees.

## Clarifications still needed
- Define cancellation eligibility per status, precise `fulfillment` inventory movements, partial orders / multi-line order effects, merchant stock adjustment policy, and API response semantics for repeated requests.
- Choose concurrency strategy after reviewing transaction races; compare locks, guarded SQL updates, and optimistic versioning with executed tests.
- The V1 accounting model may evolve to a durable movement ledger as complexity grows, with explicit migration and ADR.

## Verification required later
- Last-unit concurrent checkout; cancel twice sequentially and concurrently; cancel vs fulfill race; attempted restock when physical units are missing; rollback on failure injection; tenant-authorized adjustments. None verified yet.
