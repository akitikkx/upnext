# Upnext: Dashboard Redesign Concept

The user feedback is highly accurate: if the primary "Dashboard" (the landing screen of the app) looks like a list of shows airing today/tomorrow, it resembles a streaming service (like Netflix) rather than a **personal TV tracker**. 

To hit the "1M User Baseline", the landing screen needs to answer one question immediately: **"What should *I* watch next?"**

## Current State Analysis
*   **Dashboard (`DashboardScreen.kt`):** Currently shows generic schedule data ("Yesterday, Today, Tomorrow"). This is useful information, but it's not a personal dashboard. It's a global schedule.
*   **Explore (`ExploreScreen.kt`):** Currently shows "Trending, Popular, Most Anticipated". This is good discovery content.

## Proposed Strategy: "The Personal Tracker Model"

We need to flip the paradigm. The first screen the user sees MUST be tied to *their* data (or prompt them to connect/add data).

### 1. New Home/Dashboard Screen ("My Upnext")
This screen replaces the current Dashboard. It is highly personalized.

**Components (Top to Bottom):**
1.  **"Up Next to Watch" (The Hero Section):**
    *   This is the most critical UI element.
    *   It shows a large, prominent horizontal list (or paging grid) of the *exact next episode* the user needs to watch for the shows they are currently tracking.
    *   **Crucial Action:** Each item needs a clear, 1-tap "Mark as Watched" button overlaid on the thumbnail.
    *   *If User is Not Logged In:* Instead of duplicate Explore data or a blank state, provide a compelling **"Tonight on TV" Roladex** powered by TVMaze's daily schedule (US prime time default, localized later), and a **Most Anticipated** horizontal list. This acts as an immediate utility. Interspersed will be the "Connect Trakt" CTA.
2.  **"Airing Soon for You":**
    *   A personalized schedule. "Shows *you* follow that are airing in the next 7 days."
    *   This is much more relevant than a global "Who is airing today?" list.
3.  **"Continue Watching" / "Jump Back In":**
    *   Shows the user paused mid-season or haven't watched in a few months.
4.  **"Your Recent Activity":**
    *   A small feed of what they just watched (Trakt history).

### 2. The "Discover/Explore" Screen (Updated)
This handles finding new content.

**Components:**
1.  **Search Bar (Prominent):** For direct lookups.
2.  **"Recommended for You" (Trakt Integration):**
    *   Powered by Trakt's recommendation engine based on their watch history.
3.  **"Trending This Week" (Current Explore data):**
    *   What everyone else is watching.
4.  **"Most Anticipated" (Current Explore data).**
5.  **"Popular" (Current Explore data).**

### 3. The "Schedule" Screen (The old Dashboard)
The old "Yesterday, Today, Tomorrow" logic doesn't need to be deleted, it just needs to be moved.
*   We can create a dedicated "Schedule" tab in the bottom navigation.
*   This is for users who want to see *everything* airing on TV globally on a given day, acting as a true TV Guide.

## Actionable Steps for Implementation:

1.  **Refactor Navigation:**
    *   Home -> "My Upnext" (New personalized view)
    *   Search/Explore -> (Updated with Recommendations)
    *   Schedule -> (The old Dashboard view)
    *   Account/Settings -> (The new settings area we discussed)
2.  **Dashboard Viewport:**
    *   We need to query the Trakt API (or local DB if synced) for the user's specific "Progress" to build the "Up Next to Watch" horizontal scrolling list.
3.  **UI/UX:**
    *   Design a specific "Up Next Episode" card that looks distinctly different from a "Generic Show List" card. It needs to look like an *actionable* item (e.g., circular progress bar around the play icon, clear Season/Episode numbers).
