# SIMKL Integration & Performance Optimization Plan

This plan addresses the UI test failures, the app startup regression (623% slower), and implements the requested performance benchmarking for the new SIMKL provider.

## User Review Required

> [!WARNING]
> Thank you for the clarification. Since the 623% startup degradation is on the *production* branch, the cause is the eager Dependency Injection (DI) initialization in `UpnextApplication.onCreate()`. Currently, `TraktRepository` (and all its dependencies like `OkHttpClient`, Retrofit, and Room) are injected synchronously on the main thread, but only used inside a background coroutine. I will use `dagger.Lazy<TraktRepository>` to defer this massive DI graph instantiation to the background thread, and remove unused injections like `TraktAuthManager`.

## Open Questions

> [!IMPORTANT]
> 1. Do you have a specific trace marker string for the Macrobenchmark besides `SimklDashboardFetch`? 
> 2. For the Firebase and Google Play metrics analysis, would you like me to focus solely on ANRs and startup times, or also include network latency metrics?

## Proposed Changes

### UI Test Fixes & Migration Coverage

#### [MODIFY] [MigrationTest.kt](file:///Users/ahmedtikiwa/upnext4/app/src/androidTest/java/com/theupnextapp/database/MigrationTest.kt)
- Add `migrate35To36()` to test the new SIMKL integration schema changes (`simkl_watched_episodes`, `simkl_trending_shows`, etc.). Missing migration tests often cause the Room schema verification to fail in CI.

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
- `./gradlew :app:testDebugUnitTest` (Verify FakeTraktDao fixes)
- `./gradlew :baselineprofile:connectedAndroidTest` (Verify Macrobenchmark execution)

### Manual Verification
- Deploy to an emulator/device and monitor the logcat for `Choreographer` skipped frames during app startup.
- Review Firebase Crashlytics and Google Play Console (via mock/local analysis) for regression metrics.
