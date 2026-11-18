#!/usr/bin/env bash
# Prepara en CachyOS/Arch un emulador tipo Galaxy S20+ (1440x3200, 12 GB de RAM, 128 GB).
set -euo pipefail

SDK="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
AVD="S20Plus_12GB"
RAM_MB="${RAM_MB:-12288}"   # si tu PC de 16 GB se pone pesada: RAM_MB=8192 ./setup-emulador.sh
IMG="system-images;android-35;google_apis;x86_64"

echo "==> Paquetes del sistema"
sudo pacman -S --needed --noconfirm jdk17-openjdk unzip curl gradle

echo "==> Virtualización (KVM)"
if [ ! -r /dev/kvm ] || [ ! -w /dev/kvm ]; then
  echo "Sin acceso a /dev/kvm. Activa VT-x / AMD-V (SVM) en la BIOS y agrega tu usuario al grupo kvm:"
  echo "  sudo usermod -aG kvm \$USER   # luego cierra sesión y vuelve a entrar"
  exit 1
fi

echo "==> Android SDK en $SDK"
mkdir -p "$SDK/cmdline-tools"
if [ ! -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
  tmp="$(mktemp -d)"
  curl -fL -o "$tmp/tools.zip" "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
  unzip -q "$tmp/tools.zip" -d "$tmp"
  mv "$tmp/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm -rf "$tmp"
fi

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH="$SDK/cmdline-tools/latest/bin:$SDK/platform-tools:$PATH"

yes | sdkmanager --licenses >/dev/null || true
sdkmanager "platform-tools" "emulator" "platforms;android-35" "build-tools;35.0.0" "$IMG"

echo "==> Creando el AVD $AVD"
echo no | avdmanager create avd -n "$AVD" -k "$IMG" -d pixel_6 --force
CFG="${ANDROID_AVD_HOME:-$HOME/.android/avd}/$AVD.avd/config.ini"

setcfg() {
  if grep -q "^$1=" "$CFG"; then sed -i "s|^$1=.*|$1=$2|" "$CFG"; else echo "$1=$2" >> "$CFG"; fi
}
setcfg hw.lcd.width 1440
setcfg hw.lcd.height 3200
setcfg hw.lcd.density 560
setcfg hw.ramSize "$RAM_MB"
setcfg disk.dataPartition.size 128G
setcfg hw.gpu.enabled yes
setcfg hw.gpu.mode host
setcfg hw.keyboard yes
setcfg hw.cpu.ncore 4
setcfg vm.heapSize 512

echo
echo "Listo. Siguiente:"
echo "  ./scripts/iniciar-emulador.sh"
echo "  ./scripts/instalar-app.sh"
