# Phase 2 Implementation Plan: Personal Dashboard & Schedule Tab

## Goal Description
Transform the app's initial landing screen from a generic TV schedule into a highly personalized "My Upnext" dashboard, while moving the generic global schedule to its own dedicated tab. This aligns Upnext with modern 1M+ user TV tracker paradigms.

## Proposed Changes

### 1. Navigation & Tab Structure (`:app` module)
- **[MODIFY]** `app/src/main/java/com/theupnextapp/ui/navigation/Destinations.kt`
  - Add a `@Serializable object Schedule : Destinations` route for the new tab.
- **[MODIFY]** `app/src/main/java/com/theupnextapp/ui/navigation/AppNavigation.kt`
  - Add the `Schedule` item to the `NavigationSuiteScaffold` navigation items.
  - Wire the `Schedule` destination to load the old dashboard UI.
  - Wire the `Dashboard` destination to load the *new* `MyUpnext` dashboard UI.

---
### 2. The New Schedule Screen (`:app` module)
- **[MODIFY]** `app/src/main/java/com/theupnextapp/ui/dashboard/DashboardScreen.kt`
  - Rename this file to `ScheduleScreen.kt` and move to `com.theupnextapp.ui.schedule`.
  - Rename `DashboardViewModel` to `ScheduleViewModel`.
  - *No visual changes to this screen, just relocating it to live under the new tab.*

---
### 3. The New Personalized Dashboard (`:app` & `:core:designsystem`)
- **[NEW]** `app/src/main/java/com/theupnextapp/ui/dashboard/DashboardScreen.kt` (The modern "My Upnext" UI).
- **[NEW]** `app/src/main/java/com/theupnextapp/ui/dashboard/DashboardViewModel.kt`
  - Fetch personalized data from Trakt APIs:
    - `Up Next to Watch` (Trakt sync progress API).
    - `Airing Soon for You` (Shows the user follows airing < 7 days from Trakt schedule API).
    - `Recent Activity` (Trakt history API).
  - *For Unauthenticated Users:* Fetch TVMaze `todayShows` (via `DashboardRepository`) and Trakt `mostAnticipatedShows`. Use a Compose `HorizontalPager` with `graphicsLayer` transformations to build a "Roladex" style carousel for the schedule. Use US country code for now as a baseline.
- **[MODIFY]** `app/src/main/java/com/theupnextapp/ui/dashboard/DashboardScreen.kt`
  - Render a "Tonight on TV" roladex pager (Option A) and a "Most Anticipated" list (Option B) when logged out, avoiding overlap with the Explore screen. Keep the Trakt CTA.
- **[NEW]** `core/designsystem/src/main/java/com/theupnextapp/core/designsystem/ui/widgets/UpNextEpisodeCard.kt`
  - Create a distinct, actionable UI card for the "Up Next to Watch" horizontal carousel. It should feature a large thumbnail, a circular progress bar, season/episode numbers, and a "Mark as Watched" quick-action overlay.

## Verification Plan

### Automated Tests
- Run `testDebugUnitTest` to ensure viewmodel logic works.
- Update/Add Compose UI Tests for the new `UpNextEpisodeCard` and the modified `AppNavigation`.

### Manual Verification
1. Launch the app and observe the bottom bar has a new "Schedule" icon.
2. The initial landing tab (Dashboard) displays the *new* blank/personalized UI ("My Upnext").
3. Tapping the "Schedule" tab displays the old "Yesterday / Today / Tomorrow" TVMaze data.
4. If logged into Trakt, the Dashboard should securely pull down your viewing progress.

---
## Phase 3: UI/UX Polish & 1M User Experience

### Proposed Changes

#### 1. Image Quality & Card Redesign (`:core:designsystem`)
- **[MODIFY]** `core/designsystem/src/main/java/com/theupnextapp/core/designsystem/ui/widgets/UpNextEpisodeCard.kt`
  - Change the root image `aspectRatio` to match the natural poster size (e.g. `2f/3f`) instead of forcing `16f/9f`, preventing the image from stretching or cropping unpleasantly.
  - Enhance typography and layout for a more premium, native feel.
  - Apply `Modifier.animateContentSize()` for fluid state shifts.

#### 2. "Airing Soon" Date Formatting (`:app`) 
- **[MODIFY]** `app/src/main/java/com/theupnextapp/ui/dashboard/DashboardScreen.kt`
  - Parse the `first_aired` ISO-8601 string from Trakt.
  - Format it into a user-friendly timestamp (e.g., "Oct 15, 8:00 PM") and display it on the episode card instead of just the season/episode info.

#### 3. Animations & Transitions (`:app`)
- **[MODIFY]** `app/src/main/java/com/theupnextapp/ui/navigation/AppNavigation.kt`
  - Wrap the `NavHost` in standard entry/exit transition animations (e.g. `slideInHorizontally` / `fadeOut`) to provide a seamless flow between screens.

