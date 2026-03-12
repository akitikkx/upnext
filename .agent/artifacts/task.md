# Phase 2: Dashboard Redesign & Schedule Isolation

In this phase, we will transform the generic schedule-based Dashboard into a personalized "My Upnext" hub, while relocating the general schedule to its own dedicated tab. This utilizes our new `core:designsystem` module to ensure clean architecture.

## 1. Dashboard Redesign ("My Upnext")
- [x] Build the "Up Next to Watch" horizontal carousel.
- [x] Build the "Airing Soon for You" section. (Enriched with TvMaze posters)
- [x] Build the "Continue Watching" progress tracking section.
- [x] Build the "Recent Activity" vertical list.
- [x] Update `DashboardViewModel` to fetch personalized data from Trakt APIs instead of generic TVMaze schedules.
- [x] Implement authentication state branching in `DashboardViewModel` and `DashboardScreen`.
- [x] Build the "Tonight on TV" Roladex Pager (Option A) for unauthenticated users using `DashboardRepository.todayShows`.
- [x] Build the "Most Anticipated" (Option B) list below the CTA.
- [x] Build the premium "Connect Trakt" Call to Action card.
- [x] Fix Trakt OAuth deep-link routing to immediately return straight to the Dashboard without stopping at an intermediate Account Screen.
- [x] Fix ShowDetail 404 Error by accurately mapping TVMaze IDs over Trakt IDs.
- [x] Integrate UI components exclusively from the new `:core:designsystem` module.

## 2. Implement Dedicated "Schedule" Tab
- [x] Relocate the existing "Yesterday / Today / Tomorrow" layout out of the main Dashboard screen.
- [x] Update `AppNavigation.kt` and `Destinations.kt` to introduce a dedicated `Schedule` bottom navigation tab.
- [x] Ensure back-stack and adaptive list-detail pane navigation behaves correctly with the new tab layout.

## 3. UI/UX Polish & 1M User UX
- [x] Implement a 1M user dashboard design (premium, refined layouts).
- [x] Fix image quality and stretched elements in lists and detail screens (e.g. use proper `ContentScale` and resolution).
- [x] Add animations and shared element transitions for navigation.
- [x] Update "Airing Soon" cards to show the actual air date/time instead of just season/episode.
- [x] Fix 404 error when navigating to Show Detail from unauthenticated dashboard lists (likely ID mapping issue).

## 4. Verification & Testing
- [x] Run `./gradlew :app:assembleDebug` to verify compilation.
- [x] Run style linting (`ktlintFormat`, `detekt`, `lintDebug`) to guarantee CI pipeline passes.
- [x] Execute current UI and Unit tests locally to prevent regressions.
- [x] Improve test coverage: write new unit tests for ViewModels.
- [x] Improve test coverage: write new instrumentation tests for the Dashboard and Show Detail screens.

## 5. Dashboard Theming, Edge-to-Edge Fixes & CI
- [x] Implement premium Cinematic Luxury Dark Theme palette based on `frontend-design` (`Color.kt`, `Theme.kt`).
- [x] Remove deprecated `android:windowLightStatusBar` and `android:statusBarColor` attributes from `themes.xml`.
- [x] Add `enableEdgeToEdge()` to `MainActivity`.
- [x] Strictly run the local CI verification suite (`ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `assembleRelease`) prior to git pushes.

## 6. Dashboard Trakt Corrections
- [x] Transition "Continue Watching" from `sync/playback` to a composite "Up Next" feed using `sync/watched/shows` and `progress/watched`.
- [x] Truncate lengths of the "Recent Activity" vertical list to 5 items maximum to avoid endless scrolling.
- [x] Refine "Airing Soon for You" calendar endpoint `calendars/my/shows` by jumping the lookahead query to 14-days and aggressively filtering out specials (season 0) to align with Trakt UI norms.

## 7. Dashboard UI Polish & Obfuscation Fixes
- [x] Protect new custom Trakt models (`NetworkTraktWatchedShows...`, `NetworkTraktPlayback...`) from R8 minification stripping using `@Keep` and `@JsonClass(generateAdapter = true)`.
- [x] Transmute the old "Recent Activity" vertical list into a horizontal `LazyRow` carousel with 16:9 thumbnails.
- [x] Correct grammar inside the Activity cards from "Watched watch" to cleanly readable "Watched" tags.
- [x] Deep link the `Recent Activity` interactive cards explicitly to the exact target item's `ShowSeasonEpisodes` view.

## 8. Dashboard Image Stability & Episode Thumbnails
- [x] Enhance Trakt history `Deferred` lookups inside `DashboardViewModel` with global `try/catch` wrappers to prevent isolated image 404 network errors from silently crashing and wiping the entire "Continue Watching" UI. 
- [x] Construct a specific `/shows/{id}/episodebynumber` endpoint mapping inside `TvMazeService.kt` to allow targeting `ShowSeasonEpisode` imagery.
- [x] Overhaul the `Recent Activity` UI components so it fetches legitimate TVMaze 16:9 episode screenshots instead of awkwardly scaling full vertical Series posters. Removes the `Card` background for a cleaner, modern layout. 

## 9. 'Continue Watching' Overhaul & Image Polishing
- [x] Transition "Continue Watching" from the faulty `sync/playback` (which only logged paused items) to a custom composite tracker mapping standard unwatched `getTraktShowProgress` via `getTraktRecentHistory`.
- [x] Map `ExtractedTraktInfo` lookup to seamlessly default back to the Show Poster if a designated TVMaze Episode Screenshot is missing in the database.
- [x] Align horizontal `contentPadding` on the Recent Activity `LazyRow` to fix layout bleed via specifying `PaddingValues(end = 16.dp)`.
- [x] Refactor massive initialization blocks inside `DashboardViewModel.kt` (`fetchDashboardData`) down into explicit scoped functions to respect `detekt` CI Cyclomatic Complexity constraints.

## 10: Personalized Recommendations
- [x] Create `NetworkTraktRecommendationsResponse` model.
- [x] Add `getRecommendationsAsync` to `TraktService`.
- [x] Integrate endpoint into `TraktRepository` and fetch composite TVMaze thumbnails.
- [x] Integrate into `DashboardViewModel` via `fetchRecommendations`.
- [x] Swap out `Tonight on TV` for authenticated users with `Recommended for You` section in `DashboardScreen`.

## 11: CI/CD Optimization
- [x] Consolidate the 6 individual GitHub Action verify jobs (`testDebugUnitTest`, `assembleDebug`, `ktlintCheck`, `lintDebug`, `detekt`, `assembleRelease`) into a single execution workflow to eliminate duplicated virtual environments and redundant Kotlin compilations.
- [x] Replace custom `$HOME/.gradle/caches` with `gradle/actions/setup-gradle@v3` inside `pull_request.yml` for robust state preservation.
