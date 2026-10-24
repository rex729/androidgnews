# Shared steps for build-apk-windows.sh and build-apk-mac.sh. Not meant to be run directly.
#
# Builds the app and copies the APK to dist/AndroidGNews-<versionName>-<debug|release>.apk, then
# prints the signing certificate's fingerprints and checks that the SHA-1 is registered in
# app/google-services.json; if it isn't, Google sign-in fails with "[16] Account reauth failed".
#
# Options:
#   --release   R8-optimized release build, signed with the key in local.properties
#                 (RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD)
#               or, until one is set up, with this machine's debug key
#               (default: debug build, signed with this machine's debug key)
#   --test      run the unit tests first and stop if any fail

set -euo pipefail

source "$(dirname "${BASH_SOURCE[0]}")/toolchain.sh"

build_type=debug
run_tests=false
for arg in "$@"; do
  case "$arg" in
    --release) build_type=release ;;
    --test) run_tests=true ;;
    -h|--help) sed -n '2,13p' "${BASH_SOURCE[0]}"; exit 0 ;;
    *) die "unknown option: $arg" ;;
  esac
done

version="$(sed -nE 's/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' "$gradle_file" | head -n 1)"
[ -n "$version" ] || die "couldn't read versionName from $gradle_file"

if [ "$build_type" = release ] && [ -z "$(local_prop RELEASE_STORE_FILE)" ]; then
  echo "note: no release key in local.properties, so this release APK is signed with the debug key." >&2
  echo "      Fine for testing; Google Play needs your own key (see README)." >&2
fi

# --- Build ---------------------------------------------------------------------
task="assemble$(printf '%s' "${build_type:0:1}" | tr 'a-z' 'A-Z')${build_type:1}"
cd "$project_dir"
if $run_tests; then
  step "Running unit tests"
  run_gradle testDebugUnitTest --console=plain -q
fi
step "Building $build_type APK ($task)"
run_gradle "$task" --console=plain -q

built="$project_dir/app/build/outputs/apk/$build_type/app-$build_type.apk"
[ -f "$built" ] || die "build finished but $built is missing"
mkdir -p "$project_dir/dist"
apk="$project_dir/dist/AndroidGNews-$version-$build_type.apk"
cp "$built" "$apk"

# --- Signature check -----------------------------------------------------------
# Newest build-tools version (numeric sort; macOS's sort has no -V).
build_tools="$(ls "$sdk_dir/build-tools" 2>/dev/null | sort -t. -k1,1n -k2,2n -k3,3n | tail -n 1)"
apksigner_jar="$sdk_dir/build-tools/$build_tools/lib/apksigner.jar"
if [ -f "$apksigner_jar" ]; then
  certs="$("$java" -jar "$apksigner_jar" verify --print-certs "$apk" 2>/dev/null | tr -d '\r' || true)"
  sha1="$(printf '%s\n' "$certs" | sed -nE 's/^Signer #1 certificate SHA-1 digest: ([0-9a-f]+)$/\1/p')"
  sha256="$(printf '%s\n' "$certs" | sed -nE 's/^Signer #1 certificate SHA-256 digest: ([0-9a-f]+)$/\1/p')"
  [ -n "$sha1" ] || die "apksigner couldn't read a signature from $apk"
  step "Signed with SHA-1 $sha1"
  echo "    SHA-256 $sha256"

  services="$project_dir/app/google-services.json"
  if [ -f "$services" ]; then
    if grep -qi "\"certificate_hash\": *\"$sha1\"" "$services"; then
      step "This SHA-1 is registered in Firebase: Google sign-in will work"
    else
      echo "warning: this SHA-1 is not in app/google-services.json. Add it in Firebase (project settings ->" >&2
      echo "         com.well.androidgnews -> SHA certificate fingerprints), re-download the file, and rebuild;" >&2
      echo "         otherwise Google sign-in fails with \"[16] Account reauth failed\"." >&2
    fi
  fi
else
  echo "warning: apksigner not found under $sdk_dir/build-tools; skipped the signature check" >&2
fi

step "APK ready: $apk ($(du -h "$apk" | cut -f1))"
