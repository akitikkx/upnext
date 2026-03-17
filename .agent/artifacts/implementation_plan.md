# Trakt Authentication UI State Testing Plan

The integration of `isAuthorizedOnTrakt` conditionals inside the Trakt UI screens (Episode Detail, Show Season Episodes, Show Seasons) has introduced failures in the existing testing suite due to missing mocks for `traktRepository.isAuthorizedOnTrakt()`. Furthermore, we need to guarantee that the UI components correctly reflect the authorization state through unit tests, improving overall coverage.

## Proposed Changes

### Updating Existing View Model Tests
#### [MODIFY] [EpisodeDetailViewModelTest.kt](file:///Users/ahmedtikiwa/upnext4/app/src/test/java/com/theupnextapp/ui/episodeDetail/EpisodeDetailViewModelTest.kt)
- Already successfully updated in the previous step to mock `isAuthorizedOnTrakt()` using a `MutableStateFlow(false)`.
- **Add new test**: Verify that `EpisodeDetailState` emits `isAuthorizedOnTrakt = true` when the repository emits `true`.

#### [MODIFY] [ShowSeasonEpisodesViewModelTest.kt](file:///Users/ahmedtikiwa/upnext4/app/src/test/java/com/theupnextapp/ui/showSeasonEpisodes/ShowSeasonEpisodesViewModelTest.kt)
- Fix initialization NPEs by mocking `traktAuthManager.isAuthorizedOnTrakt` and `traktRepository.isAuthorizedOnTrakt()`.
- **Add new test**: Verify `onToggleWatched` performs no action when `isAuthorizedOnTrakt` is false.
- **Add new test**: Verify `markSeasonAsWatched` and `markSeasonAsUnwatched` perform no action when `isAuthorizedOnTrakt` is false.

#### [MODIFY] [ShowSeasonsViewModelTest.kt](file:///Users/ahmedtikiwa/upnext4/app/src/test/java/com/theupnextapp/ui/showSeasons/ShowSeasonsViewModelTest.kt)
- Fix initialization NPEs by mocking `traktAuthManager.isAuthorizedOnTrakt` and `traktRepository.isAuthorizedOnTrakt()`.
- **Add new test**: Verify `onToggleSeasonWatched` performs no action when `isAuthorizedOnTrakt` is false.

## Verification Plan

### Automated Tests
Run the standard test suite execution for the modified test classes using Gradle:
```bash
./gradlew :app:testDebugUnitTest --tests "com.theupnextapp.ui.episodeDetail.EpisodeDetailViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.theupnextapp.ui.showSeasonEpisodes.ShowSeasonEpisodesViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.theupnextapp.ui.showSeasons.ShowSeasonsViewModelTest"
```

Once isolated tests execute successfully, run the full verification matrix to guarantee there are no remaining leakage issues or Ktlint stylistic violations:
```bash
./gradlew ktlintFormat detekt ktlintCheck :app:testDebugUnitTest
```
