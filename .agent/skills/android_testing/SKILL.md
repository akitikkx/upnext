---
name: Android Testing
description: Best practices for Android instrumentation tests, Hilt testing, Mockito configuration, and test resilience patterns in Upnext.
---

# Android Testing Skill

This skill provides guidance for setting up and maintaining a reliable Android test suite, including instrumentation tests, Hilt integration, and handling common pitfalls.

## 🧪 Test Suite Structure

Upnext has two test source sets:

| Source Set | Location | Purpose | Framework |
|------------|----------|---------|-----------|
| **Unit Tests** | `app/src/test/` | Fast, JVM-only tests | JUnit, Mockito, Robolectric |
| **Instrumentation Tests** | `app/src/androidTest/` | On-device/emulator tests | AndroidJUnit4, Compose Testing |

> [!IMPORTANT]
> Do not duplicate tests between source sets. If a test can run as a unit test with Robolectric, prefer that over instrumentation tests for speed and reliability.

---

## ⚙️ Hilt Testing Configuration

### The Problem: Duplicate File Generation

When using **KSP** for Hilt annotation processing, avoid configuring `androidTestAnnotationProcessor` for the same compiler. This causes Hilt to generate files twice, resulting in:

```
error: [Hilt] Attempt to recreate a file for type hilt_aggregated_deps._com_example_Test_GeneratedInjector
```

### Correct Configuration

In `app/build.gradle`:

```groovy
// ✅ CORRECT: Use KSP only for both main and androidTest
ksp libs.hilt.android.compiler
kspAndroidTest libs.hilt.android.compiler
kspTest libs.hilt.android.compiler

// ❌ WRONG: Don't mix KSP with annotation processor
// androidTestAnnotationProcessor libs.hilt.android.compiler  // REMOVE THIS
```

### Hilt Test Setup

For instrumentation tests that use Hilt:

```kotlin
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MyInstrumentedTest {
    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltTestRule.inject()
    }
}
```

---

## 🎭 Mockito Configuration

### The Problem: MockMaker Plugin Initialization

Standard `mockito-core` fails on Android with:

```
java.lang.IllegalStateException: Could not initialize plugin: interface org.mockito.plugins.MockMaker
```

### Solution: Use mockito-android

In `gradle/libs.versions.toml`:

```toml
mockito-core = { module = "org.mockito:mockito-core", version.ref = "mockitoCore" }
mockito-android = { module = "org.mockito:mockito-android", version.ref = "mockitoCore" }
mockito-kotlin = { module = "org.mockito.kotlin:mockito-kotlin", version.ref = "mockitoKotlin" }
```

In `app/build.gradle`:

```groovy
// Unit tests (JVM)
testImplementation libs.mockito.core
testImplementation libs.mockito.kotlin

// Instrumentation tests (Device/Emulator)
androidTestImplementation libs.mockito.android  // ✅ Not mockito-core!
androidTestImplementation libs.mockito.kotlin
```

---

## 🛡️ Test Resilience Patterns

### Using assumeTrue for Graceful Skipping

UI tests that depend on network data or specific app state should skip gracefully rather than fail:

```kotlin
import org.junit.Assume.assumeTrue

@Test
fun testThatRequiresData() {
    // Wait for data, skip if unavailable
    val hasData = try {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("Show poster")
                .fetchSemanticsNodes().isNotEmpty()
        }
        true
    } catch (e: ComposeTimeoutException) {
        false
    }
    
    assumeTrue("Skipping: No data available", hasData)
    
    // Continue with test...
}
```

### Using @Ignore for Deprecated Tests

When a test is temporarily broken or duplicated:

```kotlin
import org.junit.Ignore

@Ignore("Duplicate of unit test. Use the /test/ version instead.")
class DeprecatedTest {
    // ...
}
```

---

## 📋 Compose Testing OptIns

For Compose UI tests, add required experimental annotations:

```kotlin
@OptIn(
    androidx.compose.animation.ExperimentalAnimationApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.ui.test.ExperimentalTestApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class
)
class MyComposeTest {
    // ...
}
```

---

## 🧩 Compose Testing Imports

### The Problem: Unresolved References in CI

Top-level imports for extension functions like `assertExists` or `fetchSemanticsNodes` can fail to resolve in CI environments (e.g., GitHub Actions) despite working locally.

```kotlin
// ❌ RISKY: May fail in CI
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.fetchSemanticsNodes

composeTestRule.onNodeWithTag("tag").assertExists()
```

### Solution: Method Chaining

Use standard method chaining syntax instead of relying on static imports for extension functions. This ensures the compiler resolves the function on the object type directly.

```kotlin
// ✅ SAFE: Resolves reliably
import androidx.compose.ui.test.onNodeWithTag

composeTestRule
    .onNodeWithTag("tag")
    .assertExists() // Resolved as method call on SemanticsNodeInteraction
```

---

## 🔧 Troubleshooting

### Common Issues

| Issue | Cause | Fix |
|-------|-------|-----|
| `Attempt to recreate a file` | Mixed KSP + KAPT | Remove `androidTestAnnotationProcessor` |
| `MockMaker initialization failed` | Using `mockito-core` on Android | Use `mockito-android` |
| `ComposeTimeoutException` | No data in UI test | Use `assumeTrue` to skip |
| Tests pass locally, fail in CI | Network/data differences | Use mock data or `assumeTrue` |

### Cleaning Build Artifacts

When Hilt generates stale files:

```bash
./gradlew clean :app:connectedDebugAndroidTest
```

### Running Specific Tests

```bash
# Single test class
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.theupnextapp.NavigationBackStackTest

# All androidTest tests
./gradlew :app:connectedDebugAndroidTest
```

---

## 🚀 CI/CD Integration

UI tests run automatically on Pull Requests via `.github/workflows/pull_request.yml`.

### Configuration

The workflow uses `reactivecircus/android-emulator-runner`:

```yaml
- name: Run Instrumented Tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 30
    arch: x86_64
    profile: pixel_6
    target: google_apis
    emulator-options: -no-snapshot-save -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim
    disable-animations: true
    script: ./gradlew :app:connectedDebugAndroidTest --continue
```

> [!WARNING]
> **Always scope to `:app:` module** when running instrumented tests. Running `connectedDebugAndroidTest` without module prefix will execute tests in **all modules** (including `core:common`, `core:data`), which may have different test configurations and cause failures.

### Key Notes

- **KVM acceleration** is enabled for faster emulator performance
- **45 minute timeout** prevents hung tests from blocking PRs
- **Test reports** are uploaded as artifacts on failure
- Tests using `assumeTrue` will show as **skipped** (not failed) when data is unavailable

---

## 📁 Key Files

- [app/build.gradle](file:///Users/ahmedtikiwa/upnext4/app/build.gradle) - Test dependencies
- [libs.versions.toml](file:///Users/ahmedtikiwa/upnext4/gradle/libs.versions.toml) - Version catalog
- [CustomTestRunner.kt](file:///Users/ahmedtikiwa/upnext4/app/src/androidTest/java/com/theupnextapp/CustomTestRunner.kt) - Hilt test runner
- [pull_request.yml](file:///Users/ahmedtikiwa/upnext4/.github/workflows/pull_request.yml) - CI workflow with UI tests
