# Title
`fix: Trakt OAuth Deep Link Redirect & Detekt UI Refinements`

# Description
This PR addresses several critical styling violations and fixes a major bug logged by production users dealing with Trakt OAuth integrations failing to process connection callbacks.

# Key Changes
*   **Trakt OAuth Deep Link Resolution**: Restored the missing `navDeepLink` routing definition in `AppNavigation.kt` for `Destinations.TraktAccount`. This intercepts `theupnextapp://callback` URIs successfully and captures the OAuth connection tokens without stranding the user.
*   **Navigation Integration Tests**: Deployed a resilient Jetpack Compose test into `NavigationTest.kt` targeting the Trakt deep link routing. Simulates native system intents and ensures components render dynamically as expected.
*   **Detekt Rule Compliance**: Corrected strict static analysis warnings inside `EpisodeDetailScreen.kt`.
    *   Resolved `LongMethod` violation by structurally extracting `EpisodeBackdrop` and `EpisodeSummaryCard` components to enhance maintainability.
    *   Addressed `MagicNumber` violations via exact `@Suppress` annotations locally on explicitly defined Hex String layout components.
*   **Main Cast Image Fix**: The missing TMDB properties have been securely propagated through `NetworkTraktEpisodePeopleResponse.kt` to the domain, enabling high-resolution- Adds main `<CastRow>` implementation gracefully positioned above the Guest Stars list.
    - Configures image loading correctly to utilize circle crop patterns for main characters.
    - Extends the domain `EpisodePeople` mapping object to inject top billing elements flawlessly.

## Dashboard Bug Fixes
- Added lifecycle-aware coroutines to `DashboardViewModel` to automatically refresh **Recent Activity** on the Trakt API whenever the background `SyncWatchProgressWorker` fires.
- Wired up the empty `onMarkAsWatchedClick` handler in `DashboardScreen.kt` to natively queue a sync event direct from the "Tonight on TV" / "Airing Soon" episode cards!
- Migrated seven `NetworkTrakt*` API Models mapping the History and Check-in responses from **Moshi** (`@Json`) to **Gson** (`@SerializedName`). This guarantees the `watched_at` payloads successfully arrive at Trakt, fixing episodes stuck in the void!
- Repaired an infinite flow blocking deadlock inside `ShowSeasonsViewModel.onToggleSeasonWatched` that was silently preventing complete Seasons from uploading their progress history to Trakt.
- Corrected a critical logic flaw across `DashboardViewModel`, `ShowSeasonsViewModel`, and `ShowSeasonEpisodesViewModel` where Jetpack `StateFlow` laziness (`SharingStarted.WhileSubscribed`) silently dropped the Trakt Access Token (`null`) when launching the `SyncWatchProgressWorker`, bypassing network uploads entirely.

## Phase 2: Trakt API Check-In & Native Notifications
- **Check-In Refactor:** Overhauled `TraktAccountDataSource.checkInToShow` to accept a raw `showTraktId`. This bypasses an obsolete, synchronous IMDB network lookup that silently crashed Check-Ins for obscure episodes.
- **Native Check-In UI:** Added an interactive "Check In" button natively into the `EpisodeDetailScreen` summary block, giving users single-tap episode tracking with Material 3 loading indicators and Snackbar success/conflict responses.
- **NotificationWorker Fix:** Investigated a widespread bug where no user received push notifications for upcoming episodes. Discovered `NotificationWorker` was deadlocking similarly to the Sync queue because it was awaiting a `WhileSubscribed` `StateFlow` emission while running headless. Bypassed the UI flow entirely by wiring the Worker to read `TraktAccessToken` and `User Preferences` securely through the native Room and DataStore layers via `firstOrNull()`.

## Testing Updates
1.  **Trakt OAuth Redirect:** Follow the `Settings -> Connect Trakt Account` web browser journey and verify successful return tracking back into the `TraktAccountScreen`.
2.  **Cast Render UI Verification:** Open a mature Show from the Dashboard, tap into an old Season Episode, and observe Main Series Cast populate directly below the Overview block.
3.  **Local Static Analysis:** Check validation output on both `./gradlew detekt` and `./gradlew ktlintAndroidTestSourceSetCheck`.
4. **WorkManager Mock Assertions**: Inserted comprehensive Unit Tests using `mockito-kotlin` across `DashboardViewModelTest`, `ShowSeasonsViewModelTest`, and `ShowSeasonEpisodesViewModelTest` ensuring `WorkManager.enqueue()` explicitly intercepts authentication sync cycles.
