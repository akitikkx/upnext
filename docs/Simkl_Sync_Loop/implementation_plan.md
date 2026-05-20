# SIMKL Integration & Performance Optimization Plan

This plan addresses the UI test failures, the app startup regression (623% slower), implements the requested performance benchmarking for the new SIMKL provider, filters out incomplete SIMKL shows, and adds a UI indicator for the active provider.

## User Review Required

> [!WARNING]
> Thank you for the clarification. Since the 623% startup degradation is on the *production* branch, the cause is the eager Dependency Injection (DI) initialization in `UpnextApplication.onCreate()`. Currently, `TraktRepository` (and all its dependencies like `OkHttpClient`, Retrofit, and Room) are injected synchronously on the main thread, but only used inside a background coroutine. I will use `dagger.Lazy<TraktRepository>` to defer this massive DI graph instantiation to the background thread, and remove unused injections like `TraktAuthManager`.

## Proposed Changes

### SIMKL Show Filtering & Provider UI Indicator

#### [MODIFY] [SimklRepository.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/main/java/com/theupnextapp/repository/SimklRepository.kt)
- Update `refreshTrendingShows` and `refreshPremieres` to filter out shows that do not have enough data to load details.
- Since Upnext relies on TVMaze for show details (which requires either an IMDB ID or a TVDB ID), any SIMKL show lacking both of these identifiers cannot be loaded and should be filtered out from the list flows.
- Filter criteria: `!it.imdbID.isNullOrEmpty() || it.tvdbID != null`

#### [MODIFY] [MainScreen.kt](file:///Users/ahmedtikiwa/upnext4/app/src/main/java/com/theupnextapp/ui/main/MainScreen.kt)
- Move `activeProvider` collection to the top of `MainScreen`.
- Modify the `TopAppBar` title in the list pane scaffold to show a subtitle indicating the active provider (e.g., "via Trakt" or "via SIMKL").

#### [MODIFY] [strings.xml](file:///Users/ahmedtikiwa/upnext4/app/src/main/res/values/strings.xml)
- Add new string resources for the provider subtitles:
  - `provider_via_trakt` ("via Trakt")
  - `provider_via_simkl` ("via SIMKL")

---

### UI Test Fixes & Migration Coverage

#### [MODIFY] [MigrationTest.kt](file:///Users/ahmedtikiwa/upnext4/app/src/androidTest/java/com/theupnextapp/database/MigrationTest.kt)
- Add `migrate35To36()` to test the new SIMKL integration schema changes (`simkl_watched_episodes`, `simkl_trending_shows`, etc.). Missing migration tests often cause the Room schema verification to fail in CI.

#### [MODIFY] [app/build.gradle](file:///Users/ahmedtikiwa/upnext4/app/build.gradle)
- Declare a custom task `copyRoomSchemas` to copy both `$projectDir/schemas` and `${project(':core:data').projectDir}/schemas` into `$buildDir/intermediates/room-schemas` with `duplicatesStrategy = DuplicatesStrategy.EXCLUDE`.
- Map `$buildDir/intermediates/room-schemas` to `androidTest.assets.srcDirs` so the migration test helper can access the version 35 and 36 JSON schemas during runtime without causing duplicate asset merge failures on common schemas like `31.json`.
- Add `testInstrumentationRunnerArguments failFast: 'true'` to terminate instrumented tests on the first failure.
- Configure `testOptions.unitTests.all { failFast = true }` to fail fast during unit test execution.

#### [MODIFY] [core/data/build.gradle](file:///Users/ahmedtikiwa/upnext4/core/data/build.gradle)
- Add `testInstrumentationRunnerArguments failFast: 'true'` and unit tests `failFast = true` configuration.

#### [MODIFY] [FakeTraktDao.kt](file:///Users/ahmedtikiwa/upnext4/app/src/test/java/com/theupnextapp/database/fakes/FakeTraktDao.kt)
- Ensure all mocked `DatabaseTrendingShows` functions (e.g., `deleteSpecificTrendingShows`) include the new `providerId` parameter introduced for multi-provider support. This resolves unit test compilation failures.

---

### App Startup Performance Fixes (Production)

#### [MODIFY] [UpnextApplication.kt](file:///Users/ahmedtikiwa/upnext4/app/src/main/java/com/theupnextapp/UpnextApplication.kt)
- Wrap `traktRepository` with `dagger.Lazy<TraktRepository>` to defer the instantiation of the entire network and database DI graph until the background worker thread actually accesses it.
- Remove the completely unused `traktAuthManager` injection from `UpnextApplication`, eliminating unnecessary DI overhead during the critical `onCreate()` path.
- Verify `initializeBackgroundTasks()` executes properly without blocking the main thread.

