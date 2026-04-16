# Tablet Portrait Layout & Network Fetch Optimization Plan

## Problem Analysis

I have investigated the two issues you reported after rotating the connected Pixel tablet from portrait to landscape:
1. **Redundant Network Calls:** The logcat output `TVMaze show data for IMDb ID tt1043813` is actually coming from `DashboardViewModel.kt`, **not** the `ShowDetailViewModel` which we guarded in the previous session. Because tablet adaptive layouts keep the `listPane` (Dashboard) alive when the `detailPane` (ShowDetail) is active, rotating the device forces `DashboardScreen` to re-enter the composition. This unconditionally re-fires `LaunchedEffect(traktAccessToken)`, which calls `viewModel.fetchDashboardData(it)`, triggering mass Trakt and TVMaze image lookups.
2. **Cramped Tablet Portrait Layout:** Currently, `ExpandedDetailArea` separates the screen into two independent vertical scrolling columns: Left (Poster & Buttons) and Right (Synopsis, Cast, Episodes, Similar Shows). On a Tablet in Portrait mode (typically ~600-840dp wide), this dual-column layout shrinks the poster to a sliver and creates enormous vertical whitespace under the poster while the right column scrolls endlessly.

## Proposed Changes

### 1. Guard DashboardViewModel Network Calls
**[MODIFY]** `app/src/main/java/com/theupnextapp/ui/dashboard/DashboardViewModel.kt`
- Add a protective guard in `fetchDashboardData(token: String)`:
  ```kotlin
  if (_airingSoonShows.value == null && !_isLoadingAiringSoon.value) fetchAiringSoonShows(bearerToken)
  if (_recommendedShows.value == null && !_isLoadingRecommendations.value) fetchRecommendations(bearerToken)
  if (_recentHistory.value == null && !_isLoadingHistory.value) fetchRecentHistory(bearerToken)
  ```
- This ensures that when the device rotates and `LaunchedEffect` is re-invoked, we don't spam the network if the data is already fetched.

### 2. Refactor ExpandedDetailArea (Tablet Layout Polish)
**[MODIFY]** `app/src/main/java/com/theupnextapp/ui/showDetail/ShowDetailScreen.kt`
- Remove the two isolated `verticalScroll` modifiers on the left and right columns.
- Apply a single unified `verticalScroll` to the parent `ExpandedDetailArea` container.
- Create a top `Row` that holds the Poster & Action Buttons on the Left (`weight=0.35f` or similar) and the Hero Title & Synopsis on the Right (`weight=0.65f`). 
- Move all subsequent lists (**Watch Providers**, **Show Cast**, **Next Episode**, **Previous Episode**, **Similar Shows**) *below* the top `Row` so they span the full horizontal width of the screen.
- This creates an immersive hero section at the top, and effectively utilizes the full width of the tablet for horizontal lists underneath.

## User Review Required
> [!IMPORTANT]
> The new adaptive layout will stack the Cast and Episode data under the primary poster/synopsis area rather than cramming them into a skinny right-hand column. This will affect both Landscape and Portrait tablet viewing. Does this alignment match your vision for the Upnext tablet experience?

## Verification Plan
After applying these changes, I will:
1. Run `unit tests` across the Dashboard and ShowDetail view models to ensure no state logic is broken.
2. Confirm the CI pipeline remains green.
3. Validate through code inspection that `fetchDashboardData` correctly short-circuits.
