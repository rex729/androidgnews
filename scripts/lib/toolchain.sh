# Locates the project, Android SDK and JDK. Sourced by the *-common.sh scripts after a
# lib/platform-*.sh file has defined: to_unix_path, platform_sdk_default, platform_jbr_globs.
#
# Sets: project_dir, gradle_file, local_props, sdk_dir, adb, emulator, JAVA_HOME (exported if found)
# Provides: step, die, local_prop KEY
# Overrides: ANDROID_HOME, JAVA_HOME

step() { printf '\033[36m==> %s\033[0m\n' "$*"; }
die() { printf '\033[31merror: %s\033[0m\n' "$*" >&2; exit 1; }

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
gradle_file="$project_dir/app/build.gradle.kts"
local_props="$project_dir/local.properties"

# Value of KEY in local.properties, with Java-properties escapes (C\:\\Users) undone.
local_prop() {
  [ -f "$local_props" ] || return 0
  grep -E "^[[:space:]]*$1[[:space:]]*=" "$local_props" | tail -n 1 \
    | sed -E 's/^[^=]*=[[:space:]]*//; s/\\(.)/\1/g' | tr -d '\r' || true
}

# --- Android SDK ---------------------------------------------------------------
sdk_dir="$(local_prop sdk.dir)"
sdk_dir="${sdk_dir:-${ANDROID_HOME:-$platform_sdk_default}}"
sdk_dir="$(to_unix_path "$sdk_dir")"
[ -d "$sdk_dir/platform-tools" ] || die "Android SDK not found at $sdk_dir (set sdk.dir in local.properties or ANDROID_HOME)"
# Our adb first on PATH: the emulator also runs whatever adb PATH finds, and two different adb
# versions keep restarting each other's server.
export PATH="$sdk_dir/platform-tools:$sdk_dir/emulator:$PATH"
adb="$sdk_dir/platform-tools/adb"
emulator="$sdk_dir/emulator/emulator"

# --- JDK -----------------------------------------------------------------------
if [ -z "${JAVA_HOME:-}" ]; then
  for candidate in $platform_jbr_globs; do
    if [ -d "$candidate" ]; then JAVA_HOME="$candidate"; break; fi
  done
fi
if [ -n "${JAVA_HOME:-}" ]; then
  export JAVA_HOME
  java="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
  java="java"
else
  die "no JDK found: install Android Studio or set JAVA_HOME"
fi
