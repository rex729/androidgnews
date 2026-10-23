#!/usr/bin/env bash
# Windows (Git Bash): builds the debug app and runs it on the emulator, starting the local backend
# and the emulator first if needed.
#   scripts/run-emulator-windows.sh [--logs] [--no-backend]
# Settings and overrides: see scripts/lib/run-emulator-common.sh.

# "C:\Users\me" or "C:/Users/me" -> "/c/Users/me", the form Git Bash needs inside PATH.
# Hand-rolled because other tools (e.g. Anaconda) can shadow cygpath with a different flavor.
to_unix_path() {
  case "$1" in
    [A-Za-z]:*)
      local p="${1//\\//}"
      printf '/%s%s' "$(printf '%s' "${p:0:1}" | tr 'A-Z' 'a-z')" "${p:2}" ;;
    *) printf '%s' "$1" ;;
  esac
}

platform_sdk_default="${LOCALAPPDATA:-$USERPROFILE/AppData/Local}/Android/Sdk"
platform_jbr_globs="/c/Program*Files/Android/Android*Studio/jbr"
venv_python_rel=".venv/Scripts/python.exe"

# The Windows wrapper, not ./gradlew: that one converts paths with cygpath, which breaks when another
# tool's cygpath comes first on PATH.
run_gradle() { cmd //c '.\gradlew.bat' "$@"; }

source "$(dirname "$0")/lib/run-emulator-common.sh"
