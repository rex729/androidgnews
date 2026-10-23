#!/usr/bin/env bash
# macOS: builds the debug app and runs it on the emulator, starting the local backend and the
# emulator first if needed.
#   scripts/run-emulator-mac.sh [--logs] [--no-backend]
# Settings and overrides: see scripts/lib/run-emulator-common.sh.

# macOS paths are already usable as-is.
to_unix_path() { printf '%s' "$1"; }

platform_sdk_default="$HOME/Library/Android/sdk"
platform_jbr_globs="/Applications/Android*Studio*.app/Contents/jbr/Contents/Home $HOME/Applications/Android*Studio*.app/Contents/jbr/Contents/Home"
venv_python_rel=".venv/bin/python"
run_gradle() { ./gradlew "$@"; }

source "$(dirname "$0")/lib/run-emulator-common.sh"
