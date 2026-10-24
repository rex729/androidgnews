# Shared steps for run-emulator-windows.sh and run-emulator-mac.sh. Not meant to be run directly.
#
# The entry script sources lib/platform-<os>.sh first (paths, run_gradle, venv_python_rel); the
# SDK and JDK are found by lib/toolchain.sh.
#
# Everything else comes from the project itself or can be overridden by environment variable:
#   ANDROID_HOME, JAVA_HOME, AVD, BACKEND_DIR, EMULATOR_ARGS, BOOT_TIMEOUT (seconds)
#
# Options: --logs (follow the app's network log), --no-backend (don't start the local backend).

set -euo pipefail

source "$(dirname "${BASH_SOURCE[0]}")/toolchain.sh"

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

# --- App and backend facts, read from the project ----------------------------
app_id="$(sed -nE 's/^[[:space:]]*applicationId[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' "$gradle_file" | head -n 1)"
[ -n "$app_id" ] || die "couldn't read applicationId from $gradle_file"

backend_url="$(local_prop BACKEND_BASE_URL)"
if [ -z "$backend_url" ]; then
  backend_url="$(sed -nE 's/.*getProperty\("BACKEND_BASE_URL", "([^"]+)"\).*/\1/p' "$gradle_file" | head -n 1)"
fi
if [ -z "$backend_url" ]; then
  # This build calls a public API directly (e.g. the store branch); there's no local backend.
  manage_backend=false
  backend_url="its API"
fi
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
  exec "$adb" logcat -s OkHttp:D AndroidRuntime:E
fi

sleep 5
result="$("$adb" logcat -d -s OkHttp:D | grep -- '<--' | tail -n 1 || true)"
if [[ "$result" == *"<-- 200"* ]]; then
  step "Done: the app loaded news from $backend_url"
elif [ -n "$result" ]; then
  echo "warning: first request didn't succeed: $result" >&2
else
  echo "warning: no network request seen yet; check the emulator screen" >&2
fi
