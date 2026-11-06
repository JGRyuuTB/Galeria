package com.GalleryYou.Pixel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Iconos propios de Galería: trazo redondeado y fino, con variante "Duo" (relleno suave detrás
 * del trazo) para los elementos seleccionados. Se tiñen con el color del tema.
 */
object AppIcons {
    val Image: ImageVector by lazy {
        build("Image",
            line("M8,3 H16 A5,5 0 0 1 21,8 V16 A5,5 0 0 1 16,21 H8 A5,5 0 0 1 3,16 V8 A5,5 0 0 1 8,3 Z"),
            line("M6.9,8.6 A1.7,1.7 0 1 0 10.3,8.6 A1.7,1.7 0 1 0 6.9,8.6 Z"),
            line("M3.5,16.5 L8.5,12 L12.5,15.5 L15.5,13 L20.5,17"),
        )
    }
    val ImageDuo: ImageVector by lazy {
        build("ImageDuo",
            fill("M8,3 H16 A5,5 0 0 1 21,8 V16 A5,5 0 0 1 16,21 H8 A5,5 0 0 1 3,16 V8 A5,5 0 0 1 8,3 Z", 0.28f),
            line("M8,3 H16 A5,5 0 0 1 21,8 V16 A5,5 0 0 1 16,21 H8 A5,5 0 0 1 3,16 V8 A5,5 0 0 1 8,3 Z"),
            line("M6.9,8.6 A1.7,1.7 0 1 0 10.3,8.6 A1.7,1.7 0 1 0 6.9,8.6 Z"),
            line("M3.5,16.5 L8.5,12 L12.5,15.5 L15.5,13 L20.5,17"),
        )
    }
    val Collections: ImageVector by lazy {
        build("Collections",
            line("M3,8.5 V17 A4,4 0 0 0 7,21 H15.5"),
            line("M11.0,3 H16.5 A4.5,4.5 0 0 1 21.0,7.5 V13.0 A4.5,4.5 0 0 1 16.5,17.5 H11.0 A4.5,4.5 0 0 1 6.5,13.0 V7.5 A4.5,4.5 0 0 1 11.0,3 Z"),
            line("M6.8,14.5 L10.5,11 L13.5,13.5 L16,11.5 L20.5,15"),
        )
    }
    val CollectionsDuo: ImageVector by lazy {
        build("CollectionsDuo",
            fill("M11.0,3 H16.5 A4.5,4.5 0 0 1 21.0,7.5 V13.0 A4.5,4.5 0 0 1 16.5,17.5 H11.0 A4.5,4.5 0 0 1 6.5,13.0 V7.5 A4.5,4.5 0 0 1 11.0,3 Z", 0.28f),
            line("M3,8.5 V17 A4,4 0 0 0 7,21 H15.5"),
            line("M11.0,3 H16.5 A4.5,4.5 0 0 1 21.0,7.5 V13.0 A4.5,4.5 0 0 1 16.5,17.5 H11.0 A4.5,4.5 0 0 1 6.5,13.0 V7.5 A4.5,4.5 0 0 1 11.0,3 Z"),
            line("M6.8,14.5 L10.5,11 L13.5,13.5 L16,11.5 L20.5,15"),
        )
    }
    val Trash: ImageVector by lazy {
        build("Trash",
            line("M4,6.5 H20"),
            line("M9,6.5 V5 A1.5,1.5 0 0 1 10.5,3.5 H13.5 A1.5,1.5 0 0 1 15,5 V6.5"),
            line("M6,6.5 L7,19 A2,2 0 0 0 9,21 H15 A2,2 0 0 0 17,19 L18,6.5"),
            line("M10,11 V16.5 M14,11 V16.5"),
        )
    }
    val TrashDuo: ImageVector by lazy {
        build("TrashDuo",
            fill("M6,7 L7,19 A2,2 0 0 0 9,21 H15 A2,2 0 0 0 17,19 L18,7 Z", 0.28f),
            line("M4,6.5 H20"),
            line("M9,6.5 V5 A1.5,1.5 0 0 1 10.5,3.5 H13.5 A1.5,1.5 0 0 1 15,5 V6.5"),
            line("M6,6.5 L7,19 A2,2 0 0 0 9,21 H15 A2,2 0 0 0 17,19 L18,6.5"),
            line("M10,11 V16.5 M14,11 V16.5"),
        )
    }
    val Settings: ImageVector by lazy {
        build("Settings",
            line("M3.5,7 H13.2 M17.8,7 H20.5 M3.5,17 H6.2 M10.8,17 H20.5"),
            line("M13.2,7 A2.3,2.3 0 1 0 17.8,7 A2.3,2.3 0 1 0 13.2,7 Z"),
            line("M6.2,17 A2.3,2.3 0 1 0 10.8,17 A2.3,2.3 0 1 0 6.2,17 Z"),
        )
    }
    val SettingsDuo: ImageVector by lazy {
        build("SettingsDuo",
            fill("M13.2,7 A2.3,2.3 0 1 0 17.8,7 A2.3,2.3 0 1 0 13.2,7 Z", 0.28f),
            fill("M6.2,17 A2.3,2.3 0 1 0 10.8,17 A2.3,2.3 0 1 0 6.2,17 Z", 0.28f),
            line("M3.5,7 H13.2 M17.8,7 H20.5 M3.5,17 H6.2 M10.8,17 H20.5"),
            line("M13.2,7 A2.3,2.3 0 1 0 17.8,7 A2.3,2.3 0 1 0 13.2,7 Z"),
            line("M6.2,17 A2.3,2.3 0 1 0 10.8,17 A2.3,2.3 0 1 0 6.2,17 Z"),
        )
    }
    val Camera: ImageVector by lazy {
        build("Camera",
            line("M3.5,9 A2.5,2.5 0 0 1 6,6.5 H7.6 L9,4.5 H15 L16.4,6.5 H18 A2.5,2.5 0 0 1 20.5,9 V17 A2.5,2.5 0 0 1 18,19.5 H6 A2.5,2.5 0 0 1 3.5,17 Z"),
            line("M8.4,12.8 A3.6,3.6 0 1 0 15.6,12.8 A3.6,3.6 0 1 0 8.4,12.8 Z"),
        )
    }
    val Share: ImageVector by lazy {
        build("Share",
            line("M15.4,5.5 A2.6,2.6 0 1 0 20.6,5.5 A2.6,2.6 0 1 0 15.4,5.5 Z"),
            line("M3.4,12 A2.6,2.6 0 1 0 8.6,12 A2.6,2.6 0 1 0 3.4,12 Z"),
            line("M15.4,18.5 A2.6,2.6 0 1 0 20.6,18.5 A2.6,2.6 0 1 0 15.4,18.5 Z"),
            line("M8.3,10.7 L15.7,6.8 M8.3,13.3 L15.7,17.2"),
        )
    }
    val Info: ImageVector by lazy {
        build("Info",
            line("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z"),
            line("M12,11 V16.2"),
            line("M12,7.8 V8.1"),
        )
    }
    val Close: ImageVector by lazy {
        build("Close",
            line("M6,6 L18,18 M18,6 L6,18"),
        )
    }
    val Check: ImageVector by lazy {
        build("Check",
            line("M5,12.5 L10,17.5 L19,7"),
        )
    }
    val CheckCircle: ImageVector by lazy {
        build("CheckCircle",
            line("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z"),
            line("M8,12.3 L10.8,15 L16,9.5"),
        )
    }
    val CheckCircleDuo: ImageVector by lazy {
        build("CheckCircleDuo",
            fill("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z", 0.28f),
            line("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z"),
            line("M8,12.3 L10.8,15 L16,9.5"),
        )
    }
    val ArrowBack: ImageVector by lazy {
        build("ArrowBack",
            line("M19,12 H5 M11,6 L5,12 L11,18"),
        )
    }
    val ArrowForward: ImageVector by lazy {
        build("ArrowForward",
            line("M5,12 H19 M13,6 L19,12 L13,18"),
        )
    }
    val Play: ImageVector by lazy {
        build("Play",
            solid("M8,5.6 V18.4 A1.2,1.2 0 0 0 9.8,19.4 L19.6,13.2 A1.4,1.4 0 0 0 19.6,10.8 L9.8,4.6 A1.2,1.2 0 0 0 8,5.6 Z"),
        )
    }
    val Pause: ImageVector by lazy {
        build("Pause",
            solid("M7.6,4.5 H8.4 A1.6,1.6 0 0 1 10,6.1 V17.9 A1.6,1.6 0 0 1 8.4,19.5 H7.6 A1.6,1.6 0 0 1 6,17.9 V6.1 A1.6,1.6 0 0 1 7.6,4.5 Z"),
            solid("M15.6,4.5 H16.4 A1.6,1.6 0 0 1 18,6.1 V17.9 A1.6,1.6 0 0 1 16.4,19.5 H15.6 A1.6,1.6 0 0 1 14,17.9 V6.1 A1.6,1.6 0 0 1 15.6,4.5 Z"),
        )
    }
    val Replay: ImageVector by lazy {
        build("Replay",
            line("M4.5,12 A7.5,7.5 0 1 0 7,6.4"),
            line("M4.5,4.2 V8.6 H8.9"),
        )
    }
    val Replay10: ImageVector by lazy {
        build("Replay10",
            line("M12,5.2 A7.6,7.6 0 1 1 5.4,9.2"),
            line("M12,2.4 L9.3,5.2 L12,8"),
            line("M9.6,10.9 L11,10.2 V16"),
            line("M14.6,10.4 A1.5,2.7 0 0 0 14.6,16 A1.5,2.7 0 0 0 14.6,10.4"),
        )
    }
    val Forward10: ImageVector by lazy {
        build("Forward10",
            line("M12,5.2 A7.6,7.6 0 1 0 18.6,9.2"),
            line("M12,2.4 L14.7,5.2 L12,8"),
            line("M9.6,10.9 L11,10.2 V16"),
            line("M14.6,10.4 A1.5,2.7 0 0 0 14.6,16 A1.5,2.7 0 0 0 14.6,10.4"),
        )
    }
    val VolumeUp: ImageVector by lazy {
        build("VolumeUp",
            solid("M4,9.6 V14.4 H7.2 L11.8,18 V6 L7.2,9.6 Z"),
            line("M15.2,9.2 A4,4 0 0 1 15.2,14.8"),
            line("M17.8,6.4 A8,8 0 0 1 17.8,17.6"),
        )
    }
    val VolumeOff: ImageVector by lazy {
        build("VolumeOff",
            solid("M4,9.6 V14.4 H7.2 L11.8,18 V6 L7.2,9.6 Z"),
            line("M16,9.6 L20.6,14.4 M20.6,9.6 L16,14.4"),
        )
    }
    val Speed: ImageVector by lazy {
        build("Speed",
            line("M3.8,16.5 A8.5,8.5 0 1 1 20.2,16.5"),
            line("M12,14.6 L15.6,9.6"),
            solid("M10.8,14.8 A1.2,1.2 0 1 0 13.2,14.8 A1.2,1.2 0 1 0 10.8,14.8 Z"),
        )
    }
    val Calendar: ImageVector by lazy {
        build("Calendar",
            line("M7.5,5 H16.5 A4,4 0 0 1 20.5,9 V16.5 A4,4 0 0 1 16.5,20.5 H7.5 A4,4 0 0 1 3.5,16.5 V9 A4,4 0 0 1 7.5,5 Z"),
            line("M3.5,10 H20.5 M8,3 V6.6 M16,3 V6.6"),
        )
    }
    val CalendarDuo: ImageVector by lazy {
        build("CalendarDuo",
            fill("M7.5,5 H16.5 A4,4 0 0 1 20.5,9 V16.5 A4,4 0 0 1 16.5,20.5 H7.5 A4,4 0 0 1 3.5,16.5 V9 A4,4 0 0 1 7.5,5 Z", 0.28f),
            line("M7.5,5 H16.5 A4,4 0 0 1 20.5,9 V16.5 A4,4 0 0 1 16.5,20.5 H7.5 A4,4 0 0 1 3.5,16.5 V9 A4,4 0 0 1 7.5,5 Z"),
            line("M3.5,10 H20.5 M8,3 V6.6 M16,3 V6.6"),
        )
    }
    val Frame: ImageVector by lazy {
        build("Frame",
            line("M4,8.5 V7 A3,3 0 0 1 7,4 H8.5 M15.5,4 H17 A3,3 0 0 1 20,7 V8.5 M20,15.5 V17 A3,3 0 0 1 17,20 H15.5 M8.5,20 H7 A3,3 0 0 1 4,17 V15.5"),
        )
    }
    val Timer: ImageVector by lazy {
        build("Timer",
            line("M4.2,13.2 A7.8,7.8 0 1 0 19.8,13.2 A7.8,7.8 0 1 0 4.2,13.2 Z"),
            line("M12,9.4 V13.4 L14.6,15"),
            line("M9.6,2.8 H14.4"),
        )
    }
    val TimerDuo: ImageVector by lazy {
        build("TimerDuo",
            fill("M4.2,13.2 A7.8,7.8 0 1 0 19.8,13.2 A7.8,7.8 0 1 0 4.2,13.2 Z", 0.28f),
            line("M4.2,13.2 A7.8,7.8 0 1 0 19.8,13.2 A7.8,7.8 0 1 0 4.2,13.2 Z"),
            line("M12,9.4 V13.4 L14.6,15"),
            line("M9.6,2.8 H14.4"),
        )
    }
    val Video: ImageVector by lazy {
        build("Video",
            line("M6.5,6 H12.5 A3.5,3.5 0 0 1 16,9.5 V14.5 A3.5,3.5 0 0 1 12.5,18 H6.5 A3.5,3.5 0 0 1 3,14.5 V9.5 A3.5,3.5 0 0 1 6.5,6 Z"),
            line("M16,10.6 L20.1,8.3 A.9,.9 0 0 1 21.4,9.1 V14.9 A.9,.9 0 0 1 20.1,15.7 L16,13.4"),
        )
    }
    val VideoDuo: ImageVector by lazy {
        build("VideoDuo",
            fill("M6.5,6 H12.5 A3.5,3.5 0 0 1 16,9.5 V14.5 A3.5,3.5 0 0 1 12.5,18 H6.5 A3.5,3.5 0 0 1 3,14.5 V9.5 A3.5,3.5 0 0 1 6.5,6 Z", 0.28f),
            line("M6.5,6 H12.5 A3.5,3.5 0 0 1 16,9.5 V14.5 A3.5,3.5 0 0 1 12.5,18 H6.5 A3.5,3.5 0 0 1 3,14.5 V9.5 A3.5,3.5 0 0 1 6.5,6 Z"),
            line("M16,10.6 L20.1,8.3 A.9,.9 0 0 1 21.4,9.1 V14.9 A.9,.9 0 0 1 20.1,15.7 L16,13.4"),
        )
    }
    val Folder: ImageVector by lazy {
        build("Folder",
            line("M3.5,7.5 A2.5,2.5 0 0 1 6,5 H9.6 L11.6,7.2 H18 A2.5,2.5 0 0 1 20.5,9.7 V17 A2.5,2.5 0 0 1 18,19.5 H6 A2.5,2.5 0 0 1 3.5,17 Z"),
            line("M9,13.4 H15 M12.8,11 L15.2,13.4 L12.8,15.8"),
        )
    }
    val FolderDuo: ImageVector by lazy {
        build("FolderDuo",
            fill("M3.5,7.5 A2.5,2.5 0 0 1 6,5 H9.6 L11.6,7.2 H18 A2.5,2.5 0 0 1 20.5,9.7 V17 A2.5,2.5 0 0 1 18,19.5 H6 A2.5,2.5 0 0 1 3.5,17 Z", 0.28f),
            line("M3.5,7.5 A2.5,2.5 0 0 1 6,5 H9.6 L11.6,7.2 H18 A2.5,2.5 0 0 1 20.5,9.7 V17 A2.5,2.5 0 0 1 18,19.5 H6 A2.5,2.5 0 0 1 3.5,17 Z"),
            line("M9,13.4 H15 M12.8,11 L15.2,13.4 L12.8,15.8"),
        )
    }
    val SelectAll: ImageVector by lazy {
        build("SelectAll",
            line("M8.5,4 H15.5 A4.5,4.5 0 0 1 20,8.5 V15.5 A4.5,4.5 0 0 1 15.5,20 H8.5 A4.5,4.5 0 0 1 4,15.5 V8.5 A4.5,4.5 0 0 1 8.5,4 Z"),
            line("M8.4,12.2 L11,14.8 L15.8,9.4"),
        )
    }
    val Add: ImageVector by lazy {
        build("Add",
            line("M12,5 V19 M5,12 H19"),
        )
    }
    val Sparkle: ImageVector by lazy {
        build("Sparkle",
            line("M11,3.5 C11.6,8.4 13.2,10 18,10.6 C13.2,11.2 11.6,12.8 11,17.7 C10.4,12.8 8.8,11.2 4,10.6 C8.8,10 10.4,8.4 11,3.5 Z"),
            line("M18.5,15.5 V20.5 M16,18 H21"),
        )
    }
    val SparkleDuo: ImageVector by lazy {
        build("SparkleDuo",
            fill("M11,3.5 C11.6,8.4 13.2,10 18,10.6 C13.2,11.2 11.6,12.8 11,17.7 C10.4,12.8 8.8,11.2 4,10.6 C8.8,10 10.4,8.4 11,3.5 Z", 0.28f),
            line("M11,3.5 C11.6,8.4 13.2,10 18,10.6 C13.2,11.2 11.6,12.8 11,17.7 C10.4,12.8 8.8,11.2 4,10.6 C8.8,10 10.4,8.4 11,3.5 Z"),
            line("M18.5,15.5 V20.5 M16,18 H21"),
        )
    }
    val Apps: ImageVector by lazy {
        build("Apps",
            line("M6.1,3.5 H8.4 A2.6,2.6 0 0 1 11.0,6.1 V8.4 A2.6,2.6 0 0 1 8.4,11.0 H6.1 A2.6,2.6 0 0 1 3.5,8.4 V6.1 A2.6,2.6 0 0 1 6.1,3.5 Z"),
            line("M15.6,3.5 H17.9 A2.6,2.6 0 0 1 20.5,6.1 V8.4 A2.6,2.6 0 0 1 17.9,11.0 H15.6 A2.6,2.6 0 0 1 13,8.4 V6.1 A2.6,2.6 0 0 1 15.6,3.5 Z"),
            line("M6.1,13 H8.4 A2.6,2.6 0 0 1 11.0,15.6 V17.9 A2.6,2.6 0 0 1 8.4,20.5 H6.1 A2.6,2.6 0 0 1 3.5,17.9 V15.6 A2.6,2.6 0 0 1 6.1,13 Z"),
            line("M15.6,13 H17.9 A2.6,2.6 0 0 1 20.5,15.6 V17.9 A2.6,2.6 0 0 1 17.9,20.5 H15.6 A2.6,2.6 0 0 1 13,17.9 V15.6 A2.6,2.6 0 0 1 15.6,13 Z"),
        )
    }
    val AppsDuo: ImageVector by lazy {
        build("AppsDuo",
            fill("M6.1,3.5 H8.4 A2.6,2.6 0 0 1 11.0,6.1 V8.4 A2.6,2.6 0 0 1 8.4,11.0 H6.1 A2.6,2.6 0 0 1 3.5,8.4 V6.1 A2.6,2.6 0 0 1 6.1,3.5 Z", 0.28f),
            line("M6.1,3.5 H8.4 A2.6,2.6 0 0 1 11.0,6.1 V8.4 A2.6,2.6 0 0 1 8.4,11.0 H6.1 A2.6,2.6 0 0 1 3.5,8.4 V6.1 A2.6,2.6 0 0 1 6.1,3.5 Z"),
            line("M15.6,3.5 H17.9 A2.6,2.6 0 0 1 20.5,6.1 V8.4 A2.6,2.6 0 0 1 17.9,11.0 H15.6 A2.6,2.6 0 0 1 13,8.4 V6.1 A2.6,2.6 0 0 1 15.6,3.5 Z"),
            line("M6.1,13 H8.4 A2.6,2.6 0 0 1 11.0,15.6 V17.9 A2.6,2.6 0 0 1 8.4,20.5 H6.1 A2.6,2.6 0 0 1 3.5,17.9 V15.6 A2.6,2.6 0 0 1 6.1,13 Z"),
            line("M15.6,13 H17.9 A2.6,2.6 0 0 1 20.5,15.6 V17.9 A2.6,2.6 0 0 1 17.9,20.5 H15.6 A2.6,2.6 0 0 1 13,17.9 V15.6 A2.6,2.6 0 0 1 15.6,13 Z"),
        )
    }
    val Palette: ImageVector by lazy {
        build("Palette",
            line("M12,3.5 A8.5,8.5 0 1 0 12,20.5 C13.5,20.5 14.2,19.4 13.8,18.2 C13.3,16.8 14.2,15.6 15.7,15.6 H17.5 A3.5,3.5 0 0 0 21,12.1 C21,7.3 17,3.5 12,3.5 Z"),
            line("M7.6,11.9 V12.2 M9.6,7.8 V8.1 M14.4,7.8 V8.1"),
        )
    }
    val PaletteDuo: ImageVector by lazy {
        build("PaletteDuo",
            fill("M12,3.5 A8.5,8.5 0 1 0 12,20.5 C13.5,20.5 14.2,19.4 13.8,18.2 C13.3,16.8 14.2,15.6 15.7,15.6 H17.5 A3.5,3.5 0 0 0 21,12.1 C21,7.3 17,3.5 12,3.5 Z", 0.28f),
            line("M12,3.5 A8.5,8.5 0 1 0 12,20.5 C13.5,20.5 14.2,19.4 13.8,18.2 C13.3,16.8 14.2,15.6 15.7,15.6 H17.5 A3.5,3.5 0 0 0 21,12.1 C21,7.3 17,3.5 12,3.5 Z"),
            line("M7.6,11.9 V12.2 M9.6,7.8 V8.1 M14.4,7.8 V8.1"),
        )
    }
    val Dashed: ImageVector by lazy {
        build("Dashed",
            line("M4,9 V7.5 A3.5,3.5 0 0 1 7.5,4 H9 M13,4 H16.5 A3.5,3.5 0 0 1 20,7.5 V9 M20,13 V16.5 A3.5,3.5 0 0 1 16.5,20 H15 M11,20 H7.5 A3.5,3.5 0 0 1 4,16.5 V15"),
        )
    }
    val Sun: ImageVector by lazy {
        build("Sun",
            line("M8,12 A4,4 0 1 0 16,12 A4,4 0 1 0 8,12 Z"),
            line("M12,2.8 V4.6 M12,19.4 V21.2 M2.8,12 H4.6 M19.4,12 H21.2 M5.5,5.5 L6.8,6.8 M17.2,17.2 L18.5,18.5 M5.5,18.5 L6.8,17.2 M17.2,6.8 L18.5,5.5"),
        )
    }
    val SunDuo: ImageVector by lazy {
        build("SunDuo",
            fill("M8,12 A4,4 0 1 0 16,12 A4,4 0 1 0 8,12 Z", 0.28f),
            line("M8,12 A4,4 0 1 0 16,12 A4,4 0 1 0 8,12 Z"),
            line("M12,2.8 V4.6 M12,19.4 V21.2 M2.8,12 H4.6 M19.4,12 H21.2 M5.5,5.5 L6.8,6.8 M17.2,17.2 L18.5,18.5 M5.5,18.5 L6.8,17.2 M17.2,6.8 L18.5,5.5"),
        )
    }
    val Moon: ImageVector by lazy {
        build("Moon",
            line("M20,14.6 A8.4,8.4 0 1 1 9.4,4 A6.6,6.6 0 0 0 20,14.6 Z"),
        )
    }
    val MoonDuo: ImageVector by lazy {
        build("MoonDuo",
            fill("M20,14.6 A8.4,8.4 0 1 1 9.4,4 A6.6,6.6 0 0 0 20,14.6 Z", 0.28f),
            line("M20,14.6 A8.4,8.4 0 1 1 9.4,4 A6.6,6.6 0 0 0 20,14.6 Z"),
        )
    }
    val Phone: ImageVector by lazy {
        build("Phone",
            line("M9.2,2.8 H14.8 A3.2,3.2 0 0 1 18,6.0 V18.0 A3.2,3.2 0 0 1 14.8,21.2 H9.2 A3.2,3.2 0 0 1 6,18.0 V6.0 A3.2,3.2 0 0 1 9.2,2.8 Z"),
            line("M10.4,17.8 H13.6"),
        )
    }
    val PhoneDuo: ImageVector by lazy {
        build("PhoneDuo",
            fill("M9.2,2.8 H14.8 A3.2,3.2 0 0 1 18,6.0 V18.0 A3.2,3.2 0 0 1 14.8,21.2 H9.2 A3.2,3.2 0 0 1 6,18.0 V6.0 A3.2,3.2 0 0 1 9.2,2.8 Z", 0.28f),
            line("M9.2,2.8 H14.8 A3.2,3.2 0 0 1 18,6.0 V18.0 A3.2,3.2 0 0 1 14.8,21.2 H9.2 A3.2,3.2 0 0 1 6,18.0 V6.0 A3.2,3.2 0 0 1 9.2,2.8 Z"),
            line("M10.4,17.8 H13.6"),
        )
    }
    val PlayCircle: ImageVector by lazy {
        build("PlayCircle",
            line("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z"),
            solid("M10.2,8.8 L15.4,12 L10.2,15.2 Z"),
        )
    }
    val PlayCircleDuo: ImageVector by lazy {
        build("PlayCircleDuo",
            fill("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z", 0.28f),
            line("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z"),
            solid("M10.2,8.8 L15.4,12 L10.2,15.2 Z"),
        )
    }
    val Corner: ImageVector by lazy {
        build("Corner",
            line("M4,20 V11 A7,7 0 0 1 11,4 H20"),
        )
    }
    val Blur: ImageVector by lazy {
        build("Blur",
            line("M12,3.5 C9.2,7.2 6,10.4 6,14.6 A6,6 0 0 0 18,14.6 C18,10.4 14.8,7.2 12,3.5 Z"),
            line("M9.4,15.2 A2.9,2.9 0 0 0 11.6,17.6"),
        )
    }
    val BlurDuo: ImageVector by lazy {
        build("BlurDuo",
            fill("M12,3.5 C9.2,7.2 6,10.4 6,14.6 A6,6 0 0 0 18,14.6 C18,10.4 14.8,7.2 12,3.5 Z", 0.28f),
            line("M12,3.5 C9.2,7.2 6,10.4 6,14.6 A6,6 0 0 0 18,14.6 C18,10.4 14.8,7.2 12,3.5 Z"),
            line("M9.4,15.2 A2.9,2.9 0 0 0 11.6,17.6"),
        )
    }
    val TrashX: ImageVector by lazy {
        build("TrashX",
            line("M4,6.5 H20"),
            line("M9,6.5 V5 A1.5,1.5 0 0 1 10.5,3.5 H13.5 A1.5,1.5 0 0 1 15,5 V6.5"),
            line("M6,6.5 L7,19 A2,2 0 0 0 9,21 H15 A2,2 0 0 0 17,19 L18,6.5"),
            line("M10.2,11.2 L13.8,15.6 M13.8,11.2 L10.2,15.6"),
        )
    }
    val TrashXDuo: ImageVector by lazy {
        build("TrashXDuo",
            fill("M6,7 L7,19 A2,2 0 0 0 9,21 H15 A2,2 0 0 0 17,19 L18,7 Z", 0.28f),
            line("M4,6.5 H20"),
            line("M9,6.5 V5 A1.5,1.5 0 0 1 10.5,3.5 H13.5 A1.5,1.5 0 0 1 15,5 V6.5"),
            line("M6,6.5 L7,19 A2,2 0 0 0 9,21 H15 A2,2 0 0 0 17,19 L18,6.5"),
            line("M10.2,11.2 L13.8,15.6 M13.8,11.2 L10.2,15.6"),
        )
    }
    val TrashRestore: ImageVector by lazy {
        build("TrashRestore",
            line("M4,6.5 H20"),
            line("M9,6.5 V5 A1.5,1.5 0 0 1 10.5,3.5 H13.5 A1.5,1.5 0 0 1 15,5 V6.5"),
            line("M6,6.5 L7,19 A2,2 0 0 0 9,21 H15 A2,2 0 0 0 17,19 L18,6.5"),
            line("M12,17 V11.6 M9.8,13.8 L12,11.6 L14.2,13.8"),
        )
    }
    val TrashRestoreDuo: ImageVector by lazy {
        build("TrashRestoreDuo",
            fill("M6,7 L7,19 A2,2 0 0 0 9,21 H15 A2,2 0 0 0 17,19 L18,7 Z", 0.28f),
            line("M4,6.5 H20"),
            line("M9,6.5 V5 A1.5,1.5 0 0 1 10.5,3.5 H13.5 A1.5,1.5 0 0 1 15,5 V6.5"),
            line("M6,6.5 L7,19 A2,2 0 0 0 9,21 H15 A2,2 0 0 0 17,19 L18,6.5"),
            line("M12,17 V11.6 M9.8,13.8 L12,11.6 L14.2,13.8"),
        )
    }
    val Globe: ImageVector by lazy {
        build("Globe",
            line("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z"),
            line("M12,3 A4.2,9 0 0 0 12,21 A4.2,9 0 0 0 12,3 Z"),
            line("M3.2,12 H20.8"),
        )
    }
    val GlobeDuo: ImageVector by lazy {
        build("GlobeDuo",
            fill("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z", 0.28f),
            line("M3,12 A9,9 0 1 0 21,12 A9,9 0 1 0 3,12 Z"),
            line("M12,3 A4.2,9 0 0 0 12,21 A4.2,9 0 0 0 12,3 Z"),
            line("M3.2,12 H20.8"),
        )
    }
    val Send: ImageVector by lazy {
        build("Send",
            line("M21,3 L3,10 L10.2,13.4 L13.6,21 Z"),
            line("M21,3 L10.2,13.4"),
        )
    }
    val SendDuo: ImageVector by lazy {
        build("SendDuo",
            fill("M21,3 L3,10 L10.2,13.4 L13.6,21 Z", 0.28f),
            line("M21,3 L3,10 L10.2,13.4 L13.6,21 Z"),
            line("M21,3 L10.2,13.4"),
        )
    }
    val Megaphone: ImageVector by lazy {
        build("Megaphone",
            line("M4,10 V14 A1,1 0 0 0 5,15 H7.5 L15.5,19 V5 L7.5,9 H5 A1,1 0 0 0 4,10 Z"),
            line("M8,15 L9,19.6 H11.6 L10.8,16.6"),
            line("M18.6,9.4 A3.6,3.6 0 0 1 18.6,14.6"),
        )
    }
    val MegaphoneDuo: ImageVector by lazy {
        build("MegaphoneDuo",
            fill("M4,10 V14 A1,1 0 0 0 5,15 H7.5 L15.5,19 V5 L7.5,9 H5 A1,1 0 0 0 4,10 Z", 0.28f),
            line("M4,10 V14 A1,1 0 0 0 5,15 H7.5 L15.5,19 V5 L7.5,9 H5 A1,1 0 0 0 4,10 Z"),
            line("M8,15 L9,19.6 H11.6 L10.8,16.6"),
            line("M18.6,9.4 A3.6,3.6 0 0 1 18.6,14.6"),
        )
    }
    val GitBranch: ImageVector by lazy {
        build("GitBranch",
            line("M3.7,5.8 A2.3,2.3 0 1 0 8.3,5.8 A2.3,2.3 0 1 0 3.7,5.8 Z"),
            line("M3.7,18.2 A2.3,2.3 0 1 0 8.3,18.2 A2.3,2.3 0 1 0 3.7,18.2 Z"),
            line("M15.7,8.6 A2.3,2.3 0 1 0 20.3,8.6 A2.3,2.3 0 1 0 15.7,8.6 Z"),
            line("M6,8.1 V15.9"),
            line("M18,10.9 C18,14.6 14.6,14.6 11.6,14.8 C8.4,15 6,15.6 6,15.9"),
        )
    }
    val Download: ImageVector by lazy {
        build("Download",
            line("M12,4 V14.8 M7.6,10.6 L12,15 L16.4,10.6"),
            line("M4.5,19.5 H19.5"),
        )
    }
    val Refresh: ImageVector by lazy {
        build("Refresh",
            line("M19.5,12 A7.5,7.5 0 1 1 16.9,6.4"),
            line("M19.5,4.5 V8.8 H15.2"),
        )
    }

    // ------------------------------------------------------------------ Constructor

    private class Part(val d: String, val kind: Int, val alpha: Float)

    private fun fill(d: String, alpha: Float) = Part(d, 0, alpha)
    private fun line(d: String) = Part(d, 1, 1f)
    private fun solid(d: String) = Part(d, 2, 1f)

    private fun build(name: String, vararg parts: Part): ImageVector {
        val b = ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
        for (p in parts) {
            val nodes = PathParser().parsePathString(p.d).toNodes()
            when (p.kind) {
                0 -> b.addPath(
                    pathData = nodes,
                    fill = SolidColor(Color.Black),
                    fillAlpha = p.alpha,
                )
                1 -> b.addPath(
                    pathData = nodes,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
                else -> b.addPath(
                    pathData = nodes,
                    fill = SolidColor(Color.Black),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }
        return b.build()
    }
}