#### 4. Unauthenticated 404 Bug Fix (`:app`)
- **[MODIFY]** `app/src/main/java/com/theupnextapp/ui/dashboard/DashboardScreen.kt`
  - Fix the "Tonight on TV" TvMaze route ID mapping. `show.id` incorrectly returns the episode ID, passing it to the `/shows/id` endpoint. Update to `show.showId.toString()`.

### Verification Plan
- Launch app unauthenticated, verify clicking "Tonight on TV" cards correctly loads the Show Details page without a 404 error.
- Verify images on "Airing Soon" are crisp and un-stretched.
- Verify screen transitions are smooth.
- Verify the Airing Soon text shows the exact date/time.

---
## Phase 4: Dashboard Theming, Edge-to-Edge Fixes & CI Process

### Proposed Changes

#### 1. Premium Aesthetic Transformation (`:core:designsystem` & `frontend-design` SKILL)
- **[MODIFY]** `core/designsystem/src/main/java/com/theupnextapp/core/designsystem/ui/theme/Color.kt`
  - Replace the default generic Material Teal/Purple (`Teal500`, `Purple500`, etc.) with a bespoke "Cinematic Luxury" premium dark theme palette. E.g., true OLED Black backgrounds (`#000000`), deep charcoal surfaces (`#141414`), and a luxurious Cinematic Gold (`#E5B211`) for primary high-priority accents (like the air date ribbons).
- **[MODIFY]** `core/designsystem/src/main/java/com/theupnextapp/core/designsystem/ui/theme/Theme.kt`
  - Map the new colors securely to `DarkColorTheme` and `LightColorTheme` palettes.
  
#### 2. Resolving Play Console Edge-to-Edge Warnings (`:app`) 
- **[MODIFY]** `app/src/main/res/values/themes.xml`
  - Remove deprecated hardcoded `android:statusBarColor`, `android:navigationBarColor`, `android:windowLightStatusBar`, and `android:windowLightNavigationBar` attributes from `Base.Theme.MaterialTheme` which trigger the Google Play Console Vitals warning.
- **[MODIFY]** `app/src/main/java/com/theupnextapp/MainActivity.kt`
  - Inject the modern `androidx.activity.enableEdgeToEdge()` API in `onCreate(savedInstanceState)` before `setContent {}` to transparently and legally render edge-to-edge under system bars without warnings, supporting the immersive hero poster designs.
  
#### 3. CI Pipeline & Cost Optimization
- I will strictly execute a full `ci_check` equivalent (`./gradlew ktlintFormat detekt lintDebug testDebugUnitTest assembleRelease`) completely **locally** to simulate the Pull Request workflow before pushing to GitHub. This prevents pushing broken iterative commits and ensures zero wasted GitHub Action CI minutes from bots/mistakes.

### Verification Plan
- Launch the Pixel emulator to ensure the "Connect Trakt" and background look distinctly premium and legible, without the generic sickly green tint.
- Re-run lint checks, confirm no warnings regarding edge-to-edge APIs.
- Visually verify edge-to-edge system bars seamlessly blend with the app content padding in Compose.

---
## Phase 5: Completing the Personalized Dashboard

### Proposed Changes

#### 1. Implement "Continue Watching" Section (`:app` & `:core:designsystem`)
- **[NEW]** `core/designsystem/src/main/java/com/theupnextapp/core/designsystem/ui/widgets/ContinueWatchingCard.kt`
  - Build a custom composable for the "Up Next to Watch" horizontal carousel. It should feature a large 16:9 thumbnail (pulled from Tmdb/Trakt), a circular progress bar indicating season/episode advancement, and a "Mark as Watched" quick-action overlay. Follow the `frontend-design` SKILL (Cinematic Minimalism, OLED Blacks).
- **[MODIFY]** `app/src/main/java/com/theupnextapp/ui/dashboard/DashboardScreen.kt`
  - Render the new `ContinueWatchingCard` inside a `HorizontalPager` populated by the Trakt Sync Progress API data from `DashboardViewModel`. Place this above "Airing Soon for You" when the user is authenticated.

#### 2. Implement "Recent Activity" List (`:app`) 
- **[MODIFY]** `app/src/main/java/com/theupnextapp/ui/dashboard/DashboardScreen.kt`
  - Build a vertical `LazyColumn` (or flow) below "Airing Soon" for the authenticated user, displaying a history of their recently watched episodes or newly added shows from the Trakt History API.
  - Utilize the existing list item components or design a sleek minimal row.

#### 3. Merge Preparation
- Once the authenticated UI is finalized, ensure `ktlintFormat`, `detekt`, and unit tests pass locally.
- Review and update `walkthrough.md` with final screenshots and feature summaries for the PR description.

### Verification Plan
- Launch app authenticated. Verify the "Continue Watching" section tracks the user's current episode progress correctly.
- Verify the "Recent Activity" log matches their Trakt web profile.
- Confirm CI checks remain green.
