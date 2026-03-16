# Upnext: The Journey to 1 Million Users

## Scale Assessment: Is Upnext Ready for 1M?
**Verdict:** 🟡 Not Quite Yet. 
While the foundational architecture (Clean Architecture, Compose, offline-first Room databases, WorkManager background syncing) is incredibly robust and built to modern Android standards, reaching and *retaining* 1 million active users requires shifting focus from pure feature development to **Performance, Retention, and Global Scale**.

Here is what Upnext needs to safely scale to 1M users:

### 1. Performance & Reliability
*   **Firebase Performance Monitoring (APM):** Crashlytics is integrated, but we need APM to track network latency, screen rendering times, and frozen frames on low-end devices.
*   **R8 Full Mode & App Bundles:** Ensure R8 aggressive shrinking is enabled to minimize APK size. Smaller apps have higher install rates.
*   **Macrobenchmark & Baseline Profiles:** The `baselineprofile` module exists, but needs to be rigorously maintained to pre-compile Compose UI paths, ensuring instant app startups. 1M users means hitting tens of thousands of low-end hardware variants where Compose rendering can stutter originally.
*   **Intelligent Sync Workers:** The Trakt sync worker currently relies on `CoroutineWorker`. We need exponential backoff policies and constraint checks (unmetered network, battery not low) heavily audited so we don't accidentally drain a million batteries during Trakt API outages.

### 2. User Retention & Engagement (Growth Engineering)
*   **Push Notifications (FCM):** Engagement is the hardest part of 1M users. We need backend/cloud-functions tracking Trakt calendars and pushing notifications: *"New Episode of The Last of Us airs tonight!"*
*   **Frictionless Onboarding:** A beautiful, animated onboarding splash that explains *why* the user should connect Trakt. Currently, Trakt auth is buried in settings.
*   **App Indexing & Deep Links:** While OAuth deep-linking works, we need Android App Links (`https://upnext.app/show/123`) so when users share shows, it drives app installations organically.

### 3. Global Reach
*   **Localization (i18n):** Trakt provides translations. Upnext needs fully localized strings for Spanish, Hindi, French, and German to capture the non-US market. Time formats must observe device Locales natively (12h vs 24h).
*   **Accessibility (a11y):** Screen reader optimization (TalkBack), adequate touch targets (48dp min), and dynamic font scaling. Google Play vigorously boosts apps with flawless accessibility scores.

---

## The Trakt API Integration Checklist

Upnext's current Trakt integration is solid, but it's only using about 40% of the API's potential. 

### 1. Check-In Functionality (Priority)
> *Requirement: Trakt requires apps to support Check-Ins if they allow real-time watching behavior.*
*   **Current State:** The backend Domain/Data architecture for this is actually already built in Upnext! (`TraktRepositoryImpl.checkInToShow()`, `NetworkTraktCheckInRequest.kt` exist).
*   **Missing:** The UI. We need a "Check In" floating action button or action item on the Episode Details Screen.
*   **Actionable Task:** Wire up the existing `checkInToShow` repository method to a new ViewModel function (`EpisodeDetailViewModel.onCheckIn()`) and expose the check-in UI status.

### 2. Ratings & Reviews
*   **Current State:** Upnext shows community read-only ratings.
*   **Missing:** No ability for the user to rate an episode out of 10 stars, nor the ability to leave a Trakt review. This is a massive missed opportunity for user engagement. Users love giving opinions.
*   **API Available:** `POST /sync/ratings` and `POST /comments`.

### 3. Native Trakt Watchlists (vs Local Favorites)
*   **Current State:** Upnext relies on a local Room database for "Favorites" (upnext-specific).
*   **Missing:** Trakt has a powerful native "Watchlist" (Shows I *want* to watch) and "Custom Lists" (e.g. "Best Sci-Fi"). Local favorites do not sync to Trakt's ecosystem, meaning if a user uninstalls, their list is gone. 
*   **Actionable Task:** Deprecate purely local Favorites. Migrate "Add to Watchlist" to sync natively with `GET /users/me/watchlist`.

### 4. Advanced Filtering & Hidden Items
*   **Current State:** Dashboard shows everything.
*   **Missing:** Users frequently abandon shows. We need to respect the Trakt `Hidden Items` API so abandoned shows disappear from "Up Next / Progress" and "Dashboard".
*   **API Available:** `GET /users/hidden/progress_watched`.

### 5. Social & Trending
*   **Current State:** Explore shows trending shows globally.
*   **Missing:** "Friends" integration. Trakt allows users to see what their friends are watching. Adding a "Social" tab drives massive user retention.
