---
name: Kotlin Code Style
description: Guidelines and strict rules for Kotlin code formatting, specifically concerning imports, fully qualified names, and avoiding codebase pollution.
---

# Kotlin Code Style & Imports

When developing or refactoring Kotlin code in Upnext, you MUST adhere to the following strict code style guidelines regarding classes and imports.

## 1. Do Not Use Inline Fully Qualified Names
Always prefer utilizing standard `import` statements at the top of the file over inline fully qualified class names. 

### ❌ Anti-pattern:
```kotlin
// BAD: Using the fully qualified name inline
viewModelScope.launch {
    try {
        repository.getData()
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    }
}
```

### ✅ Correct Pattern:
```kotlin
import kotlinx.coroutines.CancellationException

// GOOD: Leveraging imports
viewModelScope.launch {
    try {
        repository.getData()
    } catch (e: CancellationException) {
        throw e
    }
}
```

### Exceptions (When Fully Qualified Names ARE Allowed):
You may use fully qualified names **only** to resolve explicit naming collisions.
For example, if you are mapping between a network model and a domain model that share the same name, or if you are using `kotlin.Result` alongside a custom `com.theupnextapp.domain.Result`:

```kotlin
// ACCEPTABLE: Resolving collisions between two 'Result' types
fun mapResponse(): kotlin.Result<com.theupnextapp.domain.Result> { ... }
```

## 2. Detekt and IDE Inspections
Our current version of Detekt does not have the `UnnecessaryFullyQualifiedName` rule natively available. Therefore, it is the agent's responsibility to proactively implement these checks and refrain from outputting fully qualified names in generated code. 

Before proposing PRs or delivering a walkthrough, verify that you haven't lazily injected fully qualified paths into your Kotlin classes!
