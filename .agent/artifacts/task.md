# Notifications & Watch Providers Task Tracker

## 1. Branch Safety
- [x] Extract uncommitted code from kernel panic.
- [x] Stash and re-apply recovered code onto `feature/notifications-and-providers` branch.
- [x] Initial commit and push to remote to ensure data integrity.

## 2. Notification Suite
- [x] Implement `NotificationWorker` to fetch daily Trakt schedule.
- [x] Schedule `NotificationWorker` via Jetpack WorkManager.
- [x] Wire user Notification opt-in/out toggles directly into the `SettingsScreen`.
- [x] Format rich notifications with deep links to `ShowDetailScreen`.
- [x] Utilize `NotificationCompat.InboxStyle` to seamlessly group multiple shows airing on the same day to prevent notification spam.

## 3. Testing & Verification
- [x] Cover `NotificationWorker` integration pathways.
- [x] Cover `EpisodeDetailViewModel` async flow states.
- [x] Verify KtLint/Detekt code styles locally.
- [x] Fix remote CI build error (`UnusedMaterial3ScaffoldPaddingParameter` in `EpisodeDetailScreen`).

## 5. Episode Detail Screen
- [x] Implement `EpisodeDetailArg` and `Destinations.EpisodeDetail` routing.
- [x] Build `EpisodeDetailViewModel` to fetch episode metadata.
- [x] Build `EpisodeDetailScreen` Compose UI.
- [x] Connect URL scheme deep links inside `AndroidManifest.xml` and `AppNavigation.kt`.
- [x] Update `NotificationWorker` to launch the Deep Link intent for single-episode notifications.

## 5. UI/UX Refinement
- [x] Fix missing `clickable` action on `ShowSeasonEpisodeCard` to route to `Destinations.EpisodeDetail`.

## 6. Phase 3: Premium UI & Deep Links
- [x] Expand `EpisodeDetailArg` and navigation graph destinations to receive and parcelize `showImageUrl` and `showBackgroundUrl`.
- [x] Overhaul `EpisodeDetailScreen` UI with sleek `AsyncImage` backdrops, gradients, and premium adaptive layouts.
- [x] Refactor Dashboard "Recent Activity" cards to deep-link directly into `Destinations.EpisodeDetail` instead of `ShowSeasonEpisodes`.

## 7. Phase 4: TMDb Watch Providers
### Network & DI Layer
- [x] Inject `TMDB_ACCESS_TOKEN` via `local.properties` & `core/data/build.gradle`.
- [x] Create `NetworkTmdbWatchProvidersResponse.kt` DTO mapping.
- [x] Establish `TmdbService.kt` Retrofit endpoint mapping.
- [x] Configure specialized OkHttp Client & Retrofit module in `NetworkModule.kt` for `api.themoviedb.org`.

### Domain & Repository Layer
- [x] Expand `ShowInfo.kt` and `NetworkTraktShowInfoResponse.kt` flows to properly extract `tmdbId: Int?`.
- [x] Map `NetworkTmdbWatchProvidersResponse` array blocks into a clean `TmdbWatchProviders` Domain presentation object.
- [x] Wire `ShowDetailRepository.getShowWatchProviders(tmdbId)` endpoint utilizing `TmdbService`.

### UI Integration
- [x] Feed `watchProviders` state tracking logic into `ShowDetailViewModel` bound by the `tmdbID` lookup.
- [x] Rebuild `WatchProvidersSection` component in `ShowDetailScreen` using `LazyRow`, Coil image loading (`image.tmdb.org`), and fallback empty states.

## 8. Phase 5: UI Polishing
- [x] Upgrade `ShowDetailButtons` from Text clickables to structural Material 3 `Button` / `OutlinedButton` components.
- [x] Reposition the "Where to Watch" section to the top-half of the detail screen (just below CTAs) for improved hierarchy.
- [x] Investigate expanding the `TraktShowRating` to feature Rotten Tomatoes or IMDb ratings without adding new APIs.

## 9. Episode Detail Visuals
- [x] Rework the `EpisodeDetailScreen` hierarchy to safely pad underneath the transparent `TopAppBar` system insets.
- [x] Refactor the Episode Rating display to sit side-by-side with the subtitle labels and format as a bold percentage scale.
- [x] Extend the `EpisodeDetailArg` bundle to natively support a standalone `episodeImageUrl`.
- [x] Tap into the `DashboardScreen` and `ShowSeasonEpisodesScreen` routing listeners to push the item-specific screenshot frame instead of the generic show-level banner.
- [x] Delete `TopAppBar` from `EpisodeDetailScreen` to eliminate text legibility issues over complex imagery.
- [x] Implement a custom high-contrast floating `ArrowBack` button and embed `showTitle` directly into the `ElevatedCard` layout layer.

