### 2. Full UI Localization (Dutch and Global)

The goal is to ensure all remaining hardcoded strings in the UpNext application are extracted to `strings.xml` and fully translated into Dutch (testing language). This includes the Show Detail, Schedule, Explore, Search, and Account screens.

## User Review Required

Please review the proposed keys and English/Dutch string mappings. I will use standard string extraction to `res/values/strings.xml` and provide Dutch translations in `res/values-nl/strings.xml`. I will also run the test suite afterwards.

## Proposed Changes

---

### `values/strings.xml` and `values-nl/strings.xml`
I will add or update the following string keys in both the default English `strings.xml` and the Dutch `values-nl/strings.xml` file. 
For existing keys that are simply missing in the Dutch file, I will add the translated equivalents.

- **Schedule / Explore**:
  - `title_yesterday_shows` (Aired Yesterday -> Gisteren uitgezonden)
  - `title_today_shows` (Airing Today -> Vandaag op TV)
  - `title_tomorrow_shows` (Airing Tomorrow -> Morgen op TV)
  - `title_trending_shows` (Trending -> Trending)
  - `title_popular_shows` (Popular -> Populair)
  - `title_anticipated_shows` (Anticipated -> Verwacht)

- **Search**:
  - `search_input_hint` (Search for a show... -> Zoek naar een serie...)
  - `search_empty_title` (No results found -> Geen resultaten gevonden)

- **Show Detail** (missing Dutch equivalents for existing keys):
  - `show_detail_genres_heading` (Genres -> Genres)
  - `show_detail_air_days_heading` (Airs on -> Uitzenddagen)
  - `show_detail_next_episode_heading` (Next Episode -> Volgende Aflevering)
  - `show_detail_previous_episode_heading` (Previous Episode -> Vorige Aflevering)
  - `btn_show_detail_seasons` (Seasons -> Seizoenen)
  - `show_detail_cast_list` (Cast -> Cast)
  - `show_detail_rating_heading` (Trakt Rating -> Trakt Beoordeling)
  - `show_detail_where_to_watch` (Where to Watch -> Waar te bekijken)
  - `show_detail_add_to_watchlist_button` (Add to watchlist -> Toevoegen aan watchlist)
  - `show_detail_remove_from_watchlist_button` (Remove from watchlist -> Verwijderen van watchlist)

- **Account / Watchlist**:
  - `trakt_unlock_personalization` (Unlock Personalization -> Ontgrendel Personalisatie)
  - `trakt_connect_benefits` (Connect your Trakt account to automatically track your watch progress, sync your history securely, and manage your watchlists seamlessly. -> Koppel je Trakt-account om je voortgang automatisch bij te houden, je geschiedenis veilig te synchroniseren en je watchlists naadloos te beheren.)
  - `connect_to_trakt_button` (Connect Trakt Account -> Verbind Trakt Account)

---

### UI Components

#### [MODIFY] `app/src/main/java/com/theupnextapp/ui/explore/ExploreScreen.kt`
- Replace hardcoded `listOf("Trending", "Popular", "Anticipated")` with `listOf(stringResource(R.string.title_trending_shows), stringResource(R.string.title_popular_shows), stringResource(R.string.title_anticipated_shows))`.

#### [MODIFY] `app/src/main/java/com/theupnextapp/ui/showDetail/ShowDetailScreen.kt`
- Replace hardcoded `"Where to Watch"` with `stringResource(id = R.string.show_detail_where_to_watch)`.

#### [MODIFY] `app/src/main/java/com/theupnextapp/ui/traktAccount/TraktAccountScreen.kt`
- Replace hardcoded `"Unlock Personalization"` with `stringResource(id = R.string.trakt_unlock_personalization)`.
- Replace hardcoded `"Connect your Trakt account to..."` with `stringResource(id = R.string.trakt_connect_benefits)`.

## Verification Plan

### Automated Tests
- Run `./gradlew testDebugUnitTest` to ensure no UI snapshot tests or logic tests are broken by the string changes.
- Ensure `./gradlew ktlintCheck detekt` continues to pass, given the import/code modifications.

### Manual Verification
Ensure that no instrumented UI tests fail as a result of the network changes.
