#!/usr/bin/env bash
# Compila la app e instala el APK en el emulador o en el teléfono conectado por USB.
# Con varios dispositivos a la vez: ANDROID_SERIAL=<serial> ./instalar-app.sh   (ver con: adb devices)
set -euo pipefail

SDK="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_SDK_ROOT="$SDK"

cd "$ROOT"
echo "sdk.dir=$SDK" > local.properties
[ -x ./gradlew ] || gradle wrapper --gradle-version 8.9
./gradlew assembleDebug

APK="app/build/outputs/apk/debug/app-debug.apk"
"$SDK/platform-tools/adb" install -r "$APK"
echo "APK: $ROOT/$APK"
