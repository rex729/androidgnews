#!/usr/bin/env bash
# macOS: builds the debug app and runs it on the emulator, starting the local backend and the
# emulator first if needed.
#   scripts/run-emulator-mac.sh [--logs] [--no-backend]
# Settings and overrides: see scripts/lib/run-emulator-common.sh.

source "$(dirname "$0")/lib/platform-mac.sh"
source "$(dirname "$0")/lib/run-emulator-common.sh"
