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
| `RELEASE_BACKEND_CERT_PINS` | release | none: **release builds fail without it** |
| `BACKEND_CERT_PINS` | debug | none (debug's local backend is plain HTTP) |

## SSL pinning

Release builds pin the backend's certificate by hash comparison: OkHttp's `CertificatePinner`
hashes the public key (SPKI, SHA-256) of each certificate in the server's chain and drops the
connection unless one matches a configured pin. See
`app/src/main/java/com/well/androidgnews/data/remote/CertificatePinning.kt`.

Get the pins of a deployed backend (Git Bash, needs `openssl`):

```bash
scripts/cert-pins.sh your-backend.example.com
# sha256/gsQh...=   CN = your-backend.example.com          <- leaf
# sha256/LoMH...=   C = US, O = Let's Encrypt, CN = YR1    <- intermediate CA
# sha256/fk6I...=   C = US, O = ISRG, CN = Root YR         <- root CA
```

At least two pins are required; one alone is rejected. Pinning is a trade-off: if every pin stops
matching, installed apps can't reach the backend until users update. Pick a pair that won't all
change at once:

- **Leaf + intermediate CA**: survives a leaf renewal with a new key, but breaks if the CA moves you
  to a different intermediate (Let's Encrypt rotates intermediates).
- **Leaf + a spare key you generated and keep offline** (most robust): when rotating, issue the new
  certificate from the spare key, then ship an update pinning the next spare.

Leaf pins only stay valid across renewals if the certificate is renewed with the **same key**
(e.g. certbot `--reuse-key`); a new key means a new pin.

```properties
RELEASE_BACKEND_BASE_URL=https://your-backend.example.com/
RELEASE_BACKEND_CERT_PINS=sha256/<leaf>=,sha256/<backup>=
```

## Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

JVM unit tests only (`app/src/test`): ViewModels, repository mapping, the API contract with the
backend, error messages, relative time, and certificate pinning against a real local TLS server.
