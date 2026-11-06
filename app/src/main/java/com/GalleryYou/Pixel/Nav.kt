package com.GalleryYou.Pixel

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

data class NavDest(@StringRes val labelRes: Int, val icon: ImageVector, val iconSelected: ImageVector)

val GalleryNavItems = listOf(
    NavDest(R.string.nav_photos, AppIcons.Image, AppIcons.ImageDuo),
    NavDest(R.string.nav_albums, AppIcons.Collections, AppIcons.CollectionsDuo),
    NavDest(R.string.nav_trash, AppIcons.Trash, AppIcons.TrashDuo),
    NavDest(R.string.nav_settings, AppIcons.Settings, AppIcons.SettingsDuo),
)

/**
 * Barra de navegación flotante de cristal: una píldora con los destinos (el activo muestra icono
 * y nombre dentro de su propia píldora) sobre un fondo de cristal.
 */
@Composable
fun GlassNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: BackdropState? = null,
) {
    val scheme = MaterialTheme.colorScheme
    BoxWithConstraints(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .glassSurface(
                        state = backdrop,
                        tint = scheme.surfaceContainerHigh.copy(alpha = 0.42f),
                        fallback = scheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        corner = 32.dp,
                    )
                    // Absorbe los toques en toda la píldora: los huecos entre iconos ya no llegan
                    // a las fotos o álbumes que hay detrás.
                    .pointerInput(Unit) { detectTapGestures { } }
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { i, dest ->
                    NavItem(dest, i == selected, animations) { onSelect(i) }
                }
            }
        }
    }
}

@Composable
private fun NavItem(dest: NavDest, selected: Boolean, animations: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colorSpec: AnimationSpec<Color> =
        if (animations) spring(stiffness = Spring.StiffnessMediumLow) else snap()
    val bg by animateColorAsState(
        targetValue = if (selected) scheme.primaryContainer else Color.Transparent,
        animationSpec = colorSpec,
        label = "navBg",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        animationSpec = colorSpec,
        label = "navFg",
    )
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) dest.iconSelected else dest.icon,
            contentDescription = stringResource(dest.labelRes),
            tint = fg,
            modifier = Modifier.size(24.dp),
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(if (animations) 200 else 0)) + expandHorizontally(
                animationSpec = if (animations) spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow) else snap(),
            ),
            exit = fadeOut(tween(if (animations) 100 else 0)) + shrinkHorizontally(
                animationSpec = if (animations) tween(180) else snap(),
            ),
        ) {
            Text(
                stringResource(dest.labelRes),
                color = fg,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
