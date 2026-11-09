# ADR-0001 — Begin as a modular monolith

**Status:** Proposed (learner approval pending)
**Date:** 2026-10-25
**Decider:** Implementing engineer

## Context
One engineer, approximately five weeks to an independently demonstrated V1, modest local hardware/budget, no proven high-scale traffic. Need readable module boundaries, transactions, testability, and deployability.

## Proposed decision
One Spring Boot deployable, modular by business capability, one PostgreSQL database; React frontend as separate app. No initial microservices, queue or cache without evidence.

## Alternatives considered
1. Microservices at launch: offer independent deployments, but multiply network failure modes, auth, tracing, consistency and delivery complexity without proven need.
2. Undifferentiated monolith: fast initially but risks uncontrolled module coupling and unclear domain ownership.
3. Modular monolith (proposed): single deployment/transaction model with internal boundaries that can be tested.

## Consequences
Benefits: faster end-to-end delivery, transaction simplicity, less ops burden. Costs: shared deployment/scaling, need to enforce modularity via boundaries/tests/architecture review. Future extraction only when supported by measurement and organizational need.

## Implementation checkpoint — 2026-11-09

The repository already follows the **proposed** modular-monolith shape: one Java 21/Spring Boot 4.1.2 backend, PostgreSQL with Flyway and `JdbcClient`, React/TypeScript as a separate frontend, feature-local `merchant` and `identity` application/persistence packages. V2 identity/membership schema is merged (PR #8), and the V3 verification service was merged (PR #9). **Architecture use does not by itself constitute explicit acceptance of this ADR**; status remains Proposed pending the learner's design review. No microservice, queue, cache, deployment or proven high-scale traffic.

## Verification
Learner draws module dependency diagram, identifies where transaction and authorization rules live, demonstrates feature changes limited to relevant modules. Update status to Accepted only after explanation and review.
