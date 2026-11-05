# Pixel Gallery 🖼️

<p align="center">
  <b>Galería de fotos y videos de código abierto para Android</b><br>
  Con el estilo de las apps nativas de Pixel, hecha con Jetpack Compose y Material Design 3
</p>

<p align="center">
  <img src="assets/screenshot1.png" width="23%" alt="Fotos">
  <img src="assets/screenshot2.png" width="23%" alt="Visor de video">
  <img src="assets/screenshot3.png" width="23%" alt="Ajustes">
  <img src="assets/screenshot4.png" width="23%" alt="Acerca de">
</p>

<p align="center">
  <a href="https://github.com/JGRyuuTB/Galeria/releases/latest"><img src="https://img.shields.io/github/v/release/JGRyuuTB/Galeria?include_prereleases&logo=github&style=for-the-badge&label=Última%20versión" alt="Última versión"></a>
  <a href="https://github.com/JGRyuuTB/Galeria/releases"><img src="https://img.shields.io/github/downloads/JGRyuuTB/Galeria/total?logo=github&style=for-the-badge" alt="Descargas"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-green?style=for-the-badge&logo=android" alt="Android 8.0+">
  <img src="https://img.shields.io/badge/Kotlin-100%25-purple?style=for-the-badge&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Licencia-MIT-blue?style=for-the-badge" alt="MIT">
</p>

---

## ✨ Características

### 🎨 Interfaz moderna

- **Material You**: colores dinámicos que se adaptan a tu fondo de pantalla.
- **Barra de navegación flotante de cristal** con desenfoque opcional (Ninguno / Suave / Fuerte).
- **Animaciones fluidas**: transiciones y efectos de movimiento que puedes desactivar.
- **Tema claro, oscuro o del sistema**.
- **Esquinas redondeadas** en las miniaturas y borde personalizable en el panel del reproductor.
- **Icono Material You** que toma los colores de tu fondo de pantalla (o icono por defecto).
- **Retroceso predictivo** de Android.

### 📷 Fotos y álbumes

- **Cuadrícula fluida** con precarga de miniaturas.
- **Pellizca la pantalla** para cambiar el número de columnas (2 a 5).
- **Columnas independientes** para Fotos y para Álbumes.
- **Espacio entre miniaturas** configurable: Ninguno, Pequeño o Grande.
- **Álbumes** y opción de mover fotos entre ellos.

### 🔍 Visor

- **Zoom** en fotos.
- **Reproductor de video propio**: velocidad de reproducción, saltos de 10 s, volumen y barra de progreso.
- **Reproducir al abrir**: los videos pueden empezar solos.
- **Desliza hacia abajo** para cerrar una foto o un video.
- Acciones rápidas: compartir, eliminar y ver detalles.

### 🗑️ Papelera

- Eliminar con restauración posterior.
- Borrado definitivo y vaciado de la papelera.
- Requiere Android 11 o superior.

### 🔒 Privacidad

- **Sin anuncios.**
- **Sin rastreadores.**
- **Sin conexión a internet**: la app no necesita ni pide el permiso de red.

---

## 🛠️ Tecnologías

| Categoría | Tecnología |
|---|---|
| **Lenguaje** | [Kotlin](https://kotlinlang.org/) 100% |
| **Interfaz** | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| **Diseño** | [Material Design 3](https://m3.material.io/) |
| **Reproductor de video** | [Media3 ExoPlayer](https://developer.android.com/guide/topics/media/media3) |
| **Miniaturas** | [Coil](https://coil-kt.github.io/coil/) + coil-video |
| **Compilación** | Gradle (Kotlin DSL) + GitHub Actions |

---

## 📱 Requisitos

- **Android 8.0** (API 26) o superior.
- **Android 12+** para el desenfoque de cristal.
- **Android 11+** para la papelera.

---

## ⬇️ Descargar

Descarga el APK desde la sección de [Releases](https://github.com/JGRyuuTB/Galeria/releases/latest).

También puedes enterarte de las nuevas versiones en el canal de Telegram: [Dragon's Bastion](https://t.me/dragonsbastion).

---

## 🚀 Compilar

El proyecto se compila automáticamente con GitHub Actions (`.github/workflows/build-apk.yml`):

1. Haz push a `main` y descarga el artefacto `Galeria-apk` desde la pestaña **Actions**.
2. Para publicar una versión, crea una etiqueta `v1.0.0` y súbela: el APK se adjunta al Release.

En local:

```bash
git clone https://github.com/JGRyuuTB/Galeria.git
cd Galeria
gradle assembleRelease   # requiere JDK 17
```

---

## 📂 Estructura del proyecto

```
app/src/main/java/com/GalleryYou/Pixel/
├── MainActivity.kt   # Actividad principal
├── Nav.kt            # Navegación y barra flotante
├── Screens.kt        # Pantallas de Fotos, Álbumes y Papelera
├── Viewer.kt         # Visor de fotos y reproductor de video
├── Settings.kt       # Ajustes y preferencias
├── About.kt          # Pantalla "Acerca de"
├── Onboarding.kt     # Bienvenida de primer uso
├── Panels.kt         # Paneles y diálogos
├── Backdrop.kt       # Efecto de desenfoque tipo cristal
├── MediaRepo.kt      # Acceso a fotos y videos
├── Thumbs.kt         # Miniaturas
├── AppIcons.kt       # Set de iconos propio
└── Theme.kt          # Colores y tema
```

---

## 🔐 Permisos

| Permiso | Para qué |
|---|---|
| Fotos y videos | Mostrar tu colección |

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas!

1. Haz un fork del proyecto.
2. Crea tu rama (`git checkout -b feature/MiMejora`).
3. Haz commit de tus cambios (`git commit -m 'Agrego MiMejora'`).
4. Sube la rama (`git push origin feature/MiMejora`).
5. Abre un Pull Request.

---

## 📝 Registro de cambios

Mira el [CHANGELOG](CHANGELOG.md) para ver las novedades de cada versión.

---

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---

## 📬 Contacto

- GitHub: [JGRyuuTB](https://github.com/JGRyuuTB)
- Telegram: [@JG_ryuu](https://t.me/JG_ryuu)
- Canal: [Dragon's Bastion](https://t.me/dragonsbastion)

Pixel Gallery, hecha con ❤️ por **JG Ryuu**
