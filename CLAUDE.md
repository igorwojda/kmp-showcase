# Documentation
- After updating or renaming files make sure READMe.md file is up to date.

# Project Conventions

## Architecture
- Clean Architecture is used as a base for the project. The project is divided into three layers:
  - `Domain` - Contains business logic and entities.
  - `Data`- Responsible for data management, including repositories and data sources.
  - `Presentation` - Handles UI logic and user interactions.

## ViewModel
- Every method that is called from the UI should have `on` prefix. e.g. `onButtonClick()`.

## File naming

### Platform-specific Files Use A Platform Suffix
- Use a platform suffix (Pattern: `<Name>.<platform>.kt`); The base name stays platform-agnostic 
  - Shared code: `MyFile.kt`
  - iOS code: `MyFile.ios.kt`
  - Android code: `MyFile.android.kt`
- Never embed the platform in the name (❌ `MyFileIos.kt`).
