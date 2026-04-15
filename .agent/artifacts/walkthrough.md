# Hand-over & AGP 9.0 Migration

The automated Android Studio Upgrade Assistant encountered numerous fatal Gradle failures during its execution due to the persistence of older plugins, specifically `FirebasePerfPlugin` and outdated Dagger Hilt compilers. 

I took over the Gradle ecosystem synchronization manually and was able to successfully modernize all modules to fully conform with **Android Gradle Plugin 9.1.1** requirements.

## 1. Resolved Legacy `Transform` API Crashes
The legacy Android `Transform` API was fully purged in AGP 9.0. Your `firebase-perf` plugin was completely blocking the project's evaluation phase.
*   **Fix:** Bumped `firebasePerfGradlePlugin` to version `2.0.2` in `libs.versions.toml`, which internally migrated off `Transform` and now relies natively on `AsmClassVisitorFactory`.

## 2. Dagger Hilt & Baseline Profile Compliance
Older dependencies still explicitly hunted for the `BaseExtension` API which AGP 9 deleted.
*   **Fix:** Upgraded `daggerHilt` to version `2.59.2`, ensuring seamless dependency injection graph generation under AGP 9+.
*   **Fix:** Upgraded `baselineProfile` to `1.5.0-alpha05` and refactored the legacy `managedDevices.devices` DSL to `managedDevices.localDevices.create()` directly inside `baselineprofile/build.gradle`.

## 3. Emptied `kotlin.android` For Built-in Kotlin Integration
AGP 9 handles Kotlin compilation inherently now.
*   **Fix:** Stripped `alias(libs.plugins.kotlin.android)` logic across every module (`app`, `core:domain`, `core:data`, `core:common`, `core:designsystem`, and `baselineprofile`).
*   **Fix:** Migrated away from deprecated `kotlinOptions { jvmTarget = "17" }` closures by shifting directly to pure toolchain enforcement: `kotlin { jvmToolchain(17) }`.
*   **Fix:** Appended `android.disallowKotlinSourceSets=false` as a protective toggle against KSP implicitly attempting to generate legacy SourceSet containers.

## 4. Verification
The task graph has been fully unblocked! `BUILD SUCCESSFUL in 1m 9s`. The AGP 9 upgrade has been completed, and your project is natively primed to execute moving forward!
