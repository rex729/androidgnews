# R8 is on for release (isMinifyEnabled + isShrinkResources). No app-specific rules are needed so
# far: Retrofit, kotlinx.serialization, OkHttp, Firebase and Credential Manager ship their own
# consumer rules. Verified by installing the release APK and exercising sign-in, headlines,
# search and favorites. Add keep rules here if a release build starts failing at runtime.