#### [MODIFY] [DashboardViewModel.kt](file:///Users/ahmedtikiwa/upnext4/app/src/main/java/com/theupnextapp/ui/dashboard/DashboardViewModel.kt)
- Move `SimklSyncWorker` enqueue logic to a background coroutine to prevent WorkManager's internal SQLite database accesses from blocking the UI thread during view model initialization.

---

### Macrobenchmark & CI Integration

#### [MODIFY] [SimklPerformanceBenchmark.kt](file:///Users/ahmedtikiwa/upnext4/baselineprofile/src/main/java/com/theupnextapp/baselineprofile/SimklPerformanceBenchmark.kt)
- Enhance the trace section metrics to accurately measure `SimklDashboardFetch` and report custom trace telemetry.

#### [NEW] [performance_benchmarks.yml](file:///Users/ahmedtikiwa/upnext4/.github/workflows/performance_benchmarks.yml)
- Create a new GitHub Actions workflow to run the Macrobenchmark suite automatically on a weekly schedule and on PRs modifying core architecture, leveraging `ubuntu-latest` and hardware-accelerated emulators.

---

### SIMKL Sync Loop Hardening

#### [MODIFY] [SimklSyncWorker.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/main/java/com/theupnextapp/work/SimklSyncWorker.kt)
- Wrap `simklSyncManager.sync(token)` in a `Mutex` or `Mutex`-like synchronization block within `SimklSyncManager` to ensure thread safety if multiple syncs trigger simultaneously.

## Verification Plan

### Automated Tests
- `./gradlew :app:connectedDebugAndroidTest` (Run UI and migration tests)
- `./gradlew :app:testDebugUnitTest` (Verify FakeTraktDao fixes and repository filters)
- `./gradlew :baselineprofile:connectedAndroidTest` (Verify Macrobenchmark execution)

### Manual Verification
- Deploy to an emulator/device and verify that the TopAppBar shows the active provider subtitle correctly.
- Verify that shows without IMDB/TVDB IDs are no longer displayed on the Dashboard/Explore screens.

## Part 2: SIMKL Integration Fixes & Provider UI Improvements (Approved)

This plan addresses the blank SIMKL Premieres section on the dashboard, the blank Trakt recent history after provider switches, and adds a subtle UI indicator for the active provider.

### Proposed Changes

#### SIMKL Show Filtering (Part 2)
- [SimklRepository.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/main/java/com/theupnextapp/repository/SimklRepository.kt): Do not construct a URL if `networkShow.poster` is null or empty. Relax the filter in `refreshPremieres` and `refreshTrendingShows` to allow shows with a valid poster image and a `simkl_id`. Filter criteria: `!it.mediumImageUrl.isNullOrEmpty() && (!it.imdbID.isNullOrEmpty() || it.tvdbID != null || it.id != null)`.
- [SimklRepositoryTest.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/test/java/com/theupnextapp/repository/SimklRepositoryTest.kt): Update unit tests to reflect the new mapping and filtering logic.

#### Dashboard History Sync & Switching (Part 2)
- [DashboardViewModel.kt](file:///Users/ahmedtikiwa/upnext4/app/src/main/java/com/theupnextapp/ui/dashboard/DashboardViewModel.kt): Keep track of `currentHistoryProvider` and clear cache (`_recentHistory.value = null` and `_historyImages.value = emptyMap()`) on provider changes.

#### UI Provider Indicators (Part 2)
- [DashboardScreen.kt](file:///Users/ahmedtikiwa/upnext4/app/src/main/java/com/theupnextapp/ui/dashboard/DashboardScreen.kt): In the "My Upnext" section header, add a subtle, modern badge indicating the active provider ("Trakt" or "SIMKL").
- [ShowDetailScreen.kt](file:///Users/ahmedtikiwa/upnext4/app/src/main/java/com/theupnextapp/ui/showDetail/ShowDetailScreen.kt) / [BackdropAndTitle.kt](file:///Users/ahmedtikiwa/upnext4/app/src/main/java/com/theupnextapp/ui/showDetail/BackdropAndTitle.kt): Pass `activeProvider` to `BackdropAndTitle` and display a subtle subtitle "via Trakt" or "via SIMKL" next to show status/certification.


