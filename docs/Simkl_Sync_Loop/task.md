# SIMKL Integration & Performance Optimization Tasks

- `[x]` **UI Test & Compilation Fixes**
    - `[x]` Update `FakeTraktDao.kt` to match `TraktDao` (add `providerId` to `TrendingShows` functions).
    - `[x]` Update `FakeTraktRepository.kt` to match `TraktRepository` (implement `refreshMostAnticipatedShows`).
    - `[x]` Add `migrate35To36` to `MigrationTest.kt`.
- `[x]` **App Startup Performance (Production Fix)**
    - `[x]` Update `UpnextApplication.kt` to use `dagger.Lazy<TraktRepository>`.
    - `[x]` Remove `traktAuthManager` from `UpnextApplication.kt`.
    - `[x]` Ensure `DashboardViewModel.kt` enqueues `SimklSyncWorker` in a background coroutine.
- `[x]` **SIMKL Sync Loop Hardening**
    - `[x]` Add concurrency safety (Mutex) to `SimklSyncManager.kt` / `SimklSyncWorker.kt`.
- `[x]` **Macrobenchmark & CI Integration**
    - `[x]` Refine `SimklPerformanceBenchmark.kt`.
    - `[x]` Create `.github/workflows/ci.yml` and configure benchmarks dependency.
- `[x]` **Verification**
    - `[x]` Run unit tests (`testDebugUnitTest`).
    - `[x]` Run Android tests (`assembleDebugAndroidTest` / `connectedDebugAndroidTest`).
