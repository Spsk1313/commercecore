# Changelog — verified engineering changes

## 2026-10-25 — Planning only
- Established initial product mission, module proposal, release-quality gates, delivery board, evidence and public-visibility workflows.
- **No implementation, tests, deployments, performance gains or real users claimed.**

- Accepted V1 cart/checkout inventory policy in ADR-0002 after UX-versus-availability design review; design only, not yet implemented.

## Change template
Date — Feature/incident ID — What changed — Impact on API/schema/security or user experience — Tests/CI evidence — Commit or release — Limitations and follow-up.

- 2026-10-25: Drafted ADR-0003 (inventory accounting and cancellation/retry semantics); requirements remain proposed, not implemented.

- 2026-10-25: Accepted ADR-0003 V1 logical inventory accounting and concurrent cancel/fulfill state-transition policy. Recorded database atomicity requirement and caveat about physical dispatch. Planning only; no implementation or test evidence.

- 2026-10-25: Accepted ADR-0004 logical tenant-ownership and cart authorization policy from design review, documented composite-FK caveat. Planning only, not deployed/tested.

- 2026-10-25: Product mission and six high-level user capabilities approved during COM-001 inception. Detailed release scope remains draft; planning only.

- 2026-10-25: Accepted COM-001.3 authenticated customer checkout and public browsing (ADR-0005). Recorded extra login friction and deferral of guest checkout. Product design only; no implementation or test evidence.
