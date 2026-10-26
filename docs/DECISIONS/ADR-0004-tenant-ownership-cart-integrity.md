# ADR-0004 — Tenant ownership, cart authorization and relational integrity

Date: 2026-10-25
Status: Accepted policy; implementation/schema design pending
Owner: Implementing engineer; reviewer: engineering coach

## Context
CommerceCore V1 uses Merchant as the tenant (one storefront per merchant). A customer's cart for merchant B must not contain a product owned by merchant A. A malicious or mistaken request may send any product ID or cart ID. A matching product-merchant relation by itself is not sufficient: the caller must also be authorized to modify the particular cart.

## Decision
- Authoritative merchant ownership: Merchant owns Products, Carts and Orders. Product inventory belongs to Product. CartItem connects Cart and Product.
- Resolve caller identity from authenticated credentials/session, never trust an arbitrary client-provided customer or merchant ID as permission.
- At the service/domain boundary, authorize cart ownership, then require cart.merchantId == product.merchantId. Use appropriately scoped lookups / safe errors to avoid leaking other tenants' private resources. Public published catalog visibility is a separate policy.
- Ordinary independent FKs to carts and products cannot by themselves enforce equal tenant IDs. Consider a cart_items.merchant_id and two composite FKs to (cart.id, cart.merchant_id) and (product.id, product.merchant_id), with corresponding UNIQUE referenced pairs; examine alternatives before final schema approval. DB constraints complement, not replace, application authorization.
- Document how customer accounts, active carts and tenant-scoped uniqueness are modeled when concrete schema is proposed. Negative cross-merchant and cross-customer tests are required before release.

## Consequences and tradeoffs
Repeated merchant_id in child tables can be justified by cross-table integrity constraints but requires deliberate constraints and indexes. Composite FKs establish relational consistency, not caller identity authorization. Avoid blindly applying this pattern to every table.

## Verification state
Design review discussion only. No migration, code, negative tests, commit or deployment has been verified. Exact SQL schema and API status policy remain to be written and reviewed.
