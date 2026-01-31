---
name: Navigation & Adaptive UI
description: Best practices for Type-Safe Navigation (Nav 3) and Adaptive Layouts in Upnext.
---

# Navigation & Adaptive UI Skill

This skill encapsulates the critical patterns for implementing adaptive layouts and navigation in the Upnext application. It specifically addresses "Gotchas" related to `ListDetailPaneScaffold` and Compose Navigation 3.

## 📱 Adaptive Layouts (List-Detail)

Upnext uses `NavigableListDetailPaneScaffold` to support phones, tablets, and foldables.

### ⚠️ Critical Rule: Navigator Mismatch
**NEVER** use `rememberSupportingPaneScaffoldNavigator` with `NavigableListDetailPaneScaffold`. This causes back navigation conflicts and UI flickering.

**✅ Correct Usage:**
```kotlin
// MainScreen.kt
val listDetailNavigator = rememberListDetailPaneScaffoldNavigator<Any>()

NavigableListDetailPaneScaffold(
    navigator = listDetailNavigator,
    defaultBackBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange, // Let scaffold handle pane back
    // ...
)
```

### Back Navigation
The scaffold handles back navigation between panes (Detail -> List) automatically if `defaultBackBehavior` is set correctly.
*   **Do NOT** manually intercept back presses for pane navigation using `BackHandler` if the scaffold can handle it.
*   **Do** use `BackHandler` only for top-level destination changes (e.g., Explore -> Dashboard).

---

## 🧭 Type-Safe Navigation (Nav 3)

We use the official [Compose Navigation 3](https://developer.android.com/guide/navigation/design/type-safety) with Kotlin Serialization.

### 1. Defining Routes
Routes are defined in `Destinations.kt` as `@Serializable` classes or objects.

```kotlin
@Serializable
object Dashboard : Destinations

@Serializable
data class ShowDetail(
    val showId: Long,
    val showTitle: String?
) : Destinations
```

### 2. Navigating
```kotlin
navController.navigate(Destinations.ShowDetail(showId = 123, showTitle = "Arcane"))
```

### 3. Extracting Arguments (The "Title Unknown" Fix)
To extract arguments (e.g., for a TopBar title), use `toRoute<T>()` on the `NavBackStackEntry`.

**✅ Correct Pattern:**
```kotlin
// AppNavigation.kt
val currentEntry = navBackStackEntry
val showTitle = if (currentEntry?.destination?.hasRoute<Destinations.ShowDetail>() == true) {
    try {
        currentEntry.toRoute<Destinations.ShowDetail>().showTitle
    } catch (e: Exception) { null }
} else { null }
```

---

## 🔧 Common Pitfalls

### "Title Unknown"
*   **Cause:** Hardcoding titles or failing to parse args from the current route.
*   **Fix:** Always attempt to extract the specific route args (like `ShowDetail`) when the destination matches.

### Infinite Navigation Loops
*   **Cause:** Updating state (like `isDetailFlowActive`) inside a `LaunchedEffect` that observes that same state without proper checks.
*   **Fix:** Ensure state updates only happen when the *target* state differs from *current* state.

### Bottom Bar Visibility
*   **Rule:** The `NavigationSuiteScaffold` (Bottom Bar / Rail) should wrap the content.
*   **Logic:** The list-detail scaffold lives *inside* the navigation suite.
