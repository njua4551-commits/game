#!/usr/bin/env bash
# Arranca el emulador del S20+. En Wayland (Hyprland/Caelestia) corre vía XWayland.
set -euo pipefail

SDK="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
export QT_QPA_PLATFORM=xcb

# Si ves la pantalla negra o glitches, prueba: ./iniciar-emulador.sh -gpu swiftshader_indirect
exec "$SDK/emulator/emulator" -avd S20Plus_12GB -gpu host -netdelay none -netspeed full "$@"
