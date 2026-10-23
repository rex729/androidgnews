# Shared steps for run-emulator-windows.sh and run-emulator-mac.sh. Not meant to be run directly.
#
# The platform script sets these before sourcing this file:
#   platform_sdk_default   Android SDK location if local.properties/ANDROID_HOME don't say
#   platform_jbr_globs     candidate JDK homes (Android Studio's bundled JBR), space-separated globs
#   venv_python_rel        backend venv's python, relative to the backend folder
#   run_gradle()           runs the Gradle wrapper in the project folder with the given arguments
#   to_unix_path()         converts a native path into one usable inside this shell's PATH
#
# Everything else comes from the project itself or can be overridden by environment variable:
#   ANDROID_HOME, JAVA_HOME, AVD, BACKEND_DIR, EMULATOR_ARGS, BOOT_TIMEOUT (seconds)
#
# Options: --logs (follow the app's network log), --no-backend (don't start the local backend).

set -euo pipefail

step() { printf '\033[36m==> %s\033[0m\n' "$*"; }
die() { printf '\033[31merror: %s\033[0m\n' "$*" >&2; exit 1; }

follow_logs=false
manage_backend=true
for arg in "$@"; do
  case "$arg" in
    --logs) follow_logs=true ;;
    --no-backend) manage_backend=false ;;
    -h|--help) sed -n '2,15p' "${BASH_SOURCE[0]}"; exit 0 ;;
    *) die "unknown option: $arg" ;;
  esac
done

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
elif ! command -v java >/dev/null 2>&1; then
  die "no JDK found: install Android Studio or set JAVA_HOME"
fi

# --- App and backend facts, read from the project ----------------------------
app_id="$(sed -nE 's/^[[:space:]]*applicationId[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' "$gradle_file" | head -n 1)"
[ -n "$app_id" ] || die "couldn't read applicationId from $gradle_file"

backend_url="$(local_prop BACKEND_BASE_URL)"
if [ -z "$backend_url" ]; then
  backend_url="$(sed -nE 's/.*getProperty\("BACKEND_BASE_URL", "([^"]+)"\).*/\1/p' "$gradle_file" | head -n 1)"
fi
[ -n "$backend_url" ] || die "couldn't work out the debug backend URL"
backend_host="$(printf '%s' "$backend_url" | sed -E 's#^[a-z]+://([^/:]+).*#\1#')"
backend_port="$(printf '%s' "$backend_url" | sed -nE 's#^[a-z]+://[^/:]+:([0-9]+).*#\1#p')"
if [ -z "$backend_port" ]; then
  case "$backend_url" in https://*) backend_port=443 ;; *) backend_port=80 ;; esac
fi
backend_dir="${BACKEND_DIR:-$(cd "$project_dir/.." && pwd)/AndroidGNewsBackend}"

# --- Backend -------------------------------------------------------------------
backend_up() { curl -fsS -m 3 "http://127.0.0.1:$backend_port/healthz" >/dev/null 2>&1; }

case "$backend_host" in
  10.0.2.2|localhost|127.0.0.1) ;;
  *) manage_backend=false ;;  # app points at a remote backend; nothing to start here
esac

if $manage_backend; then
  if backend_up; then
    step "Backend already running on port $backend_port"
  else
    python="$backend_dir/$venv_python_rel"
    [ -x "$python" ] || die "backend venv not found at $python (see $backend_dir/README.md)"
    [ -f "$backend_dir/.env" ] || die "missing $backend_dir/.env (copy .env.example and add the GNews key)"
    step "Starting backend on port $backend_port"
    (cd "$backend_dir" && nohup "$python" -m uvicorn app.main:app \
        --host 127.0.0.1 --port "$backend_port" >server.log 2>server.err.log &)
    for _ in $(seq 60); do backend_up && break; sleep 0.5; done
    backend_up || die "backend didn't start; see $backend_dir/server.err.log"
  fi
fi

# --- Emulator ------------------------------------------------------------------
booted() { [ "$("$adb" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; }

# Any emulator listed counts, including one still booting ("offline"), so we don't start a second.
if "$adb" devices | grep -qE '^emulator-[0-9]+'; then
  step "Emulator already running"
else
  avd="${AVD:-$("$emulator" -list-avds | tr -d '\r' | head -n 1)}"
  [ -n "$avd" ] || die "no emulator (AVD) found; create one in Android Studio's Device Manager"
  # Cold boot by default: a quick-boot snapshot left by a killed emulator can hang at "offline".
  step "Booting emulator $avd (log: $project_dir/emulator.log)"
  # shellcheck disable=SC2086  # EMULATOR_ARGS is intentionally word-split
  (nohup "$emulator" -avd "$avd" ${EMULATOR_ARGS:--no-snapshot-load} >"$project_dir/emulator.log" 2>&1 &)
fi

boot_timeout="${BOOT_TIMEOUT:-180}"
waited=0
until booted; do
  [ "$waited" -lt "$boot_timeout" ] || die "emulator didn't finish booting within ${boot_timeout}s; see $project_dir/emulator.log"
  sleep 2; waited=$((waited + 2))
done

# --- Build, install, launch ----------------------------------------------------
step "Building and installing the debug app"
(cd "$project_dir" && run_gradle installDebug --console=plain -q)

launcher="$("$adb" shell cmd package resolve-activity --brief -c android.intent.category.LAUNCHER "$app_id" \
  | tr -d '\r' | tail -n 1)"
[[ "$launcher" == */* ]] || die "couldn't find the launcher activity of $app_id"

step "Launching $launcher"
"$adb" shell am force-stop "$app_id"
"$adb" logcat -c
"$adb" shell am start -W -n "$launcher" >/dev/null

if $follow_logs; then
  step "App log (Ctrl+C to stop)"
  exec "$adb" logcat -s OkHttp:I AndroidRuntime:E
fi

sleep 5
result="$("$adb" logcat -d -s OkHttp:I | grep -- '<--' | tail -n 1 || true)"
if [[ "$result" == *"<-- 200"* ]]; then
  step "Done: the app loaded news from $backend_url"
elif [ -n "$result" ]; then
  echo "warning: first request didn't succeed: $result" >&2
else
  echo "warning: no network request seen yet; check the emulator screen" >&2
fi
