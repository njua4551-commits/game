# Modo Juego

App Android (Kotlin + Compose) para el Galaxy S20+. Es una app normal: sin root, sin ROM, sin tocar el sistema. Se desinstala cuando quieras.

## Conseguir el APK sin instalar nada (GitHub Actions)

1. Crea un repositorio nuevo en GitHub (puede ser privado) y sube el contenido de esta carpeta.
2. Entra a la pestaña **Actions**. El flujo **Build APK** corre solo con el primer push (o ejecútalo con *Run workflow*).
3. Cuando termine (unos 4-6 min), abre la ejecución y descarga **ModoJuego-apk** en *Artifacts*. Dentro viene `app-debug.apk`.
4. Pásalo al teléfono e instálalo (permite "instalar apps desconocidas" para el navegador o el gestor de archivos).

Si falla la compilación, copia el error del log y te lo corrijo.

## Qué hace

- **Activar / Detener** el modo juego con un botón, o lanzar un juego desde Inicio (activa el modo y abre el juego).
- **Cierra procesos en segundo plano** de las apps de usuario cada ~40 s mientras está activo. Nunca toca: el juego en curso, las apps marcadas **MANTENER**, el teléfono, el teclado, el launcher ni apps de sistema.
- **Filtra notificaciones** de apps que no estén en MANTENER. Las llamadas y alarmas siempre pasan. Al terminar muestra un resumen de lo que se ocultó, para no perderte nada.
- **No molestar** opcional (solo llamadas y alarmas, sin avisos emergentes). Guarda tu configuración anterior y la restaura al detener.
- **Contador en pantalla**: RAM usada y temperatura/carga de la batería.

## Límites (sin root)

- El cierre usa `killBackgroundProcesses`: cierra procesos en segundo plano, no hace *force stop*. Alguna app puede reiniciarse sola.
- No sube el rendimiento de CPU/GPU ni cambia el kernel. Ayuda liberando RAM y evitando interrupciones.
- La temperatura que se muestra es la de la batería; Android no deja leer la del CPU.
- **Volumen independiente por app: no incluido.** Android no lo permite a una app normal sin root.
- Una notificación bloqueada se descarta (no vuelve). Por eso existe el resumen final.

## Primer uso en el teléfono

Abre la app y concede los permisos de la sección **Permisos**.

**Samsung / Android 13+:** al ser una app instalada fuera de la tienda, "Acceso a notificaciones" puede aparecer bloqueado. Ve a Ajustes > Aplicaciones > Modo Juego > menú de tres puntos > **Permitir ajustes restringidos**, y vuelve a intentarlo.

## Emulador en CachyOS (opcional)

```bash
chmod +x scripts/*.sh
./scripts/setup-emulador.sh      # una sola vez: SDK, imagen de Android 15 y AVD S20+
./scripts/iniciar-emulador.sh    # arranca el emulador
./scripts/instalar-app.sh        # compila e instala la app
```

- Necesita virtualización activada en la BIOS (VT-x / SVM).
- El AVD usa 12 GB de RAM. Con 16 GB en la PC, si va pesado: `RAM_MB=8192 ./scripts/setup-emulador.sh`.
- El almacenamiento de 128 GB es un archivo que crece según uso, no ocupa todo de golpe.
- El emulador sirve para probar diseño y lógica. El rendimiento real en juegos solo se mide en el teléfono.
- Para instalar en el teléfono por USB: activa Depuración USB en Opciones de desarrollador y ejecuta `./scripts/instalar-app.sh` (con `ANDROID_SERIAL` si también tienes el emulador abierto).

## Abrir en Android Studio

Abre la carpeta `ModoJuego` tal cual. Usa JDK 17 y Gradle 8.9 (se descarga solo).
