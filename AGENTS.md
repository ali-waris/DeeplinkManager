# AGENTS.md

Android app (Kotlin, Jetpack Compose, MVVM/Clean Architecture, Hilt, Room). Single `:app` module, package `com.hc.deeplinkmanager`. Built entirely offline; no network layer.

## Build & test

- Build: `./gradlew :app:assembleDebug`
- Unit tests: `./gradlew :app:testDebugUnitTest` (JUnit4 + Mockk + coroutines-test are declared, but **no test sources exist yet** — create `app/src/test/...`)
- No lint or formatter is configured. Requires JDK 17+ and an Android SDK (`local.properties` or `ANDROID_HOME`; `local.properties` is gitignored).
- Toolchain pins: Gradle wrapper 9.5.0 but AGP 8.7.3 / Kotlin 2.0.21 / KSP 2.0.21-1.0.28. These are outside each other's officially supported ranges (Kotlin 2.0.21 supports Gradle up to 8.8). If a build fails with plugin-internal errors, this mismatch is the likely cause; verify with a build before assuming your change broke it.
- All dependencies are declared in `gradle/libs.versions.toml` and referenced via `libs.*` aliases — add new deps there, never inline coordinates.

## Domain invariants (easy to violate)

- **"Ungrouped" is a system tag with fixed id `1`** (`TagEntity.UNGROUPED_ID`), seeded in `DeeplinkDatabase.onCreate`. It must never be renamed or deleted (guards exist in `TagRepositoryImpl` and `MainViewModel`). New logic must preserve this.
- Deleting a tag reassigns its deeplinks to Ungrouped inside a Room transaction (`TagRepositoryImpl.deleteAndReassign`) and returns the moved count; the ViewModel resets an active filter to All afterward.
- Duplicate URLs are only guarded client-side (`MainViewModel.save` via `findIdByUrl`) — there is no DB unique index on `url`. Keep the guard when changing save logic.
- Tag names are unique case-insensitively (unique DB index + `TagNameDialog` "already exists" check).
- `DeeplinkRepository.upsert` treats `id == 0L` as insert, otherwise update.
- Room uses `fallbackToDestructiveMigration()` and `exportSchema = false`: schema changes wipe user data, no migration scripts exist.
- Use the `@IoDispatcher` qualifier (defined in `di/AppModules.kt`) for IO-bound coroutines instead of `Dispatchers.IO`.

## Code layout & conventions

- `ui/main/MainScreen.kt` (666 lines) holds **all** composables — new UI goes here unless extracted deliberately. UI text is hardcoded inline (only `app_name` lives in `strings.xml`); do not introduce string resources for one-off labels.
- Layers: `ui/` (Compose + ViewModels + state), `data/local` (Room entities/DAOs), `data/repo`, `di`, `util`.
- DI is Hilt: `@HiltAndroidApp` app, `@AndroidEntryPoint` activity, all modules in `di/AppModules.kt`.
- Import/export is **CSV**, not JSON (`util/CsvExporter.kt`: `CsvExporter`, `CsvImporter`, `ExportStorage`). Export writes to Downloads (MediaStore on API 29+, legacy `WRITE_EXTERNAL_STORAGE` on API ≤28 — manifest declares it with `maxSdkVersion="28"`); import is content-URI based (`OpenDocument`). Import dedupes against existing `name|url` keys.

## Docs

README.md is stale where it says import/export is JSON — the code is CSV. Trust the code.
