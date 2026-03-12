# Replacing 'Tonight on TV' with Personalized Recommendations

The "Tonight on TV" calendar logic currently duplicates the user-specific "Airing Soon" behavior when authenticated. To provide better UX, we'll replace the redundant UI with Trakt's personalized `/recommendations/shows` endpoint.

## Proposed Changes

### 1. Network Layer (`TraktService`)
We will create a specific model mapping for Trakt's personalized recommendations module and inject it into the network interface.

#### [NEW] `NetworkTraktRecommendationsResponse.kt`
- Create a list response to map the returned array of Trakt `Show` objects (structurally identical to `NetworkTraktPopularShowsResponse`).

#### [MODIFY] `TraktService.kt`
- Add `@GET("recommendations/shows") fun getRecommendationsAsync(@Header("Authorization") token: String, @Query("limit") limit: Int = 20, @Query("extended") extended: String = "full"): Deferred<NetworkTraktRecommendationsResponse>`

---
### 2. Repository Layer
We will build the repository bridge to hit the recommendations endpoint and bundle TvMaze thumbnail images alongside the results.

#### [MODIFY] `TraktRepository.kt` & `TraktRepositoryImpl.kt`
- Add a suspend function `getTraktRecommendations(token: String)` that delegates to a newly appended method inside `TraktAccountDataSource.kt` or directly in `TraktRepositoryImpl`, fetching and resolving image URLs safely.

---
### 3. ViewModel & State (`DashboardViewModel.kt`)
We will integrate the newly defined repository function into the Dashboard's async dispatch loop.

#### [MODIFY] `DashboardViewModel.kt`
- Define `_recommendedShows` and `recommendedShowsImages` `StateFlow` structures.
- Implement `fetchRecommendations(bearerToken: String)` to retrieve from the repository and map `getShowImageAndTvmazeId` concurrently, just like `fetchAiringSoonShows`.
- Invoke it during `fetchDashboardData(token: String)`.

---
### 4. User Interface (`DashboardScreen.kt`)
We will replace the duplicated UI block with a custom "Recommended for You" horizontal row tailored for authenticated Trakt users.

#### [MODIFY] `DashboardScreen.kt`
- Remove the `Tonight on TV` module conditional block for authenticated users, explicitly replacing it with a `Recommended for You` section.
- Iterate over `recommendedShows` and display a horizontal list of shows using `DashboardCard` or similar components with poster images and title.

## Verification Plan

### Automated Tests
- Run Gradle Detekt and Ktlint format checks.
- Build and ensure `testDebugUnitTest` passes for mocked DataSources.

### Manual Verification
- Compile `assembleDebug` and visually verify that an authenticated emulator now shows "Recommended for You" instead of "Tonight on TV" on the Dashboard.
