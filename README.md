# AndroidGNews (store branch)

Android news reader (Kotlin, Jetpack Compose) for Traditional Chinese news from Taiwan.

This `store` branch is the build for the Play Store: it calls the GNews API directly with the API key
packed into the app. `main` instead talks to its own backend,
[androidgnewsbackend](https://gitlab.com/k8stest2/androidgnewsbackend), which keeps the key off the
device. Trade-offs of this branch:

- The key can be extracted from the APK.
- Every install shares the key's daily quota (100 requests/day on the free GNews plan).
- No SSL pinning: gnews.io is a third-party server whose certificates we don't control.

Reading news requires signing in with Google; signed-in users can save articles to favorites,
stored in Cloud Firestore under their account so they follow them across devices.

## Setup

1. Put your GNews key in `local.properties`: `GNEWS_API_KEY=...` (release builds refuse to build
   without it).
2. Firebase (sign-in and favorites):
   1. In your Firebase project, add an Android app with package `com.well.androidgnews` and the
      SHA-1 of every key that signs it (`.\gradlew.bat :app:signingReport` prints the debug one;
      for Play, also add the upload key's and Play App Signing's).
   2. Authentication → Sign-in method → enable **Google**.
   3. Firestore Database → create it, then paste `firestore.rules` into its Rules tab and publish.
   4. Download `google-services.json` into `app/` (git-ignored; the build fails without it).
3. Open this folder in Android Studio and run, or:

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

## Build an APK

```bash
scripts/build-apk-windows.sh            # Windows (Git Bash); macOS: scripts/build-apk-mac.sh
scripts/build-apk-windows.sh --test     # run the unit tests first
scripts/build-apk-windows.sh --release  # release build, signed with the key below
```

The APK lands in `dist/AndroidGNews-<versionName>-<debug|release>.apk` (git-ignored). The script
prints the signing certificate's SHA-1/SHA-256 and warns if that SHA-1 isn't registered in
`app/google-services.json`, since Google sign-in fails without it.

Debug APKs are signed with the machine's debug key. Release APKs need a signing key of your own:

```bash
keytool -genkeypair -v -keystore androidgnews-upload.jks -alias upload   -keyalg RSA -keysize 2048 -validity 10000
```

Keep the `.jks` and its passwords outside the repo (it's public) and backed up, point
`local.properties` at it (keys below), and register its SHA-1 in Firebase.

## local.properties

Not committed; each machine keeps its own.

| Key | Used by | Default |
|---|---|---|
| `GNEWS_API_KEY` | debug and release | none: **release builds fail without it** |
| `RELEASE_STORE_FILE` | release | none: path to the signing keystore (`.jks`) |
| `RELEASE_STORE_PASSWORD` | release | none |
| `RELEASE_KEY_ALIAS` | release | none |
| `RELEASE_KEY_PASSWORD` | release | none |

## Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

JVM unit tests only (`app/src/test`): ViewModels (news, login, favorites), repository mapping, the
request contract with GNews, the API key interceptor, the Firestore favorite document (including a
check that it matches the allow-list in `firestore.rules`), error messages, relative time.
