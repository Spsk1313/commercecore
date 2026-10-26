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

## Verification
Learner draws module dependency diagram, identifies where transaction and authorization rules live, demonstrates feature changes limited to relevant modules. Update status to Accepted only after explanation and review.
