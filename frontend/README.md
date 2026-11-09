# CommerceCore — Frontend

React 19 + TypeScript + Vite 8 frontend workspace for the CommerceCore multi-merchant commerce project.

**Status (2026-11-09):** [PR #6](https://github.com/Spsk1313/commercecore/pull/6) frontend bootstrap and [PR #7](https://github.com/Spsk1313/commercecore/pull/7) local merchant-ID integration **merged**; [PR #8](https://github.com/Spsk1313/commercecore/pull/8) backend identity/membership schema **merged**. [PR #9](https://github.com/Spsk1313/commercecore/pull/9) backend email-verification foundation is **merged** as `9d192d0`, but adds **no new frontend functionality**. Current UI remains the temporary merchant lookup through Vite's `/api` proxy. No merchant dashboard, storefront, registration/login screens, protected merchant access or production deployment.

## Prerequisites

- Node.js 22 (verified locally with v22.23.2 and in GitHub Actions)
- npm (verified locally with 10.9.8)

## Install and run

From the repository root:

```bash
cd frontend
npm ci
npm run dev
```

Vite prints the local development URL (previously observed at `http://localhost:5173/`). The development server supports hot module replacement: changes to frontend source generally appear without a manual rebuild.

## Development API connection

Start PostgreSQL and Spring Boot using the [operations runbook](../docs/OPERATIONS.md); then start Vite with `npm run dev`. The Vite `server.proxy` forwards `/api` requests to `http://localhost:8080` only while the development server runs. The production build does **not** include a reverse proxy. Smoke-test the proxy with `curl -i http://localhost:5173/api/v1/merchants/999` (expected 404 ProblemDetail). To test a successful lookup, insert a disposable merchant in local PostgreSQL using `RETURNING id`, search that ID in the browser, then delete the test row. Do not expose the unauthenticated merchant endpoint publicly.

## Verify

From `frontend/`:

```bash
npm run lint
npm run build
```

- `npm ci` uses the committed `package-lock.json` and does a clean dependency installation.
- `npm run lint` runs ESLint.
- `npm run build` checks TypeScript using `tsc -b` and creates a Vite production bundle in `dist/`.
- `npm run preview` can serve a previously built bundle for local inspection; it is not a production deployment.

There is currently **no frontend unit-test or browser end-to-end test suite**. A passing build/lint workflow verifies the configured checks, not application correctness.

## Continuous integration

[Frontend CI](../.github/workflows/frontend-ci.yml) runs on pull requests targeting `main`. It uses Ubuntu, Node.js 22, `npm ci`, `npm run lint`, and `npm run build`; caching is keyed using `frontend/package-lock.json`. [PR #7 frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/41375979226) and its [documentation-head CI](https://github.com/Spsk1313/commercecore/actions/runs/41376782817) passed; PR #8 latest-head frontend CI passed. [PR #9 latest-head frontend CI](https://github.com/Spsk1313/commercecore/actions/runs/42701618610) passed at `598ebf3` before merge, despite no frontend code changes. Future PRs need fresh regression checks.

Backend CI runs separately. See the [root README](../README.md) and [engineering evidence ledger](../docs/EVIDENCE_LEDGER.md).

## Known limitations and security follow-up

- The merchant-ID lookup is a **development integration harness**, not an implemented merchant dashboard or storefront; the API has no authentication/tenant authorization.
- As observed locally on 2026-11-01, `npm audit` reported 12 moderate findings associated with `minimatch@10.2.6` through development tooling (ESLint and TypeScript-ESLint). `npm audit --omit=dev` reported zero vulnerabilities. The full audit is **not** clean, and this is not a blanket security assurance.
- No forced dependency downgrade/major-version override was applied. Re-evaluate compatible fixes and re-run the full audit on a later dependency-maintenance checkpoint.
- A browser-to-API-to-PostgreSQL read path was manually verified locally, not automated as an end-to-end test. Merchant authentication, tenant authorization and product workflows are not implemented.


## Next engineering milestone

PR #9 merged the *backend verification foundation* after review and passing latest-head CI. The frontend is unchanged by this slice. Next, add the secure registration/verification API and email workflow on the backend, followed by registration/login UI and genuinely authorized merchant onboarding. Keep the temporary merchant-ID form as an integration harness; don't mistake it for a user-facing storefront.
