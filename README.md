# Galería

Galería de fotos y videos de código abierto para Android, con el estilo de las apps nativas de Pixel.

- Paquete: `com.GalleryYou.Pixel`
- Versión: 1.0.0 (estable)
- Android 8.0 (API 26) o superior. El desenfoque de cristal requiere Android 12+.
- Licencia: MIT

## Características

- Fotos, videos, álbumes y papelera.
- Visor con zoom, reproductor de video propio y gesto de deslizar hacia abajo para salir.
- Interfaz Material 3 con colores dinámicos, barra de navegación flotante de cristal y desenfoque opcional.
- Sin anuncios, sin rastreadores y sin conexión a internet.

## Compilar

El proyecto se compila con GitHub Actions (`.github/workflows/build-apk.yml`):

1. Haz push a `main` y descarga el artefacto `Galeria-apk`.
2. Para publicar una versión, crea una etiqueta `v1.0.0` y súbela: el APK se adjunta al Release.

En local: `gradle assembleRelease` (JDK 17).

## Permisos

- Fotos y videos: para mostrar tu colección.
- La cámara se abre con la app de cámara del sistema, sin permisos adicionales.
