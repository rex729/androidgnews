# AndroidGNews (store branch)

Android news reader (Kotlin, Jetpack Compose) for Traditional Chinese news from Taiwan.

This `store` branch is the build for the Play Store: it calls the GNews API directly with the API key
packed into the app. `main` instead talks to its own backend,
[androidgnewsbackend](https://gitlab.com/k8stest2/androidgnewsbackend), which keeps the key off the
device. Trade-offs of this branch:

- The key can be extracted from the APK.
- Every install shares the key's daily quota (100 requests/day on the free GNews plan).
- No SSL pinning: gnews.io is a third-party server whose certificates we don't control.

## Setup

1. Put your GNews key in `local.properties`: `GNEWS_API_KEY=...` (release builds refuse to build
   without it).
2. Open this folder in Android Studio and run, or:

   ```powershell
   $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
   .\gradlew.bat installDebug
   ```

## One-command run

Starts the emulator if needed, then builds, installs and launches the app:

```bash
scripts/run-emulator-windows.sh   # Windows, from Git Bash
scripts/run-emulator-mac.sh       # macOS
# option: --logs (follow the app's network log)
```

Paths and app ID are read from `local.properties` / `app/build.gradle.kts`; override with
`ANDROID_HOME`, `JAVA_HOME`, `AVD`, `EMULATOR_ARGS`, `BOOT_TIMEOUT`. The emulator cold boots by
default, since a quick-boot snapshot left by a killed emulator can hang at "offline".

## local.properties

Not committed; each machine keeps its own.

| Key | Used by | Default |
|---|---|---|
| `GNEWS_API_KEY` | debug and release | none: **release builds fail without it** |

## Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

JVM unit tests only (`app/src/test`): ViewModels, repository mapping, the request contract with
GNews, the API key interceptor, error messages, relative time.
