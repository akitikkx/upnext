# Feature Release: Trakt Check-In & Push Notification Revival

## Goal Description
1. **Trakt Check-In Implementation:** Trakt requires apps tracking episode progress to support their check-in API. Currently, the `TraktAccountDataSource.checkInToShow` is structurally flawed. It requires an `imdbId`, performs a secondary `idLookupAsync` network call to Trakt to dynamically resolve it into a Trakt ID, and then posts the check-in. If the `imdbId` is missing or the network flakes, the check-in fails. We will refactor this to directly utilize the locally verified `showTraktId`. Next, we'll design and expose a native Check-In UI mechanism directly on the `EpisodeDetailScreen.kt`.
2. **Notification Fix:** Users report they receive no background push notifications for upcoming episodes. `NotificationWorker.kt` reads the user's settings and Trakt Access Tokens by applying `.first()` on `StateFlow` objects heavily restricted by `SharingStarted.WhileSubscribed`. Because no UI components are collecting the state when the background worker spins up, the Flows never initialize, causing the Worker to evaluate a false/empty initial state, skipping the notification block completely. We resolve this by bypassing the `StateFlow` and querying the DataStore/Room directly.

## User Review Required
None natively, but it's important to understand the notification pipeline relies on local device alarm scheduling (via `WorkManager`), rather than a centralized push-notification server (FCM). Trakt's API doesn't push to APNS/FCM. The `NotificationWorker` wakes up in the background out-of-band and assesses the Trakt schedule. 

## Proposed Changes

### 1. The Trakt API Check-In Overhaul
- Rewrite `TraktAccountDataSource.checkInToShow` to drop the `imdbId` parameter safely, directly receiving `showTraktId`, `seasonNumber`, and `episodeNumber`.
- Ensure `EpisodeDetailViewModel.kt` properly formats and invokes the optimized Check-In repository functions.
- Update `EpisodeDetailScreen.kt` to present a floating action button (or native card action) for checking in, tied to the ViewModel's check-in status listener (`TraktCheckInStatus`). 
- Suppress repetitive mock HTTP validation calls.

### 2. NotificationWorker Rescue
- Read `UpnextApplication.kt` and `NotificationWorker.kt`.
- Replace `settingsRepository.areNotificationsEnabled.first()` with a new direct suspending read extension `settingsRepository.getAreNotificationsEnabledSync()`.
- Replace `traktRepository.traktAccessToken.first()` with `traktDao.getTraktAccessTokens().firstOrNull()`.
- Confirm `NotificationWorkerTest.kt` aligns with this logic.

## Verification Plan

### Automated Verification
- Run `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest --tests "com.theupnextapp.work.NotificationWorkerTest"`.
- Validate static lint rules are intact.

### Manual Verification
- Deploy to an emulator with Trakt authenticated. Toggle Notifications ON. Trigger a manual sync of `NotificationWorker.kt` natively via App Inspector or Device Shell to verify the notification bundle paints to the system tray.
- Launch `EpisodeDetailScreen` and check-in to an episode. Verify via Logcat network tracking that `POST /checkin` receives a `200 OK` instantaneously without a secondary lookup.
