# CommerceCore — Frontend

React 19 + TypeScript + Vite 8 frontend workspace for the CommerceCore multi-merchant commerce project.

**Status (2026-11-01):** Frontend tooling and CI verified on [PR #6](https://github.com/Spsk1313/commercecore/pull/6). The application now displays a minimal CommerceCore heading and description; the Vite starter UI/assets were removed in commit `d1ecd86`. There are no commerce product pages, routes, authenticated flows, API integration, or production deployment yet.

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

[Frontend CI](../.github/workflows/frontend-ci.yml) runs on pull requests targeting `main`. It uses Ubuntu, Node.js 22, `npm ci`, `npm run lint`, and `npm run build`; caching is keyed using `frontend/package-lock.json`. [Frontend CI for the application-shell cleanup](https://github.com/Spsk1313/commercecore/actions/runs/41350363240) passed on 2026-11-01 at commit `d1ecd86`.

Backend CI runs separately. See the [root README](../README.md) and [engineering evidence ledger](../docs/EVIDENCE_LEDGER.md).

## Known limitations and security follow-up

- This is a **development scaffold**, not an implemented merchant dashboard or storefront.
- As observed locally on 2026-11-01, `npm audit` reported 12 moderate findings associated with `minimatch@10.2.6` through development tooling (ESLint and TypeScript-ESLint). `npm audit --omit=dev` reported zero vulnerabilities. The full audit is **not** clean, and this is not a blanket security assurance.
- No forced dependency downgrade/major-version override was applied. Re-evaluate compatible fixes and re-run the full audit on a later dependency-maintenance checkpoint.
- No browser-to-API-to-PostgreSQL integration, merchant authentication, authorization, or product workflows are implemented.

## Next engineering milestone

Finish and review the bootstrap PR; then implement a narrow browser → Spring Boot API → PostgreSQL flow under COM-002. Merchant product/catalog UI belongs in a subsequent feature branch, not the infrastructure bootstrap.
