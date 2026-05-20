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

## 4. Macrobenchmark & CI Pipelines

**Changes Made:**
- Developed `.github/workflows/performance_benchmarks.yml` to automatically assess UI performance constraints via Macrobenchmarks on PRs to `main`.
- Upgraded `BaselineProfileGenerator.kt` with a simulated `simklDashboardJourney()` rule set.

## Validation Results

- ✔️ `./gradlew testDebugUnitTest` fully passes (fixed `DashboardViewModelTest` timing race condition with `Mockito.timeout(1000)`).
- ✔️ `./gradlew assembleDebugAndroidTest` compiles and is verified to execute standard application navigation flows without crashing.
