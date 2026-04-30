# Phase 2: API Localization Tasks

- [x] **1. TMDB Interceptor Localization**
  - [x] Update `provideTmdbService` in `NetworkModule.kt` to inject the `language` query parameter using `Locale.getDefault().toLanguageTag()`.
- [x] **2. Trakt Interceptor Localization**
  - [x] Add `TraktLanguageInterceptor` to `NetworkModule.kt`.
  - [x] Apply `TraktLanguageInterceptor` to `provideTraktService` to inject `Accept-Language` headers dynamically based on device locale.
- [ ] **3. Verification**
  - [ ] Run Lint and Unit tests to ensure no regressions.
