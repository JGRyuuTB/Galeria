package com.GalleryYou.Pixel

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun isAppDark(settings: AppSettings): Boolean {
    val system = isSystemInDarkTheme()
    return when (settings.themeMode) {
        1 -> false
        2 -> true
        else -> system
    }
}

@Composable
fun GaleriaTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = isAppDark(settings)
    val context = LocalContext.current
    // Material You: colores dinámicos del fondo de pantalla (Android 12+), como en un Pixel.
    val useDynamic = settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val scheme = when {
        useDynamic -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
