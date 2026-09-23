# Documentation
- After updating or renaming files make sure READMe.md file is up to date.

# Project Conventions

## Architecture
- Clean Architecture is used as a base for the project. The project is divided into three layers:
  - `Domain` - Contains business logic and entities.
  - `Data`- Responsible for data management, including repositories and data sources.
  - `Presentation` - Handles UI logic and user interactions.

## Data Layer

### Network Models
- Classes used for network requests and responses use a `Model` suffix: `...RequestModel`, `...ResponseModel`
  (e.g. `ForecastRequestModel`, `ForecastResponseModel`).

## Presentation Layer

### ViewModel
- Every method that is called from the UI should have `on` prefix. e.g. `onButtonClick()`.

## UI

A feature's native UI sits next to its shared logic, in the feature module, not in the app modules. The app
modules only wire things together:

```
feature/forecast/src/
├── commonMain/kotlin/…/presentation/   ViewModels, state (shared)
├── androidMain/kotlin/…/presentation/  Jetpack Compose screens and components
└── iosMain/swift/presentation/         SwiftUI screens and components
```

- Swift can't be compiled by Gradle, so the files are compiled by the Xcode app target through a
  synchronized folder e.g `forecast` in `iosApp.xcodeproj`, pointing at `feature/forecast/src/iosMain/swift`). New files
  there are picked up automatically.

## File naming

### Platform-specific Files Use A Platform Suffix
- Use a platform suffix (Pattern: `<Name>.<platform>.kt`); The base name stays platform-agnostic 
  - Shared code: `MyFile.kt`
  - iOS code: `MyFile.ios.kt`
  - Android code: `MyFile.android.kt`
- Never embed the platform in the name (❌ `MyFileIos.kt`).
