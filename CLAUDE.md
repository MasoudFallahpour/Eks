# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Eks is an Android app that shows the latest versions of the official AndroidX libraries and notifies the user when they change. There is no API: the data comes from scraping https://developer.android.com/jetpack/androidx/versions with Jsoup.

PRs target the `dev` branch, which is the default branch. `master` holds releases.

## Build setup

- `local.properties` **must exist**, or `app/build.gradle.kts` throws at configuration time. It is gitignored. It provides `storePassword` / `keyPassword` for release signing with `Eks.jks`. Empty values are fine for debug builds. CI generates the file from GitHub secrets.
- JDK 21 in CI; modules compile to Java 17. AGP 9, Kotlin 2.4, Gradle 9. Versions live in `gradle/libs.versions.toml`.
- `minSdk` 23 is deliberate: some dependencies require API 23 as their minimum. Don't lower it.
- Release builds use R8 with minify and resource shrinking (`app/proguard-rules.pro`). Changes that rely on reflection need a smoke test of the release APK.

## Commands

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew test                          # all JVM unit tests (Robolectric where needed)
./gradlew :data:testDebugUnitTest       # one module's unit tests
./gradlew :app:testDebugUnitTest --tests "ir.fallahpoor.eks.worker.LibraryDifferTest"
./gradlew :app:testDebugUnitTest --tests "*LibrariesViewModelTest.some_test_name"
./gradlew lint                          # Android lint (app includes Compose lint checks)
./gradlew connectedCheck                # instrumented tests, needs a running emulator/device
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ir.fallahpoor.eks.libraries.ui.ToolbarTest
./gradlew assembleRelease               # signed release APK -> app/build/outputs/apk/release/Eks-release.apk
```

Don't pass `--offline` to the connected-test tasks. They fetch UTP test-plugin artifacts at execution time, and those may not be in the Gradle cache.

## Architecture

There are three Gradle modules: `:app` depends on `:data`. `:commonTest` is an Android library that both modules' test source sets depend on.

**`:data`** holds everything below the UI. Its implementations are `internal`. The only public API is the `LibraryRepository` and `StorageRepository` interfaces, the models in `repository/model`, `SortOrder`, `ConnectivityChecker` and `Constants`. Hilt bindings are in `data/di/Module.kt`.
- `network/LibrariesFetcher` scrapes the versions page. It takes every `<tr>` that has exactly 6 `<td>` cells (name/description/url, release date, stable, RC, beta, alpha). If Google changes that page's layout, this is the code that breaks.
- `database/` is a Room DB with a single `LibraryEntity` table. Versions are embedded in that table, not stored in a separate table. The exported schema is in `data/schemas/`, and the DB is at version 1 with no migrations yet. A schema change needs a version bump and a migration.
- `storage/` is a Preferences DataStore holding the sort order and the last refresh date.
- `LibraryRepositoryImpl.getLibraries()` triggers a network refresh when the DB is empty. `refreshLibraries()` replaces the whole table, but first carries over the `pinned` flag by library name. Sorting happens in Kotlin; search filtering happens in SQL (`LIKE`).
- Mapping goes `LibraryDto` → `LibraryEntity` → `Library` (domain), using extension functions next to each type.

**`:app`**
- The UI follows MVI. `LibrariesViewModel` exposes one `StateFlow<LibrariesScreenUiState>`, which combines its own state with the refresh-date flow from storage. The view model takes input only through `handleEvent(Event)`. Events go through a `MutableSharedFlow` collected with `collectLatest`, so a new event cancels the one still being handled. `LibrariesState` is `Loading` / `Success` / `Error`. `ExceptionParser` turns throwables into user-facing messages.
- The UI is Compose only (`libraries/ui/`), hosted by the single `MainActivity`. `MainActivity` also requests `POST_NOTIFICATIONS` on API 33+.
- Background work: `EksApp` enqueues the unique periodic `RefreshLibrariesWorker`, which runs daily on a connected network with battery not low, starting after an initial 10-minute delay. `EksApp` implements `Configuration.Provider` with `HiltWorkerFactory`, so the manifest removes `androidx.startup.InitializationProvider`. Keep that removal, or WorkManager initializes itself without Hilt. The worker snapshots the libraries, refreshes them, and diffs the two lists with `LibraryDiffer` / `NotificationBodyMaker`. It shows a notification only if something changed. Any failure returns `Result.retry()`.

## Testing conventions

- Shared fakes live in `:commonTest`: `FakeLibraryRepository`, `FakeStorageRepository`, `TestData` and `MainDispatcherRule`. Use them in both unit and instrumented tests instead of writing new mocks. `:data` has its own internal fakes (`FakeLibraryDao`, `FakeStorage`) in its test source set.
- Unit tests use JUnit4, Truth, MockK, Turbine (for flows) and `kotlinx-coroutines-test`.
- Compose UI tests are instrumented tests (`app/src/androidTest`). They use the **v2** compose test rule (`androidx.compose.ui.test.junit4.v2.createComposeRule`) and the **robot pattern**: each composable has a robot in `libraries/ui/robots/` that owns its finders, actions and assertions. Tests are written as Given/When/Then and named in `snake_case`.
- `RefreshLibrariesWorkerTest` builds the worker with `TestListenableWorkerBuilder` and a custom `WorkerFactory`. When the worker's constructor changes, update that factory too.

## CI / release

- `.github/workflows/build.yml` runs on pushes and PRs to `dev`. It runs `test`, then `connectedCheck` on an API 34 x86_64 emulator, then `build`.
- `.github/workflows/release.yml` runs **only when a `v*` tag is pushed**. Merging to `master` alone doesn't publish anything. The workflow runs the same tests and `assembleRelease`, then creates a GitHub release that uses `CHANGELOG.md` verbatim as its body. Before tagging, bump `versionCode`/`versionName` in `app/build.gradle.kts` and add the CHANGELOG entry.
