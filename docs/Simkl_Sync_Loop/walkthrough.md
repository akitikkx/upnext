# SIMKL Integration & Performance Optimization Walkthrough

The following implementation steps successfully stabilized the SIMKL sync pipeline and resolved the production app startup performance degradation.

## 1. App Startup Performance Fix (Production)

> [!TIP]
> **Issue Identified:** A 623% degradation in app startup time on the production branch was traced to eager initialization of the massive `TraktRepository` DI graph inside `UpnextApplication.onCreate()`.

**Changes Made:**
- Modified `UpnextApplication.kt` to inject `dagger.Lazy<TraktRepository>`. By deferring instantiation to `traktRepository.get()`, the app completely bypasses the initialization of Retrofit, Moshi, OkHttp, and the underlying SQLite Room database during the critical `onCreate()` loop.
- Eliminated `TraktAuthManager` from the application component injection as it was unused, further slimming down the startup phase DI payload.

## 2. SIMKL Sync Loop Hardening

> [!WARNING]
> **Race Conditions:** Frequent or redundant `triggerSyncIfAuthenticated` calls from the dashboard or history UI previously kicked off unsafe concurrent sync requests.

**Changes Made:**
- Implemented `kotlinx.coroutines.sync.Mutex` directly inside `SimklSyncManager.sync()`. The `syncMutex.withLock` wrapper natively ensures thread safety across all overlapping invocations of the background sync worker.
- Offloaded `WorkManager.enqueue()` inside `DashboardViewModel.kt` to the `Dispatchers.IO` background coroutine, preventing Strict Mode violations and potential UI jank associated with WorkManager's internal SQLite allocations.

## 3. UI Test & Compilation Stability

**Changes Made:**
- Resolved `FakeTraktDao` abstract method mismatch by correctly implementing `deleteSpecificTrendingShows` with the newly introduced `providerId` footprint.
- Updated `FakeTraktRepository` with implementations to align with changes made to `TraktRepository`.
- Added missing `MIGRATION_35_36` backwards-compatibility verifications directly into `MigrationTest.kt` to ensure standard Android testing paradigms evaluate the new `simkl_watched_episodes` schema.

## 4. Macrobenchmark & CI Integration

**Changes Made:**
- Combined pull request checks and performance benchmarks into a unified CI pipeline `.github/workflows/ci.yml`. The macOS-based Macrobenchmark job runs sequentially after code verification (`verify`) and instrumented UI tests (`ui-tests`) succeed to optimize GitHub Action minutes.
- Upgraded `BaselineProfileGenerator.kt` with a simulated `simklDashboardJourney()` rule set.

## 5. SIMKL Show Filtering & Provider UI Indicator

**Changes Made:**
- Updated `refreshTrendingShows` and `refreshPremieres` in `SimklRepository.kt` to filter out shows that lack both an `imdbId` and `tvdbId` using the criteria `.filter { !it.imdbID.isNullOrEmpty() || it.tvdbID != null }`. This prevents empty, unclickable shows from displaying on the dashboard.
- Added a new unit test in `SimklRepositoryTest.kt` (`refreshPremieres filters out shows without imdbId or tvdbId`) to verify filtering works as expected.
- Added string resources (`provider_via_trakt`, `provider_via_simkl`) to `strings.xml`.
- Refactored `MainScreen.kt` to collect `activeProvider` at the top level and updated `TopAppBar` to display a subtitle indicating the active provider (e.g. "via Trakt" or "via SIMKL").

## 6. Test & Build Performance Optimizations

**Changes Made:**
- Resolved `MigrationTest` asset validation failures in `app/build.gradle` by declaring a custom `copyRoomSchemas` Copy task that combines `$projectDir/schemas` and `${project(':core:data').projectDir}/schemas` into `$buildDir/intermediates/room-schemas` with `duplicatesStrategy = DuplicatesStrategy.EXCLUDE`. This avoids asset merging conflicts (e.g. over `31.json`) on CI.
- Configured unit tests and instrumented tests to `failFast = true` / `failFast: 'true'` in `app/build.gradle` and `core/data/build.gradle` to abort test suites immediately on the first failure, preserving CI minutes.

## 7. Gzip/TeeSource Interceptor Conflict Resolution

**Changes Made:**
- Swapped the registration order of `ChuckerInterceptor` and `HttpLoggingInterceptor` inside `NetworkModule.kt`. 
- By registering `ChuckerInterceptor` first, it becomes the outer application interceptor, meaning `HttpLoggingInterceptor` (downstream) consumes the raw network response body stream first. This prevents `HttpLoggingInterceptor` from attempting to read and decompress a stream wrapped by Chucker's custom `TeeSource`/`DepletingSource`, completely eliminating the `java.io.IOException: gzip finished without exhausting source` error on compressed endpoints (such as TMDB).

## Validation Results

- ✔️ `./gradlew testDebugUnitTest` fully passes (verified that all repository and network configuration tests pass successfully).
- ✔️ Build and compilation verified locally (Gradle build successful in under 2 minutes).
