# Documentation
- After updating or renaming files make sure READMe.md file is up to date.

# Project Conventions

## File naming

### Platform-specific Files Use A Platform Suffix
- Use a platform suffix (Pattern: `<Name>.<platform>.kt`); The base name stays platform-agnostic 
  - Shared code: `MyFile.kt`
  - iOS code: `MyFile.ios.kt`
  - Android code: `MyFile.android.kt`
- Never embed the platform in the name (❌ `MyFileIos.kt`).
