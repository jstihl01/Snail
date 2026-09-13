# Documentación y publicación

- Antes de cada commit y push, revisar README.md contra el funcionamiento actual. Actualizar las funciones añadidas, retiradas o modificadas; mantenerlo muy conciso y limitado a la app, sin datos personales, planes futuros ni historia del desarrollo.
- Mantener el botón de descarga apuntando a `https://github.com/jstihl01/Snail/releases/latest/download/Snail.apk`.
- Cada push a main ejecuta `.github/workflows/apk.yml`: tests, compilación y publicación del APK firmado como nueva release. Verificar el resultado y el enlace de descarga antes de declarar publicada la versión.
- Nunca versionar claves de firma, secretos, APK generados ni archivos locales del IDE. La firma de CI se obtiene del secreto `ANDROID_DEBUG_KEYSTORE`; no cambiarla sin autorización.
