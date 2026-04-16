# UI Metadata Enrichment for Optimistic Watchlist Insertion

When a standard Trakt sync occurs, the application properly fetches and maps TVMaze records with heavy metadata properties (such as Network, Status, and Premiere year), ensuring the user's Watchlist looks rich and informative.
However, when a user explicitly taps **"Add to Watchlist"** from `ShowDetailScreen`, `ShowDetailViewModel` dispatches an `AddToWatchlistWorker`. While the worker completes the cloud action, it skips the heavy reverse-sync from Trakt to avoid overwriting the app's snappy local Optimistic Update with Trakt's slow-to-update cache.
Unfortunately, the optimistic update was hardcoded to only insert the `title` and `images`, leaving fields like `year`, `tvMazeID`, `network`, `status`, and `rating` forcibly `null` inside `TraktRepositoryImpl.addToWatchlist`. 

To fix this, we will pass the extra metadata properties downward across the chain, capturing them directly from the `ShowDetailScreen`'s existing TVMaze payload.

## User Review Required

Please review the proposed data-flow below. Since this involves expanding an existing Database Entity's initialization arguments, it is a low-risk change safely extending the `TraktRepository` boundaries.

## Proposed Changes

### Domain & Network Layer

We will expose the previously dormant `premiered` and `network` metadata from the core TVMaze endpoint up to the `ShowSummary` state.

#### [MODIFY] [ShowDetailSummary.kt](file:///Users/ahmedtikiwa/upnext4/core/domain/src/main/java/com/theupnextapp/domain/ShowDetailSummary.kt)
- Expand the Parcelable model to include:
  - `val network: String?`
  - `val premiered: String?`
- Apply these changes symmetrically to `constructor`, `writeToParcel`, and `#emptyShowData`.

#### [MODIFY] [NetworkShowInfoResponse.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/main/java/com/theupnextapp/network/models/tvmaze/NetworkShowInfoResponse.kt)
- In the `asDomainModel` mapping extension, assign `premiered = premiered` and `network = network?.name`.

---

### Data & Worker Layer

We will adjust the caching pipeline to persist the enriched data for optimistic viewing on the UI.

#### [MODIFY] [AddToWatchlistWorker.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/main/java/com/theupnextapp/work/AddToWatchlistWorker.kt)
- Establish static constants: `ARG_TVMAZE_ID`, `ARG_YEAR`, `ARG_NETWORK`, `ARG_STATUS`, `ARG_RATING`.
- Extract these keys from `inputData` inside `doWork()`.
- Route the populated parameters onward into `addShowToWatchlist` and identically through to the repository layer.

#### [MODIFY] [TraktRepository.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/main/java/com/theupnextapp/repository/TraktRepository.kt) & [TraktRepositoryImpl.kt](file:///Users/ahmedtikiwa/upnext4/core/data/src/main/java/com/theupnextapp/repository/TraktRepositoryImpl.kt)
- Amend the `addToWatchlist` signature.
- Provide the variables directly into the `DatabaseWatchlistShows` instantiation instead of hardcoded `nulls`.

---

### UI & Presentation Layer

We will pack the parameters originating from our view state inside the Worker creation blueprint natively.

#### [MODIFY] [ShowDetailViewModel.kt](file:///Users/ahmedtikiwa/upnext4/app/src/main/java/com/theupnextapp/ui/showDetail/ShowDetailViewModel.kt)
- In `onAddRemoveWatchlistClick`, enrich the `Data.Builder()` payload:
  - Extract `.putInt(ARG_TVMAZE_ID)` from `showSummary.id`.
  - Slice the year using `.putString(ARG_YEAR)` from `showSummary.premiered.substring(0,4)`.
  - Provide `showSummary.network` and `showSummary.status` as `.putString()`.
  - Provide `.putDouble(ARG_RATING)` mapping to the currently tracked `showRating.value?.rating`.

## Open Questions
None.

## Verification Plan

### Automated Tests
We will add and update unit tests to ensure that the augmented metadata traverses the architectural boundaries accurately:
- **`ShowDetailViewModelTest.kt`**: Verify the `WorkManager` data builder correctly maps properties from `uiState` to `Data` when `onAddRemoveWatchlistClick()` is triggered.
- **`TraktRepositoryImplTest.kt`**: Add tests to ensure that `addToWatchlist` safely incorporates the newly available `year`, `network`, `status`, etc., without defaults to `null`.
- **`AddToWatchlistWorkerTest.kt` [NEW]**: Since it doesn't currently exist, we'll configure a `TestListenableWorkerBuilder` to verify that the `doWork()` loop natively extracts our new `ARG_YEAR`, `ARG_NETWORK`, etc. from its input data and successfully calls the repository method with full parameters.
- Validate local building and running the test suite using `./gradlew :app:testDebugUnitTest`.

### Manual Verification
- Navigating to a non-watchlisted Show Detail screen.
- Watchlisting the item from the FAB button.
- Re-opening the Account Watchlist screen to verify the `year`, `status`, `rating` and `network` instantly display without requiring a swipe-down manual sync.
