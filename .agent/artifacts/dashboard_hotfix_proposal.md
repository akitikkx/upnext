# Proposed Fixes for Dashboard Discrepancies

## Issue 1 & 2: "Up Next" Empty / Account Caching
**Diagnosis:** The dashboard currently fetches from `/sync/playback/episodes` to drive "Continue Watching". This endpoint *only* returns episodes that the user has paused in the middle of watching. It does not return the next unwatched full episode of a show. Because your second account had no paused videos, it returned an empty list.
**Fix:** We need to implement a true "Up Next" composite calculation similar to the Trakt website. 
Because Trakt API lacks a single endpoint for this, we will orchestrate it efficiently:
1. Fetch the user's recently watched shows via `/sync/watched/shows`.
2. Grab the top 5-10 most recently watched shows.
3. Concurrently fetch the specific progress for these shows using `/shows/{id}/progress/watched` to extract the `next_episode` block.
4. Merge these Next Episodes with any Paused items from `/sync/playback` to construct the unified `ContinueWatching` carousel.

## Issue 3: Endless "Recent Activity" Scrolling
**Diagnosis:** The `LazyColumn` for Recent Activity is mapping against the entire 30-day API response history, flooding the dashboard.
**Fix:** We will truncate the displayed history on the Dashboard to the top 3-5 items to maintain vertical real estate. We can add a simple "View All Activity" stub for future expansion.

## Issue 4: "Airing Soon" Discrepancy
**Diagnosis:** The Trakt `calendars/my/shows` endpoint returns shows that the user watches which air between "today" and "today+7". The Trakt website usually excludes specials (Season 0), includes different air-time filtering, or looks further ahead.
**Fix:** We will filter out any returned episodes where `season == 0` (specials) to better match main-season trajectories, and increase the lookahead window to 14 days to capture more upcoming premieres.
