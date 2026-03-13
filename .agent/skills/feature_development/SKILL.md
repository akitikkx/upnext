---
name: Feature Development
description: Strict guidelines and workflow for developing new features, ensuring quality, proper branching, UI/UX standards, and comprehensive testing in Upnext.
---

# Feature Development Skill

This skill outlines the strict workflow and quality standards that must be followed when developing new features in the Upnext project. 

## 🌿 1. Branching Strategy
- **Always work on a fresh branch** created off the latest `main` branch.
- **Naming convention**: Use `feature/<feature-name>` for new features (e.g., `feature/episode-details`).
- Ensure you have the latest code before branching off to avoid conflicts.

## 🎨 2. UI/UX & Design Standards
- **Refer to the [Frontend Design Skill](../frontend_design/SKILL.md)** for UI/UX best practices.
- Ensure all screens follow the established design flow and architecture.
- Use the existing design system tokens, Material 3 components, and adaptive layouts.
- Do not take shortcuts with UI implementation; visual excellence and smooth user experience are critical.

## 🏗️ 3. Code Architecture & Quality
- Maintain a **high-quality approach** to feature development.
- Adhere to the established app architecture (Clean Architecture, MVVM, Unidirectional Data Flow).
- Avoid hacks, technical debt, or shortcuts. Code must be production-ready and scalable.
- Ensure proper separation of concerns (UI, Domain, Data layers).

## 🧪 4. Testing & Regressions
- **No regressions**: Ensure that new changes do not break existing Unit or Instrumented tests.
- **High Test Coverage**: Add comprehensive tests for all new code.
  - Write Unit Tests (JUnit, Mockito, Turbine) for ViewModels, Repositories, and Domain logic.
  - Write/Update Instrumented Tests for UI components and Navigation flows where applicable.
- Refer to the **[Android Testing Skill](../android_testing/SKILL.md)** for testing best practices.

## 🔍 5. Code Analysis & Formatting
- **Linting & Detekt**: You must run code analysis tools before finalizing the feature.
  - Run `./gradlew :app:ktlintFormat` to format styling.
  - Run `./gradlew :app:detekt` to ensure no static analysis rules are broken.
- **Build Verification**: Run `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest` to ensure the project compiles and tests pass successfully locally.
- Fix any formatting, linting, or analysis errors immediately.

## 🚀 6. Commit & Push Protocol
- **Only commit and push once all the above steps are completed and verified locally.**
- Commit messages should be clear, descriptive, and follow conventional commit formats.
- **CI Safety**: Ensure that your local verifications are thorough so that Continuous Integration (CI) is NOT the place where bugs, broken tests, or Detekt violations are discovered. All code must be validated locally before the push.
