#!/usr/bin/env bash
# macOS: builds the app and copies the APK to dist/.
#   scripts/build-apk-mac.sh [--release] [--test]
# Details: see scripts/lib/build-apk-common.sh.

source "$(dirname "$0")/lib/platform-mac.sh"
source "$(dirname "$0")/lib/build-apk-common.sh"
