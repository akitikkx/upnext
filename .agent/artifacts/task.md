# Adaptive Detail Layout Execution

- [x] Refactor `SynopsisArea.kt`
    - [x] Create `SynopsisAreaTextOnly` composable for Expanded mode (no poster).
    - [x] Route `WindowWidthSizeClass.Expanded` to use `SynopsisAreaTextOnly`.
- [x] Refactor `ShowDetailScreen.kt`
    - [x] Add `ExpandedDetailArea()` composable with `Row` (Left Pane / Right Pane).
    - [x] Build Left Pane: `PosterImage`, `ShowDetailButtons`, `TraktRatingSummary`.
    - [x] Build Right Pane: Title, Status, `SynopsisArea`, `WatchProviders`, `ShowCast`, `NextEpisode`, `PreviousEpisode`, `SimilarShows`.
    - [x] Update main `DetailArea` to dynamically render `CompactDetailArea` (the old vertical stack) or `ExpandedDetailArea`.
- [x] Update `ShowDetailButtons`
    - [x] Adjust the layout/wrappers so the buttons fill the left pane neatly when in Expanded mode.
- [x] Verify build and layout rendering locally without crashing.
