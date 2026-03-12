# Upnext: Feature Baseline & Trakt Capabilities Analysis

To reach the 1M+ user mark, Upnext needs to move beyond simple tracking and offer a comprehensive "TV Management" experience. Based on an analysis of top competitors (like TV Time, Hobi, SeriesGuide) and Trakt.tv's core capabilities, here is a breakdown of what constitutes a "Baseline" versus "Niche-out/Premium" features.

## 1. The "1M User Baseline" (Must-Haves)
These are features that users consider table stakes. If an app using Trakt/TVMaze doesn't have these, users will churn to competitors.

*   **The "What's Next" Dashboard:**
    *   **Current State:** Upnext has a dashboard.
    *   **Baseline Expectation:** A highly refined view showing *exactly* which episode is next for every active show. It shouldn't just list shows; it should list the specific `S02E04` the user needs to watch right now, with an immediate "Mark Watched" action.
*   **Progress Tracking & Visuals:**
    *   **Baseline Expectation:** Clear visual indicators (progress bars, pie charts) showing how much of a season/show is completed (e.g., "15/20 episodes watched").
*   **Comprehensive Release Calendar:**
    *   **Baseline Expectation:** A personalized calendar showing when upcoming episodes for *watched/watchlist* shows are airing.
*   **Push Notifications for Airing Episodes:**
    *   **Baseline Expectation:** The holy grail of retention. Users must get an alert (local or push) saying: *"New episode of Succession airs tonight at 9 PM."*
*   **Robust List Management:**
    *   **Current State:** Upnext has a Watchlist.
    *   **Baseline Expectation:** Trakt offers custom lists (e.g., "Cozy Fall Watches", "To Binge With Partner"). Upnext should support viewing and managing these custom Trakt lists, not just the default Watchlist.
*   **"Where to Watch" Integration:**
    *   **Current State:** Mostly absent.
    *   **Baseline Expectation:** Users need to know *where* to watch the show (Netflix, Hulu, Max). Trakt provides this data via JustWatch integration. This is critical for utility.

## 2. Leveraging Trakt.tv's Power Features
When a user marks a season as watched on Trakt, it unlocks several data points we aren't fully utilizing:

*   **Algorithmic Discovery (Personalized Recommendations):**
    *   Trakt generates highly accurate recommendations based on watched history. Upnext should prominently feature a "Recommended for You" section powered directly by the `GET /recommendations/shows` Trakt endpoint.
*   **Viewing History & Statistics:**
    *   Trakt completely tracks watch time. Users love seeing their "All-Time Stats" (e.g., "You've watched 45 days of TV"). Upnext could build a beautiful "My Stats" profile page leveraging this Trakt data.
*   **Social & Community:**
    *   Trakt has comments, ratings, and reviews for every episode/show. Pulling top community comments into the Show/Episode details screen adds a social layer without us needing to host a backend.
*   **Hidden/Archived Shows:**
    *   When a user gives up on a show, they "hide" it on Trakt. Upnext needs to respect the Trakt "Hidden Items" list so recommended or dashboard feeds aren't polluted with shows the user dropped.

## 3. "Niche-Out" / Premium Differentiators
These are the features that make an app "World-Class" and give users a reason to switch *to* Upnext from an established competitor.

*   **Deep Offline Support (Data Saver):**
    *   Allowing users to download their entire cached dashboard and next-up list for viewing on airplanes or subways, with offline "Mark as Watched" actions that sync later.
*   **Adaptive UI / Form Factor Polish:**
    *   What you're currently working on: having a gorgeous, adaptive UI supporting Foldables and landscape tablets perfectly. Many older competitors look dated or are phones-only.
*   **The "Binge" Mode (Custom UI):**
    *   A specific viewing mode when a user is catching up on an old show, allowing rapid-fire "Next, Next, Next" check-ins without leaving a full-screen image-heavy view.
*   **Widget Ecosystem:**
    *   Beautiful Android Home Screen widgets ("Up Next to Watch", "Airing Today").

## Proposed Next Steps based on this Assessment:

If we agree on this baseline, our priority order should shift to maximize retention and basic utility:

1.  **Notifications (Baseline):** Implement the `NotificationWorker` to alert users of airing episodes.
2.  **The "Next Episode" Dashboard Polish (Baseline):** Refine the dashboard to make it explicitly clear *what* to watch next and offer 1-tap check-ins.
3.  **"Where to Watch" (Baseline):** Integrate streaming provider data into Show Details.
4.  **Settings & Statistics (Leveraging Trakt):** Build the profile/settings area to show Trakt account status and viewing stats.
