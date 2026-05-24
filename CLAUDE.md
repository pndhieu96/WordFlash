# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Requirements

Full product spec at [`doc/requirement.md`](doc/requirement.md) — covers all screen specs, DB schema, API choices, and implementation status. Consult it for feature scope and acceptance criteria.

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run lint
./gradlew lint

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.hieupnd.wordflash.ExampleTest"
```

## API Keys (local.properties)

`local.properties` is git-ignored. Required key:
```
GEMINI_API_KEY=<your_key>
```
The key is read in `app/build.gradle.kts` and exposed via `BuildConfig.GEMINI_API_KEY`. `AppConfig.kt` wraps it for access outside DI modules.

## Architecture

Clean Architecture + MVVM with three layers:

**domain/** — pure Kotlin, no Android deps
- `model/` — domain entities (`VocabularyCard`, `SentenceCard`, `Example`, `GeminiWordInfo`, `ReviewItem`)
- `repository/` — interfaces only (`VocabularyRepository`, `SentenceRepository`)
- `usecase/` — one class per operation, organised by feature (`vocabulary/`, `sentence/`, `review/`, `sync/`)

**data/** — implements domain interfaces
- `local/` — Room database (`AppDatabase` v2), DAOs, entities. `examples` column is stored as JSON string serialised/deserialised via Gson in the repository layer.
- `remote/api/` — Retrofit interfaces (`DictionaryApi`, `DatamuseApi`)
- `remote/gemini/` — `GeminiService` (Google AI SDK, not Retrofit); calls `gemini-2.5-flash-lite`, returns structured JSON parsed into `GeminiWordInfo`
- `remote/firebase/` — Firebase Auth + Firestore sync implementations
- `repository/` — implementations injected by Hilt

**presentation/** — Jetpack Compose screens
- Each screen folder has `Screen`, `ViewModel`, `UiState` files
- State flows down from ViewModel via `StateFlow<UiState>`; events go up as lambda params

**di/** — Hilt modules: `NetworkModule`, `GeminiModule`, `DatabaseModule`, `FirebaseModule`, `RepositoryModule`

## Navigation

`AppNavigation.kt` defines a bottom-nav with three routes: `Vocabulary`, `Sentence`, `Review` (see `Screen.kt`). All screens receive `innerPadding` from the root `Scaffold`.

## Key Data Flow — Vocabulary word lookup

`VocabularyViewModel.searchWord()` fires two parallel `async` calls:
1. `SearchWordUseCase` → `DictionaryApi` (IPA, audio URL, word type)
2. `GetWordInfoFromGeminiUseCase` → `GeminiService` (Vietnamese meaning, 3 example sentences EN+VI, 5 image keywords → loremflickr.com URLs)

Dictionary result lands first; Gemini result follows. Both populate `VocabularyUiState`.

## Spaced Repetition

`GetReviewCardsUseCase` merges vocab + sentence cards and builds a weighted list:  
`weight = baseWeight(level) + daysSinceLastReview`. Level 0 → weight 10, level 1 → weight 5, level 2+ → weight 1. Cards are then shuffled.

## Image Loading

`WordFlashAsyncImage` (in `presentation/components/`) wraps Coil's `SubcomposeAsyncImage` with automatic retry (up to 3 times, 500 ms delay). Error state shows a red `errorContainer` background. Images are displayed at `aspectRatio(4f/3f)` with `ContentScale.Fit` throughout the app.

## Room Migrations

Migrations are defined at the top of `AppDatabase.kt`. Always add a new `Migration` object and register it in the `Room.databaseBuilder` call in `DatabaseModule.kt` when changing the schema.
