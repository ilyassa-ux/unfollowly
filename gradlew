#!/usr/bin/env sh
set -eu
APP_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=8.10.2
BOOT_DIR="$APP_DIR/.gradle-bootstrap"
GRADLE_BIN="$BOOT_DIR/gradle-$GRADLE_VERSION/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$BOOT_DIR"
  ZIP="$BOOT_DIR/gradle-$GRADLE_VERSION-bin.zip"
  URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  if command -v curl >/dev/null 2>&1; then curl -fL "$URL" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then wget -O "$ZIP" "$URL"
  else echo "Install curl or wget, or run this project from Android Studio." >&2; exit 1; fi
  unzip -q -o "$ZIP" -d "$BOOT_DIR"
fi
exec "$GRADLE_BIN" "$@"
