# Product specification — CommerceCore


**Status (2026-11-09):** Product direction and authenticated-checkout policy approved (2026-10-25); detailed V1 release scope remains under review. Local COM-002 integration and V2 identity/membership schema are merged. [PR #9](https://github.com/Spsk1313/commercecore/pull/9) merged the email-verification foundation (not a complete registration feature). No account signup/login, security-enforced merchant flow or commerce journey has shipped.

## Approved product statement — 2026-10-25
> CommerceCore is a multi-merchant e-commerce application where independent businesses manage their own product catalogues and inventory, customers browse merchant products and place orders, and merchants fulfill orders through a secure web application.

Implementing engineer explicitly approved: e-commerce, multiple merchant tenants, secure web UI, merchant-controlled product catalogues and inventory, customer browsing/order submission, and merchant fulfillment. This approves product direction only; it does NOT authorize marking feature scope, payment/auth details, implementation, CI, or deployment verified.

### Proposed minimal release boundary (pending review)
- Included: merchant account and isolated admin area; one storefront per merchant; product publication and inventory; public catalogue browsing; cart; simulated/pay-on-pickup checkout with trusted totals, last-unit safety, and idempotent retries; merchant order confirmation/fulfillment/cancellation; basic operational tests and reproducible deployment.
- Deliberately not included initially: real payments, returns/refunds, taxes/shipping calculations, discounts, reviews, marketplace search, advanced analytics, Redis, message broker, AI, microservices. Those may be later increments after a working verified release.
- **Approved in COM-001.3:** V1 checkout requires an authenticated customer account. Catalogue browsing remains public. Guest checkout and anonymous-cart merging are excluded from the initial release. Cart login timing is still to be specified; do not infer that all cart UI actions require login merely from the checkout decision.


## Implemented foundation and current COM-003 checkpoint — PRs #7–#9

[PR #6](https://github.com/Spsk1313/commercecore/pull/6) established the React frontend. [PR #7](https://github.com/Spsk1313/commercecore/pull/7) merged the temporary local merchant-ID read path: backend 200 response, 404 ProblemDetail and 400 for invalid path types; React uses a Vite development proxy. This is **not** merchant onboarding, an authorized dashboard, a public storefront or verified market value.

[PR #8](https://github.com/Spsk1313/commercecore/pull/8) **merged** Flyway V2 `users` and `merchant_memberships` (case-insensitive email uniqueness, role/FK and pair-uniqueness constraints). Learner reported 24 local tests; latest-head CI passed. This schema does not validate credentials, hash passwords, authenticate sessions or enforce tenant isolation.

[PR #9](https://github.com/Spsk1313/commercecore/pull/9) **merged** as `9d192d0`: V3 adds `email_verified_at` and single-row-per-user token storage; service uses secure random token/hash utilities and one database transaction to consume a valid token and mark an account verified. Learner reported 32 backend tests and successful rollback mutation experiment; final-head PR #9 CI passed. **There is no registration or verify HTTP endpoint, token issuance/email sending, resend/rotation, login or production rollout.**

The token-design rationale is recorded in [provisional ADR-0006](DECISIONS/ADR-0006-email-verification-tokens.md); public signup and delivery policy remain open.

**Next product-enabling work:** implement secure registration and pending-email verification (including Argon2id, input validation and abuse controls), email-delivery strategy and verification API; then authenticated sessions, atomic merchant+OWNER membership creation, and negative tenant authorization tests before catalog work.

## Users and jobs
- **Merchant/operator:** create an account/store, manage sellable items and finite inventory, review orders, fulfill or cancel orders.
- **Customer:** browse store publicly, authenticate to submit a simulated/pay-on-pickup order, get clear success/failure and authorized access to their orders.
- **Administrator/developer:** operate the system without accessing tenant data casually or storing payment credentials.

## Proposed V1 journey
1. Merchant authenticates and creates a store.
2. Merchant creates/publishes a product with a price and on-hand quantity.
3. Customer views the public storefront, authenticates before order submission, and submits an order with one or more lines.
4. Server validates price/inventory using trusted server data, not client-submitted totals.
5. Inventory is safely allocated; order is stored atomically or clearly rejected.
6. Merchant views and updates the order through allowed transitions.
7. Both interfaces handle loading, invalid input, conflicts and retries without ambiguous state.

## Nonnegotiable invariants (to finalize in design review)
- One merchant cannot read, mutate, enumerate or fulfill another merchant's private resources (tenant isolation, including object IDs).
- Inventory is never negative; a single remaining unit cannot be sold twice concurrently.
- Monetary amounts use decimal/integer minor units, not floating point. Order-line price snapshot prevents historical repricing from changing placed orders.
- Repeated submission using the same legitimate idempotency key cannot create multiple orders; exact replay semantics and key retention TBD.
- Order status changes follow an explicit state machine; fulfillment isn't replayed as a second side effect.
- A failed order transaction cannot leave partially committed order/inventory state.
- Client-supplied merchant ID, price, authorization role or order total must not be trusted without server verification.

## Accepted policy decisions (2026-10-25)
- **ADR-0002:** Cart additions are non-reserving. For simulated/pay-on-pickup V1 checkout, successful order submission atomically allocates stock and creates the order. A stale cart may encounter a recoverable stock conflict at checkout. The product must not promise that cart contents are guaranteed.
- Still open: exact API status/error payload, concrete locking implementation, and physical dispatch workflow. **ADR-0003 is accepted** for V1 logical inventory accounting and mutually exclusive `CONFIRMED -> CANCELLED` or `CONFIRMED -> FULFILLED` transitions, with atomic database enforcement and matching inventory movement.

## Initial feature backlog (priority, not calendar promise)
- COM-001 Architecture/contract and threat model (design only)
- COM-002 Development skeleton: version control, Spring Boot, React/TS, PostgreSQL, Flyway, Docker compose and CI
- COM-003 Merchant identity, store setup, roles and isolation
- COM-004 Product catalog, publication and inventory management
- COM-005 Public storefront, cart and accessible front-end states
- COM-006 Atomic order creation, price snapshot and no-oversell concurrency behavior
- COM-007 Merchant order workflow and idempotent updates
- COM-008 Automated tests, observability, delivery automation, security verification and deployment
- COM-009 Measured load/fault drills, documented limits, demo and external review

## Deliberate V1 exclusions
No real card collection; no marketplace discovery engine; no multi-region service mesh; no preemptive microservices; no mandatory AI assistant, Redis or message broker; no claim of production SLA or million-user capacity. Advanced capabilities require a justified requirement, hypothesis, metric and ADR.

## Open design questions (learner owns)
- Registration validation, Argon2id parameters, verification-token issuance/delivery/resend, session authentication and CSRF/security implications?
- Merchant/store/tenant relationship and customer privacy?
- How will order idempotency behave if a request times out after commit?
- How exactly does cancellation return allocated stock without double-release? V1 allocation decision is accepted in ADR-0002.
- Stock schema/constraints and transaction isolation/locking strategy?
- How will money and tax handling be represented within simulated checkout scope?
- Will any actual pilot merchant participate? If yes, consent, data handling and support expectations?

## Acceptance criteria for V1
Working browser-to-API-to-PostgreSQL flow, protected merchant write APIs, appropriate authorization negative tests, concurrent last-unit checkout tests, reliable order retrieval/fulfillment, deterministic CI, deployment with HTTPS/secrets/health checks, diagnostics and truthful walkthrough. Evidence belongs in `EVIDENCE_LEDGER.md`.

## 2026-10-25 design review: inventory and cancellation (accepted logical policy)
The learner identified that cancellation reason matters when physical inventory is missing or unsellable, and that retrying cancellation must not double-release stock. ADR-0003 accepts separating physical on-hand from allocated and available stock, releasing an existing allocation at most once, and modeling physical discrepancies with a distinct audited stock adjustment. The learner agreed that concurrent cancellation/fulfillment cannot both succeed; atomic guarded transitions and transactional stock accounting will be required. Physical dispatch orchestration remains open. No behavior implemented.

## 2026-10-25 design review: tenant isolation (policy approved, implementation pending)
Merchant is the V1 tenant and operates one storefront. Products, Carts and Orders are merchant-owned. A customer may have historical carts but at most one active cart per merchant. Adding a product from a different merchant to a cart is forbidden. The authenticated customer must also be authorized to modify the cart. Use service/domain authorization and matching merchant ownership, backed where practical by relational constraints including composite foreign keys; ordinary independent foreign keys do not enforce equality of parent merchant IDs. See ADR-0004. API error/status behavior and schema to be implemented; no tests or code verified.

## 2026-10-25 COM-001.3 accepted decision — authenticated checkout
- A customer must be authenticated before placing an order in V1. Product browsing remains public.
- Customer identity/ownership and authorization must be checked server-side; the client must not choose an arbitrary `customer_id`.
- Guest checkout, guest-order recovery, and anonymous-cart merging are deferred.
- Record user-experience tradeoff: requiring login adds conversion friction; measure when pilot users exist.
- Unresolved implementation choices: account/session mechanism, exact cart-before-login UX, merchant staff role model, and formal HTTP contract; do not label them implemented.
- See `DECISIONS/ADR-0005-authenticated-checkout.md`.
