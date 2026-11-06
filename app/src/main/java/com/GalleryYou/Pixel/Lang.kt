package com.GalleryYou.Pixel

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.PluralsRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.Locale

/** Idioma de la app: etiqueta de idioma y nombre escrito en su propio idioma. */
data class AppLanguage(val tag: String, val nativeName: String)

/**
 * Idiomas disponibles. Para añadir uno nuevo: crea su carpeta `res/values-xx/strings.xml`
 * y agrega una línea aquí (y en `res/xml/locales_config.xml`).
 */
val AppLanguages = listOf(
    AppLanguage("es", "Español"),
    AppLanguage("en", "English"),
    AppLanguage("pt", "Português"),
    AppLanguage("fr", "Français"),
    AppLanguage("de", "Deutsch"),
    AppLanguage("it", "Italiano"),
    AppLanguage("ru", "Русский"),
    AppLanguage("ja", "日本語"),
    AppLanguage("zh-CN", "中文（简体）"),
)

private const val LANG_PREFS = "galeria_settings"
private const val LANG_KEY = "language"

/** Idioma elegido en Ajustes, o "" para seguir el del sistema. */
fun storedLanguage(context: Context): String =
    context.getSharedPreferences(LANG_PREFS, Context.MODE_PRIVATE).getString(LANG_KEY, "") ?: ""

/** Devuelve un contexto con el idioma elegido en Ajustes (o el mismo si se sigue el del sistema). */
fun Context.withAppLocale(): Context {
    val tag = storedLanguage(this)
    if (tag.isEmpty()) return this
    val locale = Locale.forLanguageTag(tag)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

/** Selector de idioma: "Idioma del sistema" y la lista de idiomas, cada uno en su propio idioma. */
@Composable
fun LanguageDialog(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                val options = listOf(AppLanguage("", stringResource(R.string.language_system))) + AppLanguages
                options.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(lang.tag) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = lang.tag == current, onClick = { onSelect(lang.tag) })
                        Spacer(Modifier.width(8.dp))
                        Text(lang.nativeName, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Texto con plural correcto en cada idioma (por ejemplo "1 elemento" / "5 elementos"). */
@Composable
fun quantityString(@PluralsRes id: Int, count: Int): String =
    LocalContext.current.resources.getQuantityString(id, count, count)
