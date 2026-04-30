# Next Steps Analysis — Upnext → 1M

## Current Analytics Snapshot (Mar 2026)

| Metric | Value | Trend | Context |
|--------|-------|-------|---------|
| Install base | **52** | ↘ -7.14% | Losing ~2 devices/period |
| D1 Retention | **20%** | — | **2.4× peer median** (8.48%) |
| Cumulative acquisitions | **2.58K** | ↗ +0.58% | Lifetime downloads |
| Devices lost (30d avg) | **0.63** | ↘ -40.63% | Churn stabilizing |
| Top geos | US (21%), UK (8%), Brazil (6%), Czechia (6%) | — | US-heavy, opportunity in English |

> [!TIP]
> **20% D1 retention is exceptional** — peer 75th percentile is only 16.55%. The *product sticks* once people try it. The bottleneck is **acquisition**, not retention.

## What's Done on This Branch

- [x] Watchlist native Trakt sync + optimistic UI
- [x] Pull-to-refresh + ON_RESUME lifecycle hooks
- [x] Background sync cadence: 120min → 30min
- [x] All unit + instrumentation tests green on CI

## Prioritized Next Steps

### Option A: 🧪 UI Test Coverage (Phase 1 completion)
**What**: The single remaining Phase 1 item — add instrumentation tests for Login, Search, and Show Detail flows.

**Impact**: Low direct user impact but protects quality at scale. Currently only 893 LOC across 11 test files; `DashboardScreenTest` and `ShowDetailScreenTest` are stubs (28 lines each).

**Effort**: ~2-3 hours

---

### Option B: ⭐ Continue Ratings & Reviews Polish (1M Growth)
**What**: The Rate button UX is already implemented on this branch. Could continue polishing: inline "You: 8★" on the aggregate pill, adaptive button layout for tablets.

**Impact**: Medium — differentiator vs Trakt official app where rating is painful (a top complaint).

**Effort**: ~1-2 hours

---

### Option C: 🔍 Advanced Filtering & Sorting Across All Lists (1M Growth)
**What**: Add genre/year/rating filters to Explore, Dashboard, and Watchlist screens. Search & Sort already exists on Watchlist; extend pattern.

**Impact**: High — directly addresses Product Principle #3 (Search & Sort Everywhere) and a major Trakt app complaint.

**Effort**: ~4-6 hours

---

### Option D: 👥 Social / Friends Activity (1M Growth)
**What**: Show what friends are watching, their recent activity, shared watchlists.

**Impact**: Very high for viral growth (the core loop that drove Letterboxd). But requires significant Trakt API research and new screens.

**Effort**: ~8-12 hours

---

### Option E: 🔄 Multi-Provider Sync (SIMKL Integration)
**What**: Abstract the existing Trakt-specific syncing layer into a generic `SyncProvider` interface to support SIMKL integration natively. Enables users to sync watched episodes, watchlists, and ratings to/from SIMKL instead of or alongside Trakt.

**Impact**: High for user acquisition. Allows users with existing SIMKL tracking history to onboard seamlessly without rebuilding their data. Satisfies direct user requests.

**Effort**: ~15-20 hours (Major Architecture Epic)

---

## My Recommendation

Given the analytics:
- **Retention is your superpower** (20% vs 8.48% median) — the app is already sticky
- **Acquisition is the gap** — 52 active installs from 2.58K lifetime downloads means most people try and leave early
- **Churn is down** — the -40.63% device loss trend is encouraging

**For immediate impact, I'd recommend Option C (Advanced Filtering)** because:
1. It's the #1 complaint about the official Trakt app (poor list management)
2. It makes the *existing* content more discoverable without building new screens
3. It benefits every user on every session (Dashboard, Explore, Watchlist)
4. It compounds with the watchlist sync work we just shipped

**For long-term acquisition strategy, Option E (SIMKL Integration)** should be prioritized as the next major Epic (Phase 2). By supporting an alternative provider, we remove the friction for users already invested in the SIMKL ecosystem, directly addressing the acquisition gap.

Alternatively, if you want to close out Phase 1 cleanly first, **Option A** is a quick win (~2h) before moving to growth features.

Which direction would you like to go?
