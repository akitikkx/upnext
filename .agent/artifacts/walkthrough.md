# Feature Walkthrough: Dashboard Redesign & Dedicated Schedule Tab

## Objective
This pull request completely revamps the initial landing experience of Upnext. We transitioned from a generic, unauthenticated "Yesterday / Today / Tomorrow" TV schedule into a personalized "My Upnext" hub, utilizing Trakt integration to provide "Tonight on TV", "Most Anticipated", and "Airing Soon for You" features.
- **Trakt Personalized Recommendations**: Authenticated users will now see a `Recommended for You` section at the top of their dashboard dynamically fetched across the `recommendations/shows` API and populated with TVMaze metadata, utilizing Jetpack Compose `HorizontalPager`.
- **Recommendations Schema Hotfix:** Redefined `NetworkTraktRecommendationsResponseItem` from nesting properties under a generic `show` object to explicitly mirroring the unwrapped JSON arrays returned natively by Trakt.
- **Image Resolution Hotfix:** Inverted the fallback prioritization logic inside the `DashboardRepository` methods so `original` high-definition posters from TVMaze are queried preferenced before falling back to `medium`.

## Validation

- Tested unit test suite utilizing Mockito configurations with the `testDebugUnitTest` gradle task. Passed successfully.
- Ran static Kotlin analyzer logic format suite using `./gradlew detekt ktlintFormat`. Passed without warnings.
- Compiles properly natively with `assembleDebug`.

Concurrently, the aesthetic design language of the application was completely overhauled into a premium "Cinematic Luxury" dark theme, abandoning the generic Material neon defaults.

## Key Changes Made

### 1. Navigation & Architecture Refactoring
- **Dedicated Schedule Tab:** Extracted the global "Yesterday / Today / Tomorrow" TVMaze schedule into its own dedicated bottom navigation tab (`Schedule`).
- **Dashboard Hub:** Re-purposed the `DashboardScreen` as the landing destination, serving a highly personalized feed.
- **Empty Detail States:** Resolved a fatal navigational crash `Destination with route Dashboard cannot be found` during Trakt OAuth callbacks by injecting a clean `EmptyDetail` view state into the Adaptive List-Detail layout on mobile.

### 2. Personalized Dashboard Features (`My Upnext`)
- **Unauthenticated View:** 
    - Implemented a "Tonight on TV" horizontal Roladex carousel featuring aspect-ratio-corrected 2:3 posters.
    - Added a premium "Connect Trakt" Call to Action box to drive user conversions.
    - Displayed a "Most Anticipated" Trakt API list for general discovery.
- **Authenticated View:**
    - **Continue Watching:** Built a striking `16:9` progress carousel using `ContinueWatchingCard` to help users seamlessly resume in-progress shows, featuring an OLED-black cinematic gradient overlay and inline progress indicators.
    - **Airing Soon:** Integrated a personalized "Airing Soon for You" list, showing exactly when the user's tracked shows will air locally. Features human-readable ISO-8601 parsing ("In 7 Hrs").
    - **Recent Activity:** Added a vertically scrolling list (`LazyColumn`) below Airing Soon to display the user's most recent Trakt sync history timeline.

### 3. "Cinematic Luxury" UI Theme Overlay
- **Bespoke Color Palette:** Created a premium OLED-friendly Dark Theme. The layout now features true `OLED Black` backgrounds (`#000000`), deep `Charcoal` surface cards (`#141414`), and a high-contrast `Cinematic Gold` (`#E5B211`) for primary Call to Action buttons and accents. This eliminates the previous jarring green/purple tint.
- **Removed Dynamic Colors:** Disabled Android 12+ Wallpaper Dynamic Theming internally to ensure the application's cinematic branding isn't overridden by system defaults.
- **Edge-to-Edge Vitals Fix:** Removed legacy hardcoded `statusBarColor` overrides from `themes.xml` and fully integrated `androidx.activity.enableEdgeToEdge()` inside `MainActivity`. The UI now correctly flows beneath transparent system navigation bars natively on Android 15.

## Verification
- ✅ **CI Pipeline Integrity:** The entire validation suite (`ktlintFormat`, `detekt`, `lintDebug`, `testDebugUnitTest`, `assembleRelease` with R8 minification) was executed locally to simulate and guarantee the `.github/workflows/pull_request.yml` sanity check passes cleanly, yielding zero wasted Actions minutes.
- ✅ **Image Aspect Ratios:** Fixed stretched TVMaze imagery by enforcing `aspectRatio(2f/3f)` and fixed widths on poster cards across pagers.
- ✅ **ID Mapping Bug (`404 Error` Fix):** Corrected the routing logic on the "Tonight on TV" carousel so it accurately routes to `/shows/id` using the TVMaze `show.showId.toString()`, preventing blank pages.

## Phase 6: Dashboard Trakt Corrections
As part of post-merge adjustments, the following discrepancies with the Trakt API on the Dashboard were fixed:
- **True "Up Next" Feed:** Transitioned the "Continue Watching" UI from tracking paused videos (`/sync/playback`) to a composite Trakt-authenticated feed that pulls the top 15 recently watched shows and aggregates their `next_episode` to perfectly mirror Trakt.tv's official behavior.
- **Activity Feed Truncation:** Capped the "Recent Activity" infinite vertical list to a hard limit of `5` items to prevent the dashboard scroll from bleeding endlessly.
- **Airing Soon Calendar Refining:** Adjusted the lookup query for "Airing Soon for You" from 7 days to `14` days, and aggressively filtered out special episodes (`season == 0`) so users only see premiering main-season arcs.

## Phase 7: Dashboard UI Polish & R8 App Bundling Fixes
- **Obfuscation Rules:** Added `@Keep` and `@JsonClass(generateAdapter = true)` attributes to the new Trakt API Payload data classes. This ensures that the R8 production minifier doesn't strip out the JSON keys which was causing the 'Continue Watching' logic to silently drop in the Release build.
- **Recent Activity Redesign:** Transformed the Recent Activity layout from a vertical list into a sleek, horizontal `LazyRow` carousel. Each card now prominently displays a 16:9 poster crop mapping closer to an episode thumbnail aesthetic.
- **Grammar & Flow Alignment:** Removed the redundant "Watched watch" text tagging in favor of a clean "Watched" label. Updated the navigation graph action to deep-link users directly into the exact `ShowSeasonEpisodes` screen rather than the general show overview.

## Phase 8: Dashboard Image Stability & Episode Thumbnails
- **Trakt API Try-Catch Guards:** Wrapped the Trakt API's `Deferred` HTTP Image callbacks inside `DashboardViewModel` with global `try/catch` loops. This prevents completely innocuous and isolated image 404 network errors on TVMaze from silently crashing and entirely wiping the "Continue Watching", "Airing Soon", and "Recent Activity" panels. 
- **Episode Image Fetching:** Constructed a new `getEpisodeByNumberAsync` endpoint bridge inside `TvMazeService.kt` to empower the application to fetch highly specific 16:9 episode screenshots for the Recent Activity list, instead of awkwardly cropping universal portrait series posters.
- **Recent Activity Layout Polish:** Dropped the restrictive `CardDefaults` and floating grey surfaces off the Recent Activity UI blocks. It now sits flush and transparent in a condensed column, matching Trakt's official UX standard.
