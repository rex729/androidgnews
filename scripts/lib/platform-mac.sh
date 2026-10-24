# macOS specifics, sourced by the scripts/*-mac.sh entry points.

# macOS paths are already usable as-is.
to_unix_path() { printf '%s' "$1"; }

platform_sdk_default="$HOME/Library/Android/sdk"
platform_jbr_globs="/Applications/Android*Studio*.app/Contents/jbr/Contents/Home $HOME/Applications/Android*Studio*.app/Contents/jbr/Contents/Home"
venv_python_rel=".venv/bin/python"
run_gradle() { ./gradlew "$@"; }
