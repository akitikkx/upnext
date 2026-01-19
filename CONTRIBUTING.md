# Contributing to Upnext: TV Series Manager

First off, thank you for considering contributing to Upnext! It's people like you that make the open source community such an amazing place to learn, inspire, and create.

## 🤝 How to Contribute

### Reporting Bugs
Bugs are tracked as [GitHub Issues](https://github.com/akitikkx/upnext/issues). Create an issue on the repository and provide as much detail as possible:
*   Use a clear and descriptive title.
*   Describe the exact steps to reproduce the problem.
*   Provide screenshots or screen recordings if possible.

### Suggesting Enhancements
Enhancement suggestions are also tracked as GitHub Issues.
*   Explain why this enhancement would be useful.
*   Mockups or design ideas are highly encouraged.

### Pull Requests
1.  **Fork** the repo on GitHub.
2.  **Clone** the project to your own machine.
3.  **Create a branch** for your feature or bugfix (`git checkout -b feature/amazing-feature`).
4.  **Commit** your changes to your own branch.
5.  **Push** your work back to your fork.
6.  **Submit a Pull Request** so that we can review your changes.

## 💻 Code Style & Standards

We strive to keep the codebase clean and consistent.

*   **Kotlin Only**: All new code must be written in Kotlin.
*   **Ktlint**: We use `ktlint` to enforce code style. Please run the formatter before committing:
    ```bash
    ./gradlew ktlintFormat
    ```
*   **Architecture**: Please adhere to the existing Clean Architecture + MVVM patterns. UI logic belongs in ViewModels, business logic in UseCases/Interactors.

## 🧪 Testing

*   Please add unit tests for any new logic (especially for ViewModels and Repositories).
*   Run local tests to ensure no regressions:
    ```bash
    ./gradlew testDebugUnitTest
    ```

## 📜 License

By contributing, you agree that your contributions will be licensed under its MIT License.
