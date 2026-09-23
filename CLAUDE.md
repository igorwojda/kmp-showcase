# Documentation
- After updating or renaming files make sure READMe.md file is up to date.

# Project Conventions

## ViewModel
- Every method that is called from the UI should have `on` prefix. e.g. `onButtonClick()`.

## File naming

### Platform-specific Files Use A Platform Suffix
- Use a platform suffix (Pattern: `<Name>.<platform>.kt`); The base name stays platform-agnostic 
  - Shared code: `MyFile.kt`
  - iOS code: `MyFile.ios.kt`
  - Android code: `MyFile.android.kt`
- Never embed the platform in the name (❌ `MyFileIos.kt`).
