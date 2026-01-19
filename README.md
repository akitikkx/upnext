# Upnext: TV Series Manager

[![Google Developers Dev Library](https://img.shields.io/badge/Google_Dev_Library-Accepted-green)](https://devlibrary.withgoogle.com/products/android/repos/akitikkx-upnext)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)

**Upnext** is a modern Android application for tracking TV series, built with industry-standard best practices and cutting-edge libraries. It serves as a reference implementation for a scalable, production-ready Android app in 2026.

## 🚀 Key Features

*   **100% Jetpack Compose**: Fully modern UI built with Material 3.
*   **Adaptive Layouts**: Optimized for phones, tablets, and foldables using Window Size Classes.
*   **Offline First**: Robust offline support with Room and WorkManager sync.
*   **Trakt Integration**: Seamlessly syncs with Trakt.tv for tracking episodes and history.
*   **Modern Architecture**: Clean Architecture, MVVM, and Dependency Injection with Hilt.

## 🛠 Tech Stack

This project leverages the latest Android development tools and libraries:

*   **Language**: [Kotlin 2.1](https://kotlinlang.org/)
*   **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
*   **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
*   **Async**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
*   **Network**: [Retrofit](https://squareup.github.io/retrofit/) & [OkHttp](https://squareup.github.io/okhttp/)
*   **Local Storage**: [Room](https://developer.android.com/training/data-storage/room) & [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
*   **Image Loading**: [Coil](https://coil-kt.github.io/coil/) & [Glide](https://bumptech.github.io/glide/)
*   **Background Work**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
*   **Build System**: Gradle with Version Catalogs (`libs.versions.toml`)

## 🏗 Architecture

The app follows **Clean Architecture** principles with a logical separation of concerns:

1.  **UI Layer**: Jetpack Compose screens and ViewModels.
2.  **Domain Layer**: Use Cases / Interactors (Logical business rules).
3.  **Data Layer**: Repositories, Data Sources (Local/Remote), and API definitions.

Although currently structured as a monolithic `app` module for simplicity, the logical boundaries are strictly enforced to facilitate future modularization.

## 💻 Setup & Build

### Prerequisites
*   **Android Studio**: Ladybug or newer.
*   **JDK**: Java 17 (Ensure Gradle is using JDK 17 in simple Settings).

### Trakt API Configuration
To run the app, you need a Trakt.tv API key.

1.  Register an application at [Trakt.tv](https://trakt.tv/oauth/applications).
2.  Set the Redirect URI to: `theupnextapp://callback`
3.  Create a `local.properties` file in the project root (do NOT commit this file).
4.  Add your keys:

```properties
TraktClientID="YOUR_CLIENT_ID"
TraktClientSecret="YOUR_CLIENT_SECRET"
TraktRedirectURI="theupnextapp://callback"
```

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to propose bug fixes and new features.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
