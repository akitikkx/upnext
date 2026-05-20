# SIMKL Integration & Performance Optimization Tasks

- `[/]` **UI Test & Compilation Fixes**
    - `[ ]` Update `FakeTraktDao.kt` to match `TraktDao` (add `providerId` to `TrendingShows` functions).
    - `[ ]` Update `FakeTraktRepository.kt` to match `TraktRepository` (implement `refreshMostAnticipatedShows`).
    - `[ ]` Add `migrate35To36` to `MigrationTest.kt`.
- `[/]` **App Startup Performance (Production Fix)**
    - `[x]` Update `UpnextApplication.kt` to use `dagger.Lazy<TraktRepository>`.
    - `[x]` Remove `traktAuthManager` from `UpnextApplication.kt`.
    - `[x]` Ensure `DashboardViewModel.kt` enqueues `SimklSyncWorker` in a background coroutine.
- `[x]` **SIMKL Sync Loop Hardening**
    - `[x]` Add concurrency safety (Mutex) to `SimklSyncManager.kt` / `SimklSyncWorker.kt`.
- `[x]` **Macrobenchmark & CI Integration**
    - `[x]` Refine `SimklPerformanceBenchmark.kt`.
    - `[x]` Create `.github/workflows/performance_benchmarks.yml`.
- `[x]` **Verification**
    - `[x]` Run unit tests (`testDebugUnitTest`).
    - `[x]` Run Android tests (`assembleDebugAndroidTest` / `connectedDebugAndroidTest`).
