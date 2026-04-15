# Manual AGP 9.0 Migration & Build Recovery Plan

The Android Studio Upgrade Assistant crashed midway because it encountered the `FirebasePerfPlugin` attempting to invoke the legacy `Transform` API, which was fully removed from the AGP 9.0 build cycle. Furthermore, it halted before it could safely migrate your project to use AGP 9's new "built-in Kotlin" mechanism, leaving your build scripts in a broken, hybrid state.

I will take over the migration manually using the formal AGP-9-Upgrade skill.

## User Review Required

> [!WARNING]
> While Hilt `2.56.2` and KSP are now fully compatible natively with AGP 9, the legacy `firebase-perf` plugin is notorious for lagging behind AGP removals. My first step is forcing the `firebase-perf` plugin to its absolute latest version (`2.0.2`). 
> **Are you okay with me temporarily disabling the `firebase-perf` plugin from the build entirely if version 2.0.2 still causes native `Transform` crashes during compilation?**

## Proposed Changes

### 1. Resolve Plugin Incompatibilities 

#### [MODIFY] `gradle/libs.versions.toml`
*   Bump `firebasePerfGradlePlugin` from `2.0.0` (or `1.4.2`) to the latest `2.0.2` to eliminate the legacy `com.android.build.api.transform.Transform` invocation.

### 2. Built-in Kotlin Migration (AGP 9 Standard)

#### [MODIFY] `app/build.gradle` (and all submodules)
*   **Remove** `alias(libs.plugins.kotlin.android)` from the `plugins {}` block. AGP 9 replaces this explicit plugin.
*   Instead of calling `kotlinOptions {}`, I will ensure we shift configuration strictly to the native AGP built-in `android { kotlin { jvmToolchain(17) } }` block which is already partially declared!

### 3. Gradle Properties Cleanup

#### [MODIFY] `gradle.properties`
*   Review if the Upgrade Assistant injected `android.builtInKotlin=false` or similar stopgap patches and remove them to enforce pure AGP 9 defaults.

## Open Questions

> [!IMPORTANT]
> To definitively clear the corrupt Gradle Daemon states caused by the assistant crashing, I will need you to hit "Stop Gradle build processes" in Android Studio and run `./gradlew --stop` on your terminal before I execute this plan. Are your local daemons cleared?

## Verification Plan

### Automated Tests
- Execute `./gradlew build --dry-run` to prove the AGP configuration graph finalizes without throwing `Transform` exceptions.
- Execute `./gradlew testDebugUnitTest` to prove the new built-in Kotlin AGP compiler successfully tests the codebase.
