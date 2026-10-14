# Kursor

An offline-first Android app for watching currency rates. It opens with the last known
rates immediately, without a spinner and without waiting for the network, and refreshes
them in the background.

The app is a pet project built around one theme: **WorkManager as the backbone of a data
layer**, not as a decoration. A home screen widget and threshold alerts have to work while
the app is closed and its process is dead — exactly where a coroutine in a `ViewModel` does
not reach and a foreground service is too much. `AlarmManager` is not an option either:
exact alarms need `SCHEDULE_EXACT_ALARM`, denied by default since Android 14, and its
auto-granted twin `USE_EXACT_ALARM` is reserved for alarm clocks and calendars, which a
rate watcher is not. Inexact alarms need no permission but know nothing about network
constraints and do not survive a reboot.

## What works today

- **Watchlist** with rates from [Frankfurter](https://frankfurter.dev), rendered from the
  database, with a freshness row that tells when the data was last refreshed and ages in
  place while the screen is open.
- **Room as the single source of truth.** The UI never waits for a request; the network
  only writes into the database, and the screen observes it through `Flow`.
- **Background sync.** A periodic worker every six hours and a one-off run behind the
  refresh button, both unique, with exponential backoff and a cap of three attempts.
- **A sync log.** Every run leaves one row — source, trigger, timings, result, error kind,
  number of updated rates, attempt number. The watchlist reads its freshness from that log,
  so "updated 40 minutes ago" survives a restart, and a transient failure can promise
  another attempt while a permanent one cannot.
- **Error classification.** Network failures are sorted into transient and permanent, which
  is what decides between a retry and a final failure.

Coming next: a sync diagnostics screen over the same log, pair details with a chart, a
Glance widget, threshold alerts and a converter.

## Stack

Kotlin · Coroutines + Flow · Jetpack Compose + Material 3 · Hilt · Room (KSP) ·
WorkManager · Retrofit + OkHttp + kotlinx.serialization · Gradle version catalog with
convention plugins · `minSdk 28`

## Modules

| Module | What lives there |
| --- | --- |
| `app` | Application, `Configuration.Provider` for Hilt workers, navigation host |
| `core:model` | Plain Kotlin types shared by every layer |
| `core:database` | Room entities, DAOs, type converters, exported schemas |
| `core:network` | Retrofit clients and remote DTOs |
| `core:data` | Repositories, mappers, error classification |
| `core:domain` | Use cases and the `SyncScheduler` interface |
| `core:sync` | Workers, scheduling policy, sync logging |
| `core:designsystem` | Colours, typography, dimensions, shared components |
| `core:ui` | MVI base classes for view models |
| `feature:watchlist` | The watchlist screen |

The database is the boundary between layers: `core:database` knows only Room, and the
strings it stores are described by sealed hierarchies next to the entities, so an unknown
value read from an older database degrades instead of crashing.

## Build and test

```bash
./gradlew assembleDebug           # build
./gradlew testDebugUnitTest       # unit tests
./gradlew connectedDebugAndroidTest   # Room tests, needs a device or emulator
```

Unit tests cover the pure parts: outcome and column mappers, relative time gradations, and
the mapping from work state plus sync history to what the screen shows. Room is tested
in-memory on a device, including that the log returns the last *successful* run rather than
simply the last one.
