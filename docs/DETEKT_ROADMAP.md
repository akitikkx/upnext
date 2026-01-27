# Detekt Rules Roadmap

This document tracks the currently suppressed Detekt rules and the plan to address them progressively.

## Current Status

**Detekt passes** with the following rules disabled or relaxed. These were suppressed to achieve a passing build while prioritizing other Phase 1 work.

---

## Phase 2: Magic Numbers & Constants (Next Sprint)

| Rule | Current State | Target |
|------|--------------|--------|
| `MagicNumber` | Disabled | Enabled |

### Files to Address:
- `TableUpdateInterval.kt` - Define constants for time intervals (hours, minutes)
- `TraktAuthenticator.kt` - Define HTTP status code constants
- `TraktAuthManager.kt` - Define timeout constants
- `DatabaseMigration.kt` - Define migration version constants
- UI files - Define dimension constants

### Approach:
1. Create a `Constants.kt` file for common magic numbers
2. Define UI-specific constants in their respective Composable files
3. Re-enable `MagicNumber` rule

---

## Phase 3: Long Methods & Complexity (Post Phase 2)

| Rule | Current State | Target |
|------|--------------|--------|
| `LongMethod` | Threshold: 180 | Threshold: 80 |
| `CyclomaticComplexMethod` | Threshold: 30 | Threshold: 15 |

### Files to Address:
- `MainScreen.kt` (167 lines) - Extract navigation logic, split into smaller composables
- `Migrations.kt` (170 lines) - Already at limit, consider splitting by version ranges
- `TraktRecommendationsDataSource.kt` - Extract error handling into helper functions

### Approach:
1. Identify common patterns that can be extracted into reusable functions
2. Split large Composable functions into smaller, focused components
3. Gradually reduce thresholds

---

## Phase 4: Line Length & Formatting (TBD)

| Rule | Current State | Target |
|------|--------------|--------|
| `MaxLineLength` | Disabled | Enabled (120) |
| `MaximumLineLength` (formatting) | Disabled | Enabled |

### Files to Address:
- SQL queries in DAOs
- Long parameter lists in function signatures
- Long string literals

### Approach:
1. Use multi-line strings for SQL queries
2. Use named parameters and wrap function signatures
3. Re-enable MaxLineLength

---

## Rules Intentionally Disabled (Platform Conventions)

These rules are disabled because they conflict with platform conventions:

| Rule | Reason | Status |
|------|--------|--------|
| `FunctionNaming` | Compose uses `PascalCase` for `@Composable` | Permanent |
| `ConstructorParameterNaming` | JSON models use `snake_case` for `@SerializedName` | Permanent |
| `VariableNaming` | Consistency with constructor naming | Permanent |
| `SpreadOperator` | Idiomatic Kotlin pattern | Permanent |
| `EmptyFunctionBlock` | Test fakes use empty implementations | Permanent |
| `NoWildcardImports` | Used sparingly for coroutines | Permanent |

---

## Tracking

Last Updated: 2026-01-27
Current Detekt Status: ✅ PASSING
