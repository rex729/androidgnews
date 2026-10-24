#!/usr/bin/env bash
# Windows (Git Bash): builds the app and copies the APK to dist/.
#   scripts/build-apk-windows.sh [--release] [--test]
# Details: see scripts/lib/build-apk-common.sh.

source "$(dirname "$0")/lib/platform-windows.sh"
source "$(dirname "$0")/lib/build-apk-common.sh"
