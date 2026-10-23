# AndroidGNews

Android news reader (Kotlin, Jetpack Compose) for Traditional Chinese news from Taiwan. It never
talks to GNews directly: it calls its own backend,
[androidgnewsbackend](https://gitlab.com/k8stest2/androidgnewsbackend), which holds the GNews API key.

## Setup

1. Start the backend on this PC (see its README); it listens on port 8080.
2. Open this folder in Android Studio and run on the emulator, or:

   ```powershell
   $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
   .\gradlew.bat installDebug
   ```

Debug builds call `http://10.0.2.2:8080/` (this PC as seen from the emulator). Reaching it on
Android 17 needs the `ACCESS_LOCAL_NETWORK` permission: `installDebug` grants it automatically, and
otherwise (e.g. Android Studio's Run, which can uninstall first) the debug app asks for it on launch
before loading anything. Tap **Allow**; if you tapped Don't allow, every request times out with a
network error until you grant it in Settings or with
`adb shell pm grant com.well.androidgnews android.permission.ACCESS_LOCAL_NETWORK`.

## One-command run

Starts the local backend and the emulator if needed, then builds, installs and launches the app:

```bash
scripts/run-emulator-windows.sh   # Windows, from Git Bash
scripts/run-emulator-mac.sh       # macOS
# options: --logs (follow the app's network log), --no-backend
```

Paths, port and app ID are read from `local.properties` / `app/build.gradle.kts`; override with
`ANDROID_HOME`, `JAVA_HOME`, `AVD`, `BACKEND_DIR`, `EMULATOR_ARGS`, `BOOT_TIMEOUT`. The emulator cold
boots by default, since a quick-boot snapshot left by a killed emulator can hang at "offline".

## local.properties

Not committed; each machine keeps its own.

| Key | Used by | Default |
|---|---|---|
| `BACKEND_BASE_URL` | debug | `http://10.0.2.2:8080/` |
| `RELEASE_BACKEND_BASE_URL` | release | `https://backend.invalid/` (placeholder) |

## Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

JVM unit tests only (`app/src/test`): ViewModels, repository mapping, the API contract with the
backend, error messages, relative time.