## 10. Phase 6: Delightful Animations & Content Loading
- [x] Build `Shimmer.kt` modifier for sweeping gradient skeletal loading states in `:core:designsystem`.
- [x] Build `BounceClick.kt` modifier for custom spring-based scale feedback in `:core:designsystem`.
- [x] Apply `Shimmer` placeholders into `DashboardScreen` (Hero banners, Recent Activity, Trending).
- [x] Apply `Shimmer` placeholders into `ExploreScreen` and `ShowDetailScreen` (Cast, Providers).
- [x] Swap standard `clickable()` for `onBounceClick()` across `ListPosterCard`, `UpNextEpisodeCard`, and `ShowSeasonEpisodeCard`.
- [x] Verify transition animations and 60fps performance across the new custom modifiers.

## 11. Phase 7: Episode Detail Enhancements
### Network & Domain Layer
- [x] Define DTO `NetworkTraktEpisodePeopleResponse` for Crew and Guest Stars.
- [x] Add `getEpisodePeopleAsync` endpoint to `TraktService` and `EpisodeDetailRepository`.
- [x] Update `EpisodeDetailViewModel` state to fetch and hold Episode Cast/Crew data.

### UI Integration
- [x] Expose `Total Votes` metrics alongside the visual rating representation in `EpisodeDetailScreen`.
- [x] Make `First Aired Date` more prominent using `RelativeTimeSpan` or structured date formats.
- [x] Build CTAs / Jump Links for External IDs (IMDB, TVDB) to open in native web browsers.
- [x] Construct `GuestStarsRow` and `CrewRow` utilizing the custom `Shimmer` loading states pending Trakt data.

### Episode Details Cast & Crew Images
- [x] Fetch `tmdbId` natively through `NetworkTraktEpisodePeopleResponse` mappings.
- [x] Integrate `tmdbId` into core Domain classes `TraktCast` and `TraktCrew`.
- [x] Resolve `originalImageUrl` from `TmdbService.getPersonImagesAsync` inside `ShowDetailRepository`.
- [x] Render Guest Star and Crew images using `SubcomposeAsyncImage` in `EpisodeDetailScreen` with a native fallback Icon.
- [x] Repair local Data Source mock instantiations missing the explicit `tmdbId` constructor injection.
- [x] Migrate `NetworkTmdbPersonImagesResponse` from Moshi (`@Json`) to Gson (`@SerializedName`) to fix silent null mappings.
- [x] Integrate Main Series `cast` into the `EpisodePeople` Domain mapping to display prominent stars directly above Guest Stars and Crew.

## 12. Bug Fixes
- [x] Fix Trakt OAuth deep link failure (`theupnextapp://callback`) caused by missing `navDeepLink` mapping in `AppNavigation.kt`.
- [x] Add `NavigationTest` UI assertions to cover the Trakt deep link routing.
- [x] Resolve `DashboardScreen` "Recent Activity" bug by observing `WatchProgressRepository.isSyncing` flows.
- [x] Wire `UpNextEpisodeCard` on `DashboardScreen` to immediately dispatch check-ins to Trakt.
- [x] Migrate `NetworkTrakt*` API Models from Moshi (`@Json`) to Gson (`@SerializedName`) to fix malformed `/sync/history` payloads.
- [x] Correct implicit infinite flow collection in `ShowSeasonsViewModel.onToggleSeasonWatched` to accurately fire `SyncWatchProgressWorker` when marking whole seasons.

## 13. Testing
- [x] Write Unit Tests for ViewModels triggering Trakt sync.
  - [x] DashboardViewModelTest.
  - [x] ShowSeasonsViewModelTest.
- [x] Verify older apps' localized tracking queue upgrades successfully out-of-the-box.

## 14. Phase 2 Features: Trakt Customizations
- [x] Refactor `TraktAccountDataSource.checkInToShow` to bypass the IMDB lookup and use `showTraktId`.
- [x] Connect `EpisodeDetailViewModel.kt` to the check-in data source.
- [x] Implement a Check-in floating action button or UI card in `EpisodeDetailScreen.kt`.
- [x] Fix Check-In button to reflect successful completion state.
- [x] Implement Cancel Check-in functionality to dismiss active tracking.
- [x] Solve the `NotificationWorker.kt` StateFlow freeze by accessing user settings and tokens via `firstOrNull()`.
- [x] Fix all failing Unit Tests (`NotificationWorkerTest`, `NavTest`, `SearchViewModelTest`, `EpisodeDetailViewModelTest`).
## 15. Technical Capability
- [x] Integrate `aldefy/compose-skill` to enhance AGENTIC context for Jetpack Compose UI refinements.
