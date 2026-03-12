# Upnext: Module Architecture Strategy

To answer your question directly: **No, the current module strategy is not the most optimal for a 1M+ user app that wants to scale.**

## 1. The Current State: Layer-Based Modularization
Right now, Upnext is modularized by **Layer**:
*   `:app` (Contains EVERY feature UI: Dashboard, Explore, Search, Details)
*   `:core:data` (Contains all Repositories, Databases, API calls)
*   `:core:domain` (Contains all models)
*   `:core:common` (Utils)

### The Problem with Layer-Based
*   **The Monolithic `:app` Module:** Because every single screen (Dashboard, Explore, Schedule, Settings) lives in the `:app` module, any change to a Compose file in one feature causes the entire `:app` module to recompile. As the app grows, build times will become painful.
*   **Coupling:** It's very easy for the `DashboardViewModel` to accidentally reach into something meant for `ExploreViewModel` because they live in the same module.

## 2. The Recommended State: Feature-Based Modularization
The modern "gold standard" for Android architecture (championed by Google's "Now in Android" app) is to modularize by **Feature** AND **Layer**.

We should break the monolithic `:app` module into functional slices. 

### The Target Architecture

**The `:app` Module (The Shell):**
This module becomes practically empty. Its only job is to wire things together: tying the Navigation Graph to the distinct Feature modules and initializing Hilt/App-level dependencies.

**Feature Modules (New):**
Each major section gets its own module. They depend on `core` but **not** on each other.
*   `:feature:dashboard` (Your new "Personal Tracker" home)
*   `:feature:schedule` (The "Yesterday/Today/Tomorrow" generic guide)
*   `:feature:explore` (Trending/Search)
*   `:feature:showdetail` (The comprehensive detail screen)
*   `:feature:settings` (Account/App preferences)

**Core Modules (Existing, but refined):**
*   `:core:data` (Still handles network/DB)
*   `:core:domain` (Still handles models)
*   `:core:designsystem` (**Completed in Phase 1.5**: Reusable Composables, Theme, and UI utils pulled out of `:app` into its own isolated UI library module).

## 3. Why this works for the "1M Goal"
*   **Blazing Fast Builds:** If you are working on the new `:feature:schedule` screen, Gradle *only* recompiles that module. Exploring and Dashboard are untouched.
*   **Strict Boundaries:** The `Schedule` feature physically cannot use UI components meant tightly for the `Dashboard` feature unless they are moved to a shared `:core:designsystem` module, forcing good UI reusability practices.
*   **Dynamic Delivery:** Down the line, if you build a massive feature (like a "Social Video Review" feature), you can make its feature module a "Dynamic Delivery" module, meaning it's only downloaded from the Play Store when the user clicks on it, keeping your base APK size tiny.

## Should we do this *right now*?

**Recommendation:** Proceed with caution. 
Extracting an existing monolith `:app` into feature modules is a significant refactoring effort ("yak shaving"). 

If the primary goal for Phase 1 is hitting feature parity (Settings, Notifications, Dashboard UI refresh), I recommend **building the *new* Schedule and Dashboard UIs inside the existing `:app` module for now**, but designing them with strict package separation (e.g., ensuring `com.theupnextapp.ui.dashboard` doesn't import from `com.theupnextapp.ui.explore`). 

Once the UI logic is nailed down and stable, moving a well-isolated package into its own `:feature:` module in Phase 2 or 3 is much easier than trying to architect and design the UIs simultaneously.
