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

## 7. Lenient Gzip Response Decompression (TMDB Fix)

> [!IMPORTANT]
> **Issue Identified:** Swapping interceptor order still failed on certain TMDB requests because TMDB occasionally returns extra trailing bytes (whitespace or newlines) at the end of the GZIP stream. Since application interceptors run after OkHttp's built-in `BridgeInterceptor` has already stripped the `Content-Encoding` header, Okio's strict `GzipSource` is used by OkHttp under-the-hood. When `HttpLoggingInterceptor` or `ChuckerInterceptor` buffers the response body using `source.request(Long.MAX_VALUE)`, `GzipSource` fails due to the trailing bytes, throwing `gzip finished without exhausting source`.

**Changes Made:**
- Implemented `GzipDecompressionInterceptor` inside [NetworkModule.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/main/java/com/theupnextapp/di/NetworkModule.kt).
- Registered it as a **Network Interceptor** (`addNetworkInterceptor`). Running as a network interceptor allows it to intercept raw responses directly from the network *before* `BridgeInterceptor` runs.
- The interceptor uses Java's built-in `java.util.zip.GZIPInputStream` to decompress the body stream. `GZIPInputStream` is lenient and gracefully discards any trailing bytes/newlines at the end of the GZIP stream.
- The interceptor strips the `Content-Encoding: gzip` and `Content-Length` headers, passing a clean, uncompressed body upstream. Downstream application interceptors (`HttpLoggingInterceptor`, `ChuckerInterceptor`) and the Retrofit converters receive plain text, resolving the decompression crashes and preventing `A resource failed to call close` warnings.
- Added comprehensive unit tests in [GzipDecompressionInterceptorTest.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/test/java/com/theupnextapp/common/utils/GzipDecompressionInterceptorTest.kt) to verify decompression, lenient handling of trailing bytes, and unmodified forwarding of non-gzipped content.

## Validation Results

- ✔️ `./gradlew :core:data:testDebugUnitTest` fully passes, including the new `GzipDecompressionInterceptorTest`.
- ✔️ The entire build, static analysis, and code quality checks successfully pass locally:
  ```bash
  ./gradlew ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug assembleRelease
  ```
  Completed successfully in `12m 46s` with a zero-exit status.
