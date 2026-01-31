---
name: Release Engineering
description: Expert capabilities for managing Upnext application releases, CI/CD pipelines, and versioning.
---

# Release Engineering Skill

This skill provides a comprehensive guide for managing the release lifecycle of the Upnext Android application. It encapsulates logic for GitHub Actions, Fastlane, Semantic Versioning, and troubleshooting.

## 🚀 Triggering a Release

The release pipeline is fully automated via GitHub Actions, prioritizing stability and reducing noise.

### 1. Nightly Scheduled Builds (CD)
Automated deployments run daily at **2:00 AM UTC** via `.github/workflows/deploy.yml`. This batches all dependency updates and merges from the day into a single release.

**Triggers:**
- **Schedule:** `cron: '0 2 * * *'`
- **Manual:** `workflow_dispatch` (Run via GitHub Actions UI)

**Process:**
1.  **Versioning:** Bumps `VERSION_CODE` and `VERSION_NAME` in `version.properties`.
2.  **Build:** Generates signed Release Bundle (`.aab`).
3.  **Distribution:** Fastlane uploads to **Google Play Internal Track**.
4.  **Tagging:** Pushes a git tag (e.g., `v2025.1.4-215`).

### 2. Pull Request Verification (CI)
Every Pull Request to `main` undergoes strict quality checks via `.github/workflows/pull_request.yml`.

**Checks Run:**
- **KtLint:** `ktlintCheck` (Code Style)
- **Detekt:** `detekt` (Static Analysis)
- **Android Lint:** `lintDebug` (Android best practices, permissions, security)
- **Unit Tests:** `testDebugUnitTest`
- **Release Integrity:** `assembleRelease` (Verifies R8/ProGuard shrinking without crashing)

> **Note on Signing in Pull Requests:**
> PR builds do not have access to production signing keys. `app/build.gradle` is configured to **fallback to debug signing** automatically when the release keystore is missing. This allows `assembleRelease` to verify compilation and shrinking logic in CI without needing secrets.

---

## 🛠 Manual Release & Debugging

You can run the release logic locally using Fastlane to verify builds before pushing.

### Prerequisites
- `fastlane` installed.
- `key.properties` present (or environment variables set).
- `service-account.json` for Google Play API access.

### Commands

**Run Internal Track Deployment locally:**
```bash
fastlane deploy_internal
```
*Note: This will perform the build, signing, and upload to Google Play, and will commit/tag version bumps.*

**Run Specific CI Checks Locally:**
```bash
# Verify Code Style
./gradlew ktlintCheck

# Verify Static Analysis
./gradlew detekt

# Verify Android Lint
./gradlew lintDebug

# Verify Release Build (Compilation + R8)
./gradlew assembleRelease
```

---

## 📦 Versioning Strategy

Upnext uses **Calendar Versioning (CalVer)**: `YYYY.M.Patch`

- **Major**: Year (e.g., 2025)
- **Minor**: Month (e.g., 1 for January)
- **Patch**: Incremental number for releases within that month.

**File:** `version.properties`
```properties
VERSION_CODE=214
VERSION_NAME=2025.1.4
```

**Automated Bumping:**
The `Fastfile` (`deploy_internal` lane) automatically:
1. Increments `VERSION_CODE` for every build.
2. Sets `VERSION_NAME` to `YYYY.M.1` (if new month) or `YYYY.M.Patch+1` (if same month).

---

## 🔧 Troubleshooting Guide

### 1. HTTP 403 / "Title Unknown" (Trakt API)
*   **Symptom:** App runs, but data fails to load or showing titles are missing.
*   **Cause:** Missing Trakt API Client ID/Secret in `local.properties` or Secrets.
*   **Fix:** Ensure `TRAKT_CLIENT_ID` and `TRAKT_CLIENT_SECRET` are set in GitHub Actions Secrets.

### 2. Release Build "Missing Keystore"
*   **Symptom:** `assembleRelease` fails with `SigningConfig "release" is missing required property "storeFile"`.
*   **Context:** This should NOT happen in CI anymore due to our fallback logic.
*   **Fix:** Check `app/build.gradle`. Ensure the `buildTypes.release` block contains the fallback logic:
    ```groovy
    if (signingConfigs.release.storeFile != null) { ... } else { signingConfig signingConfigs.debug }
    ```

### 3. Missing Resources / Permissions (Lint Failures)
*   **Symptom:** `lintDebug` fails with "Missing permission" (e.g., `WAKE_LOCK`) or "Resource not found".
*   **Cause:** Multi-module projects (`core:common`, `core:data`) often need their own `AndroidManifest.xml` and resource definitions to satisfy lint, even if the app module provides them.
*   **Fix:** Add `AndroidManifest.xml` with `<uses-permission>` to the specific module (e.g., `core/common/src/main/AndroidManifest.xml`) or define the missing resource in that module.

### 4. Git Identity Issues
*   **Symptom:** "Please tell me who you are" during CI/CD.
*   **Fix:** The `deploy.yml` configures git user/email before tagging using `github-actions[bot]`.

---

## 📂 Key Files
*   `.github/workflows/deploy.yml`: Nightly CD workflow.
*   `.github/workflows/pull_request.yml`: PR CI workflow.
*   `fastlane/Fastfile`: Build automation script.
*   `version.properties`: App version definition.
*   `app/build.gradle`: Build & signing configuration.
