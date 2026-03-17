---
name: Upnext Product Principles
description: Core product design and development principles for Upnext, derived from competitive analysis of the official Trakt app's negative user feedback. Use this skill when proposing new features, designing UI/UX, or prioritizing the roadmap.
---

# Upnext Product Principles

When designing features or writing code for Upnext, you MUST adhere to the following product principles. These principles ensure we capitalize on the gaps left by the official Trakt application and win over disenchanted users.

## 1. Maximize Information Density & Customization
- **Avoid Excessive Horizontal Scrolling:** Users despise UI updates that force large, single-item horizontal carousels. Always provide dense vertical lists or multi-column grids (showing 3-5 items at a glance).
- **Customizable Dashboards:** Allow users to choose what tabs they see and how many items are displayed in sections like "Next Up" or "Trending".

## 2. Frictionless Navigation & Actions
- **Minimize Clicks:** Core actions like rating a show/season/episode, marking an item as watched, or adding it to a collection should take 1-2 clicks maximum.
- **Quick Episode Traversal:** Ensure episode detail screens have intuitive user flows (e.g., "Previous/Next Episode" arrows or swipe gestures). Do not force users to back out to the season list to change episodes.

## 3. Robust List & Collection Management
- **Search & Sort Everywhere:** All lists (custom lists, watch history, collections) must be searchable and sortable (by genre, date added, release date, rating). 
- **Granular List Items:** Allow users to add entire shows, specific seasons, or individual episodes to their custom lists seamlessly.

## 4. Comprehensive Data Display
- **Show the Details:** Never hide useful metadata. Always display full release dates/times, complete cast and guest actor lists, and detailed watch history (not just the calendar date).

## 5. Value-Driven Monetization (Anti-Paywall)
- **Essential Features are Free:** Do not paywall basic tracking functionalities (like list capacity, manual collection adding, or UI sorting). Ensure basic data management, using the Trakt API on behalf of the user, remains frictionless and unrestricted.
